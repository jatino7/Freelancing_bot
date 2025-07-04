package com.o7solutions.freelancing_bot.data_classes

data class job(
    val userId: String,
    val title: String,
    val description: String,
    val cost: String,
    val deadline: String,
    val timestamp: Long,
    var status: Int = 0
) {
    constructor(): this("","","","","",0,0)
}
