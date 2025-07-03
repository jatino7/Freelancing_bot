package com.o7solutions.freelancing_bot.data_classes

data class Users(
    val id: String,
    val name: String,
    val role: Int,
    val email: String,
    val description: String,
) {

    constructor(): this("","",0,"","")
}