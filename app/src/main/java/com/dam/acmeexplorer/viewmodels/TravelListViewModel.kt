package com.dam.acmeexplorer.viewmodels

import android.content.Context
import android.content.Intent
import android.location.Location
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dam.acmeexplorer.R
import com.dam.acmeexplorer.activities.TravelDetailActivity
import com.dam.acmeexplorer.exceptions.AlertException
import com.dam.acmeexplorer.extensions.tryRequestLocationUpdates
import com.dam.acmeexplorer.models.FilterParams
import com.dam.acmeexplorer.models.Travel
import com.dam.acmeexplorer.repositories.FilterRepository
import com.dam.acmeexplorer.repositories.TravelRepository
import com.dam.acmeexplorer.utils.Units
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class TravelListViewModel(private val travelRepository: TravelRepository,
                          private val userTravels: MutableMap<String, Boolean>,
                          private val filterRepository: FilterRepository) : ViewModel() {

    private val _travels = MutableLiveData(listOf<Travel>())
    val travels: LiveData<List<Travel>> get() = _travels

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> get() = _loading

    private val _toastMessage = MutableLiveData<String>()
    val toastMessage: LiveData<String> get() = _toastMessage

    private val _travelDistances = MutableLiveData<MutableList<Double>>(mutableListOf())
    val travelDistances: LiveData<MutableList<Double>> get() = _travelDistances

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            locationResult ?: return
            val location = locationResult.lastLocation
            if(travelDistances.value!!.size == 0) return

            travels.value?.forEachIndexed { i, travel ->
                val travelLocation = Location("")
                travelLocation.latitude = travel.weather.coords.latitude
                travelLocation.longitude = travel.weather.coords.longitude
                _travelDistances.value!![i] = location.distanceTo(travelLocation) * Units.M_TO_KM
            }

            // Notify the activity
            _travelDistances.value = _travelDistances.value
        }
    }

    fun startLocation(locationServices: FusedLocationProviderClient) {
        locationServices.tryRequestLocationUpdates(locationCallback)
    }

    fun stopLocation(locationServices: FusedLocationProviderClient) {
        locationServices.removeLocationUpdates(locationCallback)
    }

    fun requestTravels(context: Context, filterParams: FilterParams? = null) = viewModelScope.launch {

        _loading.value = true

        try {
            val travels = withContext(Dispatchers.IO) {
                val params = filterParams ?: filterRepository.loadFilter(context)
                if(params == null) {
                    travelRepository.getTravels()
                } else {
                    travelRepository.getTravels(params)
                }
            }

            _travelDistances.value = MutableList(travels.size) { -1.0 }
            _travels.value = travels
        } catch (e: AlertException) {
            _toastMessage.value = e.asString(context)
        }

        _loading.value = false
    }

    fun saveFilter(context: Context, filterParams: FilterParams) {
        filterRepository.saveFilter(context, filterParams)
    }

    fun getTravelIntent(context: Context, position: Int): Intent {
        return Intent(context, TravelDetailActivity::class.java).apply {
            putExtra(TravelDetailActivity.INTENT_TRAVEL, travels.value!![position])
        }
    }

    fun onCheckboxClicked(position: Int, checked: Boolean) {
        val id = travels.value!![position].id
        if(!checked) {
            userTravels.remove(id)
        } else {
            userTravels[id] = true
        }
    }
}
