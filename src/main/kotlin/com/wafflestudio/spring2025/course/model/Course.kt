package com.wafflestudio.spring2025.course.model

import com.wafflestudio.spring2025.common.Term
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("courses")
class Course(
    @Id
    var id: Long? = null,
    var year: Int,
    var term: Term,
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
