package com.o7solutions.freelancing_bot.data_classes

data class job(
    val jobId: String = "",
    val posterId: String = "", // The person or company who posted it
    val title: String = "",
    val companyName: String = "",
    val companyLogoUrl: String = "",
    val location: String = "",
    val description: String = "",
    val jobType: String = "", // e.g., Full-time, Internship, Remote
    val salaryRange: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val skillsRequired: List<String> = emptyList(),
    var status: Int = 0 // 0 for active, 1 for closed
) {
    // Secondary constructor for Firebase/NoSQL compatibility
    constructor() : this("", "", "", "", "", "", "", "", "", 0L, emptyList(), 0)
}