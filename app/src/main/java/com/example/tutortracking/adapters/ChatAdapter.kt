package com.example.tutortracking.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.example.tutortracking.R
import com.example.tutortracking.data.localdata.models.Message
import com.example.tutortracking.databinding.IncomingMessageListItemBinding
import com.example.tutortracking.databinding.OutgoingMessageListItemBinding
import com.example.tutortracking.util.ChatBubble

class ChatAdapter : ListAdapter<ChatBubble, ChatAdapter.ChatViewHolder>(DiffCallback()) {

    // TODO: 12/28/2021 add functionality when clicked on username incoming message to
    // TODO: 12/28/2021 show up a dialog containing user info
    private var onNameClicked: ((String) -> Unit)? = null

    fun setOnNameClickListener(onClick: (String) -> Unit) {
        onNameClicked = onClick
    }

    sealed class ChatViewHolder(binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {

        abstract fun bind(message: Message)

        class IncomingChat(
            private val binding: IncomingMessageListItemBinding,
            private val onClick: (senderId: String) -> Unit
        ) : ChatViewHolder(binding) {
            override fun bind(message: Message) {
                with(binding) {
                    incomingTvSender.text = message.senderName
                    incomingTvMessageTime.setOnClickListener { onClick }
                    incomingTvMessageText.text = message.messageText
                    incomingTvMessageTime.text = message.messageTime
                }
            }
        }

        class OutgoingChat(private val binding: OutgoingMessageListItemBinding) :
            ChatViewHolder(binding) {
            override fun bind(message: Message) {
                with(binding) {
                    outgoingTvMessageText.text = message.messageText
                    outgoingTvMessageTime.text = message.messageTime
                }
            }

        }

    }

    class DiffCallback : DiffUtil.ItemCallback<ChatBubble>() {
        override fun areItemsTheSame(oldItem: ChatBubble, newItem: ChatBubble): Boolean {
            return when {
                oldItem is ChatBubble.Incoming && newItem is ChatBubble.Incoming -> {
                    oldItem.message == newItem.message
                }
                oldItem is ChatBubble.Outgoing && newItem is ChatBubble.Outgoing -> {
                    oldItem.message == newItem.message
                }
                else -> true
            }

        }

        override fun areContentsTheSame(oldItem: ChatBubble, newItem: ChatBubble): Boolean {
            return when {
                oldItem is ChatBubble.Incoming && newItem is ChatBubble.Incoming -> {
                    oldItem.message.messageId == newItem.message.messageId
                }
                oldItem is ChatBubble.Outgoing && newItem is ChatBubble.Outgoing -> {
                    oldItem.message.messageId == newItem.message.messageId
                }
                else -> true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        return when (viewType) {
            R.layout.outgoing_message_list_item -> {
                ChatViewHolder.OutgoingChat(
                    OutgoingMessageListItemBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            R.layout.incoming_message_list_item -> {
                ChatViewHolder.IncomingChat(
                    IncomingMessageListItemBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    ),
                    onNameClicked!!
                )
            }
            else -> throw IllegalArgumentException()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is ChatBubble.Incoming -> R.layout.incoming_message_list_item
            is ChatBubble.Outgoing -> R.layout.outgoing_message_list_item
            else -> throw IllegalArgumentException()
        }
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        when (val chat = getItem(position)) {
            is ChatBubble.Outgoing -> (holder as ChatViewHolder.OutgoingChat).bind(chat.message)
            is ChatBubble.Incoming -> (holder as ChatViewHolder.IncomingChat).bind(chat.message)
        }

    }
}