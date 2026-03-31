package com.o7solutions.freelancing_bot.data_classes

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val headline: String = "",         // e.g., "Senior Android Developer | Kotlin Enthusiast"
    val profileImageUrl: String = "",
    val coverImageUrl: String = "",
    val location: String = "",
    val about: String = "",            // Renamed from description for professional feel
    val role: Int = 0,
    var resumeUrl: String ="",// 0: Candidate, 1: Recruiter, 2: Company Admin
    val skills: ArrayList<String> = ArrayList(),
    var experience: ArrayList<Experience> = ArrayList(),
    val connectionsCount: Int = 0
) {
    constructor() : this("", "", "", "", "", "", "", "", 0,"", ArrayList(), ArrayList(), 0)
}

data class Experience(
    var id: String = "",
    var title: String = "",            // e.g., "Software Engineer"
    var companyName: String = "",
    var location: String = "",
    var startDate: String = "",        // e.g., "Jan 2022"
    var endDate: String = "",          // e.g., "Present"
    var isCurrentRole: Boolean = false,
    var description: String = ""       // Summary of responsibilities
) {
    constructor() : this("", "", "", "", "", "", false, "")
}