package com.dam.acmeexplorer.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import java.util.*

@Parcelize
data class Travel(val id: String,
                  val title: String,
                  val imagesURL: List<String>,
                  val startDate: Date,
                  val endDate: Date,
                  val price: Int,
                  val startPlace: String,
                  val weather: OpenWeatherResponse? = null) : Parcelable
