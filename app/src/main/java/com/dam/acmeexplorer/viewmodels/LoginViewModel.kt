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
import com.dam.acmeexplorer.extensions.showYesNoDialog
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.*


class LoginViewModel(private val auth: FirebaseAuth, private val githubProvider: OAuthProvider, private val gso: GoogleSignInOptions) : ViewModel() {

    private val _toastMessage = MutableLiveData<String>()
    val toastMessage: LiveData<String> get() = _toastMessage

    private val _loadingWheel = MutableLiveData(false)
    val loadingWheel: LiveData<Boolean> get() = _loadingWheel

    private val _emailErrors = MutableLiveData("")
    val emailErrors: LiveData<String> get() = _emailErrors

    private val _passwordErrors = MutableLiveData("")
    val passwordErrors: LiveData<String> get() = _passwordErrors

    fun isLogged() = auth.currentUser != null

    fun getGoogleSignInIntent(activity: Activity): Intent {
        return GoogleSignIn.getClient(activity, gso).signInIntent
    }

    fun login(activity: Activity, email: String, password: String, onComplete: () -> Unit) {

        if(!validateForm(activity, email, password)) return

        _loadingWheel.value = true
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(activity) {
            _loadingWheel.value = false

            if(!it.isSuccessful) {
                _toastMessage.value = activity.getString(R.string.wrongCredentials)
                return@addOnCompleteListener
            }

            val user = it.result?.user
            if(user == null) {
                _toastMessage.value = activity.getString(R.string.loginError)
                return@addOnCompleteListener
            }

            if(!user.isEmailVerified) {
                activity.showYesNoDialog(activity.getString(R.string.emailVerificationQuestion)) {
                    sendConfirmationEmail(activity, user)
                }
                return@addOnCompleteListener
            }

            onComplete()
        }
    }

    private fun sendConfirmationEmail(activity: Activity, user: FirebaseUser) {
        _loadingWheel.value = true
        user.sendEmailVerification().addOnCompleteListener {
            _loadingWheel.value = false
            if(it.isSuccessful) {
                _toastMessage.value = activity.getString(R.string.verificationMailSent)
            } else {
                _toastMessage.value = activity.getString(R.string.verificationMailNotSent)
            }
        }
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

        _loadingWheel.value = true

        try {
            val accountTask = GoogleSignIn.getSignedInAccountFromIntent(intent)
            val account = accountTask.getResult(ApiException::class.java)!!
            val idToken = account.idToken!!
            val credential = GoogleAuthProvider.getCredential(idToken, null)

            auth.signInWithCredential(credential).addOnCompleteListener(activity) { task ->
                _loadingWheel.value = false

                if (!task.isSuccessful) {
                    _toastMessage.value = activity.getString(R.string.loginError)
                    return@addOnCompleteListener
                }

                onComplete()
            }
        } catch (e: Exception) {
            _loadingWheel.value = false
            _toastMessage.value = activity.getString(R.string.loginError)
        }
    }

    fun githubLogin(activity: Activity, onComplete: () -> Unit) {

        _loadingWheel.value = true
        val pendingResultTask = auth.pendingAuthResult

        if(pendingResultTask != null) {
            pendingResultTask
                    .addOnSuccessListener {
                        _loadingWheel.value = false
                        onComplete()
                    }
                    .addOnFailureListener {
                        _loadingWheel.value = false
                        _toastMessage.value = activity.getString(R.string.loginError)
                    }
        } else {
            auth.startActivityForSignInWithProvider(activity, githubProvider)
                    .addOnSuccessListener {
                        _loadingWheel.value = false
                        onComplete()
                    }
                    .addOnFailureListener{
                        _loadingWheel.value = false
                        if(it is FirebaseAuthUserCollisionException) {
                            _toastMessage.value = activity.getString(R.string.userCollision)
                        } else {
                            _toastMessage.value = activity.getString(R.string.loginError)
                        }
                    }
        }
    }
}
