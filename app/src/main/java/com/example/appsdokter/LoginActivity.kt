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
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LoginActivity : AppCompatActivity() {

    private var isPasswordVisible = false
    private lateinit var auth: FirebaseAuth
    private lateinit var dbRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // 1. Inisialisasi FirebaseApp, Auth, dan Realtime Database
        FirebaseApp.initializeApp(this)
        auth = FirebaseAuth.getInstance()

        // Menggunakan instance default RTDB (otomatis membaca URL dari google-services.json)
        val database = FirebaseDatabase.getInstance()
        dbRef = database.reference

        // Tes koneksi ke Realtime Database
        testRtdbConnection()

        // 2. Inisialisasi Komponen UI sesuai ID pada activity_login.xml
        val etEmailOrPhone = findViewById<EditText>(R.id.et_email)
        val etPassword = findViewById<EditText>(R.id.et_password)
        val btnEye = findViewById<ImageView>(R.id.btn_eye)
        val tvForgotPassword = findViewById<TextView>(R.id.tv_forgot_password)
        val btnLogin = findViewById<Button>(R.id.btn_login)
        val tvRegister = findViewById<TextView>(R.id.tv_register)
        val progressBar = findViewById<ProgressBar>(R.id.progress_bar)

        // 3. Fungsi Show/Hide Password
        btnEye.setOnClickListener {
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

        // 4. Navigasi ke Lupa Password
        tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, LupaPasswordActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        // 5. Navigasi ke Daftar (RegisterActivity)
        tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        // 6. Aksi Tombol Masuk (Login dengan Firebase Auth & RTDB)
        btnLogin.setOnClickListener {
            val identifier = etEmailOrPhone.text.toString().trim()
            val password = etPassword.text.toString().trim()

            // Validasi input kosong
            if (identifier.isEmpty()) {
                etEmailOrPhone.error = "Email atau Nomor HP wajib diisi"
                etEmailOrPhone.requestFocus()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                etPassword.error = "Password wajib diisi"
                etPassword.requestFocus()
                return@setOnClickListener
            }

            setLoadingState(true, btnLogin, progressBar)

            // Cek apakah input berupa Email atau Nomor HP
            if (Patterns.EMAIL_ADDRESS.matcher(identifier).matches()) {
                // Jika formatnya Email, langsung proses ke Firebase Auth
                signInWithEmail(identifier, password, btnLogin, progressBar)
            } else {
                // Jika formatnya Nomor HP, cari email pengguna di node "users" RTDB terlebih dahulu
                loginWithPhoneNumber(identifier, password, btnLogin, progressBar)
            }
        }
    }

//    override fun onStart() {
//        super.onStart()
//        // Jika user sebelumnya sudah login, langsung arahkan ke MainActivity
//        if (auth.currentUser != null) {
//            navigateToMain()
//        }
//    }

    private fun testRtdbConnection() {
        val testData = mapOf(
            "nama" to "Halo Sehat",
            "status" to "Berhasil terhubung ke Realtime Database",
            "timestamp" to System.currentTimeMillis()
        )
        dbRef.child("connection_test").child("test_android").setValue(testData)
            .addOnSuccessListener {
                Log.d("FIREBASE_TEST", "Data percobaan berhasil dikirim ke Realtime Database!")
            }
            .addOnFailureListener { error ->
                Log.e("FIREBASE_TEST", "Gagal mengirim data ke RTDB: ${error.message}")
            }
    }

    private fun loginWithPhoneNumber(
        phoneInput: String,
        password: String,
        btnLogin: Button,
        progressBar: ProgressBar
    ) {
        val normalizedPhone = normalizePhone(phoneInput)

        // Mencari data user berdasarkan field "no_hp" di Realtime Database
        dbRef.child("users")
            .orderByChild("no_hp")
            .equalTo(normalizedPhone)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        var foundEmail: String? = null
                        for (userSnap in snapshot.children) {
                            foundEmail = userSnap.child("email").getValue(String::class.java)
                            if (!foundEmail.isNullOrEmpty()) break
                        }

                        if (!foundEmail.isNullOrEmpty()) {
                            signInWithEmail(foundEmail, password, btnLogin, progressBar)
                        } else {
                            setLoadingState(false, btnLogin, progressBar)
                            Toast.makeText(
                                this@LoginActivity,
                                "Email untuk nomor HP ini tidak ditemukan di database.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        setLoadingState(false, btnLogin, progressBar)
                        Toast.makeText(
                            this@LoginActivity,
                            "Nomor HP belum terdaftar. Silakan daftar akun terlebih dahulu.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    setLoadingState(false, btnLogin, progressBar)
                    Toast.makeText(
                        this@LoginActivity,
                        "Gagal memeriksa nomor HP: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun signInWithEmail(
        email: String,
        password: String,
        btnLogin: Button,
        progressBar: ProgressBar
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid
                    if (uid != null) {
                        fetchUserDataFromRtdb(uid, btnLogin, progressBar)
                    } else {
                        setLoadingState(false, btnLogin, progressBar)
                        navigateToMain()
                    }
                } else {
                    setLoadingState(false, btnLogin, progressBar)
                    Toast.makeText(
                        this,
                        "Login gagal: Email/Password salah atau akun belum terdaftar.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun fetchUserDataFromRtdb(uid: String, btnLogin: Button, progressBar: ProgressBar) {
        val userRef = dbRef.child("users").child(uid)

        // Perbarui waktu terakhir login
        userRef.child("last_login").setValue(System.currentTimeMillis())

        // Ambil nama pengguna dari Realtime Database untuk sapaan
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                setLoadingState(false, btnLogin, progressBar)
                val nama = snapshot.child("nama").getValue(String::class.java) ?: "Pengguna"
                Toast.makeText(this@LoginActivity, "Selamat datang, $nama!", Toast.LENGTH_SHORT).show()
                navigateToMain()
            }

            override fun onCancelled(error: DatabaseError) {
                setLoadingState(false, btnLogin, progressBar)
                Log.w("FIREBASE_RTDB", "Gagal memuat profil: ${error.message}")
                navigateToMain()
            }
        })
    }

    private fun normalizePhone(phone: String): String {
        val cleaned = phone.replace("[^0-9+]".toRegex(), "")
        return when {
            cleaned.startsWith("+62") -> "0" + cleaned.substring(3)
            cleaned.startsWith("62") -> "0" + cleaned.substring(2)
            else -> cleaned
        }
    }

    private fun setLoadingState(isLoading: Boolean, btnLogin: Button, progressBar: ProgressBar) {
        btnLogin.isEnabled = !isLoading
        btnLogin.text = if (isLoading) "Memproses..." else "Masuk"
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }
}