package com.studies.rrbmustudies.OthersFeatures

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.studies.rrbmustudies.OthersFeatures.adapter.NotificationAdapter
import com.studies.rrbmustudies.OthersFeatures.model.NotificationModel
import com.studies.rrbmustudies.databinding.ActivityNotificationsBinding

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationsBinding
    private lateinit var mAdapter: NotificationAdapter

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        db = Firebase.firestore

        getDataFromDB()

        mAdapter = NotificationAdapter { link ->
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Log.e(TAG, "onCreate: No activity found to Open the link. Error: $e")
                Toast.makeText(
                    applicationContext,
                    "No app found to open the link",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                Log.e(TAG, "onCreate: Error: $e")
                Toast.makeText(
                    applicationContext,
                    "Error: $e",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.notificationRV.apply {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(false)
            adapter = mAdapter
        }

        binding.searchEdt.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
                text?.let {
                    if (it.isNotEmpty()) {
                        searchForNotification(it.toString())
                    }
                }
            }

            override fun afterTextChanged(p0: Editable?) {}

        })

        binding.refreshBtn.setOnClickListener { getDataFromDB() }

    }

    private fun searchForNotification(searchQuery: String) {

        CoroutineScope(Dispatchers.IO).launch {

            db.collection("notifications")
                .get()
                .addOnSuccessListener { documents ->

                    val filteredDocs = documents.filter {
                        it["title"].toString().contains(searchQuery, true)
                    }

                    if (filteredDocs.isNotEmpty()) {
                        val list = mutableListOf<NotificationModel>()

                        for (document in filteredDocs) {

                            val notif = NotificationModel(
                                document["title"].toString(),
                                document["link"].toString()
                            )

                            list.add(notif)
                        }

                        mAdapter.submitList(list)

                    } else {
                        Toast.makeText(
                            applicationContext,
                            "No results found",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Error getting documents: ", exception)
                }

        }

    }

    private fun getDataFromDB() {
        CoroutineScope(Dispatchers.IO).launch {

            if (auth.currentUser != null) {

                db.collection("notifications")
                    .get()
                    .addOnSuccessListener { documents ->

                        val listOfNotification = mutableListOf<NotificationModel>()

                        for (document in documents) {

                            val notifTitle = document["title"].toString()
                            val notifLink = document["link"].toString()
                            val notif = NotificationModel(notifTitle, notifLink)
                            listOfNotification.add(notif)
                        }

                        mAdapter.submitList(listOfNotification)
                    }
                    .addOnFailureListener { exception ->
                        Log.e(TAG, "getDataFromDB: Error: $exception")
                        Toast.makeText(
                            applicationContext,
                            "Fatal error occurred. Error: $exception",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }

        }
    }


}