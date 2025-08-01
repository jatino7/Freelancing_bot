package com.o7solutions.freelancing_bot.data_classes

data class Message(
    var senderId: String ?= null,
    var message: String ?= null,
    var time: Long ?= 0
)
