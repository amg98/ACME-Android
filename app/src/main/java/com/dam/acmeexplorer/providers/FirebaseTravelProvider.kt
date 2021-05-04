package com.dam.acmeexplorer.providers

import android.net.Uri
import android.util.Log
import com.dam.acmeexplorer.models.Travel
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class FirebaseTravelProvider(private val db: FirebaseFirestore, private val storage: FirebaseStorage) : TravelProvider {

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
                Log.d("DB", doc.toString())
                val images = (doc["images"] as List<*>)
                    .map {
                        async { getDownloadURL(it as String) }
                    }
                    .map { it.await().toString() }

                val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.US)
                val travel = Travel(doc.id,
                    doc["title"] as String,
                    images,
                    dateFormatter.parse(doc["startDate"] as String)!!,
                    dateFormatter.parse(doc["endDate"] as String)!!,
                    (doc["price"] as Long).toInt(),
                    doc["startPlace"] as String)

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
}
