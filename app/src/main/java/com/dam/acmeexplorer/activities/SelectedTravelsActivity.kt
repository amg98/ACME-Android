package com.dam.acmeexplorer.activities

import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dam.acmeexplorer.databinding.ActivitySelectedTravelsBinding
import com.dam.acmeexplorer.listadapters.TravelListAdapter
import com.dam.acmeexplorer.viewmodels.SelectedTravelsViewModel
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import org.koin.android.viewmodel.ext.android.viewModel

class SelectedTravelsActivity : AppCompatActivity() {

    private val vm: SelectedTravelsViewModel by viewModel()
    private lateinit var binding: ActivitySelectedTravelsBinding
    private lateinit var travelDistances: MutableList<Double>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySelectedTravelsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding) {
            vm.travels.observe(this@SelectedTravelsActivity) {

                travelDistances = MutableList(it.size) { 0.0 }

                travelList.adapter = TravelListAdapter(this@SelectedTravelsActivity, it, vm.userTravels, travelDistances) { travelPos: Int, checkboxClicked: Boolean ->
                    onItemClick(travelPos, checkboxClicked)
                }

                if(it.size > 0) {
                    Toast.makeText(this@SelectedTravelsActivity, "Hay ${it.size} viajes seleccionados", Toast.LENGTH_SHORT).show()
                    getLocation()
                }
            }
        }

        vm.requestTravels()
    }

    private fun onItemClick(travelPos: Int, checkboxClicked: Boolean): Boolean {
        if (checkboxClicked) {
            with(binding) {
                if(travelList.isComputingLayout){
                    return true
                }

                vm.onCheckboxClicked(travelPos)

                travelList.adapter?.notifyItemRemoved(travelPos)
                travelList.adapter?.notifyItemRangeChanged(travelPos, vm.userTravels.size)
            }
        } else {
            startActivity(vm.getTravelIntent(this@SelectedTravelsActivity, travelPos))
        }

        return false
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            locationResult ?: return
            val location = locationResult.lastLocation

            if(travelDistances.size == 0) return

            vm.travels.value?.forEachIndexed { i, travel ->
                val travelLocation = Location("")
                travelLocation.latitude = travel.weather.coords.latitude
                travelLocation.longitude = travel.weather.coords.longitude
                travelDistances[i] = location.distanceTo(travelLocation) / 1000.0
            }

            binding.travelList.adapter?.notifyDataSetChanged()
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
