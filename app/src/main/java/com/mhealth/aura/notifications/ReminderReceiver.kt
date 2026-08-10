package com.mhealth.aura.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mhealth.aura.data.db.AuraDatabase
import com.mhealth.aura.data.db.entity.DoseLogEntity
import com.mhealth.aura.domain.DoseSchedule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                handle(context.applicationContext, intent)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun handle(context: Context, intent: Intent) {
        val db = AuraDatabase.getDatabase(context)
        val doseDao = db.doseLogDao()
        val medicationId = intent.getLongExtra(EXTRA_MEDICATION_ID, 0L)
        val medication = intent.getStringExtra(EXTRA_MEDICATION).orEmpty()
        val doseLabel = intent.getStringExtra(EXTRA_DOSE_LABEL).orEmpty()
        val dayStart = intent.getLongExtra(EXTRA_DAY_START, startOfToday())
        val dayEnd = dayStart + 86_400_000L
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 30_000)

        if (intent.action == ACTION_MARK_TAKEN) {
            if (doseDao.countTakenForDose(medicationId, doseLabel, dayStart, dayEnd) == 0) {
                doseDao.insertLog(
                    DoseLogEntity(
                        medicationId = medicationId,
                        medicationName = medication,
                        dateMillis = System.currentTimeMillis(),
                        doseLabel = doseLabel,
                        status = "taken",
                        takenAtMillis = System.currentTimeMillis()
                    )
                )
            }
            NotificationHelper.showDoseRecorded(context, doseLabel, notificationId)
            return
        }

        when (intent.getStringExtra(EXTRA_TYPE)) {
            TYPE_PRE_DOSE -> {
                if (doseDao.countTakenForDose(medicationId, doseLabel, dayStart, dayEnd) == 0) {
                    NotificationHelper.showDoseReminder(
                        context = context,
                        medication = medication,
                        medicationId = medicationId,
                        doseLabel = doseLabel,
                        scheduledTime = intent.getStringExtra(EXTRA_SCHEDULED_TIME).orEmpty(),
                        dayStartMillis = dayStart,
                        notificationId = notificationId
                    )
                }
            }
            TYPE_MISSED_DOSE -> {
                if (doseDao.countTakenForDose(medicationId, doseLabel, dayStart, dayEnd) == 0) {
                    NotificationHelper.showMissedDoseAlert(
                        context,
                        intent.getStringExtra(EXTRA_MEDICATION).orEmpty(),
                        doseLabel,
                        notificationId
                    )
                }
            }
            TYPE_DAILY_SUMMARY -> {
                val planned = intent.getIntExtra(EXTRA_PLANNED_DOSES, 0)
                val taken = doseDao.countTakenForDay(dayStart, dayEnd)
                NotificationHelper.showDailySummary(context, taken, planned, notificationId)
            }
        }
    }

    private fun startOfToday(): Long {
        val now = java.util.Calendar.getInstance()
        now.set(java.util.Calendar.HOUR_OF_DAY, 0)
        now.set(java.util.Calendar.MINUTE, 0)
        now.set(java.util.Calendar.SECOND, 0)
        now.set(java.util.Calendar.MILLISECOND, 0)
        return now.timeInMillis
    }

    companion object {
        const val ACTION_REMINDER = "com.mhealth.aura.action.REMINDER"
        const val ACTION_MARK_TAKEN = "com.mhealth.aura.action.MARK_TAKEN"

        const val TYPE_PRE_DOSE = "pre_dose"
        const val TYPE_MISSED_DOSE = "missed_dose"
        const val TYPE_DAILY_SUMMARY = "daily_summary"

        const val EXTRA_TYPE = "extra_type"
        const val EXTRA_MEDICATION_ID = "extra_medication_id"
        const val EXTRA_MEDICATION = "extra_medication"
        const val EXTRA_FREQUENCY = "extra_frequency"
        const val EXTRA_DOSE_LABEL = "extra_dose_label"
        const val EXTRA_SCHEDULED_TIME = "extra_scheduled_time"
        const val EXTRA_DAY_START = "extra_day_start"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
        const val EXTRA_PLANNED_DOSES = "extra_planned_doses"
    }
}
