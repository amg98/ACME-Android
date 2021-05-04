package com.dam.acmeexplorer.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dam.acmeexplorer.databinding.ActivityTravelListBinding
import com.dam.acmeexplorer.listadapters.TravelListAdapter
import com.dam.acmeexplorer.listadapters.TravelListSmallAdapter
import com.dam.acmeexplorer.models.Travel
import com.dam.acmeexplorer.viewmodels.TravelListViewModel
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.qualifier.named
import java.util.*

class TravelListActivity : AppCompatActivity() {

    private val vm: TravelListViewModel by viewModel()
    private lateinit var binding: ActivityTravelListBinding
    private val userTravels: MutableMap<String, Boolean> by inject(named("UserTravels"))
    private var itemCheckedState = false
    private var selectedItem = -1

    private val getFilterParams = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode != Activity.RESULT_OK) return@registerForActivityResult

        val data = result.data?.extras
        if(data == null) {
            vm.requestTravels()
            return@registerForActivityResult
        }

        val startDate = data.get("startDate") as Calendar
        val endDate = data.get("endDate") as Calendar
        val minPrice = data.get("minPrice") as Int
        val maxPrice = data.get("maxPrice") as Int

        saveFilter(startDate, endDate, minPrice, maxPrice)

        vm.requestTravels(startDate.time, endDate.time, minPrice, maxPrice)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTravelListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding) {

            vm.travels.observe(this@TravelListActivity) {
                setupList(it, binding.columnSwitch.isChecked)
            }

            columnSwitch.setOnCheckedChangeListener { _, isChecked: Boolean ->
                setupList(vm.travels.value!!, isChecked)
            }

            searchButton.setOnClickListener {
                getFilterParams.launch(Intent(this@TravelListActivity, FilterParamsActivity::class.java))
            }

            addButton.setOnClickListener {
                startActivity(Intent(this@TravelListActivity, NewTravelActivity::class.java))
            }
        }

        requestTravels()
    }

    override fun onStart() {
        super.onStart()
        refreshList()
    }

    private fun setupList(travels: List<Travel>, smallItems: Boolean) {
        with(binding) {
            if(smallItems) {
                travelList.adapter = TravelListSmallAdapter(this@TravelListActivity, travels, userTravels) { travelPos: Int, checkboxClicked: Boolean ->
                    onItemClick(travelPos, checkboxClicked)
                }
            } else {
                travelList.adapter = TravelListAdapter(this@TravelListActivity, travels, userTravels) { travelPos: Int, checkboxClicked: Boolean ->
                    onItemClick(travelPos, checkboxClicked)
                }
            }

            travelList.layoutManager = GridLayoutManager(this@TravelListActivity, if(smallItems) 2 else 1)
        }
    }

    private fun onItemClick(travelPos: Int, checkboxClicked: Boolean): Boolean {
        return if (checkboxClicked) {
            vm.onCheckboxClicked(travelPos)
        } else {
            val travels = vm.travels.value!!
            selectedItem = travelPos
            itemCheckedState = userTravels.contains(travels[travelPos].id)
            startActivity(vm.getTravelIntent(this@TravelListActivity, travelPos))
            true
        }
    }

    private fun refreshList() {
        if(selectedItem == -1) return
        with(binding) {
            val travels = vm.travels.value!!
            val id = travels[selectedItem].id
            if(itemCheckedState != userTravels.contains(id)) {
                (travelList.adapter as RecyclerView.Adapter).notifyItemChanged(selectedItem)
                selectedItem = -1
            }
        }
    }

    private fun saveFilter(startDate: Calendar, endDate: Calendar, minPrice: Int, maxPrice: Int) {
        val sharedPref = getSharedPreferences("filter", Context.MODE_PRIVATE) ?: return
        with (sharedPref.edit()) {
            putLong("startDate", startDate.timeInMillis)
            putLong("endDate", endDate.timeInMillis)
            putInt("minPrice", minPrice)
            putInt("maxPrice", maxPrice)
            apply()
        }
    }

    private fun requestTravels() {
        val sharedPref = getSharedPreferences("filter", Context.MODE_PRIVATE)
        if(sharedPref == null) {
            vm.requestTravels()
            return
        }

        val startDate = sharedPref.getLong("startDate", -1)
        val endDate = sharedPref.getLong("endDate", -1)
        val minPrice = sharedPref.getInt("minPrice", -1)
        val maxPrice = sharedPref.getInt("maxPrice", -1)

        if(startDate < 0 || endDate < 0 || minPrice < 0 || maxPrice < 0) {
            vm.requestTravels()
            return
        }

        val startDateCalendar = Calendar.getInstance()
        val endDateCalendar = Calendar.getInstance()
        startDateCalendar.timeInMillis = startDate
        endDateCalendar.timeInMillis = endDate

        vm.requestTravels(startDateCalendar.time, endDateCalendar.time, minPrice, maxPrice)
    }
}
