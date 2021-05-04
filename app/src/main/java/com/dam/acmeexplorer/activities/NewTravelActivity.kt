package com.dam.acmeexplorer.activities

import android.app.Activity
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.dam.acmeexplorer.R
import com.dam.acmeexplorer.databinding.ActivityNewTravelBinding
import com.dam.acmeexplorer.listadapters.ImageListAdapter
import com.dam.acmeexplorer.viewmodels.NewTravelViewModel
import org.koin.android.viewmodel.ext.android.viewModel
import java.util.*

class NewTravelActivity : AppCompatActivity() {

    private val vm: NewTravelViewModel by viewModel()
    private lateinit var binding: ActivityNewTravelBinding
    private var selectedImage = -1

    private val getFile = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode != Activity.RESULT_OK) return@registerForActivityResult

        val fileUri = result.data?.data ?: return@registerForActivityResult

        if(selectedImage < 0) {
            vm.pushNewImage(fileUri)
            binding.imagesList.adapter?.notifyItemInserted(0)
        } else {
            vm.changeImage(selectedImage, fileUri)
            binding.imagesList.adapter?.notifyItemChanged(selectedImage)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNewTravelBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding) {

            startDate.setText(vm.formatDate(vm.startDate))
            startDate.setOnClickListener {
                showDatePickerDialog(vm.startDate, startDate, null) { day, month, year ->
                    vm.onStartDate(day, month, year)
                }
            }

            endDate.setText(vm.formatDate(vm.startDate))
            endDate.setOnClickListener {
                showDatePickerDialog(vm.endDate, endDate, vm.startDate) { day, month, year ->
                    vm.onEndDate(day, month, year)
                }
            }

            imagesList.adapter = ImageListAdapter(vm.images) { imagePos: Int, isAdd: Boolean ->
                selectedImage = if(isAdd) -1 else imagePos
                showFileChooser()
            }

            addButton.setOnClickListener {
                val titleVal = title.text.toString()
                val priceVal = if(price.text.isNotEmpty()) Integer.parseInt(price.text.toString()) else -1
                val startPlaceVal = startPlace.text.toString()
                vm.submitTravel(this@NewTravelActivity, titleVal, priceVal, startPlaceVal) {
                    setResult(RESULT_OK, Intent())
                    finish()
                }
            }
        }

        vm.toastMessage.observe(this) {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDatePickerDialog(
        cal: Calendar,
        editText: EditText,
        minDate: Calendar?,
        onDate: (Int, Int, Int) -> String
    ) {
        val dialog = DatePickerDialog(this, { _, year, month, day ->
            editText.setText(onDate(day, month, year))
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
        dialog.datePicker.minDate = minDate?.timeInMillis ?: 0
        dialog.show()
    }

    private fun showFileChooser() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        try {
            getFile.launch(Intent.createChooser(intent, getString(R.string.selectImageText)))
        } catch (ex: ActivityNotFoundException) {
            Toast.makeText(this, getString(R.string.noExplorerFound), Toast.LENGTH_SHORT).show()
        }
    }
}
