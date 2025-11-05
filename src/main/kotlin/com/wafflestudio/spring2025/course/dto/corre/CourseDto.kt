package com.wafflestudio.spring2025.course.dto.corre

import com.wafflestudio.spring2025.course.model.Course

data class CourseDto(
    val id: Long,
    val category: String?,
    val college: String?,
    val department: String?,
    val program: String?,
    val grade: Int?,
    val rawTime: String?,
    val courseCode: String,
    val classCode: String,
    val title: String,
    val credit: Int,
    val room: String?,
    val professor: String?,
) {
    constructor(course: Course) : this(
        id = course.id!!,
        category = course.category,
        college = course.college,
        department = course.department,
        program = course.program,
        grade = course.grade,
        rawTime = course.rawTime,
        courseCode = course.courseCode,
        classCode = course.classCode,
        title = course.title,
        credit = course.credit,
        room = course.room,
        professor = course.professor,
    )
}
