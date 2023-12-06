package com.studies.rrbmustudies.UI.Activities

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.studies.rrbmustudies.Adapters.Home_Adapters
import com.studies.rrbmustudies.Models.home_model
import com.studies.rrbmustudies.R
import com.studies.rrbmustudies.databinding.ActivityCategoryBinding

import kotlin.collections.listOf

class CategoryActivity : AppCompatActivity() {
    private val binding by lazy{
        ActivityCategoryBinding.inflate(layoutInflater)
    }
    lateinit var mAdView : AdView

    lateinit var adapter: Home_Adapters

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.getRoot())
        MobileAds.initialize(this)
        val findViewById: View = findViewById(R.id.CategoryadView)

        MobileAds.initialize(this)

        mAdView = binding.CategoryadView
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        val list = listOf(
            home_model(R.drawable.category_book, "Books"),
            home_model(R.drawable.category_notes, "Notes"),
            home_model(R.drawable.category_practicalfile, "Practical Files"),
            home_model(R.drawable.category_oldpapers, "Old Papers"),
            home_model(R.drawable.category_1weekseries, "One Week Series"),
            home_model(R.drawable.category_syllabus, "Syllabus"),
            home_model(R.drawable.category_assignment, "Assignment")
        )

        val homeTitle = intent.getStringExtra("homeTitle")
        val CourseType = intent.getStringExtra("CourseType")
        val Year = intent.getStringExtra("Year")


        adapter=(Home_Adapters(this, list, R.layout.home_item_view, homeTitle, CourseType, Year))
        binding.categoryRecyclerview.adapter=adapter


    }


}
