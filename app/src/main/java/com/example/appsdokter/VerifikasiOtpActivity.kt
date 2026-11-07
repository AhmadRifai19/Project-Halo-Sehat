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
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.addCallback

class VerifikasiOtpActivity : AppCompatActivity() {

    private var receivedUid: String? = null
    private var receivedNama: String? = null
    private var receivedEmail: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verifikasi_otp)

        onBackPressedDispatcher.addCallback(this) {
            finish()
            @Suppress("DEPRECATION")
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right) // sesuaikan dengan exit anim Anda
        }

        // Menerima data dari Intent RegisterActivity
        receivedUid = intent.getStringExtra("EXTRA_UID")
        receivedNama = intent.getStringExtra("EXTRA_NAMA")
        receivedEmail = intent.getStringExtra("EXTRA_EMAIL")

        val btnKembali = findViewById<LinearLayout>(R.id.btnKembali)
        val tvKembaliLupaPassword = findViewById<TextView>(R.id.tvKembaliLupaPassword)
        val btnVerifikasi = findViewById<Button>(R.id.btnVerifikasi)

        btnKembali?.setOnClickListener {
            finishWithAnimation()
        }

        tvKembaliLupaPassword?.setOnClickListener {
            finishWithAnimation()
        }

        btnVerifikasi?.setOnClickListener {
            // Validasi kode OTP (simulasi sederhana: pastikan terisi 6 digit)
            val otp1 = findViewById<EditText>(R.id.otp1).text.toString()
            val otp2 = findViewById<EditText>(R.id.otp2).text.toString()
            val otp3 = findViewById<EditText>(R.id.otp3).text.toString()
            val otp4 = findViewById<EditText>(R.id.otp4).text.toString()
            val otp5 = findViewById<EditText>(R.id.otp5).text.toString()
            val otp6 = findViewById<EditText>(R.id.otp6).text.toString()

            val completeOtp = otp1 + otp2 + otp3 + otp4 + otp5 + otp6

            if (completeOtp.length < 6) {
                Toast.makeText(this, "Masukkan 6 digit kode OTP secara lengkap", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Jika OTP valid (simulasi sukses), arahkan ke halaman sukses / lengkapi data berikutnya
            val intent = Intent(this, VerifikasiBerhasilActivity::class.java).apply {
                putExtra("EXTRA_UID", receivedUid)
            }
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            finish()
        }

        setupOtpInputs()
    }

    private fun setupOtpInputs() {
        val otp1 = findViewById<EditText>(R.id.otp1)
        val otp2 = findViewById<EditText>(R.id.otp2)
        val otp3 = findViewById<EditText>(R.id.otp3)
        val otp4 = findViewById<EditText>(R.id.otp4)
        val otp5 = findViewById<EditText>(R.id.otp5)
        val otp6 = findViewById<EditText>(R.id.otp6)

        val otpBoxes = listOf(otp1, otp2, otp3, otp4, otp5, otp6)

        for (i in otpBoxes.indices) {
            val currentBox = otpBoxes[i]
            // Perbaikan syntax error pada indeks array berikutnya
            val nextBox = if (i < otpBoxes.size - 1) otpBoxes[i + 1] else null
            val prevBox = if (i > 0) otpBoxes[i - 1] else null

            // Pindah otomatis ke kotak berikutnya saat angka dimasukkan
            currentBox.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    if (!s.isNullOrEmpty()) {
                        nextBox?.requestFocus() ?: run {
                            // Tutup keyboard jika sudah di kotak terakhir
                            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                            imm?.hideSoftInputFromWindow(currentBox.windowToken, 0)
                        }
                    }
                }
            })

            // Mundur ke kotak sebelumnya saat tombol hapus (Backspace) ditekan
            currentBox.setOnKeyListener { _, keyCode, event ->
                if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL) {
                    if (currentBox.text.isNullOrEmpty()) {
                        prevBox?.apply {
                            requestFocus()
                            setSelection(text.length)
                        }
                    }
                }
                false
            }
        }
    }

    private fun finishWithAnimation() {
        finish()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}