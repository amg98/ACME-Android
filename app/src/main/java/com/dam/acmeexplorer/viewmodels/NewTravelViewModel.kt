package com.dam.acmeexplorer.viewmodels

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dam.acmeexplorer.R
import com.dam.acmeexplorer.exceptions.AlertException
import com.dam.acmeexplorer.extensions.formatted
import com.dam.acmeexplorer.extensions.uploadFile
import com.dam.acmeexplorer.models.Travel
import com.dam.acmeexplorer.models.TravelUpload
import com.dam.acmeexplorer.repositories.TravelRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.logging.Level.parse
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class NewTravelViewModel(private val travelRepository: TravelRepository) : ViewModel() {

    val startDate: Calendar = Calendar.getInstance()
    val endDate: Calendar = Calendar.getInstance()

    private val _toastMessage = MutableLiveData<String>()
    val toastMessage: LiveData<String> get() = _toastMessage

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> get() = _loading

    val images = mutableListOf("")

    fun setStartDate(day: Int, month: Int, year: Int): String {
        startDate.set(Calendar.YEAR, year)
        startDate.set(Calendar.MONTH, month)
        startDate.set(Calendar.DAY_OF_MONTH, day)

        return startDate.formatted()
    }

    fun setEndDate(day: Int, month: Int, year: Int): String {
        endDate.set(Calendar.YEAR, year)
        endDate.set(Calendar.MONTH, month)
        endDate.set(Calendar.DAY_OF_MONTH, day)

        return endDate.formatted()
    }

    fun submitTravel(context: Context, destination: String, price: String, startPlace: String, onDone: () -> Unit) {

        _loading.value = true
        if(!validateTravel(context, destination, price, startPlace)) return

        viewModelScope.launch {

            val travel = TravelUpload(destination, images, startDate.time, endDate.time, Integer.parseInt(price), startPlace)

            try {
                withContext(Dispatchers.IO) { travelRepository.addTravel(travel) }
                onDone()
            } catch(e: AlertException) {
                _toastMessage.value = e.asString(context)
            }

            _loading.value = false
        }
    }

    private fun validateTravel(context: Context, destination: String, price: String, startPlace: String): Boolean {
        if(destination.isEmpty()) {
            _toastMessage.value = context.getString(R.string.destinyEmpty)
            return false
        }
        if(price.isEmpty()) {
            _toastMessage.value = context.getString(R.string.priceEmpty)
            return false
        }
        if(startPlace.isEmpty()) {
            _toastMessage.value = context.getString(R.string.startPlaceEmpty)
            return false
        }
        if(images.size == 1) {
            _toastMessage.value = context.getString(R.string.atLeastOneImage)
            return false
        }

        return true
    }

    fun pushNewImage(fileUri: Uri) {
        images.add(0, fileUri.toString())
    }

    fun changeImage(imagePos: Int, fileUri: Uri) {
        images[imagePos] = fileUri.toString()
    }
}
