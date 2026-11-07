package com.example.appsdokter

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    private var isPasswordVisible = false
    private lateinit var auth: FirebaseAuth
    private lateinit var dbRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_account)

        // 1. Inisialisasi Firebase Auth & Realtime Database
        FirebaseApp.initializeApp(this)
        auth = FirebaseAuth.getInstance()
        dbRef = FirebaseDatabase.getInstance().reference

        // 2. Inisialisasi Komponen UI sesuai ID pada activity_register_account.xml
        val etNama = findViewById<EditText>(R.id.et_nama)
        val etEmail = findViewById<EditText>(R.id.et_email)
        val etPassword = findViewById<EditText>(R.id.et_password)
        val btnEye = findViewById<ImageView>(R.id.btn_eye)
        val btnLanjutkan = findViewById<Button>(R.id.btn_lanjutkan)
        val tvBackLogin = findViewById<TextView>(R.id.tv_back_login)
        val tvMasuk = findViewById<TextView>(R.id.tv_masuk)
        val progressBar = findViewById<ProgressBar>(R.id.progress_bar)

        // 3. Fungsi Show/Hide Password
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

        // 4. Tombol Lanjutkan (Registrasi ke Firebase Auth & Simpan ke RTDB)
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

            if (password.length < 6) {
                etPassword.error = "Password minimal harus 6 karakter"
                etPassword.requestFocus()
                return@setOnClickListener
            }

            // Mulai proses registrasi ke Firebase
            registerUserToFirebase(nama, email, password, btnLanjutkan, progressBar)
        }

        // 5. Navigasi Kembali ke Login
        tvBackLogin?.setOnClickListener {
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

    private fun registerUserToFirebase(
        nama: String,
        email: String,
        password: String,
        btnLanjutkan: Button,
        progressBar: ProgressBar?
    ) {
        setLoadingState(true, btnLanjutkan, progressBar)

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid
                    if (uid != null) {
                        saveInitialUserDataToRtdb(uid, nama, email, btnLanjutkan, progressBar)
                    } else {
                        setLoadingState(false, btnLanjutkan, progressBar)
                        Toast.makeText(this, "Gagal mendapatkan User ID.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    setLoadingState(false, btnLanjutkan, progressBar)
                    val exception = task.exception
                    val errorMessage = when (exception) {
                        is FirebaseAuthUserCollisionException -> "Email ini sudah terdaftar. Silakan gunakan email lain atau Masuk."
                        else -> "Registrasi gagal: ${exception?.localizedMessage ?: "Terjadi kesalahan"}"
                    }
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                    Log.e("REGISTER_AUTH", "Error: ", exception)
                }
            }
    }

    private fun saveInitialUserDataToRtdb(
        uid: String,
        nama: String,
        email: String,
        btnLanjutkan: Button,
        progressBar: ProgressBar?
    ) {
        // Struktur data awal di node users/{uid} (Langkah 1 dari 2)
        // Field data pribadi (no_hp, nik, ttl, alamat) disiapkan agar siap di-update di Langkah 2
        val userMap = mapOf(
            "uid" to uid,
            "nama" to nama,
            "email" to email,
            "no_hp" to "",
            "nik" to "",
            "ttl" to "",
            "alamat" to "",
            "registration_step" to 1,
            "is_verified" to false,
            "created_at" to System.currentTimeMillis()
        )

        dbRef.child("users").child(uid).setValue(userMap)
            .addOnSuccessListener {
                setLoadingState(false, btnLanjutkan, progressBar)
                Toast.makeText(
                    this,
                    "Data akun berhasil disimpan!",
                    Toast.LENGTH_SHORT
                ).show()

                // Lanjut ke tahap berikutnya (VerifikasiOtpActivity / Langkah 2)
                // Membawa data uid, nama, dan email lewat Intent
                val intent = Intent(this, VerifikasiOtpActivity::class.java).apply {
                    putExtra("EXTRA_UID", uid)
                    putExtra("EXTRA_NAMA", nama)
                    putExtra("EXTRA_EMAIL", email)
                }
                startActivity(intent)
                @Suppress("DEPRECATION")
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                finish()
            }
            .addOnFailureListener { error ->
                setLoadingState(false, btnLanjutkan, progressBar)
                Toast.makeText(
                    this,
                    "Gagal menyimpan data ke database: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
                Log.e("REGISTER_RTDB", "Database Error: ${error.message}")
            }
    }

    private fun setLoadingState(isLoading: Boolean, button: Button, progressBar: ProgressBar?) {
        button.isEnabled = !isLoading
        button.text = if (isLoading) "Memproses..." else "Lanjutkan"
        progressBar?.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun navigateBackToLogin() {
        finish()
        @Suppress("DEPRECATION")
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}