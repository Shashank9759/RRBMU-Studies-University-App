package com.studies.rrbmustudies.Adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.studies.rrbmustudies.Models.home_model
import com.studies.rrbmustudies.OthersFeatures.AboutTeamActivity
import com.studies.rrbmustudies.OthersFeatures.FeedbackActivity
import com.studies.rrbmustudies.OthersFeatures.NotificationsActivity
import com.studies.rrbmustudies.R
import com.studies.rrbmustudies.UI.Activities.BooksActivity
import com.studies.rrbmustudies.UI.Activities.CategoryActivity
import com.studies.rrbmustudies.UI.Activities.CoursesActivity
import com.studies.rrbmustudies.UI.Activities.GovermentExamPaperActivity
import com.studies.rrbmustudies.UI.Activities.HomeActivity
import com.studies.rrbmustudies.UI.Activities.PracticalFileActivity
import com.studies.rrbmustudies.UI.Activities.bookChapterActivity

class Home_Adapters(val context: Context, val list:List<home_model>,val itemLayout:Int,val homeTitle:String?
,val CourseType:String?,val Year :String?):RecyclerView.Adapter<Home_Adapters.ViewHolder>() {




    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view=LayoutInflater.from(parent.context).inflate(itemLayout,parent,false)
       return ViewHolder(view)
    }

    override fun getItemCount(): Int {
      return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var currentItem= list.get(position)
        holder.image.setImageResource(currentItem.image)
        holder.title.setText(currentItem.title)

        if(context is HomeActivity){
            if(currentItem.title.equals("Arts")||currentItem.title.equals("Commerce")||currentItem.title.equals("Science")
                ||currentItem.title.equals("Time Table")){
            holder.parent.setOnClickListener {


                val intent = Intent(context, CoursesActivity::class.java)
                intent.putExtra("homeTitle", currentItem.title)
                context.startActivity(intent)
            }

            }



            when(currentItem.title){



                "Goverment"->{

                    holder.parent.setOnClickListener {

                        val intent = Intent(context, GovermentExamPaperActivity::class.java)
                        context.startActivity(intent)
                    }



                }



                "Notification"->{

                    holder.parent.setOnClickListener {
                        val intent = Intent(context, NotificationsActivity::class.java)
                        intent.putExtra("homeTitle", currentItem.title)
                        context.startActivity(intent)



                    }





                }


                "About Team"->{

                    holder.parent.setOnClickListener {
                        val intent = Intent(context, AboutTeamActivity::class.java)
                        intent.putExtra("homeTitle", currentItem.title)
                        context.startActivity(intent)



                    }



                }



                "Feedback"->{


                    holder.parent.setOnClickListener {

                        val intent = Intent(context, FeedbackActivity::class.java)
                        intent.putExtra("homeTitle", currentItem.title)
                        context.startActivity(intent)


                    }





                }


            }

        }

        if(context is CategoryActivity) {

            when (currentItem.title) {
                "Books" -> {

                    holder.parent.setOnClickListener {
                        val intent = Intent(context, BooksActivity::class.java)
                        intent.putExtra("homeTitle", homeTitle)
                        intent.putExtra("CourseType", CourseType)
                        intent.putExtra("Year", Year)
                        intent.putExtra("MaterialType", "Books")
                        context.startActivity(intent)


                    }

                }

                "Notes" -> {
                    holder.parent.setOnClickListener {
                        val intent = Intent(context, BooksActivity::class.java)
                        intent.putExtra("homeTitle", homeTitle)
                        intent.putExtra("CourseType", CourseType)
                        intent.putExtra("Year", Year)
                        intent.putExtra("MaterialType", "Notes")
                        context.startActivity(intent)

                    }


                }
                "Practical Files" -> {
                    holder.parent.setOnClickListener {
                        val intent = Intent(context, PracticalFileActivity::class.java)
                        intent.putExtra("homeTitle", homeTitle)
                        intent.putExtra("CourseType", CourseType)
                        intent.putExtra("Year", Year)
                        intent.putExtra("MaterialType", "Practical Files")
                        context.startActivity(intent)

                    }


                }

                "Old Papers" -> {
                    holder.parent.setOnClickListener {
                        val intent = Intent(context, bookChapterActivity::class.java)
                        intent.putExtra("homeTitle", homeTitle)
                        intent.putExtra("CourseType", CourseType)
                        intent.putExtra("Year", Year)
                        intent.putExtra("MaterialType", "Old Papers")
                        intent.putExtra("Context", "CategoryActivity")
                        context.startActivity(intent)

                    }


                }

                "One Week Series" -> {
                    holder.parent.setOnClickListener {
                        val intent = Intent(context, bookChapterActivity::class.java)
                        intent.putExtra("homeTitle", homeTitle)
                        intent.putExtra("CourseType", CourseType)
                        intent.putExtra("Year", Year)
                        intent.putExtra("MaterialType", "One Week Series")
                        intent.putExtra("Context", "CategoryActivity")
                        context.startActivity(intent)

                    }


                }

                "Syllabus" -> {
                    holder.parent.setOnClickListener {
                        val intent = Intent(context, bookChapterActivity::class.java)
                        intent.putExtra("homeTitle", homeTitle)
                        intent.putExtra("CourseType", CourseType)
                        intent.putExtra("Year", Year)
                        intent.putExtra("MaterialType", "Syllabus")
                        intent.putExtra("Context", "CategoryActivity")
                        context.startActivity(intent)

                    }


                }

                "Assignment" -> {
                    holder.parent.setOnClickListener {
                        val intent = Intent(context, bookChapterActivity::class.java)
                        intent.putExtra("homeTitle", homeTitle)
                        intent.putExtra("CourseType", CourseType)
                        intent.putExtra("Year", Year)
                        intent.putExtra("MaterialType", "Assignment")
                        intent.putExtra("Context", "CategoryActivity")
                        context.startActivity(intent)

                    }


                }


            }
        }}

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val parent =itemView.findViewById<LinearLayout>(R.id.homeLinearLayout)
        val image=itemView.findViewById<ImageView>(R.id.home_image)
        val title=itemView.findViewById<TextView>(R.id.home_image_title)
    }
}