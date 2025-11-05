package com.wafflestudio.spring2025.timetable

import com.wafflestudio.spring2025.DomainException
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode

sealed class TimeTableException(
    errorCode: Int,
    httpStatusCode: HttpStatusCode,
    msg: String,
    cause: Throwable? = null,
) : DomainException(errorCode, httpStatusCode, msg, cause)

class TimeTableNameBlankException :
    TimeTableException(
        errorCode = 0,
        httpStatusCode = HttpStatus.BAD_REQUEST,
        msg = "TimeTable name is blank",
    )

class TimeTableNotFoundException :
    TimeTableException(
        errorCode = 1,
        httpStatusCode = HttpStatus.NOT_FOUND,
        msg = "TimeTable not found",
    )

class TimeTableForbiddenException :
    TimeTableException(
        errorCode = 2,
        httpStatusCode = HttpStatus.FORBIDDEN,
        msg = "Forbidden to access this timetable",
    )

class TimeTableCourseOverlappedException :
    TimeTableException(
        errorCode = 3,
        httpStatusCode = HttpStatus.CONFLICT,
        msg = "Course time overlaps with existing timetable",
    )
