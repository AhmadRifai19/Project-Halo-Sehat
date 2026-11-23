package com.example.appsdokter

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment

class AiKonsulFragment : Fragment(R.layout.activity_ai_konsul) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val pesan = view.findViewById<EditText>(R.id.et_ai_message)
        val kirim = view.findViewById<ImageButton>(R.id.btn_send_message)

        kirim.setOnClickListener {
            val isiPesan = pesan.text.toString().trim()
            if (isiPesan.isNotEmpty()) {
                bukaChat(isiPesan)
                pesan.text.clear()
            } else {
                Toast.makeText(requireContext(), "Masukkan pesan terlebih dahulu", Toast.LENGTH_SHORT).show()
            }
        }

        view.findViewById<LinearLayout>(R.id.topic_gejala).setOnClickListener {
            bukaChat("Saya ingin mengenali gejala yang saya alami. Bantu saya memahami kemungkinan penyebabnya dan saran awalnya.")
        }

        view.findViewById<LinearLayout>(R.id.topic_obat).setOnClickListener {
            bukaChat("Saya ingin informasi seputar obat. Bantu jelaskan cara pakai, dosis, dan efek samping yang perlu diperhatikan.")
        }

        view.findViewById<LinearLayout>(R.id.topic_tips).setOnClickListener {
            bukaChat("Berikan tips menjaga kesehatan sehari-hari yang praktis dan mudah dilakukan.")
        }

        view.findViewById<LinearLayout>(R.id.topic_dokter).setOnClickListener {
            bukaChat("Saya ingin persiapan konsultasi dokter. Bantu saya membuat daftar hal yang perlu disiapkan dan ditanyakan.")
        }
    }

    private fun bukaChat(pesanAwal: String) {
        val intent = Intent(requireContext(), AiConversationActivity::class.java)
        intent.putExtra("INITIAL_MESSAGE", pesanAwal)
        startActivity(intent)
    }
}