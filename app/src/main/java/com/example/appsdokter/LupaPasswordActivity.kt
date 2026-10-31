package com.example.appsdokter

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class LupaPasswordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lupa_password)

        val btnKembaliLogin = findViewById<LinearLayout>(R.id.btnKembaliLogin)
        val tvKembaliLogin = findViewById<TextView>(R.id.tvKembaliLogin)
        val btnKirimOTP = findViewById<Button>(R.id.btnKirimOTP)

        btnKembaliLogin.setOnClickListener {
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        tvKembaliLogin.setOnClickListener {
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        btnKirimOTP.setOnClickListener {
            startActivity(Intent(this, VerifikasiOtpActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}
