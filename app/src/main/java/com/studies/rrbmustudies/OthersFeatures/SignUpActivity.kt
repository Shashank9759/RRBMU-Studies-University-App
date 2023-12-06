package com.studies.rrbmustudies.OthersFeatures

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.studies.rrbmustudies.OthersFeatures.model.UserModel
import com.studies.rrbmustudies.UI.Activities.HomeActivity
import com.studies.rrbmustudies.databinding.ActivitySignUpBinding

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

const val TAG = "SignUpActivity"

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        db = Firebase.firestore

        binding.apply {

            emailSignUpBtn.setOnClickListener {

                // Show Loading Progress Bar
                showLoadingBar(true)

                // Get Name, Email, Password & Confirm Password
                val name = nameEdt.text.toString()
                val email = emailAddressEdt.text.toString()
                val password = passwordEdt.text.toString()
                val confirmPassword = confirmPasswordEdt.text.toString()

                if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()
                    && confirmPassword.isNotEmpty()
                ) {

                    // Check if both the password and confirm password fields are same but not empty
                    if (password == confirmPassword) {

                        // Sign up the user
                        signUpUserWithEmailPassword(name, email, password)

                    } else {
                        Toast.makeText(
                            applicationContext,
                            "Passwords do not match. Please re-enter Password and Confirm Password",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                } else {
                    Toast.makeText(
                        applicationContext,
                        "Please enter all the fields to continue",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }
        }

    }

    private fun showLoadingBar(value: Boolean) {
        if (value) {
            binding.loadingProgress.visibility = View.VISIBLE
            binding.emailSignUpBtn.isEnabled = false
        } else {
            binding.loadingProgress.visibility = View.GONE
            binding.emailSignUpBtn.isEnabled = true
        }
    }

    private fun signUpUserWithEmailPassword(name: String, email: String, password: String) {
        CoroutineScope(Dispatchers.IO).launch {
            auth
                .createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->

                    if (task.isSuccessful) {

                        // Notify user about signup success
                        Toast.makeText(
                            applicationContext,
                            "Congratulations! Sign up successful",
                            Toast.LENGTH_SHORT
                        ).show()

                        // auto login the user after signup success
                        loginUser(name, email, password)

                    } else { // Sign up Unsuccessful

                        // Stop Loading Progress Bar
                        showLoadingBar(false)

                        // Notify the user about signup failure
                        Toast.makeText(
                            applicationContext,
                            "Signup Failed. Error: ${task.exception}",
                            Toast.LENGTH_LONG
                        ).show()

                        Log.e(TAG, "signUpUser: ${task.exception}")

                    }
                }
        }
    }

    private fun loginUser(name: String, email: String, password: String) {
        CoroutineScope(Dispatchers.IO).launch {

            // auto login the user with the email and password
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->

                    // Stop Loading Progress Bar
                    showLoadingBar(false)

                    // Login Successful
                    if (task.isSuccessful) {

                        // Notify the user about login success
                        Toast.makeText(
                            applicationContext,
                            "Congratulations! Login successful",
                            Toast.LENGTH_SHORT
                        ).show()

                        postDetailsToDB(name, auth.currentUser, email)

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

    private fun postDetailsToDB(name: String, currentUser: FirebaseUser?, email: String) {
        if (currentUser != null) {

            CoroutineScope(Dispatchers.IO).launch {

                val userModel = UserModel(
                    uId = currentUser.uid,
                    name = name,
                    email = email
                )

                db.collection("users")
                    .document(userModel.uId)
                    .set(userModel)
                    .addOnCompleteListener { task ->

                        if (task.isSuccessful) {

                            // Move to HomeActivity post successful login
                            val intent = Intent(this@SignUpActivity, HomeActivity::class.java)
                            startActivity(intent)
                            finishAffinity()

                        } else {

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
}