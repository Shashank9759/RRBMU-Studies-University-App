package com.studies.rrbmustudies.OthersFeatures

import android.content.Intent

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast

import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

import com.google.android.gms.common.api.ApiException
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.studies.rrbmustudies.OthersFeatures.model.UserModel
import com.studies.rrbmustudies.R
import com.studies.rrbmustudies.UI.Activities.HomeActivity
import com.studies.rrbmustudies.databinding.ActivityLoginBinding

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

const val LOGIN_TAG = "LoginActivity"

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var googleSingInClient: GoogleSignInClient
    private val GOOGLE_SIGN_IN_REQUEST_CODE = 1002

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        db = Firebase.firestore

        binding.apply {

            emailLoginBtn.setOnClickListener {

                showLoadingBar(true)

                val email = emailAddressEdt.text.toString()
                val password = passwordEdt.text.toString()

                if (email.isNotEmpty() && password.isNotEmpty()) {

                    // login user through email and password
                    loginUser(email, password)

                } else {
                    Toast.makeText(
                        applicationContext,
                        "Please enter email and password to continue",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }

            googleSignInBtn.setOnClickListener {

                // Use Google Sign In
                googleSignIn()

            }


            signUpBtn.setOnClickListener {
                // Navigate to Sign Up Screen
                startActivity(Intent(this@LoginActivity, SignUpActivity::class.java))
            }

        }

    }

    private fun googleSignIn() {

        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(resources.getString(R.string.my_web_client_id))
            .requestEmail()
            .build()

        googleSingInClient = GoogleSignIn.getClient(this, googleSignInOptions)

        try {

            val intent = googleSingInClient.signInIntent
            startActivityForResult(intent, GOOGLE_SIGN_IN_REQUEST_CODE)

        } catch (e: Exception) {

            Log.e(LOGIN_TAG, "googleSignIn: Error: $e")

            // Notify user about Login Failure
            Toast.makeText(
                applicationContext,
                "Login failed! Error: $e",
                Toast.LENGTH_SHORT
            ).show()

        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {

            GOOGLE_SIGN_IN_REQUEST_CODE -> {

                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                try {

                    val account = task.result
                    val idToken = account.idToken

                    signInUserWithGoogleIdToken(idToken)

                } catch (e: ApiException) {
                    Log.e(LOGIN_TAG, "googleSignIn: Error: $e")

                    // Notify user about Login Failure
                    Toast.makeText(
                        applicationContext,
                        "Login failed! Error: $e",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }

            else -> {

                Log.e(LOGIN_TAG, "googleSignIn: Different Request Code")

                // Notify user about Login Failure
                Toast.makeText(
                    applicationContext,
                    "Login failed! Error: Different Request Code",
                    Toast.LENGTH_SHORT
                ).show()

            }

        }


    }

    private fun signInUserWithGoogleIdToken(idToken: String?) {
        CoroutineScope(Dispatchers.IO).launch {

            if (idToken != null) {

                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                auth.signInWithCredential(firebaseCredential)
                    .addOnCompleteListener { task ->

                        if (task.isSuccessful) {

                            // Notify user about Login Success
                            Toast.makeText(
                                applicationContext,
                                "Login Successful",
                                Toast.LENGTH_SHORT
                            ).show()

                            postUserDetailsToDb(auth.currentUser)

                        } else {

                            Log.e(LOGIN_TAG, "googleSignIn: Error: ${task.exception}")

                            // Notify user about Login Failure
                            Toast.makeText(
                                applicationContext,
                                "Login failed! Error: ${task.exception}",
                                Toast.LENGTH_SHORT
                            ).show()

                        }

                    }

            } else {

                Log.e(LOGIN_TAG, "activityForResult: No ID Token Available")

                // Notify user about Login Failure
                Toast.makeText(
                    applicationContext,
                    "No ID Token Available",
                    Toast.LENGTH_SHORT
                ).show()

            }

        }
    }

    private fun postUserDetailsToDb(currentUser: FirebaseUser?) {

        if (currentUser != null) {

            CoroutineScope(Dispatchers.IO).launch {

                val uID = currentUser.uid
                val userEmail = currentUser.email
                val userFullName = currentUser.displayName

                if (userEmail != null && userFullName != null) {
                    val userModel = UserModel(
                        uId = uID,
                        email = userEmail,
                        name = userFullName
                    )

                    db.collection("users")
                        .document(uID)
                        .set(userModel)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // Navigate to Home
                                startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                                finishAffinity()
                            } else {

                                Log.e(LOGIN_TAG, "postUserDetailsToDB: ${task.exception}")

                                // Notify user about Login Failure
                                Toast.makeText(
                                    applicationContext,
                                    "Error occurred ${task.exception}",
                                    Toast.LENGTH_LONG
                                ).show()

                            }
                        }
                }

            }

        }

    }

    override fun onStart() {
        super.onStart()

        // Get current user
        val currentUser = auth.currentUser

        if (currentUser != null) {
            // Navigate to HomeActivity, user is already logged in
            startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
            finishAffinity()
        }
    }

    private fun showLoadingBar(value: Boolean) {
        if (value) {
            binding.loadingProgress.visibility = View.VISIBLE
            binding.emailLoginBtn.isEnabled = false
        } else {
            binding.loadingProgress.visibility = View.GONE
            binding.emailLoginBtn.isEnabled = true
        }
    }

    private fun loginUser(email: String, password: String) {
        CoroutineScope(Dispatchers.IO).launch {

            // auto login the user with the email and password
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->

                // Stop Loading Progress
                showLoadingBar(false)

                // Login Successful
                if (task.isSuccessful) {

                    // Notify the user about login success
                    Toast.makeText(
                        applicationContext,
                        "Congratulations! Login successful",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Move to HomeActivity post successful login
                    val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                    startActivity(intent)
                    finishAffinity()

                } else { // Login Unsuccessful

                    // Notify the user about login failure
                    Toast.makeText(
                        applicationContext,
                        "Login Failed. Error: ${task.exception}",
                        Toast.LENGTH_LONG
                    ).show()

                    Log.e(TAG, "loginUser: ${task.exception}")

                }

            }
        }
    }

}