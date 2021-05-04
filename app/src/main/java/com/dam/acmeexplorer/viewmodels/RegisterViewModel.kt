package com.dam.acmeexplorer.viewmodels

import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dam.acmeexplorer.R
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

    fun register(activity: Activity, email: String, password: String, repeatPassword: String, onComplete: () -> Unit) {
        if(!validateForm(activity, email, password, repeatPassword)) return

        _loadingWheel.value = true
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(activity) {
            _loadingWheel.value = false
            if(it.isSuccessful) {
                onComplete()
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
