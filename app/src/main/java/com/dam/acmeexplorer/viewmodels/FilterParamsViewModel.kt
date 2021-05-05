package com.dam.acmeexplorer.viewmodels

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dam.acmeexplorer.R
import com.dam.acmeexplorer.activities.FilterParamsActivity
import com.dam.acmeexplorer.activities.TravelListActivity
import com.dam.acmeexplorer.extensions.formatted
import com.dam.acmeexplorer.repositories.FilterRepository
import java.text.SimpleDateFormat
import java.util.*

class FilterParamsViewModel(private val filterRepository: FilterRepository) : ViewModel() {

    companion object {
        const val DEFAULT_MIN_PRICE = 1000
        const val DEFAULT_MAX_PRICE = 2000
    }

    val startDate: Calendar = Calendar.getInstance()
    val endDate: Calendar = Calendar.getInstance()

    private val _toastMessage = MutableLiveData<String>()
    val toastMessage: LiveData<String> get() = _toastMessage

    fun setStartDate(day: Int, month: Int, year: Int): String {
        startDate.set(Calendar.YEAR, year)
        startDate.set(Calendar.MONTH, month)
        startDate.set(Calendar.DAY_OF_MONTH, day)

        return startDate.formatted()
    }

    fun setEndDate(day: Int, month: Int, year: Int): String {
        endDate.set(Calendar.YEAR, year)
        endDate.set(Calendar.MONTH, month)
        endDate.set(Calendar.DAY_OF_MONTH, day)

        return endDate.formatted()
    }

    fun saveSearchParams(context: Context, minPrice: String, maxPrice: String): Intent? {

        val minPriceVal = parsePrice(minPrice)
        val maxPriceVal = parsePrice(maxPrice)

        if(minPriceVal > maxPriceVal) {
            _toastMessage.value = context.getString(R.string.validation_priceRange)
            return null
        }

        val intent = Intent()
        intent.putExtra(TravelListActivity.RESULT_START_DATE, startDate)
        intent.putExtra(TravelListActivity.RESULT_END_DATE, endDate)
        intent.putExtra(TravelListActivity.RESULT_MIN_PRICE, minPrice)
        intent.putExtra(TravelListActivity.RESULT_MAX_PRICE, maxPrice)
        return intent
    }

    fun loadFilter(context: Context, onLoad: (Int, Int) -> Unit) {

        val filterParams = filterRepository.loadFilter(context) ?: return

        startDate.time = filterParams.startDate.time
        endDate.time = filterParams.endDate.time

        onLoad(filterParams.minPrice, filterParams.maxPrice)
    }

    fun deleteFilter(context: Context) {
        filterRepository.deleteFilter(context)
    }

    private fun parsePrice(price: String): Int {
        return if(price.isNotEmpty()) Integer.parseInt(price) else 0
    }
}
