
package com.example.appsdokter

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class EditProfilActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profil)

        // Inisialisasi komponen
        val btnBack = findViewById<ImageView>(R.id.btnBack)

        val btnSimpanPerubahan =
            findViewById<Button>(R.id.btnSimpanPerubahan)

        val etNamaTampilan =
            findViewById<EditText>(R.id.etNamaTampilan)

        // Mengambil nama yang sudah pernah disimpan
        val sharedPreferences = getSharedPreferences(
            "ProfilUser",
            MODE_PRIVATE
        )

        val namaTersimpan =
            sharedPreferences.getString("nama_tampilan", "")

        etNamaTampilan.setText(namaTersimpan)

        // Menu Identitas dan Alamat
        findViewById<LinearLayout>(R.id.itemIdentitasAlamat)
            .setOnClickListener {
                val intent = Intent(
                    this,
                    IdentitasAlamatActivity::class.java
                )
                startActivity(intent)
            }

        // Menu Kontak Akun
        findViewById<LinearLayout>(R.id.itemKontakAkun)
            .setOnClickListener {
                val intent = Intent(
                    this,
                    KontakAkunActivity::class.java
                )
                startActivity(intent)
            }

        // Menu Informasi Pribadi
        findViewById<LinearLayout>(R.id.itemInformasiPribadi)
            .setOnClickListener {
                val intent = Intent(
                    this,
                    InformasiPribadiActivity::class.java
                )
                startActivity(intent)
            }

        // Tombol Kembali
        btnBack.setOnClickListener {
            finish()
        }

        // Tombol Simpan Perubahan
        btnSimpanPerubahan.setOnClickListener {

            val namaBaru = etNamaTampilan.text.toString().trim()

            if (namaBaru.isNotEmpty()) {

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
