package com.example.post28

import com.example.post28.notifications.ReplyRepository

object DIUtils {
    lateinit var replyRepository: ReplyRepository

    fun init() {
        replyRepository = ReplyRepository()
    }
}