package com.example.appsdokter

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
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
import androidx.lifecycle.lifecycleScope
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class LoginActivity : AppCompatActivity() {

    private var isPasswordVisible = false
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private val credentialManager = CredentialManager.create(this)
    private val WEB_CLIENT_ID = getString(R.string.default_web_client_id)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        FirebaseApp.initializeApp(this)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

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
            applyTransition(R.anim.fade_in, R.anim.fade_out)
        }

        tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            applyTransition(R.anim.fade_in, R.anim.fade_out)
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

    private fun signInGoogle() {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId(WEB_CLIENT_ID)
            .setFilterByAuthorizedAccounts(true)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        try {
            lifecycleScope.launch {
                val response = credentialManager.getCredential(this@LoginActivity, request)
                val credential = response.credential
                if (credential is CustomCredential) {
                    val idToken = com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
                        .createFrom(credential.data)
                        .idToken
                    firebaseAuthWithGoogle(idToken)
                } else {
                    Toast.makeText(this@LoginActivity, "Akun Google tidak ditemukan.", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: GetCredentialCancellationException) {
            // Handle cancellation
        } catch (e: GetCredentialException) {
            Toast.makeText(this, "Google Sign-In Gagal: ${e.message}", Toast.LENGTH_SHORT).show()
        }
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

                    // Simpan/Update profil user ke Firestore (merge agar field lain tidak hilang)
                    firestore.collection("users").document(uid)
                        .set(userData, SetOptions.merge())

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
                                firestore.collection("users").document(user.uid)
                                    .update("status_aktif", true)
                                    .addOnSuccessListener { fetchUserDataFromFirestore(user.uid, true, btnLogin, progressBar) }
                                    .addOnFailureListener { fetchUserDataFromFirestore(user.uid, true, btnLogin, progressBar) }
                            } else {
                                fetchUserDataFromFirestore(user.uid, false, btnLogin, progressBar)
                            }
                        }
                    }
                } else {
                    setLoadingState(false, btnLogin, progressBar)
                    Toast.makeText(this, "Login gagal: Email/Password salah.", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun fetchUserDataFromFirestore(
        uid: String,
        isVerifiedInAuth: Boolean,
        btnLogin: Button,
        progressBar: ProgressBar
    ) {
        firestore.collection("users").document(uid)
            .get()
            .addOnSuccessListener { document ->
                setLoadingState(false, btnLogin, progressBar)
                val isActiveInDb = document.getBoolean("status_aktif") ?: false
                if (isVerifiedInAuth || isActiveInDb) {
                    if (isVerifiedInAuth && !isActiveInDb) {
                        firestore.collection("users").document(uid).update("status_aktif", true)
                    }
                    navigateToMain()
                } else {
                    Toast.makeText(this@LoginActivity, "Akun belum aktif. Cek email Anda.", Toast.LENGTH_LONG).show()
                    auth.signOut()
                }
            }
            .addOnFailureListener {
                setLoadingState(false, btnLogin, progressBar)
                auth.signOut()
            }
    }

    private fun loginWithPhoneNumber(phoneInput: String, password: String, btnLogin: Button, progressBar: ProgressBar) {
        firestore.collection("users")
            .whereEqualTo("no_hp", normalizePhone(phoneInput))
            .get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    val email = snapshot.documents.firstOrNull()?.getString("email")
                    if (email != null) {
                        signInWithEmail(email, password, btnLogin, progressBar)
                    } else {
                        setLoadingState(false, btnLogin, progressBar)
                        Toast.makeText(this@LoginActivity, "Email tidak ditemukan.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    setLoadingState(false, btnLogin, progressBar)
                    Toast.makeText(this@LoginActivity, "Nomor tidak terdaftar.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                setLoadingState(false, btnLogin, progressBar)
                Toast.makeText(this@LoginActivity, "Gagal mencari nomor: ${it.message}", Toast.LENGTH_SHORT).show()
            }
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
        applyTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }
}