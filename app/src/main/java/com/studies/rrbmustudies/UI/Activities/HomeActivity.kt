package com.studies.rrbmustudies.UI.Activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.studies.rrbmustudies.Adapters.Home_Adapters
import com.studies.rrbmustudies.Models.home_model
import com.studies.rrbmustudies.R
import com.studies.rrbmustudies.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {
    lateinit var mAdView : AdView
    lateinit var adapter: Home_Adapters
    private val binding by lazy {
        ActivityHomeBinding.inflate(layoutInflater)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        MobileAds.initialize(this)

        mAdView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)



        val list = listOf<home_model>(
            home_model(R.drawable.home_art,"Arts"),
            home_model(R.drawable.home_commerce,"Commerce"),home_model(R.drawable.home_science,"Science"),
            home_model(R.drawable.home_gov,"Goverment"),home_model(R.drawable.home_timetable,"Time Table"),
            home_model(R.drawable.home_notification,"Notification"),home_model(R.drawable.home_admitcard,"Admit Card"),
            home_model(R.drawable.home_result,"Result"),home_model(R.drawable.home_aboutus,"About Team"),
            home_model(R.drawable.home_feedback,"Feedback")
        )


        adapter=Home_Adapters(this,list,R.layout.home_item_view,"","","")

        binding.homeRecyclerview.adapter=adapter

    }
}