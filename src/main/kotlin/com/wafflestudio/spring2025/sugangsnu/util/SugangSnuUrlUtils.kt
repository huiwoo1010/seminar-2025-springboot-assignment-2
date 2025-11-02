package com.wafflestudio.spring2025.sugangsnu.util

import com.wafflestudio.spring2025.common.Term

object SugangSnuUrlUtils {
    fun toSugangCode(term: Term): String =
        when (term) {
            Term.SPRING -> "U000200001U000300001"
            Term.SUMMER -> "U000200001U000300002"
            Term.FALL -> "U000200002U000300001"
            Term.WINTER -> "U000200002U000300002"
        }
}
