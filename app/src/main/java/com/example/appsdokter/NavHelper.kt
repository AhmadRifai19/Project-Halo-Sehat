package com.example.appsdokter

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.widget.TextView
import androidx.core.widget.TextViewCompat

fun Activity.setupBottomNav(activeMenuId: Int) {
    val activeColor = Color.parseColor("#2879C7")

    val navBeranda = findViewById<TextView>(R.id.navBeranda)
    val navAiKonsul = findViewById<TextView>(R.id.navAiKonsul)
    val navNotifikasi = findViewById<TextView>(R.id.navNotifikasi)
    val navProfile = findViewById<TextView>(R.id.navProfile)

    // Mengubah warna teks dan ikon menu yang sedang aktif menjadi biru & tebal
    findViewById<TextView>(activeMenuId)?.apply {
        setTextColor(activeColor)
        setTypeface(null, Typeface.BOLD)
        TextViewCompat.setCompoundDrawableTintList(this, ColorStateList.valueOf(activeColor))
    }

    navBeranda?.setOnClickListener {
        if (activeMenuId != R.id.navBeranda) {
            pindahTanpaSlide(BerandaActivity::class.java)
        }
    }

    navAiKonsul?.setOnClickListener {
        if (activeMenuId != R.id.navAiKonsul) {
            pindahTanpaSlide(AiKonsulActivity::class.java)
        }
    }

    navNotifikasi?.setOnClickListener {
        if (activeMenuId != R.id.navNotifikasi) {
            pindahTanpaSlide(NotifikasiActivity::class.java)
        }
    }

    navProfile?.setOnClickListener {
        if (activeMenuId != R.id.navProfile) {
            pindahTanpaSlide(ProfilActivity::class.java)
        }
    }
}

private fun Activity.pindahTanpaSlide(target: Class<out Activity>) {
    startActivity(Intent(this, target))
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        overrideActivityTransition(Activity.OVERRIDE_TRANSITION_OPEN, 0, 0)
        overrideActivityTransition(Activity.OVERRIDE_TRANSITION_CLOSE, 0, 0)
    } else {
        @Suppress("DEPRECATION")
        overridePendingTransition(0, 0)
    }
    finish()
}