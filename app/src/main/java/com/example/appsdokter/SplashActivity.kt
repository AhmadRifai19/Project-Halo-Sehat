package com.example.appsdokter

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

// Splash screen - tampil 2.5 detik lalu pindah ke MainActivity
// pakai SplashScreen API biar transisi mulus di Android 12+
class SplashActivity : AppCompatActivity() {

    private var keepSplash = true

    override fun onCreate(savedInstanceState: Bundle?) {
        // install splash bawaan Android 12+ dulu sebelum super.onCreate
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        // tahan splash system selama keepSplash = true
        splashScreen.setKeepOnScreenCondition { keepSplash }

        // tampilkan layout custom kita (logo + nama + loading)
        setContentView(R.layout.activity_splash)

        // delay 2.5 detik lalu ke MainActivity
        Handler(Looper.getMainLooper()).postDelayed({
            keepSplash = false
            startActivity(Intent(this, MainActivity::class.java))
            finish() // tutup splash biar gak bisa back
        }, 2500)
    }
}
