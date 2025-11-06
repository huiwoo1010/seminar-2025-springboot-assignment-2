package com.wafflestudio.spring2025.timetable.dto

import com.wafflestudio.spring2025.timetable.model.Semester

data class CreateTimeTableRequest(
    val name: String,
    val semester: Semester,
    val year: Int,
)
