package com.wafflestudio.spring2025.course.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("courses")
data class Course(
    @Id
    var id: Long? = null,
    var year: Int,
    var term: String,
    var category: String?,
    var college: String?,
    var department: String?,
    var program: String?,
    var grade: Int?,
    @Column("course_code")
    var courseCode: String,
    @Column("class_code")
    var classCode: String,
    var title: String,
    var credit: Int,
    var professor: String?,
    var room: String?,
)
