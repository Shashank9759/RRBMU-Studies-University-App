package com.studies.rrbmustudies.Adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView

import com.studies.rrbmustudies.Models.oldpapersModel
import com.studies.rrbmustudies.R
import com.studies.rrbmustudies.UI.Activities.OldPaper2Activity
import com.studies.rrbmustudies.UI.Activities.PdfView

class oldPaperAdapter (val context: Context, val list:List<oldpapersModel>,val MaterialType:String): RecyclerView.Adapter<oldPaperAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.bookchapteritemview, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var currentItem = list.get(position)
        holder.subjectName.text = currentItem.subjectName
        holder.subjectName.setOnClickListener {
//             val intent = Intent(context,PdfView::class.java)
//         intent.putExtra("pdfLink",currentItem.pdfLink)
//            context.startActivity(intent)


            val intent = Intent(context, OldPaper2Activity::class.java)
            intent.putExtra("oldPaperContentList", ArrayList(currentItem.listofContent))
            intent.putExtra("materialType", MaterialType)
            context.startActivity(intent)

        }


    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val parent = itemView.findViewById<ConstraintLayout>(R.id.bookchapterparent)
        val subjectName = itemView.findViewById<Button>(R.id.bookchapterName)

    }
}
