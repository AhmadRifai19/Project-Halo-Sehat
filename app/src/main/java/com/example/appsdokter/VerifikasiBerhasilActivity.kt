package com.example.appsdokter

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity

class VerifikasiBerhasilActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verifikasi_berhasil)

        val btnKembaliLogin = findViewById<Button>(R.id.btnKembaliLogin)

        btnKembaliLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            applyTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            finish()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
                applyExitTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            }
        })
    }
}
