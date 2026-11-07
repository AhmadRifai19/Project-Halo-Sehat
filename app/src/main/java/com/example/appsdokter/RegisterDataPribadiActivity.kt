package com.example.appsdokter

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.Locale
import java.util.Calendar

class RegisterDataPribadiActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val dbRef = FirebaseDatabase.getInstance().getReference("users")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_data_pribadi)

        // 1. Menghubungkan komponen XML dengan Kotlin
        val etNik = findViewById<EditText>(R.id.etNik)
        val etTempatLahir = findViewById<EditText>(R.id.etTempatLahir)
        val etTanggalLahir = findViewById<EditText>(R.id.etTanggalLahir)
        val spJenisKelamin = findViewById<Spinner>(R.id.spJenisKelamin)
        val etAlamat = findViewById<EditText>(R.id.etAlamat)
        val cbPersetujuan = findViewById<CheckBox>(R.id.cbPersetujuan)
        val btnDaftar = findViewById<Button>(R.id.btnDaftar)

        // Terima data dari intent
        val emailFromIntent = intent.getStringExtra("email") ?: ""
        val passwordFromIntent = intent.getStringExtra("password") ?: ""
        val namaFromIntent = intent.getStringExtra("nama") ?: ""
        val noHpFromIntent = intent.getStringExtra("no_hp") ?: ""

        val btnKembaliRegistrasi = findViewById<TextView>(R.id.btnKembaliRegistrasi)

        btnKembaliRegistrasi.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }

        // 2. Mengatur pilihan jenis kelamin
        val jenisKelamin = arrayOf(
            "Pilih jenis kelamin",
            "Laki-laki",
            "Perempuan"
        )

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            jenisKelamin
        )

        adapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        spJenisKelamin.adapter = adapter

        // 3. Menampilkan kalender tanggal lahir
        etTanggalLahir.setOnClickListener {

            val calendar = Calendar.getInstance()

            val tahun = calendar.get(Calendar.YEAR)
            val bulan = calendar.get(Calendar.MONTH)
            val tanggal = calendar.get(Calendar.DAY_OF_MONTH)

            val datePicker = DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->

                    val tanggalFormat = String.format(
                        Locale.getDefault(),
                        "%02d/%02d/%04d",
                        dayOfMonth,
                        month + 1,
                        year
                    )

                    etTanggalLahir.setText(tanggalFormat)
                },
                tahun,
                bulan,
                tanggal
            )

            // Tidak bisa memilih tanggal setelah hari ini
            datePicker.datePicker.maxDate = calendar.timeInMillis

            datePicker.show()
        }


        // 4. Mengatur tombol Daftar
        btnDaftar.setOnClickListener {

            // Mengambil data dari input
            val nik = etNik.text.toString().trim()
            val tempatLahir = etTempatLahir.text.toString().trim()
            val tanggalLahir = etTanggalLahir.text.toString().trim()
            val alamat = etAlamat.text.toString().trim()

            // Validasi NIK harus 16 digit angka
            if (!nik.matches(Regex("[0-9]{16}"))) {
                etNik.error = "NIK harus terdiri dari 16 digit angka"
                etNik.requestFocus()
                return@setOnClickListener
            }

            // Validasi tempat lahir
            if (tempatLahir.isEmpty()) {
                etTempatLahir.error = "Tempat lahir harus diisi"
                etTempatLahir.requestFocus()
                return@setOnClickListener
            }

            // Validasi tanggal lahir
            if (tanggalLahir.isEmpty()) {
                Toast.makeText(
                    this,
                    "Silakan pilih tanggal lahir",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            // Validasi jenis kelamin
            if (spJenisKelamin.selectedItemPosition == 0) {
                Toast.makeText(
                    this,
                    "Silakan pilih jenis kelamin",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            // Validasi alamat
            if (alamat.isEmpty()) {
                etAlamat.error = "Alamat harus diisi"
                etAlamat.requestFocus()
                return@setOnClickListener
            }

            // Validasi persetujuan
            if (!cbPersetujuan.isChecked) {
                Toast.makeText(
                    this,
                    "Silakan setujui Syarat dan Ketentuan",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            // Jika semua data sudah benar
            auth.createUserWithEmailAndPassword(emailFromIntent, passwordFromIntent)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val userId = auth.currentUser?.uid
                        val userMap = mapOf(
                            "nik" to nik,
                            "tempat_lahir" to tempatLahir,
                            "tanggal_lahir" to tanggalLahir,
                            "jenis_kelamin" to spJenisKelamin.selectedItem.toString(),
                            "alamat" to alamat,
                            "nama" to namaFromIntent,
                            "no_hp" to noHpFromIntent
                        )

                        userId?.let {
                            dbRef.child(it).setValue(userMap).addOnCompleteListener { dbTask ->
                                if (dbTask.isSuccessful) {
                                    val intent = Intent(this, RegisterSuccessActivity::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    startActivity(intent)
                                    finish()
                                } else {
                                    Toast.makeText(this, "Gagal simpan database: ${dbTask.exception?.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    } else {
                        Toast.makeText(this, "Gagal Daftar: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}