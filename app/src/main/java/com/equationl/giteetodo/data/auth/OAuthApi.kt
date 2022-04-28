package com.equationl.giteetodo.data.auth

import com.equationl.giteetodo.data.auth.model.response.Token
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.Query

interface OAuthApi {
    @FormUrlEncoded
    @POST("token")
    suspend fun getTokenByPsw(
        @Field("username") userName: String,
        @Field("password") password: String,
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String,
        @Field("scope") scope: String = "projects user_info issues notes",
        @Field("grant_type") grantType: String = "password"
    ): Response<Token>

    @POST("token")
    suspend fun getTokenByCode(
        @Query("code") code: String,
        @Query("client_id") clientId: String,
        @Query("redirect_uri") redirectUri: String,
        @Query("client_secret") clientSecret: String,
        @Query("grant_type") grantType: String = "authorization_code"
    ): Response<Token>

    @POST("token")
    suspend fun refreshToken(
        @Query("refresh_token") refreshToken: String,
        @Query("grant_type") grantType: String = "refresh_token"
    ): Response<Token>
}