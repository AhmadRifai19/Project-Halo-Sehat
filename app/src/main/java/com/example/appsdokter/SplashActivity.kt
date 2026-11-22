package com.example.appsdokter

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        // Tampilan Fullscreen modern menggunakan WindowCompat & WindowInsetsControllerCompat
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        insetsController.hide(WindowInsetsCompat.Type.systemBars())

        setContentView(R.layout.activity_splash)

        // Referensi View
        val logo = findViewById<ImageView>(R.id.imgLogo)
        val title = findViewById<TextView>(R.id.tvTitle)
        val desc = findViewById<TextView>(R.id.tvTagline)

        // Animasi Fade-in dan Slide-up
        logo?.let {
            it.alpha = 0f
            it.animate()
                .alpha(1f)
                .translationY(-20f)
                .setDuration(1000)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()
        }

        title?.let {
            it.alpha = 0f
            it.animate()
                .alpha(1f)
                .translationY(-20f)
                .setStartDelay(300)
                .setDuration(1000)
                .start()
        }

        desc?.let {
            it.alpha = 0f
            it.animate()
                .alpha(1f)
                .translationY(-20f)
                .setStartDelay(600)
                .setDuration(1000)
                .start()
        }

        // Delay 2.5 detik lalu cek sesi user
        Handler(Looper.getMainLooper()).postDelayed({
            val auth = FirebaseAuth.getInstance()
            val user = auth.currentUser

            if (user != null && user.isEmailVerified) {
                // Sesi valid, langsung ke Beranda
                startActivity(Intent(this, BerandaActivity::class.java))
            } else {
                // Harus login
                startActivity(Intent(this, LoginActivity::class.java))
            }
            applyTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }, 2500)
    }
}
