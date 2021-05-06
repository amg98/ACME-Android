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
import com.dam.acmeexplorer.extensions.formatted
import com.dam.acmeexplorer.extensions.showDatePickerDialog
import com.dam.acmeexplorer.extensions.showFileChooser
import com.dam.acmeexplorer.extensions.showMessage
import com.dam.acmeexplorer.listadapters.ImageListAdapter
import com.dam.acmeexplorer.viewmodels.NewTravelViewModel
import org.koin.android.viewmodel.ext.android.viewModel
import java.util.*

class NewTravelActivity : AppCompatActivity() {

    companion object {
        const val NO_IMAGE_SELECTED = -1
    }

    private val vm: NewTravelViewModel by viewModel()
    private lateinit var binding: ActivityNewTravelBinding
    private var selectedImage = NO_IMAGE_SELECTED

    private val getFile = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode != Activity.RESULT_OK) return@registerForActivityResult

        val fileUri = result.data?.data ?: return@registerForActivityResult

        if(selectedImage == NO_IMAGE_SELECTED) {
            vm.pushNewImage(fileUri)
            binding.imagesList.adapter?.notifyItemInserted(0)
            binding.imagesList.adapter?.notifyItemRangeChanged(0, vm.images.size)
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

            startDate.setText(vm.startDate.formatted())
            endDate.setText(vm.endDate.formatted())

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

            imagesList.adapter = ImageListAdapter(vm.images) { imagePos: Int ->
                selectedImage = if(vm.images[imagePos] == "") NO_IMAGE_SELECTED else imagePos
                showFileChooser(getFile, getString(R.string.selectImageText), "image/*")
            }

            addButton.setOnClickListener {

                val titleVal = title.text.toString()
                val priceVal = price.text.toString()
                val startPlaceVal = startPlace.text.toString()

                vm.submitTravel(this@NewTravelActivity, titleVal, priceVal, startPlaceVal) {
                    setResult(RESULT_OK, Intent())
                    finish()
                }
            }
        }

        vm.toastMessage.observe(this) {
            showMessage(it)
        }
    }
}
