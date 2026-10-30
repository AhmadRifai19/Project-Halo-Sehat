package com.example.appsdokter

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    private val handler = Handler(Looper.getMainLooper())
    
    // Sesuaikan MainActivity dengan nama class activity login Anda nantinya
    private val goToLogin = Runnable {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Referensi View
        val logo = findViewById<ImageView>(R.id.imgLogo)
        val title = findViewById<TextView>(R.id.tvTitle)
        val tagline = findViewById<TextView>(R.id.tvTagline)

        // Set awal transparan
        logo.alpha = 0f
        title.alpha = 0f
        tagline.alpha = 0f

        // 1. Animasi Logo (Muncul pertama, durasi 800ms)
        logo.animate()
            .alpha(1f)
            .setDuration(800)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withEndAction {
                // 2. Setelah Logo muncul, munculkan Nama Aplikasi (durasi 600ms)
                title.animate()
                    .alpha(1f)
                    .setDuration(600)
                    .setInterpolator(AccelerateDecelerateInterpolator())
                    .withEndAction {
                        // 3. Setelah Nama muncul, munculkan Penjelasan (durasi 600ms)
                        tagline.animate()
                            .alpha(1f)
                            .setDuration(600)
                            .setInterpolator(AccelerateDecelerateInterpolator())
                            .start()
                    }
                    .start()
            }
            .start()

        // Pindah ke halaman berikutnya setelah 2.5 detik
        handler.postDelayed(goToLogin, 2500)
    }

    override fun onDestroy() {
        handler.removeCallbacks(goToLogin)
        super.onDestroy()
    }
}