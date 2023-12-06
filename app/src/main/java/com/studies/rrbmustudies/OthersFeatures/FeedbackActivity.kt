package com.studies.rrbmustudies.OthersFeatures

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.hadi.emojiratingbar.RateStatus
import com.studies.rrbmustudies.OthersFeatures.model.FeedbackModel
import com.studies.rrbmustudies.UI.Activities.HomeActivity
import com.studies.rrbmustudies.databinding.ActivityFeedbackBinding

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale

const val FEEDBACK_TAG = "FeedbackActivity"

class FeedbackActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFeedbackBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFeedbackBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Auth
        auth = Firebase.auth

        // Initialize Firestore
        db = Firebase.firestore

        binding.apply {

            // Emoji Rating Bar
            emojiRatingBar.getCurrentRateStatus()

            // Submit Feedback Button
            submitFeedbackBtn.setOnClickListener {

                // Get the current rating from emoji rating bar
                val currentRating = emojiRatingBar.getCurrentRateStatus()

                if (currentRating != RateStatus.EMPTY) {

                    // User entered Additional Comments
                    val additionalComment = additionalCommentsEdt.text.toString()

                    // Show Loading Progress
                    showLoadingBar(true)

                    // Post Feedback to Firebase DB
                    postFeedback(currentRating, additionalComment)

                } else {
                    Toast.makeText(
                        applicationContext,
                        "Please click on the rating emoji to continue",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }

        }

    }

    private fun showLoadingBar(value: Boolean) {
        if (value) {
            binding.loadingProgress.visibility = View.VISIBLE
            binding.emojiRatingBar.setReadOnly(true)
            binding.submitFeedbackBtn.isEnabled = false
        } else {
            binding.loadingProgress.visibility = View.GONE
            binding.emojiRatingBar.setReadOnly(false)
            binding.submitFeedbackBtn.isEnabled = true
        }
    }

    private fun postFeedback(currentRating: RateStatus, additionalComment: String) {
        CoroutineScope(Dispatchers.IO).launch {

            // Check if the current user is not null, then post
            if (auth.currentUser != null) {

                // Get the current user email address
                val userEmail = auth.currentUser!!.email

                // Get the current timestamp
                val df: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
                val timeStamp = df.format(Calendar.getInstance().time)

                // Construct the feedback model
                val feedbackModel = FeedbackModel(
                    rating = currentRating.name,
                    additionalComment = additionalComment,
                    userEmail = userEmail!!,
                    userUID = auth.currentUser!!.uid,
                    timestamp = timeStamp
                )

                db.collection("feedback")
                    .add(feedbackModel)
                    .addOnCompleteListener { task ->

                        // Stop showing Loading Progress
                        showLoadingBar(false)

                        if (task.isSuccessful) { // Post Successful

                            // Notify the user about feedback success
                            Toast.makeText(
                                applicationContext,
                                "Feedback Posted Successfully",
                                Toast.LENGTH_SHORT
                            ).show()

                            // Navigate to Home Activity
                            startActivity(Intent(this@FeedbackActivity, HomeActivity::class.java))
                            finishAffinity()

                        } else {

                            // Notify the user about feedback failure
                            Toast.makeText(
                                applicationContext,
                                "Failed to post Feedback. Error: ${task.exception}",
                                Toast.LENGTH_LONG
                            ).show()

                            Log.e(FEEDBACK_TAG, "postFeedback: Error: ${task.exception}")

                        }
                    }

            }

        }
    }

}