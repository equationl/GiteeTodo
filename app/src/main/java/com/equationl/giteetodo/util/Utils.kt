package com.equationl.giteetodo.util

import android.util.Log
import android.webkit.CookieManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.equationl.giteetodo.data.repos.RepoApi
import com.equationl.giteetodo.data.repos.model.response.Label
import com.equationl.giteetodo.util.datastore.DataKey
import com.equationl.giteetodo.util.datastore.DataStoreUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale


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

        return this.matches(emailAddressRegex)
    }

    fun getDateTimeString(sourceDateTime: String, pattern: String = "yyyy年M月dd日"): String {

        val date = LocalDateTime.parse(sourceDateTime, DateTimeFormatter.ISO_OFFSET_DATE_TIME)

        return date.format(DateTimeFormatter.ofPattern(pattern, Locale.CHINA))
    }

    /**
     * 获取可用 label，这会优先尝试从已保存数据获取，除非本地无数据或 [forceRequest] 为 true 才会从服务器获取
     *
     * 从服务器请求后会将请求到的数据保存到本地
     *
     * @param forceRequest 是否强制从服务器请求
     * @return 如果出错则返回 [emptyList]
     * */
    suspend fun getExistLabel(repoApi: RepoApi, forceRequest: Boolean = false): List<Label> {
        var localData: String? = null

        if (!forceRequest) {
            localData = DataStoreUtils.getSyncData(DataKey.EXIST_LABEL, "")
        }

        if (localData.isNullOrBlank()) {
            val repoPath = DataStoreUtils.getSyncData(DataKey.USING_REPO, "")
            val token = DataStoreUtils.getSyncData(DataKey.LOGIN_ACCESS_TOKEN, "")

            val response = repoApi.getExistLabels(
                repoPath.split("/")[0],
                repoPath.split("/")[1],
                token
            )
            return if (response.isSuccessful) {
                DataStoreUtils.saveStringData(DataKey.EXIST_LABEL, response.body()?.toJson() ?: "")
                response.body() ?: emptyList()
            } else {
                Log.w(TAG, "getExistLabel: 请求标签失败: ${response.message()}")
                emptyList()
            }
        }
        else {
            return try {
                val listType: Type = object : TypeToken<ArrayList<Label?>?>() {}.type
                Gson().fromJson(localData, listType)
            } catch (tr: Throwable) {
                Log.w(TAG, "getExistLabel: 请求标签失败：", tr)
                emptyList()
            }
        }
    }

    fun clearCookies() {
        CookieManager.getInstance().removeAllCookies(null)
        CookieManager.getInstance().flush()
    }

    /**
     * 将十六进制字符串表示的颜色转为 [Color]
     * */
    val String.toColor
        get() = Color(android.graphics.Color.parseColor(this))

    /**
     * 将 [Color] 转为 十六进制字符串，并舍弃 Alpha 通道，返回字符串不包括 *#*
     * */
    val Color.toHexString
        get() = Integer.toHexString(this.toArgb()).substring(2).uppercase(Locale.CHINA)
}