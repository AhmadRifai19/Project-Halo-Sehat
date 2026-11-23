package com.example.appsdokter

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.TextViewCompat
import androidx.fragment.app.Fragment

class MainActivity : AppCompatActivity() {

    private lateinit var navBeranda: TextView
    private lateinit var navAiKonsul: TextView
    private lateinit var navNotifikasi: TextView
    private lateinit var navProfile: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        navBeranda = findViewById(R.id.navBeranda)
        navAiKonsul = findViewById(R.id.navAiKonsul)
        navNotifikasi = findViewById(R.id.navNotifikasi)
        navProfile = findViewById(R.id.navProfile)

        // Default halaman pertama kali dibuka: BerandaFragment
        if (savedInstanceState == null) {
            bukaFragment(BerandaFragment(), navBeranda)
        }

        navBeranda.setOnClickListener {
            bukaFragment(BerandaFragment(), navBeranda)
        }

        navAiKonsul.setOnClickListener {
            bukaFragment(AiKonsulFragment(), navAiKonsul)
        }

        navNotifikasi.setOnClickListener {
            bukaFragment(NotifikasiFragment(), navNotifikasi)
        }

        navProfile.setOnClickListener {
            bukaFragment(ProfilFragment(), navProfile)
        }
    }

    private fun bukaFragment(fragment: Fragment, tabAktif: TextView) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()

        val warnaNonAktif = Color.parseColor("#63758A")
        val warnaAktif = Color.parseColor("#2879C7")

        val semuaTab = listOf(navBeranda, navAiKonsul, navNotifikasi, navProfile)
        for (tab in semuaTab) {
            tab.setTextColor(warnaNonAktif)
            tab.setTypeface(null, Typeface.NORMAL)
            TextViewCompat.setCompoundDrawableTintList(
                tab,
                ColorStateList.valueOf(warnaNonAktif)
            )
        }

        tabAktif.setTextColor(warnaAktif)
        tabAktif.setTypeface(null, Typeface.BOLD)
        TextViewCompat.setCompoundDrawableTintList(
            tabAktif,
            ColorStateList.valueOf(warnaAktif)
        )
    }
}