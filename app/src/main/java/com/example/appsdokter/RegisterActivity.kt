package com.example.appsdokter

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {

    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_account)

        // 1. Inisialisasi Komponen UI
        val etNama = findViewById<EditText>(R.id.et_nama)
        val etEmail = findViewById<EditText>(R.id.et_email)
        val etPassword = findViewById<EditText>(R.id.et_password)
        val btnEye = findViewById<ImageView>(R.id.btn_eye)
        val btnLanjutkan = findViewById<Button>(R.id.btn_lanjutkan)
        val btnBackLogin = findViewById<ImageView>(R.id.btn_back_login)
        val tvMasuk = findViewById<TextView>(R.id.tv_masuk)

        // 2. Fungsi Show/Hide Password
        btnEye?.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                etPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                btnEye.alpha = 0.5f
            } else {
                etPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                btnEye.alpha = 1.0f
            }
            etPassword.setSelection(etPassword.text.length)
        }

        // 3. Tombol Lanjutkan (Hanya Validasi dan Navigasi)
        btnLanjutkan?.setOnClickListener {
            val nama = etNama.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            // Validasi Input
            if (nama.isEmpty()) {
                etNama.error = "Nama lengkap wajib diisi"
                etNama.requestFocus()
                return@setOnClickListener
            }

            if (email.isEmpty()) {
                etEmail.error = "Email wajib diisi"
                etEmail.requestFocus()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.error = "Format email tidak valid"
                etEmail.requestFocus()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                etPassword.error = "Password wajib diisi"
                etPassword.requestFocus()
                return@setOnClickListener
            }

            if (password.length < 8) {
                etPassword.error = "Password minimal harus 8 karakter"
                etPassword.requestFocus()
                return@setOnClickListener
            }

            // LANGSUNG PINDAH KE DATA PRIBADI (Data akan diproses di sana)
            val intent = Intent(this, RegisterDataPribadiActivity::class.java).apply {
                putExtra("email", email)
                putExtra("password", password)
                putExtra("nama", nama)
                putExtra("no_hp", "") // Default kosong dulu
            }
            startActivity(intent)
            @Suppress("DEPRECATION")
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        // 4. Navigasi Kembali ke Login
        btnBackLogin?.setOnClickListener {
            navigateBackToLogin()
        }

        tvMasuk?.setOnClickListener {
            navigateBackToLogin()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navigateBackToLogin()
            }
        })
    }

    private fun navigateBackToLogin() {
        finish()
        @Suppress("DEPRECATION")
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}