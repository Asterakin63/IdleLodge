package com.idlelodge

import android.app.Application
import com.idlelodge.notification.SessionNotificationManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class idlelodgeApp : Application() {

    @Inject lateinit var notificationManager: SessionNotificationManager

    override fun onCreate() {
        super.onCreate()
        notificationManager.createChannels()
    }
}
