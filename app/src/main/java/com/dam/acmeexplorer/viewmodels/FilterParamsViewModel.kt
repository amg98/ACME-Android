package com.dam.acmeexplorer.viewmodels

import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.text.SimpleDateFormat
import java.util.*

class FilterParamsViewModel : ViewModel() {

    companion object {
        val DEFAULT_MIN_PRICE = 1000
        val DEFAULT_MAX_PRICE = 2000
    }

    val startDate: Calendar = Calendar.getInstance()
    val endDate: Calendar = Calendar.getInstance()

    private val _toastMessage = MutableLiveData<String>()
    val toastMessage: LiveData<String> get() = _toastMessage

    fun formatDate(date: Calendar): String {
        return SimpleDateFormat("dd/MM/yyyy", Locale.US).format(date.time)
    }

    fun onStartDate(day: Int, month: Int, year: Int): String {
        startDate.set(Calendar.YEAR, year)
        startDate.set(Calendar.MONTH, month)
        startDate.set(Calendar.DAY_OF_MONTH, day)

        return formatDate(startDate)
    }

    fun onEndDate(day: Int, month: Int, year: Int): String {
        endDate.set(Calendar.YEAR, year)
        endDate.set(Calendar.MONTH, month)
        endDate.set(Calendar.DAY_OF_MONTH, day)

        return formatDate(endDate)
    }

    fun onSearch(minPrice: Int, maxPrice: Int): Intent? {

        if(startDate > endDate) {
            _toastMessage.value = "Las fechas no son coherentes"
            return null
        }

        if(minPrice > maxPrice) {
            _toastMessage.value = "El rango de precios no es coherente"
            return null
        }

        val intent = Intent()
        intent.putExtra("startDate", startDate)
        intent.putExtra("endDate", endDate)
        intent.putExtra("minPrice", minPrice)
        intent.putExtra("maxPrice", maxPrice)
        return intent
    }
}
