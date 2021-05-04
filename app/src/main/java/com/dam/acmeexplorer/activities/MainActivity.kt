package com.dam.acmeexplorer.activities

import android.Manifest
import android.R
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.dam.acmeexplorer.databinding.ActivityMainBinding
import com.dam.acmeexplorer.listadapters.MenuListAdapter
import com.dam.acmeexplorer.viewmodels.MainViewModel
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.LatLng
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.qualifier.named

class MainActivity : FragmentActivity() {

    private val userTravels: MutableMap<Int, Boolean> by inject(named("UserTravels"))
    private val vm: MainViewModel by viewModel()
    private lateinit var binding: ActivityMainBinding
    private var googleMap: GoogleMap? = null

    companion object {
        const val LOCATION_PERMISSION_ID = 0x123
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding) {
            menuList.adapter = MenuListAdapter(vm.menuEntries) { itemPosition: Int ->
                startActivity(vm.getIntentForMenuItem(this@MainActivity, itemPosition))
            }
        }

        val mapFragment = supportFragmentManager.findFragmentByTag("map") as SupportMapFragment
        mapFragment.getMapAsync {
            googleMap = it
        }

        checkLocationPermission()
    }

    override fun onDestroy() {
        super.onDestroy()
        vm.logout()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        val travelIDs = savedInstanceState.getIntegerArrayList("UserTravels")
        userTravels.clear()
        travelIDs?.forEach {
            userTravels[it] = true
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        val travelIDs = ArrayList<Int>()
        travelIDs.addAll(userTravels.keys)
        outState.putIntegerArrayList("UserTravels", travelIDs)
    }

    private fun checkLocationPermission() {
        val permissions = listOf(Manifest.permission.ACCESS_FINE_LOCATION)
        if(ContextCompat.checkSelfPermission(this, permissions[0]) != PackageManager.PERMISSION_GRANTED) {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0])) {
                // TODO Dialog
                ActivityCompat.requestPermissions(
                    this,
                    permissions.toTypedArray(),
                    LOCATION_PERMISSION_ID
                )
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    permissions.toTypedArray(),
                    LOCATION_PERMISSION_ID
                )
            }
        } else {
            getLocation()
            // TODO Stop location on onPause()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == LOCATION_PERMISSION_ID) {
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation()
            }
        }
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            locationResult ?: return
            val location = locationResult.lastLocation

            val mapLocation = LatLng(location.latitude, location.longitude)
            googleMap ?: return

            googleMap!!.clear()
            googleMap!!.addMarker(
                MarkerOptions()
                    .position(mapLocation)
                    .title("Mi posición actual")
            )
            googleMap!!.moveCamera(CameraUpdateFactory.newLatLng(mapLocation))
        }
    }

    private fun getLocation() {
        try {
            val req = LocationRequest.create()
            req.interval = 5000
            req.priority = LocationRequest.PRIORITY_LOW_POWER
            req.smallestDisplacement = 5.0f

            val locationServices = LocationServices.getFusedLocationProviderClient(this)
            locationServices.requestLocationUpdates(req, locationCallback, Looper.getMainLooper())

        } catch (e: SecurityException) {
            // TODO
        }
    }
}
