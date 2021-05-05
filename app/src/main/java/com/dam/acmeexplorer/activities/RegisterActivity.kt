package com.dam.acmeexplorer.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.dam.acmeexplorer.databinding.ActivityLoginBinding
import com.dam.acmeexplorer.databinding.ActivityRegisterBinding
import com.dam.acmeexplorer.extensions.showMessage
import com.dam.acmeexplorer.viewmodels.LoginViewModel
import com.dam.acmeexplorer.viewmodels.RegisterViewModel
import org.koin.android.viewmodel.ext.android.viewModel

class RegisterActivity : AppCompatActivity() {

    private val vm: RegisterViewModel by viewModel()
    private lateinit var binding: ActivityRegisterBinding

    companion object {
        const val INTENT_EMAIL = "email"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding) {

            val emailText = intent.extras?.get(INTENT_EMAIL) as String
            emailInputAc.setText(emailText)

            registerButton.setOnClickListener {

                val email = emailInputAc.text.toString()
                val password = passwordInputAc.text.toString()
                val repeatPassword = repeatPasswordInputAc.text.toString()

                vm.register(this@RegisterActivity, email, password, repeatPassword) {
                    setResult(RESULT_OK, it)
                    finish()
                }
            }

            vm.toastMessage.observe(this@RegisterActivity) {
                showMessage(it)
            }

            vm.loadingWheel.observe(this@RegisterActivity) {
                progressBar.visibility = if(it) View.VISIBLE else View.GONE
            }

            vm.emailErrors.observe(this@RegisterActivity) {
                emailInput.error = it
            }

            vm.passwordErrors.observe(this@RegisterActivity) {
                passwordInput.error = it
            }

            vm.repeatPasswordErrors.observe(this@RegisterActivity) {
                repeatPasswordInput.error = it
            }
        }
    }
}
