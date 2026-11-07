
package com.example.appsdokter

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class AiKonsulActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ai_konsul)

        val pesan = findViewById<EditText>(R.id.et_ai_message)
        val kirim = findViewById<ImageButton>(R.id.btn_send_message)

        kirim.setOnClickListener {
            val isiPesan = pesan.text.toString().trim()

            if (isiPesan.isNotEmpty()) {
                Toast.makeText(
                    this,
                    "Pesan berhasil dimasukkan",
                    Toast.LENGTH_SHORT
                ).show()

                pesan.text.clear()
            } else {
                Toast.makeText(
                    this,
                    "Masukkan pesan terlebih dahulu",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
