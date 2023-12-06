package com.studies.rrbmustudies.OthersFeatures.model

data class FeedbackModel(
    var rating: String,
    var additionalComment: String? = null,
    var userEmail: String,
    var userUID: String,
    var timestamp: String
)
