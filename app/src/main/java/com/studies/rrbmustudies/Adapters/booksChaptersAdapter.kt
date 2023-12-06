package com.studies.rrbmustudies.Adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.studies.rrbmustudies.Models.bookChapterModel
import com.studies.rrbmustudies.R
import com.studies.rrbmustudies.UI.Activities.PdfView

class booksChaptersAdapter (val context: Context, val list:List<bookChapterModel>): RecyclerView.Adapter<booksChaptersAdapter.ViewHolder>() {




    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view=
            LayoutInflater.from(parent.context).inflate(R.layout.bookchapteritemview,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var currentItem= list.get(position)
holder.bookchapterName.text=currentItem.chapterName
        holder.bookchapterName.setOnClickListener {
//             val intent = Intent(context,PdfView::class.java)
//         intent.putExtra("pdfLink",currentItem.pdfLink)
//            context.startActivity(intent)


            val pdfUrl = currentItem.pdfLink

            val intent = Intent(context, PdfView::class.java)
            intent.putExtra(PdfView.KEY_URL, pdfUrl)
            context.startActivity(intent)

        }


    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val parent =itemView.findViewById<ConstraintLayout>(R.id.bookchapterparent)
        val bookchapterName=itemView.findViewById<Button>(R.id.bookchapterName)

    }
}