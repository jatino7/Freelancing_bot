package com.o7solutions.freelancing_bot.data_classes

data class RecentMessage(

    val senderId: String ?= null,
    val receiverId: String ?= null,
    val message: String ?= null,
    val time: Long ?= 0
)
