package com.studies.rrbmustudies.Adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.studies.rrbmustudies.Models.govermentExamPaperCategoryModel
import com.studies.rrbmustudies.Models.oldpapersModel
import com.studies.rrbmustudies.R
import com.studies.rrbmustudies.UI.Activities.GovermentExamPaperActivity
import com.studies.rrbmustudies.UI.Activities.OldPaper2Activity

class govermentExamPaperCategoryAdapter (val context: Context, val list:List<govermentExamPaperCategoryModel>): RecyclerView.Adapter<govermentExamPaperCategoryAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.govermentexam_itemview, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var currentItem = list.get(position)
        holder.govermentExamTitle.text = currentItem.categoryTitle


        holder.parent.setOnClickListener {

            val intent = Intent(context, GovermentExamPaperActivity::class.java)
            intent.putExtra("exampaperList", ArrayList(currentItem.exampaperList))
            intent.putExtra("context"," govermentExamPaperCategory")
            context.startActivity(intent)

        }


    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val parent = itemView.findViewById<CardView>(R.id.govermentexambutton1)
        val govermentExamTitle = itemView.findViewById<TextView>(R.id.govermentExamTitle)

    }
}
