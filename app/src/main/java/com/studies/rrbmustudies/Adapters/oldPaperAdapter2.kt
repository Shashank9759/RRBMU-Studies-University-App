package com.studies.rrbmustudies.Adapters

import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

import com.studies.rrbmustudies.Models.oldPaperContentModel

import com.studies.rrbmustudies.R

import com.studies.rrbmustudies.UI.Activities.PdfView

class oldPaperAdapter2  (val context: Context, val list:List<oldPaperContentModel>): RecyclerView.Adapter<oldPaperAdapter2.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.books_child_itemview, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var currentItem = list.get(position)
        holder.OldPaperCoverTitle.text = currentItem.oldPaperTitle


        Picasso.get().load(currentItem.coverImage).into(holder.OldPaperCoverImage)
        holder.parent.setOnClickListener {
//             val intent = Intent(context,PdfView::class.java)
//         intent.putExtra("pdfLink",currentItem.pdfLink)
//            context.startActivity(intent)


            val intent = Intent(context, PdfView::class.java)
            intent.putExtra(PdfView.KEY_URL, currentItem.pdfLink)
            context.startActivity(intent)

        }


    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val parent = itemView.findViewById<ConstraintLayout>(R.id.bookchilditemviewlayout)
        val OldPaperCoverImage = itemView.findViewById<ImageView>(R.id.bookCoverImage)
        val OldPaperCoverTitle = itemView.findViewById<TextView>(R.id.bookCoverTitle)


    }
}
