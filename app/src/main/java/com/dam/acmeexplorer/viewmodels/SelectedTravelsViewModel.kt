package com.dam.acmeexplorer.viewmodels

import android.content.Context
import android.content.Intent
import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dam.acmeexplorer.R
import com.dam.acmeexplorer.activities.TravelDetailActivity
import com.dam.acmeexplorer.extensions.tryRequestLocationUpdates
import com.dam.acmeexplorer.models.Travel
import com.dam.acmeexplorer.repositories.TravelRepository
import com.dam.acmeexplorer.utils.Units
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SelectedTravelsViewModel(private val travelRepository: TravelRepository, val userTravels: MutableMap<String, Boolean>)
    : ViewModel() {

    private val _travels = MutableLiveData<MutableList<Travel>>(mutableListOf())
    val travels: LiveData<MutableList<Travel>> get() = _travels

    private val _travelDistances = MutableLiveData<MutableList<Double>>(mutableListOf())
    val travelDistances: LiveData<MutableList<Double>> get() = _travelDistances

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> get() = _loading

    private val _toastMessage = MutableLiveData<String>()
    val toastMessage: LiveData<String> get() = _toastMessage

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            locationResult ?: return
            val location = locationResult.lastLocation

            if(travelDistances.value!!.size == 0) return

            travels.value?.forEachIndexed { i, travel ->
                val travelLocation = Location("")
                travelLocation.latitude = travel.weather!!.coords.latitude
                travelLocation.longitude = travel.weather.coords.longitude
                _travelDistances.value!![i] = location.distanceTo(travelLocation) * Units.M_TO_KM
            }

            // Notify activity
            _travelDistances.value = _travelDistances.value
        }
    }

    fun startLocation(locationServices: FusedLocationProviderClient) {
        locationServices.tryRequestLocationUpdates(locationCallback)
    }

    fun stopLocation(locationServices: FusedLocationProviderClient) {
        locationServices.removeLocationUpdates(locationCallback)
    }

    fun requestTravels(context: Context) {

        if(userTravels.isEmpty()) {
            _toastMessage.value = context.getString(R.string.noSelectedTravels)
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _loading.value = true
            val travels = travelRepository.getTravels(userTravels.keys)?.toMutableList()

            if(travels == null) {
                _loading.value = false
                _toastMessage.value = context.getString(R.string.errorGettingTravels)
                return@launch
            }

            _travelDistances.value = MutableList(travels.size) { -1.0 }
            _travels.value = travels!!
            _loading.value = false
        }
    }

    fun onCheckboxClicked(travelPos: Int) {
        userTravels.remove(travels.value!![travelPos].id)
        travels.value!!.removeAt(travelPos)
    }

    fun getTravelIntent(context: Context, position: Int): Intent {
        return Intent(context, TravelDetailActivity::class.java).apply {
            putExtra(TravelDetailActivity.INTENT_TRAVEL, travels.value!![position])
            putExtra(TravelDetailActivity.INTENT_BUY, true)
        }
    }
}
