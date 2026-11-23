package com.example.appsdokter

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment

class NotifikasiFragment : Fragment(R.layout.activity_notifikasi) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tombol = view.findViewById<Button>(R.id.btn_tandai_dibaca)
        tombol.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, NotifikasiSemuaFragment())
                .commit()
        }
    }
}