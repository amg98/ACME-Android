package com.dam.acmeexplorer.models

import java.util.*

data class FilterParams(
        val startDate: Calendar,
        val endDate: Calendar,
        val minPrice: Int,
        val maxPrice: Int
)
