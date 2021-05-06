package com.dam.acmeexplorer.viewmodels

import android.content.Context
import android.location.Location
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dam.acmeexplorer.R
import com.dam.acmeexplorer.extensions.tryRequestLocationUpdates
import com.dam.acmeexplorer.utils.Units
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult

class TravelDetailViewModel(private val userTravels: MutableMap<String, Boolean>) : ViewModel() {

    private val _toastMessage = MutableLiveData<String>()
    val toastMessage: LiveData<String> get() = _toastMessage

    private val _actionButtonText = MutableLiveData<String>()
    val actionButtonText: LiveData<String> get() = _actionButtonText

    private val _distance = MutableLiveData(-1.0)
    val distance: LiveData<Double> get() = _distance

    private val travelLocation = Location("")

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            locationResult ?: return
            val location = locationResult.lastLocation

            _distance.value = location.distanceTo(travelLocation) * Units.M_TO_KM
        }
    }

    fun startLocation(locationServices: FusedLocationProviderClient) {
        locationServices.tryRequestLocationUpdates(locationCallback)
    }

    fun stopLocation(locationServices: FusedLocationProviderClient) {
        locationServices.removeLocationUpdates(locationCallback)
    }

    fun setTravelLocation(latitude: Double, longitude: Double) {
        travelLocation.latitude = latitude
        travelLocation.longitude = longitude
    }

    fun updateSelectButton(context: Context, travelID: String, buyEnabled: Boolean) {
        if(buyEnabled) {
            _actionButtonText.value = context.getString(R.string.buyText)
            return
        }

        _actionButtonText.value = context.getString(if(userTravels.contains(travelID)) R.string.removeText else R.string.selectText)
    }

    fun onActionButton(context: Context, travelID: String, buyEnabled: Boolean) {
        if(buyEnabled) {
            _toastMessage.value = context.getString(R.string.buyTimeText)
            return
        }

        if(userTravels.contains(travelID)) {
            userTravels.remove(travelID)
            _actionButtonText.value = context.getString(R.string.selectText)
        } else {
            userTravels[travelID] = true
            _actionButtonText.value = context.getString(R.string.removeText)
        }
    }
}
