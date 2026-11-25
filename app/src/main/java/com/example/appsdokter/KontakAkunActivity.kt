
package com.example.appsdokter

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class KontakAkunActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kontak_akun)

        // Inisialisasi komponen
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etNomorHP = findViewById<EditText>(R.id.etNomorHP)

        val btnBack = findViewById<ImageView>(R.id.btnBackKontak)
        val btnUbahEmail = findViewById<TextView>(R.id.btnUbahEmail)
        val btnUbahNomor = findViewById<TextView>(R.id.btnUbahNomor)

        val btnVerifikasiNomor =
            findViewById<Button>(R.id.btnVerifikasiNomor)

        val btnSimpanKontak =
            findViewById<Button>(R.id.btnSimpanKontak)

        // SharedPreferences
        val sharedPreferences = getSharedPreferences(
            "KontakUser",
            MODE_PRIVATE
        )

        // Tampilkan data yang sudah tersimpan
        etEmail.setText(
            sharedPreferences.getString("email_user", "")
        )

        etNomorHP.setText(
            sharedPreferences.getString("nomor_hp", "")
        )

        // Tombol kembali
        btnBack.setOnClickListener {
            finish()
        }

        // Tombol ubah email
        btnUbahEmail.setOnClickListener {
            etEmail.requestFocus()
            etEmail.setSelection(etEmail.text.length)
        }

        // Tombol ubah nomor HP
        btnUbahNomor.setOnClickListener {
            etNomorHP.requestFocus()
            etNomorHP.setSelection(etNomorHP.text.length)
        }

        // Tombol verifikasi nomor
        btnVerifikasiNomor.setOnClickListener {
            Toast.makeText(
                this,
                "Fitur verifikasi OTP akan dibuat selanjutnya",
                Toast.LENGTH_SHORT
            ).show()
        }

        // Tombol simpan kontak
        btnSimpanKontak.setOnClickListener {

            val email = etEmail.text.toString().trim()
            val nomorHP = etNomorHP.text.toString().trim()

            if (email.isEmpty()) {
                etEmail.error = "Email tidak boleh kosong"
                etEmail.requestFocus()
                return@setOnClickListener
            }

            if (!android.util.Patterns.EMAIL_ADDRESS
                    .matcher(email).matches()) {
                etEmail.error = "Format email tidak valid"
                etEmail.requestFocus()
                return@setOnClickListener
            }

            if (nomorHP.isEmpty()) {
                etNomorHP.error = "Nomor HP tidak boleh kosong"
                etNomorHP.requestFocus()
                return@setOnClickListener
            }

            // Simpan data kontak
            sharedPreferences.edit()
                .putString("email_user", email)
                .putString("nomor_hp", nomorHP)
                .apply()

            Toast.makeText(
                this,
                "Kontak akun berhasil disimpan",
                Toast.LENGTH_SHORT
            ).show()

            finish()
        }
    }
}