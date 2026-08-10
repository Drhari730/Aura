package com.mhealth.aura.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.mhealth.aura.MainActivity
import com.mhealth.aura.R

object NotificationHelper {
    private const val CHANNEL_DOSES = "aura_dose_reminders"
    private const val CHANNEL_ALERTS = "aura_adherence_alerts"

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_DOSES,
                context.getString(R.string.notification_channel_doses),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Upcoming antibiotic dose reminders"
                enableVibration(true)
            }
        )
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ALERTS,
                context.getString(R.string.notification_channel_alerts),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Missed-dose alerts and daily adherence summaries"
                enableVibration(true)
            }
        )
    }

    fun showDoseReminder(
        context: Context,
        medication: String,
        medicationId: Long,
        doseLabel: String,
        scheduledTime: String,
        dayStartMillis: Long,
        notificationId: Int
    ) {
        val contentIntent = appPendingIntent(context, notificationId)
        val markTakenIntent = Intent(context, ReminderReceiver::class.java).apply {
            action = ReminderReceiver.ACTION_MARK_TAKEN
            putExtra(ReminderReceiver.EXTRA_MEDICATION_ID, medicationId)
            putExtra(ReminderReceiver.EXTRA_MEDICATION, medication)
            putExtra(ReminderReceiver.EXTRA_DOSE_LABEL, doseLabel)
            putExtra(ReminderReceiver.EXTRA_DAY_START, dayStartMillis)
            putExtra(ReminderReceiver.EXTRA_NOTIFICATION_ID, notificationId)
        }
        val markTakenPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId + 50_000,
            markTakenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_DOSES)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("$doseLabel in 15 minutes")
            .setContentText("$medication is scheduled for $scheduledTime.")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "$medication is scheduled for $scheduledTime. Take it exactly as prescribed."
                )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(contentIntent)
            .addAction(0, "Mark as taken", markTakenPendingIntent)
            .setAutoCancel(true)
            .build()
        notify(context, notificationId, notification)
    }

    fun showMissedDoseAlert(
        context: Context,
        medication: String,
        doseLabel: String,
        notificationId: Int
    ) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ALERTS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Dose not recorded")
            .setContentText("$doseLabel for $medication is two hours overdue.")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "$doseLabel for $medication is two hours overdue. Follow your clinician's missed-dose advice and never double a dose."
                )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(appPendingIntent(context, notificationId))
            .setAutoCancel(true)
            .build()
        notify(context, notificationId, notification)
    }

    fun showDailySummary(
        context: Context,
        taken: Int,
        planned: Int,
        notificationId: Int
    ) {
        val message = when {
            taken >= planned -> "Excellent: all $planned planned doses were recorded today."
            taken == 0 -> "No doses were recorded today. Review your schedule and contact your clinician if needed."
            else -> "$taken of $planned planned doses were recorded today."
        }
        val notification = NotificationCompat.Builder(context, CHANNEL_ALERTS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Today's Aura adherence summary")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(appPendingIntent(context, notificationId))
            .setAutoCancel(true)
            .build()
        notify(context, notificationId, notification)
    }

    fun showDoseRecorded(context: Context, doseLabel: String, notificationId: Int) {
        NotificationManagerCompat.from(context).cancel(notificationId)
        val notification = NotificationCompat.Builder(context, CHANNEL_DOSES)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Dose recorded")
            .setContentText("$doseLabel was marked as taken.")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setTimeoutAfter(4_000)
            .build()
        notify(context, notificationId + 1, notification)
    }

    fun showTestReminder(context: Context) {
        val notification = NotificationCompat.Builder(context, CHANNEL_DOSES)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Aura reminder test")
            .setContentText("Notifications are working. Your saved antibiotic schedule is active.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(appPendingIntent(context, 98_001))
            .setAutoCancel(true)
            .build()
        notify(context, 98_001, notification)
    }

    private fun appPendingIntent(context: Context, requestCode: Int): PendingIntent {
        return PendingIntent.getActivity(
            context,
            requestCode,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun notify(
        context: Context,
        notificationId: Int,
        notification: android.app.Notification
    ) {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }
}
