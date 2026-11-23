package com.example.appsdokter

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ProfilActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profil)

        aturMenu(
            R.id.itemInformasiPribadi,
            "Informasi Pribadi",
            "Kelola informasi akun Anda",
            "Membuka Informasi Pribadi"
        )

        aturMenu(
            R.id.itemIdentitas,
            "Identitas dan Alamat",
            "Kelola identitas dan alamat",
            "Membuka Identitas dan Alamat"
        )

        aturMenu(
            R.id.itemKontak,
            "Kontak Akun",
            "Kelola email dan nomor HP",
            "Membuka Kontak Akun"
        )

        aturMenu(
            R.id.itemProfilKesehatan,
            "Profil Kesehatan",
            "Kelola informasi kesehatan",
            "Membuka Profil Kesehatan"
        )

        aturMenu(
            R.id.itemRiwayatKonsultasi,
            "Riwayat Konsultasi",
            "Lihat riwayat konsultasi Anda",
            "Membuka Riwayat Konsultasi"
        )

        aturMenu(
            R.id.itemKeamanan,
            "Keamanan Akun",
            "Kelola keamanan akun",
            "Membuka Keamanan Akun"
        )

        aturMenu(
            R.id.itemNotifikasi,
            "Pengaturan Notifikasi",
            "Atur pemberitahuan aplikasi",
            "Membuka Pengaturan Notifikasi"
        )

        // Tombol Edit Profil
        findViewById<Button>(R.id.btnEditProfil)
            .setOnClickListener {
                tampilPesan("Membuka Edit Profil")
            }

        // Tombol Keluar
        findViewById<TextView>(R.id.btnKeluar)
            .setOnClickListener {
                tampilPesan("Anda memilih keluar dari akun")
            }

        setupBottomNav(R.id.navProfile)
    }

    private fun tampilPesan(pesan: String) {
        Toast.makeText(
            this,
            pesan,
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun aturMenu(
        viewId: Int,
        judul: String,
        deskripsi: String,
        pesan: String
    ) {
        val menu = findViewById<android.view.View>(viewId)

        menu.findViewById<TextView>(R.id.tvMenuTitle)
            .text = judul

        menu.findViewById<TextView>(R.id.tvMenuSubtitle)
            .text = deskripsi

        menu.setOnClickListener {
            val intent = android.content.Intent(
                this,
                DetailProfilActivity::class.java
            )

            intent.putExtra("JUDUL", judul)
            intent.putExtra("DESKRIPSI", deskripsi)

            startActivity(intent)
        }
    }
}