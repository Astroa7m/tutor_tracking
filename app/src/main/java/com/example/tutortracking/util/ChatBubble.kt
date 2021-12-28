package com.example.tutortracking.util

import com.example.tutortracking.data.localdata.models.Message

sealed class ChatBubble {
    data class Incoming(val message: Message) : ChatBubble()
    data class Outgoing(val message: Message) : ChatBubble()
    object Loading : ChatBubble()
}