package com.studies.rrbmustudies.Adapters


import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.studies.rrbmustudies.Models.bookchildmodel

import com.studies.rrbmustudies.R
import com.studies.rrbmustudies.UI.Activities.bookChapterActivity

class booksChildAdapter(val context: Context, val list:List<bookchildmodel>):RecyclerView.Adapter<booksChildAdapter.ViewHolder>() {




    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view=LayoutInflater.from(parent.context).inflate(R.layout.books_child_itemview,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var currentItem= list.get(position)

        holder.bookName.setText(currentItem.bookName)
        Picasso.get().load(currentItem.coverImage).into(holder.coverImage);
      holder.parent.setOnClickListener {
          val intent = Intent(context, bookChapterActivity::class.java)
          intent.putExtra("bookChapterList", ArrayList(currentItem.bookchapterList))

          context.startActivity(intent)


      }



    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
      val parent =itemView.findViewById<ConstraintLayout>(R.id.bookchilditemviewlayout)
        val coverImage=itemView.findViewById<ImageView>(R.id.bookCoverImage)
        val bookName=itemView.findViewById<TextView>(R.id.bookCoverTitle)
    }
}