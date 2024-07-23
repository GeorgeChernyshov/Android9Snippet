package com.example.post28.notifications

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import androidx.core.app.RemoteInput
import com.example.post28.Post28Application
import com.example.post28.R

class NotificationHelper(private val context: Context) {

    fun showChatNotification() {
        val sender = Person.Builder()
            .setName("Carl Denson")
            .setImportant(true)
            .build()

        val user = Person.Builder()
            .setName("You")
            .setImportant(true)
            .build()

        val builder = NotificationCompat.Builder(context, Post28Application.CHAT_CHANNEL)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(context.getString(R.string.notification_text))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .addPerson(sender.uri)
            .setOnlyAlertOnce(true)
            .setStyle(
                NotificationCompat.MessagingStyle(user)
                    .addMessage(
                        NotificationCompat.MessagingStyle.Message(
                            "Check this out",
                            0L,
                            sender
                        ).setData(
                            "image/",
                            Uri.parse("android.resource://com.example.post28/" + R.drawable.big_floppa)
                        )
                    )
            )
            .addAction(
                NotificationCompat.Action.Builder(
                    R.drawable.ic_launcher_foreground,
                    "Reply",
                    PendingIntent.getBroadcast(
                        context,
                        ReplyReceiver.REQUEST_CONTENT,
                        Intent(context, ReplyReceiver::class.java),
                        PendingIntent.FLAG_UPDATE_CURRENT
                    )
                )
                .setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_REPLY)
                .addRemoteInput(
                    RemoteInput.Builder(ReplyReceiver.KEY_TEXT_REPLY)
                        .setLabel("Enter reply")
                        .setChoices(arrayOf("Nice", "Very well", "LOL"))
                        .build()
                )
                .build()
            )

        with(NotificationManagerCompat.from(context)) {
            // notificationId is a unique int for each notification that you must define
            notify(chatNotificationId, builder.build())
        }
    }

    fun showSystemNotification() {
        val builder = NotificationCompat.Builder(context, Post28Application.SYSTEM_CHANNEL)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(context.getString(R.string.notification_text))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(context)) {
            // notificationId is a unique int for each notification that you must define
            notify(systemNotificationId, builder.build())
        }
    }

    companion object {
        var chatNotificationId = 0
        var systemNotificationId = 1
    }
}