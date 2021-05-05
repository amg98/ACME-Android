package com.dam.acmeexplorer.extensions

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

suspend fun FirebaseStorage.uploadFile(fileUri: Uri, folder: String, id: Int): String? = suspendCoroutine { cont ->
    val refString = "$folder/$id"
    val uploadTask = this.reference.child(refString).putFile(fileUri)
    uploadTask.addOnSuccessListener {
        cont.resume(refString)
    }
    uploadTask.addOnFailureListener {
        cont.resume(null)
    }
}
