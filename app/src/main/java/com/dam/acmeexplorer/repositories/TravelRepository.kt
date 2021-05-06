package com.dam.acmeexplorer.repositories

import android.net.Uri
import android.util.Log
import com.dam.acmeexplorer.R
import com.dam.acmeexplorer.api.OpenWeatherService
import com.dam.acmeexplorer.exceptions.AlertException
import com.dam.acmeexplorer.extensions.formatted
import com.dam.acmeexplorer.extensions.uploadFile
import com.dam.acmeexplorer.models.FilterParams
import com.dam.acmeexplorer.models.OpenWeatherResponse
import com.dam.acmeexplorer.models.Travel
import com.dam.acmeexplorer.models.TravelUpload
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
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

    suspend fun getTravels(): List<Travel> = coroutineScope {
        val docs = queryTravels()
        return@coroutineScope processTravels(docs)
    }

    suspend fun getTravels(filterParams: FilterParams): List<Travel> = coroutineScope {
        val docs = queryTravels(filterParams)
        return@coroutineScope processTravels(docs)
    }

    suspend fun getTravels(travels: Set<String>): List<Travel> = coroutineScope {
        val docs = queryTravels(travels)
        return@coroutineScope processTravels(docs)
    }

    suspend fun addTravel(travel: TravelUpload) = coroutineScope {

        getWeather(travel.title)

        val uploadedImages = uploadImages(travel.title, travel.imagesURI)

        val travelDocument = hashMapOf(
                DOC_TITLE to travel.title,
                DOC_START_DATE to travel.startDate,
                DOC_END_DATE to travel.endDate,
                DOC_PRICE to travel.price,
                DOC_START_PLACE to travel.startPlace,
                DOC_IMAGES to uploadedImages
        )

        if(!uploadTravel(travelDocument)) {
            throw AlertException(R.string.travelCreationError)
        }
    }

    private suspend fun uploadTravel(travelDocument: HashMap<String, Any>): Boolean = suspendCoroutine { cont ->
        db.collection(COLLECTION_NAME).add(travelDocument)
                .addOnSuccessListener { cont.resume(true) }
                .addOnFailureListener { cont.resume(false) }
    }

    private suspend fun queryTravels(): List<QueryDocumentSnapshot> = suspendCoroutine { cont ->
        db.collection(COLLECTION_NAME).get()
                .addOnSuccessListener { documents -> cont.resume(documents.map { it }) }
                .addOnFailureListener { throw AlertException(R.string.errorGettingTravels) }
    }

    private suspend fun queryTravels(filterParams: FilterParams): List<QueryDocumentSnapshot> = suspendCoroutine { cont ->
        db.collection(COLLECTION_NAME)
                .whereGreaterThanOrEqualTo(DOC_PRICE, filterParams.minPrice)
                .whereLessThanOrEqualTo(DOC_PRICE, filterParams.maxPrice)
                .get()
                .addOnSuccessListener { docs ->
                    cont.resume(docs.filter {
                        val startDate = (it[DOC_START_DATE] as Timestamp).toDate()
                        val endDate = (it[DOC_END_DATE] as Timestamp).toDate()
                        return@filter (startDate > filterParams.startDate.time && endDate < filterParams.endDate.time)
                    })
                }
                .addOnFailureListener { throw AlertException(R.string.errorGettingTravels) }
    }

    private suspend fun queryTravels(travels: Set<String>): List<QueryDocumentSnapshot> = suspendCoroutine { cont ->
        db.collection(COLLECTION_NAME)
                .whereIn(FieldPath.documentId(), travels.toList())
                .get()
                .addOnSuccessListener { docs -> cont.resume(docs.map { it }) }
                .addOnFailureListener { throw AlertException(R.string.errorGettingTravels) }
    }

    private suspend fun uploadImages(destination: String, images: List<String>) = coroutineScope {
        return@coroutineScope images
                .filter { it != "" }
                .mapIndexed { i, fileUri -> async { storage.uploadFile(Uri.parse(fileUri), destination, i) } }
                .map { it.await() }
    }

    private suspend fun processTravels(documents: List<QueryDocumentSnapshot>): List<Travel> = coroutineScope {
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
                    (doc[DOC_START_DATE] as Timestamp).toDate(),
                    (doc[DOC_END_DATE] as Timestamp).toDate(),
                    (doc[DOC_PRICE] as Long).toInt(),
                    doc[DOC_START_PLACE] as String,
                    weather)
            }
    }

    private suspend fun getDownloadURL(remoteFilePath: String): Uri = suspendCoroutine { cont ->
        storage.reference.child(remoteFilePath).downloadUrl
                .addOnSuccessListener { cont.resume(it) }
                .addOnFailureListener { throw AlertException(R.string.imageDownloadError) }
    }

    private suspend fun getWeather(city: String): OpenWeatherResponse {
        val call = weatherService.getLocation(city)
        if(!call.isSuccessful) {
            throw AlertException(R.string.weatherError)
        }

        return call.body() ?: throw AlertException(R.string.serverEmptyResponse)
    }
}
