package com.studies.rrbmustudies.OthersFeatures.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

import com.rajit.rrbmustudies.util.NotificationDiffUtil
import com.studies.rrbmustudies.OthersFeatures.model.NotificationModel
import com.studies.rrbmustudies.databinding.NotificationItemviewBinding

class NotificationAdapter(
    private val onNotificationClick: (String) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    private var notificationList: List<NotificationModel> = mutableListOf()

    class NotificationViewHolder(private val binding: NotificationItemviewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(onClick:(String) -> Unit, notification: NotificationModel) {

            binding.apply {

                notificationTitle.text = notification.title
                notificationCard.setOnClickListener {
                    onClick(notification.link)
                }
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = NotificationItemviewBinding.inflate(inflater, parent, false)
        return NotificationViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return notificationList.size
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {

        val currentItem = notificationList[position]

        holder.bind(onNotificationClick, currentItem)
    }

    fun submitList(newList: List<NotificationModel>) {
        val diffUtil = NotificationDiffUtil(notificationList, newList)
        val diffUtilResult = DiffUtil.calculateDiff(diffUtil)
        notificationList = newList
        diffUtilResult.dispatchUpdatesTo(this)
    }

}