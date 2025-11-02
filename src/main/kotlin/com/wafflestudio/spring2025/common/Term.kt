package com.wafflestudio.spring2025.common

enum class Term(
    val koreanName: String,
) {
    SPRING("봄학기"),
    SUMMER("여름학기"),
    FALL("가을학기"),
    WINTER("겨울학기"),
    ;

    companion object {
        fun fromKoreanName(name: String): Term? = entries.firstOrNull { it.koreanName == name }
    }
}
