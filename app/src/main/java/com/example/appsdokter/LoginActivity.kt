package com.example.appsdokter

import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Memuat layout XML agar tampilan muncul
        setContentView(R.layout.activity_login)

        val etPassword = findViewById<EditText>(R.id.et_password)
        val btnEye = findViewById<ImageView>(R.id.btn_eye)

        // Fungsi Show/Hide Password
        btnEye.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                // Tampilkan Password
                etPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                // Memberikan efek visual sederhana (transparansi) saat aktif
                btnEye.alpha = 0.5f 
            } else {
                // Sembunyikan Password
                etPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                btnEye.alpha = 1.0f
            }
            // Memastikan kursor tetap di posisi akhir teks
            etPassword.setSelection(etPassword.text.length)
        }

        val tvForgotPassword = findViewById<TextView>(R.id.tv_forgot_password)

        // Navigasi ke Lupa Password
        tvForgotPassword.setOnClickListener {
            startActivity(android.content.Intent(this, LupaPasswordActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        // Memeriksa inisialisasi Firebase
        val firebaseApp = FirebaseApp.initializeApp(this)

        if (firebaseApp != null) {
            Log.d("FIREBASE_TEST", "Firebase berhasil diinisialisasi!")

            // Menghubungkan ke Firestore
            val db = FirebaseFirestore.getInstance()

            // Mengirim data percobaan
            val data = hashMapOf(
                "nama" to "Halo Sehat",
                "status" to "Berhasil terhubung"
            )

            db.collection("connection_test")
                .document("test_android")
                .set(data)
                .addOnSuccessListener {
                    Log.d("FIREBASE_TEST", "Data berhasil dikirim ke Firestore!")
                }
                .addOnFailureListener { error ->
                    Log.e("FIREBASE_TEST", "Gagal mengirim data: ${error.message}")
                }

        } else {
            Log.e("FIREBASE_TEST", "Firebase gagal diinisialisasi!")
        }
    }
}
