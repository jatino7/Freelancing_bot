package com.o7solutions.freelancing_bot.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.o7solutions.freelancing_bot.data_classes.Message
import com.o7solutions.freelancing_bot.databinding.ItemMessageReceivedBinding
import com.o7solutions.freelancing_bot.databinding.ItemMessageSentBinding
import com.o7solutions.freelancing_bot.utils.Functions

class MessageAdapter(
    private val messages: List<Message>,
    private val currentUserId: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val VIEW_TYPE_SENT = 1
        const val VIEW_TYPE_RECEIVED = 2
    }

    inner class SentViewHolder(val binding: ItemMessageSentBinding) :
        RecyclerView.ViewHolder(binding.root)

    inner class ReceivedViewHolder(val binding: ItemMessageReceivedBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].senderId == currentUserId) VIEW_TYPE_SENT else VIEW_TYPE_RECEIVED
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_SENT) {
            val binding = ItemMessageSentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            SentViewHolder(binding)
        } else {
            val binding = ItemMessageReceivedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            ReceivedViewHolder(binding)
        }
    }

    override fun getItemCount() = messages.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        val formattedTime = Functions.formatDateTime(message.time!!.toLong())

        if (holder is SentViewHolder) {
            holder.binding.textMessage.text = message.message
            holder.binding.textTime.text = formattedTime
        } else if (holder is ReceivedViewHolder) {
            holder.binding.textMessage.text = message.message
            holder.binding.textTime.text = formattedTime
        }
    }
}
