package com.dam.acmeexplorer.repositories

import android.content.Context
import com.dam.acmeexplorer.models.FilterParams
import java.util.*

class FilterRepository {

    companion object {
        private const val PREF_NAME = "filter"
        private const val PREF_START_DATE = "startDate"
        private const val PREF_END_DATE = "endDate"
        private const val PREF_MIN_PRICE = "minPrice"
        private const val PREF_MAX_PRICE = "maxPrice"
    }

    fun deleteFilter(context: Context): Boolean {
        val sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE) ?: return false

        with (sharedPref.edit()) {
            remove(PREF_START_DATE)
            remove(PREF_END_DATE)
            remove(PREF_MIN_PRICE)
            remove(PREF_MAX_PRICE)
            apply()
        }

        return true
    }

    fun loadFilter(context: Context): FilterParams? {
        val sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE) ?: return null

        val startDateVal = sharedPref.getLong(PREF_START_DATE, -1)
        val endDateVal = sharedPref.getLong(PREF_END_DATE, -1)
        val minPrice = sharedPref.getInt(PREF_MIN_PRICE, -1)
        val maxPrice = sharedPref.getInt(PREF_MAX_PRICE, -1)

        if(startDateVal < 0 || endDateVal < 0 || minPrice < 0 || maxPrice < 0) return null

        val startDate = Calendar.getInstance()
        startDate.timeInMillis = startDateVal

        val endDate = Calendar.getInstance()
        endDate.timeInMillis = endDateVal

        return FilterParams(startDate, endDate, minPrice, maxPrice)
    }

    fun saveFilter(context: Context, params: FilterParams): Boolean {
        val sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE) ?: return false
        with (sharedPref.edit()) {
            putLong(PREF_START_DATE, params.startDate.timeInMillis)
            putLong(PREF_END_DATE, params.endDate.timeInMillis)
            putInt(PREF_MIN_PRICE, params.minPrice)
            putInt(PREF_MAX_PRICE, params.maxPrice)
            apply()
        }

        return true
    }
}
