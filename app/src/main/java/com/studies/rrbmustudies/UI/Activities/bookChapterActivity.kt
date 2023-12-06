package com.studies.rrbmustudies.UI.Activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.studies.rrbmustudies.Adapters.booksChaptersAdapter
import com.studies.rrbmustudies.Adapters.oldPaperAdapter
import com.studies.rrbmustudies.Models.bookChapterModel
import com.studies.rrbmustudies.Models.oldPaperContentModel
import com.studies.rrbmustudies.Models.oldpapersModel
import com.studies.rrbmustudies.R
import com.studies.rrbmustudies.databinding.ActivityBookChapterBinding


class bookChapterActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityBookChapterBinding.inflate(layoutInflater)
    }
lateinit var homeTitle :String
lateinit var CourseType :String
lateinit var Year :String
lateinit var MaterialType :String
lateinit var Firebasepath:String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


if(intent.hasExtra("Context")) {
    binding.bookChapterPB.visibility= View.VISIBLE
    homeTitle=intent.getStringExtra("homeTitle").toString()
    CourseType=intent.getStringExtra("CourseType").toString()
    Year=intent.getStringExtra("Year").toString()
    MaterialType=intent.getStringExtra("MaterialType").toString()
    Firebasepath="/$homeTitle/$CourseType/$Year/$MaterialType"
binding.bookChaptersHeading.text=MaterialType



    val databaseReference: DatabaseReference =
        FirebaseDatabase.getInstance().reference.child(Firebasepath)

    databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            val oldPapersList = dataSnapshotToOldPapersList(dataSnapshot)
            binding.chapterName.adapter =
                oldPaperAdapter(this@bookChapterActivity, oldPapersList, MaterialType.toString())

            binding.bookChapterPB.visibility= View.GONE
            // Now, oldPapersList contains the converted data
            // You can use it as needed
        }

        override fun onCancelled(databaseError: DatabaseError) {
            // Handle errors here
        }
    })


}

else {
    binding.bookChapterPB.visibility=View.GONE
    val receivedList =
        intent.getSerializableExtra("bookChapterList") as? ArrayList<bookChapterModel>

    if (receivedList != null) {
        binding.chapterName.adapter = booksChaptersAdapter(this, receivedList ?: emptyList())
    }


}

    }



    fun dataSnapshotToOldPapersList(dataSnapshot: DataSnapshot): List<oldpapersModel> {
        val oldPapersList = mutableListOf<oldpapersModel>()

        for (subjectSnapshot in dataSnapshot.children) {
            val subjectName = subjectSnapshot.key
            val listofContent = mutableListOf<oldPaperContentModel>()

            for (contentSnapshot in subjectSnapshot.child("listofContent").children) {
                val oldPaperTitle = contentSnapshot.child("oldPaperTitle").getValue(String::class.java)
                val pdfLink = contentSnapshot.child("pdfLink").getValue(String::class.java)
                val coverImage = contentSnapshot.child("coverImage").getValue(String::class.java)

                val contentModel = oldPaperContentModel(
                    oldPaperTitle ?: "",
                    pdfLink ?: "",
                    coverImage ?: ""
                )
                listofContent.add(contentModel)
            }

            val oldPapersModel = oldpapersModel(subjectName ?: "", listofContent)
            oldPapersList.add(oldPapersModel)
        }

        return oldPapersList
    }

}