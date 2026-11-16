package com.example.appsdokter

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class NotifikasiActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifikasi)

        val tombol = findViewById<Button>(R.id.btn_tandai_dibaca)

        tombol.setOnClickListener {
            val intent = Intent(
                this,
                NotifikasiSemuaActivity::class.java
            )
            startActivity(intent)
        }

        // --- NAVIGASI BAWAH ---
        findViewById<TextView>(R.id.navBeranda)?.setOnClickListener {
            val intent = Intent(this, BerandaActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        findViewById<TextView>(R.id.navAiKonsul)?.setOnClickListener {
            startActivity(Intent(this, AiKonsulActivity::class.java))
            finish()
        }

        findViewById<TextView>(R.id.navProfile)?.setOnClickListener {
            Toast.makeText(this, "Halaman Profile belum tersedia", Toast.LENGTH_SHORT).show()
        }
    }
}