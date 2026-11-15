package com.example.appsdokter

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AiConversationActivity : AppCompatActivity() {

    private val attachmentList = mutableListOf<Uri>()
    private lateinit var adapter: AttachmentAdapter
    
    // Daftar API Key Gemini (Bisa ditambah lebih dari satu)
    private val aiService = AiChatService(this, listOf(
        "",
        ""
    ))
    private val chatHistory = mutableListOf<ChatMessage>()
    private val profileNotes = mutableListOf<String>()

    private val pickFileLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            attachmentList.add(it)
            adapter.notifyDataSetChanged()
            findViewById<RecyclerView>(R.id.rv_attachments).visibility = View.VISIBLE
        }
    }

    private var userProfileData: String = "Data profil tidak tersedia"
    private var activeSummary: String = "Percakapan baru dimulai."
    private var profileJob: kotlinx.coroutines.Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ai_conversation)

        val etPesan = findViewById<EditText>(R.id.etPesan)
        val btnKirimPesan = findViewById<ImageView>(R.id.btnKirimPesan)
        val btnLampiran = findViewById<ImageView>(R.id.btnLampiran)
        val rvAttachments = findViewById<RecyclerView>(R.id.rv_attachments)
        val chatLayout = findViewById<LinearLayout>(R.id.chatLayout)

        chatLayout.removeAllViews()

        profileJob = lifecycleScope.launch {
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            android.util.Log.d("AiConversation", "Mencoba memuat data untuk UID: $uid")
            
            if (uid != null) {
                try {
                    // Load Profile
                    var snapshot = FirebaseDatabase.getInstance().getReference(uid).get().await()
                    if (snapshot.exists()) {
                        val nama = snapshot.child("nama").getValue(String::class.java) ?: "User"
                        val alergi = snapshot.child("riwayat_alergi").getValue(String::class.java) ?: "Tidak ada"
                        val kondisi = snapshot.child("kondisi_medis").getValue(String::class.java) ?: "Sehat"
                        userProfileData = "Nama: $nama\nRiwayat Alergi: $alergi\nKondisi Medis: $kondisi"
                        android.util.Log.d("AiConversation", "Profil Loaded: $userProfileData")
                    } else {
                        snapshot = FirebaseDatabase.getInstance().getReference("users").child(uid).get().await()
                        if (snapshot.exists()) {
                            val nama = snapshot.child("nama").getValue(String::class.java) ?: "User"
                            val alergi = snapshot.child("riwayat_alergi").getValue(String::class.java) ?: "Tidak ada"
                            val kondisi = snapshot.child("kondisi_medis").getValue(String::class.java) ?: "Sehat"
                            userProfileData = "Nama: $nama\nRiwayat Alergi: $alergi\nKondisi Medis: $kondisi"
                            android.util.Log.d("AiConversation", "Profil Loaded dari users/: $userProfileData")
                        }
                    }
                    
                    // Load Summary
                    val summarySnapshot = FirebaseDatabase.getInstance().getReference("summaries").child(uid).get().await()
                    if (summarySnapshot.exists()) {
                        activeSummary = summarySnapshot.getValue(String::class.java) ?: activeSummary
                        android.util.Log.d("AiConversation", "Summary Loaded: $activeSummary")
                    }
                } catch (e: Exception) {
                    android.util.Log.e("AiConversation", "Error loading profile/summary: ${e.message}")
                }
            }
        }

        adapter = AttachmentAdapter(attachmentList) { uri ->
            attachmentList.remove(uri)
            adapter.notifyDataSetChanged()
            if (attachmentList.isEmpty()) rvAttachments.visibility = View.GONE
        }
        rvAttachments.adapter = adapter
        rvAttachments.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        val initialMessage = intent.getStringExtra("INITIAL_MESSAGE")
        if (!initialMessage.isNullOrEmpty()) {
            addChatMessage(initialMessage, true)
            sendToAi(initialMessage)
        }

        btnKirimPesan.setOnClickListener {
            val pesan = etPesan.text.toString().trim()
            if (pesan.isNotEmpty()) {
                addChatMessage(pesan, true)
                sendToAi(pesan)
                etPesan.text.clear()
            }
        }

        btnLampiran.setOnClickListener { showAttachmentOptions() }
        
        // Load riwayat chat
        loadChatFromFirebase()
    }

    private fun addChatMessage(text: String, isUser: Boolean) {
        val chatLayout = findViewById<LinearLayout>(R.id.chatLayout)
        val scrollView = findViewById<ScrollView>(R.id.chatScrollView)
        
        val inflater = LayoutInflater.from(this)
        val layoutRes = if (isUser) R.layout.item_chat_user_simple else R.layout.item_chat_ai_simple
        val view = inflater.inflate(layoutRes, chatLayout, false)
        
        val tvMessage = view.findViewById<TextView>(R.id.tv_message)
        tvMessage.text = text

        chatLayout.addView(view)
        
        // Auto-scroll ke pesan terbaru
        scrollView.post {
            scrollView.fullScroll(View.FOCUS_DOWN)
        }
    }

    private fun showLoadingBubble() {
        val chatLayout = findViewById<LinearLayout>(R.id.chatLayout)
        val scrollView = findViewById<ScrollView>(R.id.chatScrollView)
        
        val view = LayoutInflater.from(this).inflate(R.layout.item_chat_ai_loading, chatLayout, false)
        view.id = R.id.loading_bubble
        
        chatLayout.addView(view)
        scrollView.post { scrollView.fullScroll(View.FOCUS_DOWN) }
    }

    private fun removeLoadingBubble() {
        val chatLayout = findViewById<LinearLayout>(R.id.chatLayout)
        val loadingView = findViewById<View>(R.id.loading_bubble)
        if (loadingView != null) {
            chatLayout.removeView(loadingView)
        }
    }

    private fun sendToAi(message: String) {
        val userMsg = ChatMessage(message, true)
        chatHistory.add(userMsg)
        saveMessageToFirebase(userMsg)
        showLoadingBubble()

        // Konversi URI lampiran ke Bitmap
        val bitmaps = mutableListOf<android.graphics.Bitmap>()
        for (uri in attachmentList) {
            try {
                val inputStream = contentResolver.openInputStream(uri)
                android.graphics.BitmapFactory.decodeStream(inputStream)?.let { bitmaps.add(it) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        lifecycleScope.launch {
            try {
                // Tunggu profil selesai dimuat
                profileJob?.join()
                android.util.Log.d("AiConversation", "Mengirim ke AI, profil: $userProfileData, summary: $activeSummary")
                val (response, newNote, newSummary) = aiService.getAiResponse(message, chatHistory, profileNotes, userProfileData, activeSummary, bitmaps)
                
                removeLoadingBubble()
                val aiMsg = ChatMessage(response, false)
                chatHistory.add(aiMsg)
                saveMessageToFirebase(aiMsg)
                addChatMessage(response, false)
                
                if (newNote != null) {
                    profileNotes.add(newNote)
                    Toast.makeText(this@AiConversationActivity, "Catatan tersimpan: $newNote", Toast.LENGTH_SHORT).show()
                }

                if (newSummary != null) {
                    activeSummary = newSummary // Update sesi lokal
                    saveSummaryToFirebase(newSummary)
                    Toast.makeText(this@AiConversationActivity, "Ringkasan percakapan diperbarui", Toast.LENGTH_SHORT).show()
                }

                // Bersihkan lampiran setelah kirim
                attachmentList.clear()
                adapter.notifyDataSetChanged()
                findViewById<RecyclerView>(R.id.rv_attachments).visibility = View.GONE

            } catch (e: Exception) {
                removeLoadingBubble()
                addChatMessage("Maaf, terjadi masalah: ${e.message}", false)
            }
        }
    }

    private fun saveSummaryToFirebase(summary: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val summaryRef = FirebaseDatabase.getInstance().getReference("summaries").child(uid)
        summaryRef.setValue(summary)
    }

    private fun showAttachmentOptions() {
        val dialog = BottomSheetDialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_attachment, null)
        view.findViewById<Button>(R.id.btn_option_gallery).setOnClickListener {
            pickFileLauncher.launch("image/*"); dialog.dismiss()
        }
        view.findViewById<Button>(R.id.btn_option_file).setOnClickListener {
            pickFileLauncher.launch("application/pdf"); dialog.dismiss()
        }
        dialog.setContentView(view)
        dialog.show()
    }

    private fun loadChatFromFirebase() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        // Optimasi: Hanya ambil 20 pesan terakhir dari database
        val chatRef = FirebaseDatabase.getInstance().getReference("chats")
            .child(uid)
            .limitToLast(20)

        chatRef.get().addOnSuccessListener { snapshot ->
            android.util.Log.d("AiConversation", "Memuat history ke memori AI, jumlah: ${snapshot.childrenCount}")
            chatHistory.clear()
            
            for (child in snapshot.children) {
                val message = child.getValue(ChatMessage::class.java)
                if (message != null) {
                    chatHistory.add(message)
                }
            }
        }
    }

    private fun saveMessageToFirebase(message: ChatMessage) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val chatRef = FirebaseDatabase.getInstance().getReference("chats").child(uid)
        chatRef.push().setValue(message)
    }
}