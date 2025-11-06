package com.wafflestudio.spring2025.course

import com.wafflestudio.spring2025.DomainException
import org.springframework.http.HttpStatusCode

sealed class CourseException(
    errorCode: Int,
    httpStatusCode: HttpStatusCode,
    msg: String,
    cause: Throwable? = null,
) : DomainException(errorCode, httpStatusCode, msg, cause)

class CourseNotFoundException :
    CourseException(
        errorCode = 0,
        httpStatusCode = org.springframework.http.HttpStatus.NOT_FOUND,
        msg = "Course not found",
    )
