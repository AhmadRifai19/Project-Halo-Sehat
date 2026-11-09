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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LoginActivity : AppCompatActivity() {

    private var isPasswordVisible = false
    private lateinit var auth: FirebaseAuth
    private lateinit var dbRef: DatabaseReference
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        FirebaseApp.initializeApp(this)
        auth = FirebaseAuth.getInstance()
        dbRef = FirebaseDatabase.getInstance().reference

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val etEmailOrPhone = findViewById<EditText>(R.id.et_email)
        val etPassword = findViewById<EditText>(R.id.et_password)
        val btnEye = findViewById<ImageView>(R.id.btn_eye)
        val tvForgotPassword = findViewById<TextView>(R.id.tv_forgot_password)
        val btnLogin = findViewById<Button>(R.id.btn_login)
        val tvRegister = findViewById<TextView>(R.id.tv_register)
        val progressBar = findViewById<ProgressBar>(R.id.progress_bar)
        val btnGoogleLogin = findViewById<Button>(R.id.btn_google_login)

        btnGoogleLogin.setOnClickListener { signInGoogle() }

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

        tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, LupaPasswordActivity::class.java))
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }

        tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }

        btnLogin.setOnClickListener {
            val identifier = etEmailOrPhone.text.toString().trim()
            val password = etPassword.text.toString().trim()

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

            if (Patterns.EMAIL_ADDRESS.matcher(identifier).matches()) {
                signInWithEmail(identifier, password, btnLogin, progressBar)
            } else {
                loginWithPhoneNumber(identifier, password, btnLogin, progressBar)
            }
        }
    }

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Toast.makeText(this, "Google Sign-In Gagal: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun signInGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val uid = user?.uid ?: return@addOnCompleteListener
                    
                    val userData = mapOf(
                        "nama" to (user.displayName ?: ""),
                        "email" to (user.email ?: ""),
                        "status_aktif" to true
                    )
                    
                    dbRef.child("users").child(uid).updateChildren(userData)
                    
                    Toast.makeText(this, "Selamat datang, ${user.displayName}", Toast.LENGTH_SHORT).show()
                    navigateToMain()
                } else {
                    Toast.makeText(this, "Google Login Gagal: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun signInWithEmail(email: String, password: String, btnLogin: Button, progressBar: ProgressBar) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        user.reload().addOnCompleteListener { _ ->
                            val isVerified = user.isEmailVerified
                            if (isVerified) {
                                dbRef.child("users").child(user.uid).child("status_aktif").setValue(true)
                                    .addOnSuccessListener { fetchUserDataFromRtdb(user.uid, true, btnLogin, progressBar) }
                                    .addOnFailureListener { fetchUserDataFromRtdb(user.uid, true, btnLogin, progressBar) }
                            } else {
                                fetchUserDataFromRtdb(user.uid, false, btnLogin, progressBar)
                            }
                        }
                    }
                } else {
                    setLoadingState(false, btnLogin, progressBar)
                    Toast.makeText(this, "Login gagal: Email/Password salah.", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun fetchUserDataFromRtdb(uid: String, isVerifiedInAuth: Boolean, btnLogin: Button, progressBar: ProgressBar) {
        dbRef.child("users").child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                setLoadingState(false, btnLogin, progressBar)
                val isActiveInDb = snapshot.child("status_aktif").getValue(Boolean::class.java) ?: false
                if (isVerifiedInAuth || isActiveInDb) {
                    if (isVerifiedInAuth && !isActiveInDb) dbRef.child("users").child(uid).child("status_aktif").setValue(true)
                    navigateToMain()
                } else {
                    Toast.makeText(this@LoginActivity, "Akun belum aktif. Cek email Anda.", Toast.LENGTH_LONG).show()
                    auth.signOut()
                }
            }
            override fun onCancelled(error: DatabaseError) { setLoadingState(false, btnLogin, progressBar); auth.signOut() }
        })
    }

    private fun loginWithPhoneNumber(phoneInput: String, password: String, btnLogin: Button, progressBar: ProgressBar) {
        dbRef.child("users").orderByChild("no_hp").equalTo(normalizePhone(phoneInput))
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        var email: String? = null
                        for (s in snapshot.children) { email = s.child("email").getValue(String::class.java); break }
                        if (email != null) signInWithEmail(email, password, btnLogin, progressBar)
                        else { setLoadingState(false, btnLogin, progressBar); Toast.makeText(this@LoginActivity, "Email tidak ditemukan.", Toast.LENGTH_SHORT).show() }
                    } else { setLoadingState(false, btnLogin, progressBar); Toast.makeText(this@LoginActivity, "Nomor tidak terdaftar.", Toast.LENGTH_SHORT).show() }
                }
                override fun onCancelled(error: DatabaseError) { setLoadingState(false, btnLogin, progressBar) }
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
