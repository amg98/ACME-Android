package com.dam.acmeexplorer.viewmodels

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dam.acmeexplorer.R
import com.dam.acmeexplorer.activities.LoginActivity
import com.google.firebase.auth.FirebaseAuth

class RegisterViewModel(private val auth: FirebaseAuth) : ViewModel() {

    private val _toastMessage = MutableLiveData<String>()
    val toastMessage: LiveData<String> get() = _toastMessage

    private val _loadingWheel = MutableLiveData(false)
    val loadingWheel: LiveData<Boolean> get() = _loadingWheel

    private val _emailErrors = MutableLiveData("")
    val emailErrors: LiveData<String> get() = _emailErrors

    private val _passwordErrors = MutableLiveData("")
    val passwordErrors: LiveData<String> get() = _passwordErrors

    private val _repeatPasswordErrors = MutableLiveData("")
    val repeatPasswordErrors: LiveData<String> get() = _repeatPasswordErrors

    fun register(activity: Activity, email: String, password: String, repeatPassword: String, onComplete: (Intent) -> Unit) {

        _loadingWheel.value = true

        if(!validateForm(activity, email, password, repeatPassword)) return

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(activity) {

            _loadingWheel.value = false

            if(it.isSuccessful) {

                val intent = Intent()
                intent.putExtra(LoginActivity.RESULT_USER_EMAIL, email)
                intent.putExtra(LoginActivity.RESULT_USER_PASSWORD, password)

                onComplete(intent)
            } else {
                _toastMessage.value = activity.getString(R.string.registerError)
            }
        }
    }

    private fun validateForm(activity: Activity, email: String, password: String, repeatPassword: String): Boolean {

        if(email.isEmpty()) _emailErrors.value = activity.getString(R.string.requiredText)
        if(password.isEmpty()) _passwordErrors.value = activity.getString(R.string.requiredText)
        if(repeatPassword.isEmpty()) _repeatPasswordErrors.value = activity.getString(R.string.requiredText)

        if(email.isEmpty() || password.isEmpty() || repeatPassword.isEmpty()) return false

        if(password != repeatPassword) {
            _toastMessage.value = activity.getString(R.string.passwordsDontMatch)
            return false
        }

        if(password.length < 6) {
            _toastMessage.value = activity.getString(R.string.passwordMinLength)
            return false
        }

        _emailErrors.value = ""
        _passwordErrors.value = ""
        _repeatPasswordErrors.value = ""

        return true
    }
}
