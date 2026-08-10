package com.mhealth.aura

import android.app.Application
import com.mhealth.aura.data.db.AuraDatabase
import com.mhealth.aura.data.prefs.AppPreferences
import com.mhealth.aura.notifications.NotificationHelper

class AuraApp : Application() {
    val database by lazy { AuraDatabase.getDatabase(this) }
    val prefs by lazy { AppPreferences(this) }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannels(this)
    }
}
