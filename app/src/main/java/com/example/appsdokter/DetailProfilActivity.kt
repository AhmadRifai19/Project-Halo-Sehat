package com.example.appsdokter

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class DetailProfilActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_profil)

        val judul = intent.getStringExtra("JUDUL")
            ?: "Detail Profil"

        val deskripsi = intent.getStringExtra("DESKRIPSI")
            ?: "Informasi akun Anda"

        findViewById<TextView>(R.id.tvDetailJudul).text = judul
        findViewById<TextView>(R.id.tvDetailDeskripsi).text = deskripsi

        // Navigasi ke halaman Edit Profil
        val btnEditProfil = findViewById<Button>(R.id.btnEditProfil)

        btnEditProfil.setOnClickListener {
            val pindahHalaman = Intent(
                this,
                EditProfilActivity::class.java
            )
            startActivity(pindahHalaman)
        }
    }
}
