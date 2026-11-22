package com.example.appsdokter

import android.content.Context
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.regex.Pattern

class AiChatService(private val context: Context, private val geminiKeys: List<String>) {
    
    private var currentKeyIndex = 0

    private fun getSystemInstructions(): String {
        return try {
            context.assets.open("system_rules.md").bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            "Kamu adalah asisten kesehatan di AppsDokter."
        }
    }

    suspend fun getAiResponse(
        userMessage: String, 
        history: List<ChatMessage>, 
        profileNotes: List<String>,
        userProfile: String,
        userSummary: String,
        attachments: List<android.graphics.Bitmap> = emptyList()
    ): Triple<String, String?, String?> = withContext(Dispatchers.IO) {
        
        var lastError: Exception? = null

        for (i in geminiKeys.indices) {
            val key = geminiKeys[currentKeyIndex]
            currentKeyIndex = (currentKeyIndex + 1) % geminiKeys.size 

            try {
                return@withContext callGeminiApi(key, userMessage, history, profileNotes, userProfile, userSummary, attachments)
            } catch (e: Exception) {
                lastError = e
                continue
            }
        }
        
        throw lastError ?: Exception("Semua API Key sedang mengalami gangguan.")
    }

    private suspend fun callGeminiApi(
        apiKey: String,
        userMessage: String,
        history: List<ChatMessage>,
        profileNotes: List<String>,
        userProfile: String,
        userSummary: String,
        attachments: List<android.graphics.Bitmap>
    ): Triple<String, String?, String?> {
        val generativeModel = GenerativeModel(modelName = "gemini-3.6-flash", apiKey = apiKey)
        
        val notesStr = if (profileNotes.isEmpty()) "Belum ada catatan." else profileNotes.joinToString("\n- ")
        
        val systemPrompt = """
            ${getSystemInstructions()}
            
            === PROFIL MEDIS PENGGUNA ===
            $userProfile
            
            === RINGKASAN PERCAKAPAN SEBELUMNYA ===
            $userSummary
            
            === CATATAN PERCAKAPAN ===
            - $notesStr
        """.trimIndent()

        // Efisiensi: Hanya ambil 10 pesan terakhir untuk konteks chat
        val limitedHistory = if (history.size > 10) history.takeLast(10) else history
        
        val chat = generativeModel.startChat(history = limitedHistory.map { 
            content(if (it.isUser) "user" else "model") { text(it.text) } 
        })
        
        val response = chat.sendMessage(content {
            text(userMessage + "\n\n(System Context: $systemPrompt)")
            for (bitmap in attachments) {
                image(bitmap)
            }
        })
        val rawReply = response.text ?: ""

        var newNote: String? = null
        var newSummary: String? = null
        
        val notePattern = Pattern.compile("\\[CATATAN:\\s*(.*?)\\]", Pattern.CASE_INSENSITIVE or Pattern.DOTALL)
        val summaryPattern = Pattern.compile("\\[RINGKASAN:\\s*(.*?)\\]", Pattern.CASE_INSENSITIVE or Pattern.DOTALL)
        
        var cleanReply = rawReply
        
        val noteMatcher = notePattern.matcher(cleanReply)
        if (noteMatcher.find()) {
            newNote = noteMatcher.group(1)?.trim()
            cleanReply = cleanReply.replace(notePattern.toRegex(), "").trim()
        }
        
        val summaryMatcher = summaryPattern.matcher(cleanReply)
        if (summaryMatcher.find()) {
            newSummary = summaryMatcher.group(1)?.trim()
            cleanReply = cleanReply.replace(summaryPattern.toRegex(), "").trim()
        }

        return Triple(formatAiResponse(cleanReply.trim()), newNote, newSummary)
    }

    /**
     * Formats the AI response to look clean in the chat bubble:
     * - Removes Markdown formatting characters (**, #, *, -, etc.)
     * - Collapses multiple blank lines into a single one
     * - Collapses multiple spaces on the same line
     * - Ensures paragraphs are separated by single newlines
     */
    private fun formatAiResponse(raw: String): String {
        var text = raw
            // Hapus karakter Markdown umum
            .replace("**", " ")
            .replace("*", " ")
            .replace("#", " ")
            .replace("- ", " ")
            .trim()
        
        return text
            .split("\n")
            .map { line ->
                line.replace(Regex("\\s+"), " ").trim()
            }
            .filter { it.isNotEmpty() }
            .joinToString("\n")
            .let { it.trim() }
            .ifEmpty { raw.trim() }
    }
}