package com.dam.acmeexplorer.activities

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dam.acmeexplorer.databinding.ActivityFilterParamsBinding
import com.dam.acmeexplorer.viewmodels.FilterParamsViewModel
import org.koin.android.viewmodel.ext.android.viewModel
import java.util.*

class FilterParamsActivity : AppCompatActivity() {

    private val vm: FilterParamsViewModel by viewModel()
    private lateinit var binding: ActivityFilterParamsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFilterParamsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding) {

            minPrice.setText(FilterParamsViewModel.DEFAULT_MIN_PRICE.toString())
            maxPrice.setText(FilterParamsViewModel.DEFAULT_MAX_PRICE.toString())
            loadFilter()

            startDate.setText(vm.formatDate(vm.startDate))
            startDate.setOnClickListener {
                showDatePickerDialog(vm.startDate, startDate, null) { day, month, year ->
                    vm.onStartDate(day, month, year)
                }
            }

            endDate.setText(vm.formatDate(vm.endDate))
            endDate.setOnClickListener {
                showDatePickerDialog(vm.endDate, endDate, vm.startDate) { day, month, year ->
                    vm.onEndDate(day, month, year)
                }
            }

            searchButton.setOnClickListener {
                val minPriceVal = if(minPrice.text.isNotEmpty()) Integer.parseInt(minPrice.text.toString()) else 0
                val maxPriceVal = if(maxPrice.text.isNotEmpty()) Integer.parseInt(maxPrice.text.toString()) else 0
                val intent = vm.onSearch(minPriceVal, maxPriceVal) ?: return@setOnClickListener
                setResult(RESULT_OK, intent)
                finish()
            }

            deleteButton.setOnClickListener {
                deleteFilter()
                setResult(RESULT_OK, Intent())
                finish()
            }
        }

        vm.toastMessage.observe(this) {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDatePickerDialog(cal: Calendar, editText: EditText, minDate: Calendar?, onDate: (Int, Int, Int) -> String) {
        val dialog = DatePickerDialog(this, { _, year, month, day ->
            editText.setText(onDate(day, month, year))
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
        dialog.datePicker.minDate = minDate?.timeInMillis ?: 0
        dialog.show()
    }

    private fun loadFilter() {
        val sharedPref = getSharedPreferences("filter", Context.MODE_PRIVATE) ?: return

        val startDate = sharedPref.getLong("startDate", -1)
        val endDate = sharedPref.getLong("endDate", -1)
        val minPrice = sharedPref.getInt("minPrice", -1)
        val maxPrice = sharedPref.getInt("maxPrice", -1)

        if(startDate < 0 || endDate < 0 || minPrice < 0 || maxPrice < 0) return

        val startDateCalendar = Calendar.getInstance()
        val endDateCalendar = Calendar.getInstance()
        startDateCalendar.timeInMillis = startDate
        endDateCalendar.timeInMillis = endDate

        vm.onStartDate(startDateCalendar.get(Calendar.DAY_OF_MONTH), startDateCalendar.get(Calendar.MONTH), startDateCalendar.get(Calendar.YEAR))
        vm.onEndDate(endDateCalendar.get(Calendar.DAY_OF_MONTH), endDateCalendar.get(Calendar.MONTH), endDateCalendar.get(Calendar.YEAR))

        binding.minPrice.setText(minPrice.toString())
        binding.maxPrice.setText(maxPrice.toString())
    }

    private fun deleteFilter() {
        val sharedPref = getSharedPreferences("filter", Context.MODE_PRIVATE) ?: return
        with (sharedPref.edit()) {
            remove("startDate")
            remove("endDate")
            remove("minPrice")
            remove("maxPrice")
            apply()
        }
    }
}
