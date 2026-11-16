package com.example.appsdokter

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class BerandaActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_beranda)

        findViewById<TextView>(R.id.navAiKonsul).setOnClickListener {
            startActivity(Intent(this, AiKonsulActivity::class.java))
        }

        findViewById<TextView>(R.id.navNotifikasi).setOnClickListener {
            startActivity(Intent(this, NotifikasiActivity::class.java))
        }

        findViewById<TextView>(R.id.navProfile).setOnClickListener {
            Toast.makeText(this, "Halaman Profile belum tersedia", Toast.LENGTH_SHORT).show()
        }
    }
}