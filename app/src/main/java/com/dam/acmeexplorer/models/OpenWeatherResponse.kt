package com.dam.acmeexplorer.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class OpenWeatherCoords (
        @SerializedName("lon") val longitude: Double,
        @SerializedName("lat") val latitude: Double
        ): Parcelable

@Parcelize
data class OpenWeatherMain (
        @SerializedName("temp") val temp: Double,
        @SerializedName("pressure") val pressure: Int,
        @SerializedName("humidity") val humidity: Int
        ): Parcelable

@Parcelize
data class OpenWeatherWind (
        @SerializedName("speed") val speed: Double,
        ): Parcelable

@Parcelize
data class OpenWeatherResponse (
        @SerializedName("coord") val coords: OpenWeatherCoords,
        @SerializedName("main") val main: OpenWeatherMain,
        @SerializedName("wind") val wind: OpenWeatherWind
        ): Parcelable
