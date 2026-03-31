package com.o7solutions.freelancing_bot.data_classes

data class Proposal(
    val applicationId: String = "",
    val jobId: String = "",        // Refers to the specific Job ID
    val applicantId: String = "",  // The candidate's User ID
    val applicantName: String = "", // Helpful for quick list display
    val applicantProfilePic: String = "",
    val coverLetter: String = "",
    val resumeUrl: String = "",    // Link to PDF/Doc in Storage
    val timestamp: Long = System.currentTimeMillis(),
    var status: Int = 0,
    var posterId: String ?= null// 0: Applied, 1: Reviewed, 2: Shortlisted, 3: Rejected
) {
    // Empty constructor for Firebase/Realtime Database
    constructor() : this("", "", "", "", "", "", "", 0L, 0,"")
}