package com.example.appsdokter

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView

class IdentitasAlamatActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_identitas_alamat)

        findViewById<TextView>(R.id.btnBackIdentitas).setOnClickListener {
            finish()
        }
    }
}