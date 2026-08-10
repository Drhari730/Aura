package com.mhealth.aura.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mhealth.aura.data.db.AuraDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (
            intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            return
        }
        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val db = AuraDatabase.getDatabase(context)
                db.userDao().getUserSnapshot()?.let { user ->
                    ReminderScheduler.scheduleCourses(
                        context.applicationContext,
                        user,
                        db.medicationDao().getAllSnapshot()
                    )
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
