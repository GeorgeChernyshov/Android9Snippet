package com.example.post28

object DIUtils {
    lateinit var replyRepository: ReplyRepository

    fun init() {
        replyRepository = ReplyRepository()
    }
}