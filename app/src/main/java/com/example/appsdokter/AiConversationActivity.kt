package com.example.appsdokter

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appsdokter.BuildConfig
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AiConversationActivity : AppCompatActivity() {

    private val attachmentList = mutableListOf<Uri>()
    private lateinit var adapter: AttachmentAdapter

    // Teks saran cepat untuk chip quick chat
    private val quickChats = listOf(
        "Sakit kepala",
        "Demam",
        "Pilek & flu",
        "Gangguan tidur",
        "Jadwal minum obat",
        "Gizi harian"
    )

    // Daftar API Key Gemini (Diambil dari local.properties via BuildConfig)
    private val aiService = AiChatService(this, BuildConfig.GEMINI_API_KEY.split(","))
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

    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ai_conversation)

        val etPesan = findViewById<EditText>(R.id.etPesan)
        val btnKirimPesan = findViewById<ImageView>(R.id.btnKirimPesan)
        val btnLampiran = findViewById<ImageView>(R.id.btnLampiran)
        val rvAttachments = findViewById<RecyclerView>(R.id.rv_attachments)
        val chatLayout = findViewById<LinearLayout>(R.id.chatLayout)

        // === QUICK CHAT ===
        val row1 = findViewById<LinearLayout>(R.id.row1)
        val row2 = findViewById<LinearLayout>(R.id.row2)

        row1?.let { r1 ->
            quickChats.take(3).forEach { q ->
                val chip = LayoutInflater.from(this).inflate(R.layout.chip_quick_chat, r1, false)
                chip.findViewById<TextView>(R.id.tv_chip_text).text = q
                chip.setOnClickListener { quickChatToAi(q) }
                r1.addView(chip)
            }
        }

        row2?.let { r2 ->
            quickChats.drop(3).forEach { q ->
                val chip = LayoutInflater.from(this).inflate(R.layout.chip_quick_chat, r2, false)
                chip.findViewById<TextView>(R.id.tv_chip_text).text = q
                chip.setOnClickListener { quickChatToAi(q) }
                r2.addView(chip)
            }
        }

        chatLayout.removeAllViews()

        profileJob = lifecycleScope.launch {
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            android.util.Log.d("AiConversation", "Mencoba memuat data untuk UID: $uid")

            if (uid != null) {
                try {
                    // Cek path 1: users/UID
                    val snapshot = firestore.collection("users").document(uid).get().await()

                    if (snapshot.exists()) {
                        val nama = snapshot.getString("nama") ?: "User"
                        val alergi = snapshot.getString("riwayat_alergi") ?: "Tidak ada"
                        val kondisi = snapshot.getString("kondisi_medis") ?: "Sehat"
                        userProfileData = "Nama: $nama\nRiwayat Alergi: $alergi\nKondisi Medis: $kondisi"
                        android.util.Log.d("AiConversation", "Profil Ditemukan! Data: $userProfileData")
                    } else {
                        android.util.Log.e("AiConversation", "GAGAL: Data tidak ditemukan di path users/UID!")
                    }

                    // Load Summary (ambil item terbaru dari sub-collection items)
                    val summarySnap = firestore.collection("summaries").document(uid)
                        .collection("items")
                        .orderBy("timestamp", Query.Direction.DESCENDING)
                        .limit(1)
                        .get()
                        .await()

                    if (!summarySnap.isEmpty) {
                        val doc = summarySnap.documents.first()
                        val summaryText = doc.getString("isi_summary")
                        if (!summaryText.isNullOrEmpty()) {
                            activeSummary = summaryText
                            android.util.Log.d("AiConversation", "Summary Loaded: $activeSummary")
                        }
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
        loadChatFromFirestore()
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

        val dot1 = view.findViewById<View>(R.id.dot1)
        val dot2 = view.findViewById<View>(R.id.dot2)
        val dot3 = view.findViewById<View>(R.id.dot3)

        val dots = listOf(dot1, dot2, dot3)
        dots.forEachIndexed { index, dot ->
            dot.alpha = 0.3f
            val anim = AnimationUtils.loadAnimation(this, R.anim.dot_pulse)
            anim.startOffset = index * 400L
            dot.startAnimation(anim)
        }

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
        saveMessageToFirestore(userMsg)
        showLoadingBubble()

        // Sembunyikan chip quick chat setelah user mulai chat
        hideQuickChats()

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
                saveMessageToFirestore(aiMsg)
                addChatMessage(response, false)

                if (newNote != null) {
                    profileNotes.add(newNote)
                    Toast.makeText(this@AiConversationActivity, "Catatan tersimpan: $newNote", Toast.LENGTH_SHORT).show()
                }

                if (newSummary != null) {
                    activeSummary = newSummary // Update sesi lokal
                    saveSummaryToFirestore(newSummary)
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

    private fun quickChatToAi(quickText: String) {
        val etPesan = findViewById<EditText>(R.id.etPesan)
        addChatMessage(quickText, true)
        sendToAi(quickText)
        etPesan.text.clear()
    }

    private fun hideQuickChats() {
        findViewById<LinearLayout>(R.id.row1)?.visibility = View.GONE
        findViewById<LinearLayout>(R.id.row2)?.visibility = View.GONE
        findViewById<HorizontalScrollView>(R.id.hsv_quick_chats)?.visibility = View.GONE
    }

    private fun saveSummaryToFirestore(summary: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val summaryRef = firestore.collection("summaries").document(uid).collection("items")

        // Hapus semua item lama lalu tambah yang baru (replace-on-generate)
        lifecycleScope.launch {
            try {
                val existing = summaryRef.get().await()
                val batch = firestore.batch()
                for (doc in existing.documents) {
                    batch.delete(doc.reference)
                }
                val newDoc = summaryRef.document()
                batch.set(newDoc, mapOf(
                    "isi_summary" to summary,
                    "timestamp" to System.currentTimeMillis()
                ))
                batch.commit().await()
            } catch (e: Exception) {
                android.util.Log.e("AiConversation", "Gagal simpan summary: ${e.message}")
            }
        }
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

    private fun loadChatFromFirestore() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        // Ambil 20 pesan terakhir, urutkan descending lalu reverse
        val chatRef = firestore.collection("chats").document(uid).collection("items")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(20)

        chatRef.get().addOnSuccessListener { snapshot ->
            android.util.Log.d("AiConversation", "Memuat history ke memori AI, jumlah: ${snapshot.size()}")
            chatHistory.clear()

            val messages = mutableListOf<ChatMessage>()
            for (doc in snapshot.documents) {
                val message = doc.toObject(ChatMessage::class.java)
                if (message != null) {
                    messages.add(message)
                }
            }
            // Reverse agar urutan kronologis (asc)
            messages.reverse()
            chatHistory.addAll(messages)
        }
    }

    private fun saveMessageToFirestore(message: ChatMessage) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val chatRef = firestore.collection("chats").document(uid).collection("items")
        chatRef.add(message)
    }
}
