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
import com.dam.acmeexplorer.extensions.getLocationPermission
import com.dam.acmeexplorer.extensions.tryRequestLocationUpdates
import com.dam.acmeexplorer.listadapters.MenuListAdapter
import com.dam.acmeexplorer.viewmodels.MainViewModel
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.LatLng
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.qualifier.named

class MainActivity : FragmentActivity() {

    private val vm: MainViewModel by viewModel()
    private lateinit var binding: ActivityMainBinding
    private lateinit var locationServices: FusedLocationProviderClient

    companion object {
        const val LOCATION_PERMISSION_ID = 0x123
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        locationServices = LocationServices.getFusedLocationProviderClient(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding) {
            menuList.adapter = MenuListAdapter(vm.menuEntries) { itemPosition: Int ->
                startActivity(vm.getIntentForMenuItem(this@MainActivity, itemPosition))
            }
        }

        val mapFragment = supportFragmentManager.findFragmentByTag("map") as SupportMapFragment
        mapFragment.getMapAsync { vm.googleMap = it }

        getLocationPermission(LOCATION_PERMISSION_ID) {
            vm.startLocation(locationServices)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        vm.stopLocation(locationServices)
        vm.logout()
    }

    override fun onResume() {
        super.onResume()
        vm.stopLocation(locationServices)
    }

    override fun onPause() {
        super.onPause()
        vm.stopLocation(locationServices)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        vm.loadUserTravels(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        vm.saveUserTravels(outState)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode) {
            LOCATION_PERMISSION_ID -> {
                if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    vm.startLocation(locationServices)
                }
            }
        }
    }
}
