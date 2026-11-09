package com.example.appsdokter

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LupaPasswordActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lupa_password)

        auth = FirebaseAuth.getInstance()

        val etEmail = findViewById<EditText>(R.id.etEmailPemulihan)
        val btnKirimLink = findViewById<Button>(R.id.btnKirimOTP)
        val btnKembali = findViewById<ImageView>(R.id.btnKembali)
        val tvKembaliLogin = findViewById<TextView>(R.id.tvKembaliLogin)

        btnKirimLink.text = getString(R.string.btn_kirim_link_reset)

        btnKirimLink.setOnClickListener {
            val email = etEmail.text.toString().trim()
            resetPasswordTanpaOTP(email)
        }

        btnKembali?.setOnClickListener {
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        tvKembaliLogin?.setOnClickListener {
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }
        })
    }

    private fun resetPasswordTanpaOTP(emailInput: String) {
        if (emailInput.isEmpty()) {
            Toast.makeText(this, "Masukkan email Anda", Toast.LENGTH_SHORT).show()
            return
        }

        auth.sendPasswordResetEmail(emailInput)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Link reset password telah dikirim ke email Anda.", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    val errorMessage = task.exception?.message ?: "Gagal mengirim email reset password"
                    Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                }
            }
    }
}