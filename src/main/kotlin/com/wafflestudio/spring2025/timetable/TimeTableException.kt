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
        httpStatusCode = org.springframework.http.HttpStatus.BAD_REQUEST,
        msg = "TimeTable name is blank",
    )

// 시간표가 존재하지 않을 때
class TimeTableNotFoundException :
    TimeTableException(
        errorCode = 1,
        httpStatusCode = HttpStatus.NOT_FOUND,
        msg = "TimeTable not found",
    )

// 시간표 접근 권한이 없을 때
class TimeTableForbiddenException :
    TimeTableException(
        errorCode = 2,
        httpStatusCode = HttpStatus.FORBIDDEN,
        msg = "Forbidden to access this timetable",
    )
