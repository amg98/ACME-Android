package com.dam.acmeexplorer.providers

import com.dam.acmeexplorer.models.Travel
import java.util.*

interface TravelProvider {
    suspend fun getTravels(): List<Travel>?
    suspend fun getTravels(startDate: Date, endDate: Date, minPrice: Int, maxPrice: Int): List<Travel>?
    suspend fun getTravels(travels: Set<String>): List<Travel>?
}
