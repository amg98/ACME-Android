package com.dam.acmeexplorer.activities

import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dam.acmeexplorer.R
import com.dam.acmeexplorer.databinding.ActivitySelectedTravelsBinding
import com.dam.acmeexplorer.extensions.showMessage
import com.dam.acmeexplorer.extensions.tryRequestLocationUpdates
import com.dam.acmeexplorer.listadapters.TravelListAdapter
import com.dam.acmeexplorer.viewmodels.SelectedTravelsViewModel
import com.google.android.gms.location.*
import org.koin.android.viewmodel.ext.android.viewModel

class SelectedTravelsActivity : AppCompatActivity() {

    private val vm: SelectedTravelsViewModel by viewModel()
    private lateinit var binding: ActivitySelectedTravelsBinding
    private lateinit var locationServices: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        locationServices = LocationServices.getFusedLocationProviderClient(this)

        binding = ActivitySelectedTravelsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding) {
            vm.travels.observe(this@SelectedTravelsActivity) {

                travelList.adapter = TravelListAdapter(this@SelectedTravelsActivity, it, vm.userTravels, vm.travelDistances.value!!) { travelPos: Int, checkboxClicked: Boolean ->
                    onItemClick(travelPos, checkboxClicked)
                }

                if(it.size > 0) {
                    showMessage(getString(R.string.selectedTravels, it.size))
                }
            }

            vm.travelDistances.observe(this@SelectedTravelsActivity) {
                travelList.adapter?.notifyDataSetChanged()
            }

            vm.toastMessage.observe(this@SelectedTravelsActivity) {
                showMessage(it)
            }

            vm.loading.observe(this@SelectedTravelsActivity) {
                progressBar.visibility = if(it) View.VISIBLE else View.GONE
            }
        }

        vm.requestTravels(this)
    }

    override fun onResume() {
        super.onResume()
        if(vm.userTravels.isNotEmpty()) {
            vm.startLocation(locationServices)
        }
    }

    override fun onPause() {
        super.onPause()
        vm.stopLocation(locationServices)
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
}
