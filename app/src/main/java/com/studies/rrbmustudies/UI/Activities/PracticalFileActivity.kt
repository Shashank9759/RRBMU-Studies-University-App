package com.studies.rrbmustudies.UI.Activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.studies.rrbmustudies.Adapters.Home_Adapters
import com.studies.rrbmustudies.Adapters.practicalfile_Adapter
import com.studies.rrbmustudies.Models.home_model
import com.studies.rrbmustudies.Models.practicalfileModel
import com.studies.rrbmustudies.R
import com.studies.rrbmustudies.databinding.ActivityPracticalFileBinding

class PracticalFileActivity : AppCompatActivity() {

    private val binding by lazy{
        ActivityPracticalFileBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.practicalFilePB.visibility= View.VISIBLE

        val homeTitle= intent.getStringExtra("homeTitle").toString()
        val CourseType= intent.getStringExtra("CourseType").toString()
        val Year= intent.getStringExtra("Year").toString()
        val MaterialType= intent.getStringExtra("MaterialType").toString()
        val FirebasePath="/$homeTitle/$CourseType/$Year/Practical Files"


        val database = FirebaseDatabase.getInstance()
        val reference = database.getReference(FirebasePath)
        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val practicalFileList = mutableListOf<practicalfileModel>()

                    for (childSnapshot in snapshot.children) {
                        val image = childSnapshot.child("image").getValue(String::class.java) ?: ""
                        val title = childSnapshot.child("title").getValue(String::class.java) ?: ""
                        val pdfLink = childSnapshot.child("pdfLink").getValue(String::class.java) ?: ""

                        val practicalFileModel = practicalfileModel(image, title, pdfLink)
                        practicalFileList.add(practicalFileModel)
                    }
                    binding.practicalFilePB.visibility= View.GONE
                    binding.homeRecyclerview.adapter=practicalfile_Adapter(this@PracticalFileActivity,practicalFileList)

                }
            }

            override fun onCancelled(error: DatabaseError) {
             Toast.makeText(this@PracticalFileActivity,"Error : $error",Toast.LENGTH_LONG).show()
            }
        })


    }
}