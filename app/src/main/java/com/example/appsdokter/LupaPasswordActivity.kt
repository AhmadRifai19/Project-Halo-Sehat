package com.example.appsdokter

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.FirebaseDatabase
import java.util.concurrent.TimeUnit

class LupaPasswordActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val database = FirebaseDatabase.getInstance().getReference("users")
    private var storedVerificationId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lupa_password)

        auth = FirebaseAuth.getInstance()

        val etEmail = findViewById<EditText>(R.id.etEmailPemulihan)
        val btnKirimOTP = findViewById<Button>(R.id.btnKirimOTP)
        val btnKembali = findViewById<ImageView>(R.id.btnKembali)
        val tvKembaliLogin = findViewById<TextView>(R.id.tvKembaliLogin)

        btnKirimOTP.setOnClickListener {
            val email = etEmail.text.toString().trim()
            if (email.isNotEmpty()) {
                cariNomorHpDanKirimOTP(email)
            } else {
                etEmail.error = "Email wajib diisi"
            }
        }

        btnKembali?.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }

        tvKembaliLogin?.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }
    }

    private fun cariNomorHpDanKirimOTP(emailInput: String) {
        database.orderByChild("email").equalTo(emailInput)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    var noHp: String? = null
                    for (userSnapshot in snapshot.children) {
                        noHp = userSnapshot.child("no_hp").getValue(String::class.java)
                        break
                    }

                    if (!noHp.isNullOrEmpty()) {
                        kirimSMSOTP(noHp)
                    } else {
                        Toast.makeText(this, "Nomor HP tidak ditemukan pada akun ini", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Email tidak terdaftar", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun kirimSMSOTP(phoneNumber: String) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {}

                override fun onVerificationFailed(e: FirebaseException) {
                    Toast.makeText(this@LupaPasswordActivity, "Gagal kirim OTP: ${e.message}", Toast.LENGTH_LONG).show()
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    storedVerificationId = verificationId
                    Toast.makeText(this@LupaPasswordActivity, "OTP dikirim ke $phoneNumber", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this@LupaPasswordActivity, VerifikasiOtpActivity::class.java)
                    intent.putExtra("verificationId", storedVerificationId)
                    startActivity(intent)
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                }
            })
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }
}