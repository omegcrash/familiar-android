package com.omegcrash.familiar

import android.app.Application
import com.omegcrash.familiar.notifications.NotificationHelper

class FamiliarApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannels(this)
    }
}
