package com.example.appsdokter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.PhoneAuthProvider

class VerifikasiOtpActivity : AppCompatActivity() {

    private var verificationId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verifikasi_otp)

        onBackPressedDispatcher.addCallback(this) {
            finish()
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }

        // Menerima data dari LupaPasswordActivity
        verificationId = intent.getStringExtra("verificationId")

        val btnKembali = findViewById<ImageView>(R.id.btnKembali)
        val tvKembaliLupaPassword = findViewById<TextView>(R.id.tvKembaliLupaPassword)
        val btnVerifikasi = findViewById<Button>(R.id.btnVerifikasi)

        btnKembali?.setOnClickListener {
            finishWithAnimation()
        }

        tvKembaliLupaPassword?.setOnClickListener {
            finishWithAnimation()
        }

        btnVerifikasi?.setOnClickListener {
            verifikasiOTP()
        }

        setupOtpInputs()
    }

    private fun verifikasiOTP() {
        val otp1 = findViewById<EditText>(R.id.otp1).text.toString()
        val otp2 = findViewById<EditText>(R.id.otp2).text.toString()
        val otp3 = findViewById<EditText>(R.id.otp3).text.toString()
        val otp4 = findViewById<EditText>(R.id.otp4).text.toString()
        val otp5 = findViewById<EditText>(R.id.otp5).text.toString()
        val otp6 = findViewById<EditText>(R.id.otp6).text.toString()

        val completeOtp = otp1 + otp2 + otp3 + otp4 + otp5 + otp6

        if (completeOtp.length < 6) {
            Toast.makeText(this, "Masukkan 6 digit kode OTP", Toast.LENGTH_SHORT).show()
            return
        }

        if (verificationId != null) {
            val credential = PhoneAuthProvider.getCredential(verificationId!!, completeOtp)
            
            // Login/Verifikasi user
            val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
            auth.signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // OTP Valid
                        val intent = Intent(this, NewPasswordActivity::class.java)
                        startActivity(intent)
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                        finish()
                    } else {
                        Toast.makeText(this, "OTP Salah atau Kadaluwarsa", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun setupOtpInputs() {
        val otpBoxes = listOf(
            findViewById<EditText>(R.id.otp1), findViewById<EditText>(R.id.otp2),
            findViewById<EditText>(R.id.otp3), findViewById<EditText>(R.id.otp4),
            findViewById<EditText>(R.id.otp5), findViewById<EditText>(R.id.otp6)
        )

        for (i in otpBoxes.indices) {
            val currentBox = otpBoxes[i]
            val nextBox = if (i < otpBoxes.size - 1) otpBoxes[i + 1] else null
            val prevBox = if (i > 0) otpBoxes[i - 1] else null

            currentBox.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    val text = s?.toString() ?: ""
                    if (text.length == 1) {
                        nextBox?.requestFocus()
                    }
                }
            })

            currentBox.setOnKeyListener { _, keyCode, event ->
                if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL) {
                    if (currentBox.text.isNullOrEmpty()) {
                        prevBox?.requestFocus()
                    }
                }
                false
            }
        }
    }

    private fun finishWithAnimation() {
        finish()
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }
}