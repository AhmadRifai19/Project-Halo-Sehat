package com.example.appsdokter

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class AiKonsulActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ai_konsul)

        val pesan = findViewById<EditText>(R.id.et_ai_message)
        val kirim = findViewById<ImageButton>(R.id.btn_send_message)

        kirim.setOnClickListener {
            val isiPesan = pesan.text.toString().trim()

            if (isiPesan.isNotEmpty()) {
                bukaChat(isiPesan)
                pesan.text.clear()
            } else {
                Toast.makeText(
                    this,
                    "Masukkan pesan terlebih dahulu",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // --- QUICK CHAT: klik topik langsung buka percakapan dengan AI ---
        findViewById<LinearLayout>(R.id.topic_gejala).setOnClickListener {
            bukaChat("Saya ingin mengenali gejala yang saya alami. Bantu saya memahami kemungkinan penyebabnya dan saran awalnya.")
        }

        findViewById<LinearLayout>(R.id.topic_obat).setOnClickListener {
            bukaChat("Saya ingin informasi seputar obat. Bantu jelaskan cara pakai, dosis, dan efek samping yang perlu diperhatikan.")
        }

        findViewById<LinearLayout>(R.id.topic_tips).setOnClickListener {
            bukaChat("Berikan tips menjaga kesehatan sehari-hari yang praktis dan mudah dilakukan.")
        }

        findViewById<LinearLayout>(R.id.topic_dokter).setOnClickListener {
            bukaChat("Saya ingin persiapan konsultasi dokter. Bantu saya membuat daftar hal yang perlu disiapkan dan ditanyakan.")
        }

        // --- TAMBAHAN NAVIGASI BAWAH ---
        findViewById<TextView>(R.id.navBeranda).setOnClickListener {
            val intent = Intent(this, BerandaActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        findViewById<TextView>(R.id.navNotifikasi).setOnClickListener {
            startActivity(Intent(this, NotifikasiActivity::class.java))
            finish()
        }

        findViewById<TextView>(R.id.navProfile).setOnClickListener {
            Toast.makeText(this, "Halaman Profile belum tersedia", Toast.LENGTH_SHORT).show()
        }
    }

    private fun bukaChat(pesanAwal: String) {
        val intent = Intent(this, AiConversationActivity::class.java)
        intent.putExtra("INITIAL_MESSAGE", pesanAwal)
        startActivity(intent)
        applyTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}