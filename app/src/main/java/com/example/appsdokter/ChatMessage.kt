package com.example.appsdokter

data class ChatMessage(
    val text: String = "",
    val isUser: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)