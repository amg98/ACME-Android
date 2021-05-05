package com.dam.acmeexplorer.repositories

import android.net.Uri
import com.dam.acmeexplorer.R
import com.dam.acmeexplorer.api.OpenWeatherService
import com.dam.acmeexplorer.extensions.formatted
import com.dam.acmeexplorer.extensions.uploadFile
import com.dam.acmeexplorer.models.FilterParams
import com.dam.acmeexplorer.models.OpenWeatherResponse
import com.dam.acmeexplorer.models.Travel
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class TravelRepository(private val db: FirebaseFirestore,
                       private val storage: FirebaseStorage,
                       private val weatherService: OpenWeatherService
) {

    companion object {
        private const val COLLECTION_NAME = "travels"
        private const val DOC_TITLE = "title"
        private const val DOC_START_DATE = "startDate"
        private const val DOC_END_DATE = "endDate"
        private const val DOC_PRICE = "price"
        private const val DOC_START_PLACE = "startPlace"
        private const val DOC_IMAGES = "images"
    }

    suspend fun getTravels(): List<Travel>? = coroutineScope {
        val docs = queryTravels() ?: return@coroutineScope null
        return@coroutineScope processTravels(docs)
    }

    suspend fun getTravels(filterParams: FilterParams): List<Travel>? = coroutineScope {
        val docs = queryTravels(filterParams) ?: return@coroutineScope null
        return@coroutineScope processTravels(docs)
    }

    suspend fun getTravels(travels: Set<String>): List<Travel>? = coroutineScope {
        val docs = queryTravels(travels) ?: return@coroutineScope null
        return@coroutineScope processTravels(docs)
    }

    suspend fun addTravel(travel: Travel): Boolean = coroutineScope {

        getWeather(travel.title) ?: return@coroutineScope false

        val uploadedImages = uploadImages(travel.title, travel.imagesURL)

        val uploadError = uploadedImages.fold(initial = false) { ac, image -> ac || image == null }
        if(uploadError) return@coroutineScope false

        val travelDocument = hashMapOf(
                DOC_TITLE to travel.title,
                DOC_START_DATE to travel.startDate,
                DOC_END_DATE to travel.endDate,
                DOC_PRICE to travel.price,
                DOC_START_PLACE to travel.startPlace,
                DOC_IMAGES to uploadedImages
        )

        return@coroutineScope uploadTravel(travelDocument)
    }

    private suspend fun uploadTravel(travelDocument: HashMap<String, Any>): Boolean = suspendCoroutine { cont ->
        db.collection(COLLECTION_NAME).add(travelDocument)
                .addOnSuccessListener { cont.resume(true) }
                .addOnFailureListener { cont.resume(false) }
    }

    private suspend fun queryTravels(): QuerySnapshot? = suspendCoroutine { cont ->
        db.collection(COLLECTION_NAME).get()
                .addOnSuccessListener { documents -> cont.resume(documents) }
                .addOnFailureListener { cont.resume(null) }
    }

    private suspend fun queryTravels(filterParams: FilterParams): QuerySnapshot? = suspendCoroutine { cont ->
        db.collection(COLLECTION_NAME)
                .whereGreaterThan(DOC_START_DATE, filterParams.startDate)
                .whereLessThan(DOC_END_DATE, filterParams.endDate)
                .whereGreaterThanOrEqualTo(DOC_PRICE, filterParams.minPrice)
                .whereLessThanOrEqualTo(DOC_PRICE, filterParams.maxPrice)
                .get()
                .addOnSuccessListener { docs -> cont.resume(docs) }
                .addOnFailureListener { cont.resume(null) }
    }

    private suspend fun queryTravels(travels: Set<String>): QuerySnapshot? = suspendCoroutine { cont ->
        db.collection(COLLECTION_NAME)
                .whereIn(FieldPath.documentId(), travels.toList())
                .get()
                .addOnSuccessListener { docs -> cont.resume(docs) }
                .addOnFailureListener { cont.resume(null) }
    }

    private suspend fun uploadImages(destination: String, images: List<String>) = coroutineScope {
        return@coroutineScope images
                .filter { it != "" }
                .mapIndexed { i, fileUri -> async { storage.uploadFile(Uri.parse(fileUri), destination, i) } }
                .map { it.await() }
    }

    private suspend fun processTravels(documents: QuerySnapshot): List<Travel>? = coroutineScope {
        try {
            return@coroutineScope documents.map { doc ->
                val images = (doc[DOC_IMAGES] as List<*>)
                        .map {
                            async { getDownloadURL(it as String) }
                        }
                        .map { it.await().toString() }

                val weather = getWeather(doc[DOC_TITLE] as String)

                Travel(doc.id,
                        doc[DOC_TITLE] as String,
                        images,
                        doc[DOC_START_DATE] as Date,
                        doc[DOC_END_DATE] as Date,
                        (doc[DOC_PRICE] as Long).toInt(),
                        doc[DOC_START_DATE] as String,
                        weather!!)
            }
        } catch (e: Exception) {
            return@coroutineScope null
        }
    }

    private fun getDownloadURL(remoteFilePath: String): Uri? {
        val urlTask = storage.reference.child(remoteFilePath).downloadUrl
        return try {
            Tasks.await(urlTask, 2000, TimeUnit.MILLISECONDS)
        } catch(e: Exception) {
            null
        }
    }

    private suspend fun getWeather(city: String): OpenWeatherResponse? {
        val call = weatherService.getLocation(city)
        if(!call.isSuccessful) {
            return null
        }

        return call.body()
    }
}
