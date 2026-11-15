package com.example.appsdokter

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog

class AiKonsulActivity : AppCompatActivity() {

    private val attachmentList = mutableListOf<Uri>()
    private lateinit var adapter: AttachmentAdapter

    private val pickFileLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            attachmentList.add(it)
            adapter.notifyDataSetChanged()
            findViewById<RecyclerView>(R.id.rv_attachments).visibility = View.VISIBLE
        }
    }

    private fun showAttachmentOptions() {
        val dialog = BottomSheetDialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_attachment, null)
        
        val btnGallery = view.findViewById<Button>(R.id.btn_option_gallery)
        val btnFile = view.findViewById<Button>(R.id.btn_option_file)

        btnGallery.setOnClickListener {
            pickFileLauncher.launch("image/*")
            dialog.dismiss()
        }

        btnFile.setOnClickListener {
            pickFileLauncher.launch("application/pdf")
            dialog.dismiss()
        }

        dialog.setContentView(view)
        dialog.show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ai_konsul)

        val rvAttachments = findViewById<RecyclerView>(R.id.rv_attachments)
        adapter = AttachmentAdapter(attachmentList) { uri ->
            attachmentList.remove(uri)
            adapter.notifyDataSetChanged()
            if (attachmentList.isEmpty()) rvAttachments.visibility = View.GONE
        }
        rvAttachments.adapter = adapter
        rvAttachments.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        val pesan = findViewById<EditText>(R.id.et_ai_message)
        val kirim = findViewById<ImageButton>(R.id.btn_send_message)
        val btnAddFile = findViewById<ImageButton>(R.id.btn_add_file)
        
        // Topics Navigation
        val topicGejala = findViewById<View>(R.id.topic_gejala)
        val topicObat = findViewById<View>(R.id.topic_obat)
        val topicTips = findViewById<View>(R.id.topic_tips)
        val topicDokter = findViewById<View>(R.id.topic_dokter)

        val navigateToChat = View.OnClickListener {
            val intent = Intent(this, AiConversationActivity::class.java)
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        topicGejala.setOnClickListener(navigateToChat)
        topicObat.setOnClickListener(navigateToChat)
        topicTips.setOnClickListener(navigateToChat)
        topicDokter.setOnClickListener(navigateToChat)

        btnAddFile.setOnClickListener {
            showAttachmentOptions()
        }

        kirim.setOnClickListener {
            val isiPesan = pesan.text.toString().trim()

            if (isiPesan.isNotEmpty() || attachmentList.isNotEmpty()) {
                val intent = Intent(this, AiConversationActivity::class.java).apply {
                    putExtra("INITIAL_MESSAGE", isiPesan)
                }
                startActivity(intent)
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                
                pesan.text.clear()
                attachmentList.clear()
                adapter.notifyDataSetChanged()
                rvAttachments.visibility = View.GONE
            } else {
                Toast.makeText(
                    this,
                    "Masukkan pesan atau lampirkan file terlebih dahulu",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
