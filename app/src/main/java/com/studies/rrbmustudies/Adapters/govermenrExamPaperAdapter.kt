package com.studies.rrbmustudies.Adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.studies.rrbmustudies.UI.Activities.PdfView


import com.studies.rrbmustudies.Models.govermentExamPaperModel
import com.studies.rrbmustudies.R


class govermenrExamPaperAdapter (val context: Context, val ExamPaperlist:List<govermentExamPaperModel>): RecyclerView.Adapter<govermenrExamPaperAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.govermentexam_itemview, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return ExamPaperlist.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var currentItem = ExamPaperlist.get(position)
        holder.govermentExamTitle.text = currentItem.title


        holder.parent.setOnClickListener {

            val intent = Intent(context, PdfView::class.java)
            intent.putExtra(PdfView.KEY_URL,currentItem.pdfLink)

            context.startActivity(intent)

        }


    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val parent = itemView.findViewById<CardView>(R.id.govermentexambutton1)
        val govermentExamTitle = itemView.findViewById<TextView>(R.id.govermentExamTitle)

    }
}
