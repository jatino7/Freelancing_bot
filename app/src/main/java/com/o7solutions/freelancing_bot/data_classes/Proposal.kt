package com.o7solutions.freelancing_bot.data_classes

import com.google.firebase.Timestamp


data class Proposal(
    val userId: String,
    val description: String,
    val timestamp: Long,
    val forJob: String
) {
    constructor(): this("","",0,"")
}