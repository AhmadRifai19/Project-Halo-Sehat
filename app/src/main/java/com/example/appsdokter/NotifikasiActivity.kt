
package com.example.appsdokter

import android.os.Bundle
import android.widget.Button
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity

class NotifikasiActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifikasi)

        val tombol = findViewById<Button>(R.id.btn_tandai_dibaca)

        tombol.setOnClickListener {
            val intent = Intent(
                this,
                NotifikasiSemuaActivity::class.java
            )
            startActivity(intent)
        }
    }
}
