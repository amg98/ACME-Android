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

class SelectedTravelsViewModel(private val travelProvider: TravelProvider, val userTravels: MutableMap<String, Boolean>)
    : ViewModel() {

    private val _travels = MutableLiveData<MutableList<Travel>>(mutableListOf())
    val travels: LiveData<MutableList<Travel>> get() = _travels

    fun requestTravels() {
        viewModelScope.launch {
            _travels.value = travelProvider.getTravels(userTravels.keys)?.toMutableList()
        }
    }

    fun onCheckboxClicked(travelPos: Int) {
        userTravels.remove(travels.value!![travelPos].id)
        travels.value!!.removeAt(travelPos)
    }

    fun getTravelIntent(context: Context, position: Int): Intent {
        return Intent(context, TravelDetailActivity::class.java).apply {
            putExtra("TRAVEL", travels.value!![position])
            putExtra("BUY", true)
        }
    }
}
