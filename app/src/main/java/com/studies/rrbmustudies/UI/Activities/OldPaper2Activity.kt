package com.studies.rrbmustudies.UI.Activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.studies.rrbmustudies.Adapters.booksChaptersAdapter
import com.studies.rrbmustudies.Adapters.oldPaperAdapter
import com.studies.rrbmustudies.Adapters.oldPaperAdapter2
import com.studies.rrbmustudies.Models.bookChapterModel
import com.studies.rrbmustudies.Models.oldPaperContentModel
import com.studies.rrbmustudies.R
import com.studies.rrbmustudies.databinding.ActivityOldPaper2Binding

class OldPaper2Activity : AppCompatActivity() {
    private val binding by lazy{
        ActivityOldPaper2Binding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val materialType = intent.getStringExtra("materialType")
        binding.materialTypeText.text=materialType
        val receivedList =
            intent.getSerializableExtra("oldPaperContentList") as? ArrayList<oldPaperContentModel>

        if (receivedList != null) {
            binding.oldpapercontentRV.adapter = oldPaperAdapter2(this, receivedList ?: emptyList())
        }
    }
}