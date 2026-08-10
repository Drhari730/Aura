package com.mhealth.aura.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.mhealth.aura.data.db.entity.MedicationEntity
import com.mhealth.aura.data.db.entity.UserEntity
import com.mhealth.aura.domain.DoseSchedule
import java.util.Calendar

object ReminderScheduler {
    private const val PREFS = "aura_scheduled_reminders"
    private const val KEY_REQUEST_CODES = "request_codes"

    fun scheduleCourses(
        context: Context,
        user: UserEntity,
        medications: List<MedicationEntity>
    ) {
        cancelCourses(context)
        val active = medications.filter { it.isActive && it.name.isNotBlank() }
        if (active.isEmpty()) return

        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val requestCodes = mutableSetOf<String>()
        val now = System.currentTimeMillis()

        active.forEachIndexed { medicationIndex, medication ->
            val courseStart = startOfDay(medication.startDateMillis)
            val courseDays = (
                ((medication.endDateMillis - courseStart).coerceAtLeast(0L) /
                    AlarmManager.INTERVAL_DAY) + 1
                ).toInt().coerceIn(1, 90)

            repeat(courseDays) { dayIndex ->
                val dayStart = courseStart + dayIndex * AlarmManager.INTERVAL_DAY
                DoseSchedule.slotsFor(
                    medication.frequency,
                    medication.doseTimesCsv
                ).forEachIndexed { slotIndex, slot ->
                    val doseAt = dayStart +
                        slot.hour * 60L * 60_000L +
                        slot.minute * 60_000L
                    val baseCode =
                        10_000 + medicationIndex * 10_000 + dayIndex * 100 + slotIndex * 10

                    val preDoseAt =
                        doseAt - user.preDoseMinutes.coerceIn(0, 180) * 60_000L
                    if (user.doseRemindersEnabled && preDoseAt > now) {
                        schedule(
                            alarmManager = alarmManager,
                            context = context,
                            requestCode = baseCode + 1,
                            triggerAtMillis = preDoseAt,
                            type = ReminderReceiver.TYPE_PRE_DOSE,
                            medication = medication,
                            doseLabel = slot.label,
                            scheduledTime = slot.displayTime(),
                            dayStart = dayStart
                        )
                        requestCodes += (baseCode + 1).toString()
                    }

                    val missedAt =
                        doseAt + user.missedDoseMinutes.coerceIn(15, 720) * 60_000L
                    if (user.missedDoseAlertsEnabled && missedAt > now) {
                        schedule(
                            alarmManager = alarmManager,
                            context = context,
                            requestCode = baseCode + 2,
                            triggerAtMillis = missedAt,
                            type = ReminderReceiver.TYPE_MISSED_DOSE,
                            medication = medication,
                            doseLabel = slot.label,
                            scheduledTime = slot.displayTime(),
                            dayStart = dayStart
                        )
                        requestCodes += (baseCode + 2).toString()
                    }
                }
            }
        }

        if (user.dailySummaryEnabled) {
            val firstDay = active.minOf { startOfDay(it.startDateMillis) }
            val lastDay = active.maxOf { startOfDay(it.endDateMillis) }
            val days = (((lastDay - firstDay) / AlarmManager.INTERVAL_DAY) + 1)
                .toInt().coerceIn(1, 90)
            repeat(days) { dayIndex ->
                val dayStart = firstDay + dayIndex * AlarmManager.INTERVAL_DAY
                val planned = active
                    .filter {
                        dayStart in startOfDay(it.startDateMillis)..startOfDay(it.endDateMillis)
                    }
                    .sumOf {
                        DoseSchedule.slotsFor(it.frequency, it.doseTimesCsv).size
                    }
                val summaryAt = dayStart +
                    user.summaryHour.coerceIn(0, 23) * 60L * 60_000L +
                    user.summaryMinute.coerceIn(0, 59) * 60_000L
                if (planned > 0 && summaryAt > now) {
                    val code = 900_000 + dayIndex
                    scheduleSummary(
                        alarmManager,
                        context,
                        code,
                        summaryAt,
                        dayStart,
                        planned
                    )
                    requestCodes += code.toString()
                }
            }
        }

        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putStringSet(KEY_REQUEST_CODES, requestCodes)
            .apply()
    }

    fun cancelCourses(context: Context) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.getStringSet(KEY_REQUEST_CODES, emptySet()).orEmpty().forEach { value ->
            val requestCode = value.toIntOrNull() ?: return@forEach
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                Intent(context, ReminderReceiver::class.java).apply {
                    action = ReminderReceiver.ACTION_REMINDER
                },
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
        }
        prefs.edit().remove(KEY_REQUEST_CODES).apply()
    }

    private fun schedule(
        alarmManager: AlarmManager,
        context: Context,
        requestCode: Int,
        triggerAtMillis: Long,
        type: String,
        medication: MedicationEntity,
        doseLabel: String,
        scheduledTime: String,
        dayStart: Long
    ) {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ReminderReceiver.ACTION_REMINDER
            putExtra(ReminderReceiver.EXTRA_TYPE, type)
            putExtra(ReminderReceiver.EXTRA_MEDICATION_ID, medication.id)
            putExtra(ReminderReceiver.EXTRA_MEDICATION, medication.name)
            putExtra(ReminderReceiver.EXTRA_FREQUENCY, medication.frequency)
            putExtra(ReminderReceiver.EXTRA_DOSE_LABEL, doseLabel)
            putExtra(ReminderReceiver.EXTRA_SCHEDULED_TIME, scheduledTime)
            putExtra(ReminderReceiver.EXTRA_DAY_START, dayStart)
            putExtra(ReminderReceiver.EXTRA_NOTIFICATION_ID, 30_000 + requestCode)
        }
        setAlarm(alarmManager, context, requestCode, triggerAtMillis, intent)
    }

    private fun scheduleSummary(
        alarmManager: AlarmManager,
        context: Context,
        requestCode: Int,
        triggerAtMillis: Long,
        dayStart: Long,
        planned: Int
    ) {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ReminderReceiver.ACTION_REMINDER
            putExtra(ReminderReceiver.EXTRA_TYPE, ReminderReceiver.TYPE_DAILY_SUMMARY)
            putExtra(ReminderReceiver.EXTRA_DAY_START, dayStart)
            putExtra(ReminderReceiver.EXTRA_PLANNED_DOSES, planned)
            putExtra(ReminderReceiver.EXTRA_NOTIFICATION_ID, 30_000 + requestCode)
        }
        setAlarm(alarmManager, context, requestCode, triggerAtMillis, intent)
    }

    private fun setAlarm(
        alarmManager: AlarmManager,
        context: Context,
        requestCode: Int,
        triggerAtMillis: Long,
        intent: Intent
    ) {
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }

    private fun startOfDay(timeMillis: Long): Long = Calendar.getInstance().apply {
        timeInMillis = timeMillis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}
