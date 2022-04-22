package com.equationl.giteetodo.data

import com.equationl.giteetodo.data.auth.OAuthApi
import com.equationl.giteetodo.data.repos.ReposApi
import com.equationl.giteetodo.data.user.UserApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.Proxy
import java.util.concurrent.TimeUnit

object RetrofitManger {
    private var userApi: UserApi? = null
    private var reposApi: ReposApi? = null
    private var oAuthApi: OAuthApi? = null

    private const val CONNECTION_TIME_OUT = 10L
    private const val READ_TIME_OUT = 10L

    var BaseUrl = "https://gitee.com/api/v5/"

    fun getUserApi(): UserApi {
        if (userApi == null) {
            synchronized(this) {
                if (userApi == null) {
                    val okHttpClient =
                        buildOkHttpClient()
                    userApi =
                        buildRetrofit(
                            BaseUrl,
                            okHttpClient
                        ).create(UserApi::class.java)
                }
            }
        }
        return userApi!!
    }

    fun getReposApi(): ReposApi {
        if (reposApi == null) {
            synchronized(this) {
                if (reposApi == null) {
                    val okHttpClient =
                        buildOkHttpClient()
                    reposApi =
                        buildRetrofit(
                            BaseUrl,
                            okHttpClient
                        ).create(ReposApi::class.java)
                }
            }
        }
        return reposApi!!
    }

    fun getOAuthApi(): OAuthApi {
        if (oAuthApi == null) {
            synchronized(this) {
                if (oAuthApi == null) {
                    val okHttpClient =
                        buildOkHttpClient()
                    oAuthApi =
                        buildRetrofit(
                            "https://gitee.com/oauth/",
                            okHttpClient
                        ).create(OAuthApi::class.java)
                }
            }
        }
        return oAuthApi!!
    }

    private fun buildOkHttpClient(): OkHttpClient.Builder {
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(CONNECTION_TIME_OUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIME_OUT, TimeUnit.SECONDS)
            .proxy(Proxy.NO_PROXY)
    }

    private fun buildRetrofit(baseUrl: String, builder: OkHttpClient.Builder): Retrofit {
        val client = builder.build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client).build()
    }
}