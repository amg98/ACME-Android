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
import com.dam.acmeexplorer.extensions.formatted
import com.dam.acmeexplorer.extensions.showDatePickerDialog
import com.dam.acmeexplorer.extensions.showMessage
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
            startDate.setText(vm.startDate.formatted())
            endDate.setText(vm.endDate.formatted())

            vm.loadFilter(this@FilterParamsActivity) { minPriceVal, maxPriceVal ->
                minPrice.setText(minPriceVal.toString())
                maxPrice.setText(maxPriceVal.toString())
                startDate.setText(vm.startDate.formatted())
                endDate.setText(vm.endDate.formatted())
            }

            startDate.setOnClickListener {
                showDatePickerDialog(vm.startDate, startDate, null) { day, month, year ->
                    vm.setStartDate(day, month, year)
                }
            }

            endDate.setOnClickListener {
                showDatePickerDialog(vm.endDate, endDate, vm.startDate) { day, month, year ->
                    vm.setEndDate(day, month, year)
                }
            }

            searchButton.setOnClickListener {
                val intent = vm.saveSearchParams(this@FilterParamsActivity, minPrice.text.toString(), maxPrice.text.toString()) ?: return@setOnClickListener
                setResult(RESULT_OK, intent)
                finish()
            }

            deleteButton.setOnClickListener {
                vm.deleteFilter(this@FilterParamsActivity)
                setResult(RESULT_OK, Intent())
                finish()
            }
        }

        vm.toastMessage.observe(this) {
            showMessage(it)
        }
    }
}
