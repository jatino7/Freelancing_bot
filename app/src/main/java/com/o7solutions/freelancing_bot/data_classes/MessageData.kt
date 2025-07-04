package com.o7solutions.freelancing_bot.data_classes

data class MessageData(
    var chatId: Long ?= 0,
    var messageList : ArrayList<Message>
)

data class Message(
    val messageId: String ?= null,
    var senderId: String ?= null,
    var receiverId: String ?= null,
    var message: String ?= null,
    var status: Boolean ?= false,
    var time: Long ?= 0
)
