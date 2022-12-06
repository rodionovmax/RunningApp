package com.rodionovmax.runningapp.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.rodionovmax.runningapp.BuildConfig
import com.rodionovmax.runningapp.databinding.ActivityLoginBinding
import com.rodionovmax.runningapp.other.Constants
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    @Inject
    lateinit var sharedPref: SharedPreferences
    private lateinit var binding: ActivityLoginBinding
    @Inject
    lateinit var auth: FirebaseAuth
    private lateinit var startForSignInWithGoogle: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        startForSignInWithGoogle = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val signInAccountTask: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                if (signInAccountTask.isSuccessful) {
                    try {
                        val signInAccount: GoogleSignInAccount = signInAccountTask.result
                        val authCredential: AuthCredential =
                            GoogleAuthProvider.getCredential(signInAccount.idToken, null)

                        auth.signInWithCredential(authCredential)
                            .addOnCompleteListener(this) { task ->
                                if (task.isSuccessful) {
                                    val user = auth.currentUser!!
                                    startMainActivity(user)
                                } else {
                                    Toast.makeText(
                                        this, task.exception?.message
                                            ?: "Auth failed", Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                    } catch (e: ApiException) {
                        Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Timber.d("Something is wrong")
                }
            }
        }

        binding.loginButton.setOnClickListener {
            loginUser()
        }

        binding.continueWithGoogleButton.setOnClickListener {
            val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(BuildConfig.webclient_id)
                .requestEmail()
                .build()
            val signInClient = GoogleSignIn.getClient(this, options)
            signInClient.signInIntent.also {
                startForSignInWithGoogle.launch(it)
            }
        }

        binding.tvGoToSignUp.setOnClickListener {
            goToRegisterActivity()
        }
    }

    private fun loginUser() {
        val email = binding.etEmail.text.toString()
        val password = binding.etPassword.text.toString()
        when {
            email.isEmpty() -> Toast.makeText(
                this@LoginActivity, "Please enter email.", Toast.LENGTH_SHORT
            ).show()

            password.isEmpty() -> Toast.makeText(
                this@LoginActivity, "Please enter password", Toast.LENGTH_SHORT
            ).show()

            else -> auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user: FirebaseUser = auth.currentUser!!
                        val firebaseUser: FirebaseUser = task.result!!.user!! // same thing just leaving as example
                        startMainActivity(user)
                    } else {
                        Toast.makeText(
                            this@LoginActivity,
                            task.exception?.message.toString(),
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                }
        }
    }

    private fun goToRegisterActivity() {
        val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
        startActivity(intent)
    }

    private fun startMainActivity(user: FirebaseUser) {
        val intent = Intent(this@LoginActivity, MainActivity::class.java)
        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        sharedPref = getSharedPreferences("prefs", Context.MODE_PRIVATE)
        sharedPref.edit()
            .putString(Constants.KEY_FIREBASE_EMAIL, user.email)
            .apply()
        startActivity(intent)
        finish()
    }

}