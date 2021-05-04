package com.dam.acmeexplorer.api

import com.dam.acmeexplorer.BuildConfig
import com.dam.acmeexplorer.models.OpenWeatherResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenWeatherService {

    @GET("data/2.5/weather")
    suspend fun getLocation(@Query("q") city: String, @Query("appid") apiKey: String = BuildConfig.OPENWEATHER_API_KEY): Response<OpenWeatherResponse>
}
