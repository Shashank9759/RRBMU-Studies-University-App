package com.studies.rrbmustudies.Adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

import com.studies.rrbmustudies.Models.practicalfileModel
import com.studies.rrbmustudies.R
import com.studies.rrbmustudies.UI.Activities.PdfView


class practicalfile_Adapter (val context: Context, val list:List<practicalfileModel>): RecyclerView.Adapter<practicalfile_Adapter.ViewHolder>() {




    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view=
            LayoutInflater.from(parent.context).inflate(R.layout.practicalfile_itemview,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var currentItem= list.get(position)

        holder.fileName.setText(currentItem.title)
        Picasso.get().load(currentItem.image).into(holder.coverImage);
        holder.parent.setOnClickListener {
            val intent = Intent(context, PdfView::class.java)
            intent.putExtra(PdfView.KEY_URL, currentItem.pdfLink)

            context.startActivity(intent)


        }



    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val parent =itemView.findViewById<LinearLayout>(R.id.practicalFileParent)
        val coverImage=itemView.findViewById<ImageView>(R.id.practicalfileImage)
        val fileName=itemView.findViewById<TextView>(R.id.practicalfileTitle)

    }
}