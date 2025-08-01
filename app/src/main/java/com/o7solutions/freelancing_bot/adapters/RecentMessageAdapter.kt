package com.o7solutions.freelancing_bot.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.o7solutions.freelancing_bot.data_classes.RecentMessage
import com.o7solutions.freelancing_bot.databinding.ItemRecentMessageBinding
import com.o7solutions.freelancing_bot.utils.Functions
import java.text.SimpleDateFormat

class RecentMessageAdapter(private val list: List<RecentMessage>,var onClick: OnClick) :
    RecyclerView.Adapter<RecentMessageAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemRecentMessageBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRecentMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount() = list.size


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        if(item.receiverId == FirebaseAuth.getInstance().currentUser?.email) {
            holder.binding.textUser.text = item.senderId // or senderId based on current user
        } else {
            holder.binding.textUser.text = item.receiverId // or senderId based on current user

        }
        holder.binding.textLastMessage.text = item.message
        holder.binding.textTime.text = Functions.formatDateTime(item.time!!.toLong())

        holder.itemView.setOnClickListener {
            onClick.visit(position)
        }
    }

    interface OnClick {

        fun visit(position: Int)
    }


}
