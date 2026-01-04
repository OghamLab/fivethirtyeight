package com.ola.fivethirtyeight.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url




interface ApiService {

    companion object {

        const val BASE_URL = "https://abcnews.go.com/"
    }


    @GET
    suspend fun getFeedItems(@Url Url: String): Response<String>

    @GET

    suspend fun getInteractive(@Url Url: String): Response<String>






}