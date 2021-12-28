package com.example.tutortracking.data.localdata.models.dto

import com.example.tutortracking.data.localdata.models.Message
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.*

@Serializable
data class MessageDto(
    val messageText: String,
    val senderName: String,
    val senderId: String,
    val timeStamp: Long,
    val messageId: String
){
    fun toMessage() : Message{
        val date = Date(timeStamp)
        val formatter = SimpleDateFormat("hh:mm", Locale.getDefault())
        val messageTime = formatter.format(date)
        return Message(
            messageId = messageId,
            senderName = senderName,
            messageTime = messageTime,
            messageText = messageText,
            senderId = senderId
        )
    }
}