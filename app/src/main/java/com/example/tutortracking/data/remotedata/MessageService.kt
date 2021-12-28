package com.example.tutortracking.data.remotedata

import com.example.tutortracking.data.localdata.models.Message
import com.example.tutortracking.data.localdata.models.dto.MessageDto
import com.example.tutortracking.util.Constants.CHAT
import com.example.tutortracking.util.Constants.GET_ALL_MESSAGES
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.url
import com.example.tutortracking.util.Result
import io.ktor.client.features.websocket.*
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class MessageService(
    private val client: HttpClient
) {

    private var socket: WebSocketSession? = null

    suspend fun getAllMessages(token: String): List<Message> {
        return try {
            val listOfMessage = client.get<List<MessageDto>> {
                url(EndPoints.GetAllMessages.url)
                headers["Authorization"] = "Bearer $token"
            }.map { messageDto ->
                messageDto.toMessage()
            }
            listOfMessage
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun openSession(token: String): Result<Unit> {
        return try {
            socket = client.webSocketSession {
                url(EndPoints.Chat.url)
                headers["Authorization"] = "Bearer $token"
            }
            if (socket?.isActive == true)
                Result.Success(Unit)
            else
                Result.Error("Error connecting to server: Sokcet is closed")
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error connecting to server")
        }
    }

    suspend fun sendMessage(message: String) {
        try {
            socket?.send(message)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun observeMessage(): Flow<Message> {
        try {
            val flowOfMessage = socket?.incoming
                ?.receiveAsFlow()
                ?.filter { it is Frame.Text }
                ?.map { messageFrame ->
                    val messageJson = (messageFrame as Frame.Text)?.readText() ?: ""
                    val messageDto = Json.decodeFromString<MessageDto>(messageJson)
                    messageDto.toMessage()
                }
            return flowOfMessage!!
        } catch (e: Exception) {
            e.printStackTrace()
            return emptyFlow()
        }
    }

    suspend fun disconnect() {
        socket?.close()
    }


    sealed class EndPoints(val url: String) {
        object GetAllMessages : EndPoints(GET_ALL_MESSAGES)
        object Chat : EndPoints(CHAT)
    }
}