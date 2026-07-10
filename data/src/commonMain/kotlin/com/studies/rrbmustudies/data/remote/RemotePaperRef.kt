package com.studies.rrbmustudies.data.remote

import com.studies.rrbmustudies.data.dto.PaperDto

data class RemotePaperRef(
    val paperId: String,
    val courseId: String,
    val systemId: String,
    val partId: String,
    val dto: PaperDto,
)
