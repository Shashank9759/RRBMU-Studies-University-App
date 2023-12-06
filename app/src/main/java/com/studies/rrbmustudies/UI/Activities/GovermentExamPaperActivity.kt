package com.studies.rrbmustudies.UI.Activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.studies.rrbmustudies.Adapters.govermenrExamPaperAdapter
import com.studies.rrbmustudies.Adapters.govermentExamPaperCategoryAdapter
import com.studies.rrbmustudies.Adapters.oldPaperAdapter2
import com.studies.rrbmustudies.Models.govermentExamPaperCategoryModel
import com.studies.rrbmustudies.Models.govermentExamPaperModel
import com.studies.rrbmustudies.Models.oldPaperContentModel
import com.studies.rrbmustudies.databinding.ActivityGovermentExamPaperBinding


class GovermentExamPaperActivity : AppCompatActivity() {


    lateinit var mAdView : AdView
    private val binding by lazy {
        ActivityGovermentExamPaperBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
binding.govermentexamPB.visibility= View.VISIBLE


        MobileAds.initialize(this)

        mAdView = binding.govermentAdView
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)


if(intent.hasExtra("context")){

    binding.govermentexamPB.visibility= View.GONE

    val receivedList =
        intent.getSerializableExtra("exampaperList") as? ArrayList<govermentExamPaperModel>

    if (receivedList != null) {
        binding.GovermentRV.adapter = govermenrExamPaperAdapter(this, receivedList ?: emptyList())
    }



}else{

    val FirebasePath = "/Goverment/govermentExamPaperCategoryList"

    // Reference to your Firebase Realtime Database
    val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().reference.child(FirebasePath)

    // Using addListenerForSingleValueEvent to fetch data
    databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            if (snapshot.exists()) {
                val categoryList = mutableListOf<govermentExamPaperCategoryModel>()

                for (categorySnapshot in snapshot.children) {
                    val categoryTitle = categorySnapshot.child("categoryTitle").getValue(String::class.java)
                    val exampaperList = mutableListOf<govermentExamPaperModel>()

                    for (paperSnapshot in categorySnapshot.child("exampaperList").children) {
                        val title = paperSnapshot.child("title").getValue(String::class.java)
                        val pdfLink = paperSnapshot.child("pdfLink").getValue(String::class.java)
                        val paperModel = govermentExamPaperModel(title ?: "", pdfLink ?: "")
                        exampaperList.add(paperModel)
                    }

                    val categoryModel = govermentExamPaperCategoryModel(categoryTitle ?: "", exampaperList)
                    categoryList.add(categoryModel)
                }
                binding.govermentexamPB.visibility= View.GONE
                binding.GovermentRV.adapter=govermentExamPaperCategoryAdapter(this@GovermentExamPaperActivity,categoryList)

            } else {
                Toast.makeText(this@GovermentExamPaperActivity,"Database not Found",Toast.LENGTH_LONG).show()
            }
        }

        override fun onCancelled(error: DatabaseError) {

            Toast.makeText(this@GovermentExamPaperActivity,"Database error: $error",Toast.LENGTH_LONG).show()

        }
    })
}





}


    }
