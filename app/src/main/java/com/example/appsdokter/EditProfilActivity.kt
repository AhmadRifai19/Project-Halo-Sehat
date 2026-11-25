package com.example.appsdokter

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.widget.LinearLayout

class EditProfilActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profil)

        // Menu Identitas dan Alamat
        findViewById<LinearLayout>(R.id.itemIdentitasAlamat)
            .setOnClickListener {
                val intent = Intent(
                    this,
                    IdentitasAlamatActivity::class.java
                )
                startActivity(intent)
            }

        // Inisialisasi komponen
        val btnBack = findViewById<ImageView>(R.id.btnBack)

        val btnSimpanPerubahan =
            findViewById<Button>(R.id.btnSimpanPerubahan)

        val etNamaTampilan =
            findViewById<EditText>(R.id.etNamaTampilan)

        // Tombol Kembali
        btnBack.setOnClickListener {
            finish()
        }

        // Tombol Simpan Perubahan
        btnSimpanPerubahan.setOnClickListener {

            val namaBaru = etNamaTampilan.text.toString().trim()

            if (namaBaru.isNotEmpty()) {

                // Simpan nama menggunakan SharedPreferences
                val sharedPreferences = getSharedPreferences(
                    "ProfilUser",
                    MODE_PRIVATE
                )

                sharedPreferences.edit()
                    .putString("nama_tampilan", namaBaru)
                    .apply()

                Toast.makeText(
                    this,
                    "Profil berhasil diperbarui",
                    Toast.LENGTH_SHORT
                ).show()

                finish()

            } else {

                etNamaTampilan.error = "Nama tidak boleh kosong"

                Toast.makeText(
                    this,
                    "Silakan masukkan nama tampilan",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    }
}
