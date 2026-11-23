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

        aturMenu(view, R.id.itemInformasiPribadi, "Informasi Pribadi", "Kelola informasi akun Anda")
        aturMenu(view, R.id.itemIdentitas, "Identitas dan Alamat", "Kelola identitas dan alamat")
        aturMenu(view, R.id.itemKontak, "Kontak Akun", "Kelola email dan nomor HP")
        aturMenu(view, R.id.itemProfilKesehatan, "Profil Kesehatan", "Kelola informasi kesehatan")
        aturMenu(view, R.id.itemRiwayatKonsultasi, "Riwayat Konsultasi", "Lihat riwayat konsultasi Anda")
        aturMenu(view, R.id.itemKeamanan, "Keamanan Akun", "Kelola keamanan akun")
        aturMenu(view, R.id.itemNotifikasi, "Pengaturan Notifikasi", "Atur pemberitahuan aplikasi")

        view.findViewById<Button>(R.id.btnEditProfil).setOnClickListener {
            Toast.makeText(requireContext(), "Membuka Edit Profil", Toast.LENGTH_SHORT).show()
        }

        view.findViewById<TextView>(R.id.btnKeluar).setOnClickListener {
            Toast.makeText(requireContext(), "Anda memilih keluar dari akun", Toast.LENGTH_SHORT).show()
        }
    }

    private fun aturMenu(rootView: View, viewId: Int, judul: String, deskripsi: String) {
        val menu = rootView.findViewById<View>(viewId)
        menu.findViewById<TextView>(R.id.tvMenuTitle).text = judul
        menu.findViewById<TextView>(R.id.tvMenuSubtitle).text = deskripsi

        menu.setOnClickListener {
            val intent = Intent(requireContext(), DetailProfilActivity::class.java)
            intent.putExtra("JUDUL", judul)
            intent.putExtra("DESKRIPSI", deskripsi)
            startActivity(intent)
        }
    }
}