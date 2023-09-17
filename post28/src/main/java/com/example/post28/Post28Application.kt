package com.example.post28

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.content.Context
import android.os.Build

class Post28Application : Application() {

    override fun onCreate() {
        super.onCreate()
        DIUtils.init()

        createChannelGroups()
        createChatNotificationChannel()
        createSystemNotificationChannel()
    }

    private fun createChannelGroups() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.createNotificationChannelGroup(
                NotificationChannelGroup(CHAT_GROUP, CHAT_GROUP)
            )

            notificationManager.createNotificationChannelGroup(
                NotificationChannelGroup(SYSTEM_GROUP, SYSTEM_GROUP)
            )
        }
    }

    private fun createChatNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.notification_chat_channel_name)
            val descriptionText = getString(R.string.notification_chat_channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHAT_CHANNEL, name, importance).apply {
                description = descriptionText
                group = CHAT_GROUP
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createSystemNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.notification_system_channel_name)
            val descriptionText = getString(R.string.notification_system_channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(SYSTEM_CHANNEL, name, importance).apply {
                description = descriptionText
                group = SYSTEM_GROUP
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val CHAT_GROUP = "chatGroup"
        private const val SYSTEM_GROUP = "systemGroup"

        const val CHAT_CHANNEL = "chatChannel"
        const val SYSTEM_CHANNEL = "systemChannel"
    }
}