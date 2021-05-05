package com.dam.acmeexplorer.viewmodels

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import com.dam.acmeexplorer.R
import com.dam.acmeexplorer.activities.SelectedTravelsActivity
import com.dam.acmeexplorer.activities.TravelListActivity
import com.dam.acmeexplorer.extensions.tryRequestLocationUpdates
import com.dam.acmeexplorer.models.MenuEntry
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth

class MainViewModel(private val auth: FirebaseAuth, private val userTravels: MutableMap<Int, Boolean>) : ViewModel() {

    companion object {
        const val BUNDLE_USER_TRAVELS = "UserTravels"
    }

    val menuEntries = listOf(
        MenuEntry(R.drawable.travel, "Lista de viajes"),
        MenuEntry(R.drawable.target, "Mis viajes")
    )

    var googleMap: GoogleMap? = null

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            locationResult ?: return
            googleMap ?: return

            val location = locationResult.lastLocation

            val mapLocation = LatLng(location.latitude, location.longitude)

            with(googleMap) {
                val marker = MarkerOptions().position(mapLocation).title("Mi posición actual")
                this?.clear()
                this?.addMarker(marker)
                this?.moveCamera(CameraUpdateFactory.newLatLng(mapLocation))
            }
        }
    }

    fun startLocation(locationServices: FusedLocationProviderClient) {
        locationServices.tryRequestLocationUpdates(locationCallback)
    }

    fun stopLocation(locationServices: FusedLocationProviderClient) {
        locationServices.removeLocationUpdates(locationCallback)
    }

    fun logout() {
        auth.signOut()
    }

    fun getIntentForMenuItem(context: Context, position: Int): Intent {
        return if(position == 0) {
            Intent(context, TravelListActivity::class.java)
        } else {
            Intent(context, SelectedTravelsActivity::class.java)
        }
    }

    fun saveUserTravels(bundle: Bundle) {
        val travelIDs = ArrayList<Int>()
        travelIDs.addAll(userTravels.keys)
        bundle.putIntegerArrayList(BUNDLE_USER_TRAVELS, travelIDs)
    }

    fun loadUserTravels(bundle: Bundle) {
        val travelIDs = bundle.getIntegerArrayList(BUNDLE_USER_TRAVELS)
        userTravels.clear()
        travelIDs?.forEach {
            userTravels[it] = true
        }
    }
}
