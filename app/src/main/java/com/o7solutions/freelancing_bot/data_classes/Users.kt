package com.o7solutions.freelancing_bot.data_classes

data class Users(
    val id: String,
    val name: String,
    val role: Int,
    val email: String,
    val description: String,
    var experience: ArrayList<Experience>
) {

    constructor(): this("","",0,"","", ArrayList())
}

data class Experience(
    var id: Long ?= 0,
    var title: String ?= null,
    var description: String ?= null
)