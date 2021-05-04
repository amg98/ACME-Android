package com.dam.acmeexplorer.viewmodels

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dam.acmeexplorer.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class NewTravelViewModel(private val storage: FirebaseStorage, private val db: FirebaseFirestore) : ViewModel() {

    val startDate: Calendar = Calendar.getInstance()
    val endDate: Calendar = Calendar.getInstance()

    private val _toastMessage = MutableLiveData<String>()
    val toastMessage: LiveData<String> get() = _toastMessage

    val images = mutableListOf("")

    fun formatDate(date: Calendar): String {
        return SimpleDateFormat("dd/MM/yyyy", Locale.US).format(date.time)
    }

    fun onStartDate(day: Int, month: Int, year: Int): String {
        startDate.set(Calendar.YEAR, year)
        startDate.set(Calendar.MONTH, month)
        startDate.set(Calendar.DAY_OF_MONTH, day)

        return formatDate(startDate)
    }

    fun onEndDate(day: Int, month: Int, year: Int): String {
        endDate.set(Calendar.YEAR, year)
        endDate.set(Calendar.MONTH, month)
        endDate.set(Calendar.DAY_OF_MONTH, day)

        return formatDate(endDate)
    }

    fun submitTravel(context: Context, destination: String, price: Int, startPlace: String, onDone: () -> Unit) {
        if(destination.isEmpty()) {
            _toastMessage.value = context.getString(R.string.destinyEmpty)
            return
        }
        if(price < 0) {
            _toastMessage.value = context.getString(R.string.priceEmpty)
            return
        }
        if(startPlace.isEmpty()) {
            _toastMessage.value = context.getString(R.string.startPlaceEmpty)
            return
        }
        if(images.size == 1) {
            _toastMessage.value = context.getString(R.string.atLeastOneImage)
            return
        }

        viewModelScope.launch {

            // Save files to Firebase Storage
            val uploadedImages = images
                .filter { it != "" }
                .mapIndexed { i, fileUri -> async { uploadFile(Uri.parse(fileUri), destination, i) } }
                .map { it.await() }

            var hasErrors = false
            uploadedImages.forEach {
                if(it == null) hasErrors = true
            }

            if(hasErrors) {
                _toastMessage.value = context.getString(R.string.uploadImagesError)
                return@launch
            }

            // Create travel object
            val travel = hashMapOf(
                "title" to destination,
                "startDate" to formatDate(startDate),
                "endDate" to formatDate(endDate),
                "price" to price,
                "startPlace" to startPlace,
                "images" to uploadedImages
            )

            // Create travel in Firebase Firestore
            db.collection("travels").add(travel)
                .addOnSuccessListener {
                    onDone()
                }
                .addOnFailureListener {
                    _toastMessage.value = context.getString(R.string.travelCreationError)
                }
        }
    }

    fun pushNewImage(fileUri: Uri) {
        images.add(0, fileUri.toString())
    }

    fun changeImage(imagePos: Int, fileUri: Uri) {
        images[imagePos] = fileUri.toString()
    }

    private suspend fun uploadFile(fileUri: Uri, folder: String, id: Int): String? = suspendCoroutine { cont ->
        val refString = "{$folder}/{$id}"
        val uploadTask = storage.reference.child(refString).putFile(fileUri)
        uploadTask.addOnSuccessListener {
            cont.resume(refString)
        }
        uploadTask.addOnFailureListener {
            cont.resume(null)
        }
    }
}
