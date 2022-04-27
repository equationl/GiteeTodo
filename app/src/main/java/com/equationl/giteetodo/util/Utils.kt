package com.equationl.giteetodo.util

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

object Utils {
    private const val TAG = "Utils"

    fun String.isEmail(): Boolean {
        val emailAddressRegex = Regex(
            "[a-zA-Z0-9+._%\\-]{1,256}" +
                    "@" +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                    "(" +
                    "\\." +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                    ")+"
        )


        if (this.matches(emailAddressRegex)){
            return true
        }

        return false
    }

    fun getDateTimeString(sourceDateTime: String, pattern: String = "M月dd日"): String {

        val date = LocalDateTime.parse(sourceDateTime, DateTimeFormatter.ISO_OFFSET_DATE_TIME)

        return date.format(DateTimeFormatter.ofPattern(pattern, Locale.CHINA))
    }
}