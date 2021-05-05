package com.dam.acmeexplorer.activities

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.dam.acmeexplorer.R
import com.dam.acmeexplorer.databinding.ActivityLoginBinding
import com.dam.acmeexplorer.databinding.ActivityMainBinding
import com.dam.acmeexplorer.extensions.showMessage
import com.dam.acmeexplorer.viewmodels.LoginViewModel
import com.dam.acmeexplorer.viewmodels.MainViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel
import java.util.*

class LoginActivity : AppCompatActivity() {

    private val vm: LoginViewModel by viewModel()
    private lateinit var binding: ActivityLoginBinding

    companion object {
        const val RESULT_USER_EMAIL = "user_email"
        const val RESULT_USER_PASSWORD = "user_password"
    }

    private val registerUser = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode != Activity.RESULT_OK) return@registerForActivityResult

        val data = result.data?.extras ?: return@registerForActivityResult

        val userEmail = data.get(RESULT_USER_EMAIL) as String
        val userPassword = data.get(RESULT_USER_PASSWORD) as String

        with(binding) {
            emailInputAc.setText(userEmail)
            passwordInputAc.setText(userPassword)
        }
    }

    private val loginWithGoogle = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val intent = result.data ?: return@registerForActivityResult
        vm.googleLogin(this@LoginActivity, intent) {
            gotoMainScreen()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if(vm.isLogged()) {
            gotoMainScreen()
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding) {

            loginButton.setOnClickListener {
                vm.login(this@LoginActivity, emailInputAc.text.toString(), passwordInputAc.text.toString()) {
                    gotoMainScreen()
                }
            }

            googleLogin.setOnClickListener {
                loginWithGoogle.launch(vm.getGoogleSignInIntent(this@LoginActivity))
            }

            githubLogin.setOnClickListener {
                vm.githubLogin(this@LoginActivity) {
                    gotoMainScreen()
                }
            }

            registerButton.setOnClickListener {
                val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
                intent.putExtra(RegisterActivity.INTENT_EMAIL, emailInputAc.text.toString())
                registerUser.launch(intent)
            }

            vm.toastMessage.observe(this@LoginActivity) {
                showMessage(it)
            }

            vm.loadingWheel.observe(this@LoginActivity) {
                progressBar.visibility = if(it) View.VISIBLE else View.GONE
            }

            vm.emailErrors.observe(this@LoginActivity) {
                emailInput.error = it
            }

            vm.passwordErrors.observe(this@LoginActivity) {
                passwordInput.error = it
            }
        }
    }

    private fun gotoMainScreen() {
        startActivity(Intent(this, MainActivity::class.java))
    }
}
