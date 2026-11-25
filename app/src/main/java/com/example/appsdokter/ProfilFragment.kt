
package com.example.appsdokter

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment

class ProfilFragment : Fragment(R.layout.activity_profil) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // MENU INFORMASI PRIBADI
        aturMenu(
            view,
            R.id.itemInformasiPribadi,
            "Informasi Pribadi",
            "Kelola informasi akun Anda"
        )

        // MENU IDENTITAS DAN ALAMAT
        aturMenu(
            view,
            R.id.itemIdentitas,
            "Identitas dan Alamat",
            "Kelola identitas dan alamat"
        )

        // MENU KONTAK AKUN
        aturMenu(
            view,
            R.id.itemKontak,
            "Kontak Akun",
            "Kelola email dan nomor HP"
        )

        // MENU PROFIL KESEHATAN
        aturMenu(
            view,
            R.id.itemProfilKesehatan,
            "Profil Kesehatan",
            "Kelola informasi kesehatan"
        )

        // MENU RIWAYAT KONSULTASI
        aturMenu(
            view,
            R.id.itemRiwayatKonsultasi,
            "Riwayat Konsultasi",
            "Lihat riwayat konsultasi Anda"
        )

        // MENU KEAMANAN AKUN
        aturMenu(
            view,
            R.id.itemKeamanan,
            "Keamanan Akun",
            "Kelola keamanan akun"
        )

        // MENU PENGATURAN NOTIFIKASI
        aturMenu(
            view,
            R.id.itemNotifikasi,
            "Pengaturan Notifikasi",
            "Atur pemberitahuan aplikasi"
        )

        // TOMBOL EDIT PROFIL
        view.findViewById<Button>(R.id.btnEditProfil)
            .setOnClickListener {
                val intent = Intent(
                    requireContext(),
                    EditProfilActivity::class.java
                )
                startActivity(intent)
            }

        // TOMBOL KELUAR
        view.findViewById<TextView>(R.id.btnKeluar)
            .setOnClickListener {
                Toast.makeText(
                    requireContext(),
                    "Anda memilih keluar dari akun",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    // FUNGSI NAVIGASI SETIAP MENU
    private fun aturMenu(
        rootView: View,
        viewId: Int,
        judul: String,
        deskripsi: String
    ) {

        val menu = rootView.findViewById<View>(viewId)

        menu.findViewById<TextView>(R.id.tvMenuTitle).text = judul
        menu.findViewById<TextView>(R.id.tvMenuSubtitle).text = deskripsi

        menu.setOnClickListener {

            if (viewId == R.id.itemInformasiPribadi) {

                // BUKA HALAMAN INFORMASI PRIBADI
                val intent = Intent(
                    requireContext(),
                    InformasiPribadiActivity::class.java
                )
                startActivity(intent)

            } else if (viewId == R.id.itemIdentitas) {

                // BUKA HALAMAN IDENTITAS DAN ALAMAT
                val intent = Intent(
                    requireContext(),
                    IdentitasAlamatActivity::class.java
                )
                startActivity(intent)

            } else if (viewId == R.id.itemKontak) {

                // BUKA HALAMAN KONTAK AKUN
                val intent = Intent(
                    requireContext(),
                    KontakAkunActivity::class.java
                )
                startActivity(intent)

            } else {

                // BUKA HALAMAN DETAIL PROFIL UNTUK MENU LAINNYA
                val intent = Intent(
                    requireContext(),
                    DetailProfilActivity::class.java
                )

                intent.putExtra("JUDUL", judul)
                intent.putExtra("DESKRIPSI", deskripsi)

                startActivity(intent)
            }
        }
    }
}
