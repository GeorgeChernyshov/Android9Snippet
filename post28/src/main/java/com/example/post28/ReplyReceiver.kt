package com.example.post28

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.RemoteInput

class ReplyReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val results = RemoteInput.getResultsFromIntent(intent) ?: return
        // The message typed in the notification reply.
        val input = results.getCharSequence(KEY_TEXT_REPLY)?.toString()
        DIUtils.replyRepository.replyText = input

        if (context != null)
            NotificationHelper(context).showChatNotification()
    }

    companion object {
        const val KEY_TEXT_REPLY = "reply"
        const val REQUEST_CONTENT = 13
    }
}