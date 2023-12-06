package com.studies.rrbmustudies.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.studies.rrbmustudies.Models.bookparentmodel
import com.studies.rrbmustudies.R
import java.util.List

class booksParentAdapter(val context: Context, val list: List<bookparentmodel>?): RecyclerView.Adapter<booksParentAdapter.ViewHolder>() {




    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view= LayoutInflater.from(parent.context).inflate(R.layout.books_parent_itemview,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list!!.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var currentItem= list?.get(position)

        holder.title.setText(currentItem?.title)
        holder.recyclerView.adapter=booksChildAdapter(context,currentItem!!.childlist)

        Glide.with(context)
            .asGif()
            .load(R.drawable.scroll_icon) // Replace with the actual resource ID or URL
            .into(holder.horizontalScrollIcon)

    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val parent =itemView.findViewById<LinearLayout>(R.id.bookparentitemviewlayout)
        val title=itemView.findViewById<TextView>(R.id.subjectName)
        val recyclerView=itemView.findViewById<RecyclerView>(R.id.booksParentRV)
        val horizontalScrollIcon=itemView.findViewById<ImageView>(R.id.horizontalScroll)
    }
}