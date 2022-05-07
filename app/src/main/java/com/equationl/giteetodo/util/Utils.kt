package com.equationl.giteetodo.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
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

    val String.toColor
        get() = Color(android.graphics.Color.parseColor(this))

    /**
     * 将 [Color] 转为 十六进制字符串，并舍弃 Alpha 通道，返回字符串不包括 *#*
     * */
    val Color.toHexString
        get() = Integer.toHexString(this.toArgb()).substring(2).uppercase(Locale.CHINA)
}