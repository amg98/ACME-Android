package com.dam.acmeexplorer.providers

import android.net.Uri
import android.util.Log
import com.dam.acmeexplorer.api.OpenWeatherService
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

class FirebaseTravelProvider(private val db: FirebaseFirestore, private val storage: FirebaseStorage) : TravelProvider {

    private val weatherService = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenWeatherService::class.java)

    override suspend fun getTravels(): List<Travel>? = suspendCoroutine { cont ->
        db.collection("travels").get()
            .addOnSuccessListener { documents ->
                val travels = processTravels(documents)
                cont.resume(travels)
            }
            .addOnFailureListener {
                cont.resume(null)
            }
    }

    override suspend fun getTravels(
        startDate: Date,
        endDate: Date,
        minPrice: Int,
        maxPrice: Int
    ): List<Travel>? = suspendCoroutine { cont ->
        db.collection("travels")
            .whereGreaterThan("startDate", startDate)
            .whereLessThan("endDate", endDate)
            .whereGreaterThanOrEqualTo("minPrice", minPrice)
            .whereLessThanOrEqualTo("maxPrice", maxPrice)
            .get()
            .addOnSuccessListener { documents ->
                val travels = processTravels(documents)
                cont.resume(travels)
            }
            .addOnFailureListener {
                cont.resume(null)
            }
    }

    override suspend fun getTravels(travels: Set<String>): List<Travel>? = suspendCoroutine { cont ->
        db.collection("travels")
            .whereIn(FieldPath.documentId(), travels.toList())
            .get()
            .addOnSuccessListener { documents ->
                val selectedTravels = processTravels(documents)
                cont.resume(selectedTravels)
            }
            .addOnFailureListener {
                cont.resume(null)
            }
    }

    private fun processTravels(documents: QuerySnapshot): List<Travel> {
        val travels = mutableListOf<Travel>()

        runBlocking(Dispatchers.IO) {
            for(doc in documents) {
                val images = (doc["images"] as List<*>)
                    .map {
                        async { getDownloadURL(it as String) }
                    }
                    .map { it.await().toString() }

                val weather = getWeather(doc["title"] as String)

                val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.US)
                val travel = Travel(doc.id,
                    doc["title"] as String,
                    images,
                    dateFormatter.parse(doc["startDate"] as String)!!,
                    dateFormatter.parse(doc["endDate"] as String)!!,
                    (doc["price"] as Long).toInt(),
                    doc["startPlace"] as String,
                    weather!!)

                travels.add(travel)
            }
        }

        return travels
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
