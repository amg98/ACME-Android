package com.dam.acmeexplorer.viewmodels

import android.content.Context
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dam.acmeexplorer.activities.TravelDetailActivity
import com.dam.acmeexplorer.models.Travel
import com.dam.acmeexplorer.providers.TravelProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class TravelListViewModel(private val travelProvider: TravelProvider, private val userTravels: MutableMap<String, Boolean>) : ViewModel() {

    private val _travels = MutableLiveData(listOf<Travel>())
    val travels: LiveData<List<Travel>> get() = _travels

    fun requestTravels() {
        viewModelScope.launch {
            _travels.value = travelProvider.getTravels()
        }
    }

    fun requestTravels(startDate: Date, endDate: Date, minPrice: Int, maxPrice: Int) {
        viewModelScope.launch {
            _travels.value = travelProvider.getTravels(startDate, endDate, minPrice, maxPrice)
        }
    }

    fun getTravelIntent(context: Context, position: Int): Intent {
        return Intent(context, TravelDetailActivity::class.java).apply {
            putExtra("TRAVEL", travels.value!![position])
        }
    }

    fun onCheckboxClicked(position: Int): Boolean {
        val id = travels.value!![position].id
        return if(userTravels.contains(id)) {
            userTravels.remove(id)
            false
        } else {
            userTravels[id] = true
            true
        }
    }
}
