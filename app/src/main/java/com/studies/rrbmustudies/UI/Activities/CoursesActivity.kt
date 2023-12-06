package com.studies.rrbmustudies.UI.Activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.studies.rrbmustudies.Models.timeTableModel
import com.studies.rrbmustudies.R
import com.studies.rrbmustudies.databinding.ActivityCoursesBinding


class CoursesActivity : AppCompatActivity() {
    lateinit var mAdView : AdView
    private val binding by lazy {
        ActivityCoursesBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


        MobileAds.initialize(this)

        mAdView = findViewById(R.id.adViewCourses)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)


        val homeTitle = intent.getStringExtra("homeTitle")




        //list of timetable
         val listOfTimeTable = listOf<timeTableModel>(timeTableModel("https://www.cbse.gov.in/cbsenew/documents/Date_Sheet_Session_2022_23_30122022_Updated.pdf"),
             timeTableModel("https://www.cbse.gov.in/cbsenew/documents/Date_Sheet_Session_2022_23_30122022_Updated.pdf"),
             timeTableModel("https://www.cbse.gov.in/cbsenew/documents/Date_Sheet_Session_2022_23_30122022_Updated.pdf"),
             timeTableModel("https://www.cbse.gov.in/cbsenew/documents/Date_Sheet_Session_2022_23_30122022_Updated.pdf"),
             timeTableModel("https://www.cbse.gov.in/cbsenew/documents/Date_Sheet_Session_2022_23_30122022_Updated.pdf"),)

        binding.coursesUGFirstyr.setOnClickListener {

            if(homeTitle=="Time Table"){
               val intent= Intent(this,PdfView::class.java)
                intent.putExtra(PdfView.KEY_URL,listOfTimeTable.get(0).pdfLink)
                startActivity(intent)
            }
            else{
                val intent= Intent(this,CategoryActivity::class.java)
                intent.putExtra("homeTitle",homeTitle)
                intent.putExtra("CourseType","UG")
                intent.putExtra("Year","1st")


                startActivity(intent)

            }

        }

        binding.coursesUGSecondyr.setOnClickListener {
            if(homeTitle=="Time Table"){
                val intent= Intent(this,PdfView::class.java)
                intent.putExtra(PdfView.KEY_URL,listOfTimeTable.get(1).pdfLink)
                startActivity(intent)
            }
            else{

                val intent= Intent(this,CategoryActivity::class.java)
                intent.putExtra("homeTitle",homeTitle)
                intent.putExtra("CourseType","UG")
                intent.putExtra("Year","2nd")


                startActivity(intent)
            }


        }


        binding.coursesUGThirdyr.setOnClickListener {
            if(homeTitle=="Time Table"){
                val intent= Intent(this,PdfView::class.java)
                intent.putExtra(PdfView.KEY_URL,listOfTimeTable.get(2).pdfLink)
                startActivity(intent)
            }
            else{

                val intent= Intent(this,CategoryActivity::class.java)
                intent.putExtra("homeTitle",homeTitle)
                intent.putExtra("CourseType","UG")
                intent.putExtra("Year","3rd")


                startActivity(intent)
            }



        }


        binding.coursesPGPrevyr.setOnClickListener {
            if(homeTitle=="Time Table"){
                val intent= Intent(this,PdfView::class.java)
                intent.putExtra(PdfView.KEY_URL,listOfTimeTable.get(3).pdfLink)
                startActivity(intent)
            }
            else{


                val intent= Intent(this,CategoryActivity::class.java)
                intent.putExtra("homeTitle",homeTitle)
                intent.putExtra("CourseType","PG")
                intent.putExtra("Year","prev")


                startActivity(intent)
            }



        }

        binding.coursesPGFinalyr.setOnClickListener {
            if(homeTitle=="Time Table"){
                val intent= Intent(this,PdfView::class.java)
                intent.putExtra(PdfView.KEY_URL,listOfTimeTable.get(4).pdfLink)
                startActivity(intent)
            }
            else{

                val intent= Intent(this,CategoryActivity::class.java)
                intent.putExtra("homeTitle",homeTitle)
                intent.putExtra("CourseType","PG")
                intent.putExtra("Year","final")


                startActivity(intent)
            }



        }

    }
}