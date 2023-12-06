package com.studies.rrbmustudies.UI.Activities

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.studies.rrbmustudies.Adapters.booksParentAdapter
import com.studies.rrbmustudies.Models.bookChapterModel
import com.studies.rrbmustudies.Models.bookchildmodel
import com.studies.rrbmustudies.Models.bookparentmodel
import com.studies.rrbmustudies.databinding.ActivityBooksBinding
import java.util.ArrayList
import java.util.Iterator
import java.util.List

class BooksActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityBooksBinding.inflate(layoutInflater)
    }

    var parentList: List<bookparentmodel>? = null




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.bookPB.visibility = View.VISIBLE
        var homeTitle=intent.getStringExtra("homeTitle").toString()
      val CourseType=intent.getStringExtra("CourseType").toString()
        val  MaterialType=intent.getStringExtra("MaterialType").toString()
        val  Year=intent.getStringExtra("Year").toString()
        val Firebasepath="/$homeTitle/$CourseType/$Year/$MaterialType/parentList"

        binding.categoryTitle.text = MaterialType


        val database = FirebaseDatabase.getInstance()
        val reference = database.getReference(Firebasepath)

        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val bookList = dataSnapshotToBookList(dataSnapshot)

                parentList = bookList
                val recyclerView: RecyclerView = binding.ParentBooksRecyclerview
                binding.bookPB.visibility = View.GONE
                recyclerView.adapter = booksParentAdapter(this@BooksActivity, parentList)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@BooksActivity, "Loading Failed  : $databaseError", Toast.LENGTH_LONG).show()
            }
        })
    }

    fun dataSnapshotToBookList(dataSnapshot: DataSnapshot): List<bookparentmodel> {
        val bookList: MutableList<bookparentmodel> = ArrayList()

        for (parentSnapshot in dataSnapshot.children) {
            val title: String = parentSnapshot.child("title").getValue(String::class.java) ?: ""
            val childList: MutableList<bookchildmodel> = ArrayList()

            for (childSnapshot in parentSnapshot.child("childList").children) {
                val coverImage: String = childSnapshot.child("coverImage").getValue(String::class.java) ?: ""
                val bookName: String = childSnapshot.child("bookName").getValue(String::class.java) ?: ""
                val bookChapterList: MutableList<bookChapterModel> = ArrayList()

                for (chapterSnapshot in childSnapshot.child("bookchapterList").children) {
                    val chapterName: String = chapterSnapshot.child("chapterName").getValue(String::class.java) ?: ""
                    val pdfLink: String = chapterSnapshot.child("pdfLink").getValue(String::class.java) ?: ""

                    val chapterModel = bookChapterModel(
                        if (chapterName == null) "" else chapterName,
                        pdfLink
                    )
                    bookChapterList.add(chapterModel)
                }

                val childModel = bookchildmodel(
                    if (coverImage == null) "" else coverImage,
                    bookName,
                    bookChapterList
                )
                childList.add(childModel)
            }

            if (title != null) {
                val parentModel = bookparentmodel(title, childList)
                bookList.add(parentModel)
            }
        }

        return bookList as List<bookparentmodel>
    }

}
