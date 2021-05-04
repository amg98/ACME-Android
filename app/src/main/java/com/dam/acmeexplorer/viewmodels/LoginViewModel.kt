package com.dam.acmeexplorer.viewmodels

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dam.acmeexplorer.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.*


class LoginViewModel(private val auth: FirebaseAuth, private val githubProvider: OAuthProvider) : ViewModel() {

    private val _toastMessage = MutableLiveData<String>()
    val toastMessage: LiveData<String> get() = _toastMessage

    private val _loadingWheel = MutableLiveData(false)
    val loadingWheel: LiveData<Boolean> get() = _loadingWheel

    private val _emailErrors = MutableLiveData("")
    val emailErrors: LiveData<String> get() = _emailErrors

    private val _passwordErrors = MutableLiveData("")
    val passwordErrors: LiveData<String> get() = _passwordErrors

    fun isLogged() = auth.currentUser != null

    fun login(activity: Activity, email: String, password: String, onComplete: () -> Unit) {

        if(!validateForm(activity, email, password)) return

        _loadingWheel.value = true
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(activity) { it ->
            _loadingWheel.value = false

            if(!it.isSuccessful) {
                _toastMessage.value = activity.getString(R.string.wrongCredentials)
                return@addOnCompleteListener
            }

            val user = it.result?.user
            if(!user?.isEmailVerified!!) {
                sendConfirmationEmail(activity, user)
                return@addOnCompleteListener
            }

            onComplete()
        }
    }

    private fun sendConfirmationEmail(activity: Activity, user: FirebaseUser) {
        AlertDialog.Builder(activity).setMessage(R.string.emailVerificationQuestion)
                .setPositiveButton(R.string.yes) { _, _ ->
                    user.sendEmailVerification().addOnCompleteListener {
                        if(it.isSuccessful) {
                            _toastMessage.value = activity.getString(R.string.verificationMailSent)
                        } else {
                            _toastMessage.value = activity.getString(R.string.verificationMailNotSent)
                        }
                    }
                }
                .setNegativeButton(R.string.no) { _, _ -> { }}
                .show()
    }

    private fun validateForm(activity: Activity, email: String, password: String): Boolean {
        if(email.isEmpty()) _emailErrors.value = activity.getString(R.string.requiredText)
        if(password.isEmpty()) _passwordErrors.value = activity.getString(R.string.requiredText)

        if(email.isEmpty() || password.isEmpty()) return false

        _emailErrors.value = ""
        _passwordErrors.value = ""

        return true
    }

    fun googleLogin(activity: Activity, intent: Intent, onComplete: () -> Unit) {
        val accountTask = GoogleSignIn.getSignedInAccountFromIntent(intent)
        _loadingWheel.value = true
        try {
            val account = accountTask.getResult(ApiException::class.java)!!
            val idToken = account.idToken!!
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            auth.signInWithCredential(credential).addOnCompleteListener(activity) { task ->
                _loadingWheel.value = false
                if (task.isSuccessful) {
                    onComplete()
                } else {
                    _toastMessage.value = activity.getString(R.string.loginError)
                }
            }
        } catch (e: ApiException) {
            _loadingWheel.value = false
            _toastMessage.value = activity.getString(R.string.loginError)
        }
    }

    fun githubLogin(activity: Activity, onComplete: () -> Unit) {
        val pendingResultTask = auth.pendingAuthResult
        if(pendingResultTask != null) {
            pendingResultTask
                    .addOnSuccessListener { onComplete() }
                    .addOnFailureListener {
                        _toastMessage.value = activity.getString(R.string.loginError)
                    }
        } else {
            auth.startActivityForSignInWithProvider(activity, githubProvider)
                    .addOnSuccessListener { onComplete() }
                    .addOnFailureListener{
                        if(it is FirebaseAuthUserCollisionException) {
                            _toastMessage.value = activity.getString(R.string.userCollision)
                        } else {
                            _toastMessage.value = activity.getString(R.string.loginError)
                        }
                    }
        }
    }
}
