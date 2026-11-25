package com.example.appsdokter

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar

class InformasiPribadiActivity : AppCompatActivity() {

    private lateinit var etNama: EditText
    private lateinit var tvTanggal: TextView
    private lateinit var spJenisKelamin: Spinner

    private val prefs by lazy {
        getSharedPreferences("data_pribadi", MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_informasi_pribadi)

        etNama = findViewById(R.id.etNamaLengkapInformasi)
        tvTanggal = findViewById(R.id.tvTanggalLahir)
        spJenisKelamin = findViewById(R.id.spJenisKelaminInformasi)

        val btnBack = findViewById<ImageView>(
            R.id.btnBackInformasi
        )

        val btnPilihTanggal = findViewById<LinearLayout>(
            R.id.btnPilihTanggal
        )

        val btnSimpan = findViewById<Button>(
            R.id.btnSimpanInformasi
        )

        val tvEditData = findViewById<TextView>(
            R.id.tvEditData
        )

        // Dropdown jenis kelamin
        val daftarJenisKelamin = listOf(
            "Belum diisi",
            "Laki-laki",
            "Perempuan"
        )

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            daftarJenisKelamin
        )

        adapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        spJenisKelamin.adapter = adapter

        // Ambil data tersimpan
        etNama.setText(
            prefs.getString("nama", "Alfi Mariani")
        )

        tvTanggal.text = prefs.getString(
            "tanggal",
            "Belum diisi"
        )

        val jenisKelamin = prefs.getString(
            "jenis_kelamin",
            "Belum diisi"
        )

        val posisi = daftarJenisKelamin.indexOf(
            jenisKelamin
        )

        spJenisKelamin.setSelection(
            if (posisi >= 0) posisi else 0
        )

        // Tombol kembali
        btnBack.setOnClickListener {
            finish()
        }

        // Edit data
        tvEditData.setOnClickListener {
            etNama.requestFocus()
            etNama.setSelection(etNama.text.length)
            Toast.makeText(
                this,
                "Silakan ubah data pribadi Anda",
                Toast.LENGTH_SHORT
            ).show()
        }

        // Pilih tanggal lahir
        btnPilihTanggal.setOnClickListener {
            tampilkanDatePicker()
        }

        // Simpan informasi
        btnSimpan.setOnClickListener {
            simpanInformasi()
        }
    }

    private fun tampilkanDatePicker() {

        val calendar = Calendar.getInstance()

        val datePicker = DatePickerDialog(
            this,
            { _, tahun, bulan, hari ->

                val tanggal = String.format(
                    "%02d/%02d/%04d",
                    hari,
                    bulan + 1,
                    tahun
                )

                tvTanggal.text = tanggal

            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePicker.datePicker.maxDate =
            System.currentTimeMillis()

        datePicker.show()
    }

    private fun simpanInformasi() {

        val nama = etNama.text.toString().trim()
        val tanggal = tvTanggal.text.toString()

        val jenisKelamin =
            spJenisKelamin.selectedItem.toString()

        if (nama.isEmpty()) {
            etNama.error = "Nama lengkap wajib diisi"
            etNama.requestFocus()
            return
        }

        if (tanggal == "Belum diisi") {
            Toast.makeText(
                this,
                "Silakan isi tanggal lahir",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (jenisKelamin == "Belum diisi") {
            Toast.makeText(
                this,
                "Silakan pilih jenis kelamin",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        prefs.edit()
            .putString("nama", nama)
            .putString("tanggal", tanggal)
            .putString("jenis_kelamin", jenisKelamin)
            .apply()

        Toast.makeText(
            this,
            "Informasi pribadi berhasil disimpan",
            Toast.LENGTH_SHORT
        ).show()

        finish()
    }
}
