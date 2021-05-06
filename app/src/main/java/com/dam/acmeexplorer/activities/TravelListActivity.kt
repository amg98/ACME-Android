package com.dam.acmeexplorer.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dam.acmeexplorer.R
import com.dam.acmeexplorer.databinding.ActivityTravelListBinding
import com.dam.acmeexplorer.extensions.showMessage
import com.dam.acmeexplorer.extensions.tryRequestLocationUpdates
import com.dam.acmeexplorer.listadapters.TravelListAdapter
import com.dam.acmeexplorer.listadapters.TravelListSmallAdapter
import com.dam.acmeexplorer.models.FilterParams
import com.dam.acmeexplorer.models.Travel
import com.dam.acmeexplorer.viewmodels.TravelListViewModel
import com.google.android.gms.location.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.qualifier.named
import java.util.*

class TravelListActivity : AppCompatActivity() {

    private val vm: TravelListViewModel by viewModel()
    private lateinit var binding: ActivityTravelListBinding
    private val userTravels: MutableMap<String, Boolean> by inject(named("UserTravels"))
    private var selectedItem = NO_ITEM_SELECTED
    private var addingItem = false
    private lateinit var locationServices: FusedLocationProviderClient

    companion object {
        const val RESULT_START_DATE = "startDate"
        const val RESULT_END_DATE = "endDate"
        const val RESULT_MIN_PRICE = "minPrice"
        const val RESULT_MAX_PRICE = "maxPrice"
        const val NO_ITEM_SELECTED = -1
    }

    private val getFilterParams = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode != Activity.RESULT_OK) return@registerForActivityResult

        val data = result.data?.extras
        if(data == null) {
            vm.requestTravels(this)
            return@registerForActivityResult
        }

        val startDate = data.get(RESULT_START_DATE) as Calendar
        val endDate = data.get(RESULT_END_DATE) as Calendar
        val minPrice = data.get(RESULT_MIN_PRICE) as Int
        val maxPrice = data.get(RESULT_MAX_PRICE) as Int
        val filterParams = FilterParams(startDate, endDate, minPrice, maxPrice)

        vm.requestTravels(this, filterParams)
        vm.saveFilter(this, filterParams)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        locationServices = LocationServices.getFusedLocationProviderClient(this)

        binding = ActivityTravelListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding) {

            vm.travels.observe(this@TravelListActivity) {
                if(it.isNotEmpty()) vm.startLocation(locationServices)
                setupList(it, columnSwitch.isChecked)
            }

            columnSwitch.setOnCheckedChangeListener { _, isChecked: Boolean ->
                setupList(vm.travels.value!!, isChecked)
            }

            searchButton.setOnClickListener {
                getFilterParams.launch(Intent(this@TravelListActivity, FilterParamsActivity::class.java))
            }

            addButton.setOnClickListener {
                addingItem = true
                startActivity(Intent(this@TravelListActivity, NewTravelActivity::class.java))
            }

            vm.loading.observe(this@TravelListActivity) {
                progressBar.visibility = if(it) View.VISIBLE else View.GONE
            }

            vm.toastMessage.observe(this@TravelListActivity) {
                showMessage(it)
            }

            vm.travelDistances.observe(this@TravelListActivity) {
                travelList.adapter?.notifyItemRangeChanged(0, it.size)
            }
        }

        vm.requestTravels(this)
    }

    override fun onStart() {
        super.onStart()
        updateSelectedItem()
        if(addingItem) {
            vm.requestTravels(this)
            addingItem = false
        }
    }

    override fun onResume() {
        super.onResume()
        if(vm.travels.value!!.isNotEmpty()) vm.startLocation(locationServices)
    }

    override fun onPause() {
        super.onPause()
        vm.stopLocation(locationServices)
    }

    private fun setupList(travels: List<Travel>, smallItems: Boolean) {
        with(binding) {
            if(smallItems) {
                travelList.adapter = TravelListSmallAdapter(this@TravelListActivity, travels, userTravels) { travelPos: Int, checkboxClicked: Boolean, checked: Boolean ->
                    onItemClick(travelPos, checkboxClicked, checked)
                }
            } else {
                travelList.adapter = TravelListAdapter(this@TravelListActivity, travels, userTravels, vm.travelDistances.value!!) { travelPos: Int, checkboxClicked: Boolean, checked: Boolean ->
                    onItemClick(travelPos, checkboxClicked, checked)
                }
            }

            travelList.layoutManager = GridLayoutManager(this@TravelListActivity, if(smallItems) 2 else 1)
        }
    }

    private fun onItemClick(travelPos: Int, checkboxClicked: Boolean, checked: Boolean) {
        return if (checkboxClicked) {
            vm.onCheckboxClicked(travelPos, checked)
        } else {
            selectedItem = travelPos
            startActivity(vm.getTravelIntent(this@TravelListActivity, travelPos))
        }
    }

    private fun updateSelectedItem() {
        if(selectedItem == NO_ITEM_SELECTED) return
        binding.travelList.adapter?.notifyItemChanged(selectedItem)
        selectedItem = NO_ITEM_SELECTED
    }
}
