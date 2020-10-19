package com.nabil.todoappcrud

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    //buat variable database reference yang akan diisi oleh database firebase
    private lateinit var databaseRef : DatabaseReference

    //variable cek dara dibuat untuk read
    private lateinit var cekData : DatabaseReference

    //untuk memantau perubahan database
    private lateinit var readDataListener : ValueEventListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

       databaseRef = FirebaseDatabase.getInstance().reference

        //ketika tombol data diklik
        btn_tambah.setOnClickListener {
            //ambil text dari edittext input nama
            val nama = input_nama.text.toString()
            if (nama.isBlank()){
                toastData("Kolom Nama Harus Diisi")

            }else{
                tambahData(nama)
            }
        }

        btn_hapus.setOnClickListener {
            val nama  =input_nama.text.toString()
            if (nama.isBlank()){
                toastData("Kolom Kalimat Harus Diisi")
            }else{
                hapusData(nama)
            }
        }

        btn_update.setOnClickListener {
            val KalimatAwal = input_nama.text.toString()
            val KalimatUpdate = edt_nama.text.toString()
            if (KalimatAwal.isBlank() || KalimatUpdate.isBlank()){
                toastData("Kolom Tidak Boleh Kosong")
            }else{
                updateData(KalimatAwal, KalimatUpdate)
            }
        }

        //untuk get dari data firebase
        cekDataKalimat()

    }

    private fun updateData(kalimatAwal: String, kalimatUpdate: String) {
        val dataUpdate = HashMap<String, Any>()
        dataUpdate["Nama"] = kalimatUpdate

        val dataListener = object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.childrenCount > 0){
                    databaseRef.child("Data Nama")
                        .child(kalimatAwal)
                        .updateChildren(dataUpdate)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) toastData("Data Berhasil Diupdate")
                        }
                }else{
                    toastData("Data yang dituju tidak ada didalam database")
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        }
        val dataAsal = databaseRef.child("Daftar Nama")
            .child(kalimatAwal)
        dataAsal.addListenerForSingleValueEvent(dataListener)
    }

    private fun hapusData(nama: String) {
        //untuk listenr data firebase
        val dataListener = object : ValueEventListener{
            //ondatacange untuk mengetahui aktifitas data
            //seperti penambahan, pengurangan dan perubahan data
            override fun onDataChange(snapshot: DataSnapshot) {
                //snapshot.childern count untuk mengetahui banyak data yg telah diambil
                if (snapshot.childrenCount > 0){
                    //jika data tersbut ada, maka hapus field nama yang ada di dalam tabel daftar nama
                    databaseRef.child("Daftar Nama").child(nama)
                        .removeValue()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) toastData("$nama telah dihapus")
                        }
                }else{
                    toastData("Tidak ada data $nama")
                }
            }

            override fun onCancelled(p0: DatabaseError) {
                toastData("tidak bisa menghapus data tersebut")
            }
        }

        //untuk mengahpus data, kita perlu cek data yang ada di dalam tabel datfar nama
        val cekData = databaseRef.child("Daftar Nama")
            .child(nama)
        //addValueEventListener itu menjalankan listener terus menerus selama data yang diimputkan sama
        //sedangkan addlistenerforsingleValueEvent itu dijalankan sekali saja
        cekData.addListenerForSingleValueEvent(dataListener)
    }

    private fun cekDataKalimat() {
        readDataListener = object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.childrenCount > 0){
                    var textData = ""
                    for (data in snapshot.children){
                        val nilai = data.getValue(ModelNama::class.java) as ModelNama
                        textData += "${nilai.Nama} \n"
                    }

                    txt_nama.text = textData
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        }

        cekData = databaseRef.child("Daftar Nama")
        cekData.addValueEventListener(readDataListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        cekData.removeEventListener(readDataListener)
    }

    private fun tambahData(nama: String) {
        val data = HashMap<String, Any>()
        data["Nama"] = nama

        //logika penambahan data, yaitu cek terlebih dahulu data
        //kemudian tambahkan data jika data belum ada
        val dataListener = object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                //snapshot childrecount ini menghitung jumlah data
                //jika data kurang dari satu maka pasti tidak ada data jadi tambahkan data
                if (snapshot.childrenCount < 1){
                    val tambahData = databaseRef.child("Daftar Nama")
                        .child(nama)
                        .setValue(data)
                    tambahData.addOnCompleteListener { task ->
                        if (task.isSuccessful){
                            toastData("$nama telah ditambahkan dalam database")
                        } else {
                            toastData("$nama gagal ditambahkan")
                        }
                    }
                } else {
                    toastData("Data tersebut sudah ada di database")
                }
            }

            override fun onCancelled(p0: DatabaseError) {
                toastData("Terjadi error saat menambah data")
            }
        }

        //untuk mengecek tabel daftar nama apakah data yang ingin diinputkan ke tabel tersebut sudah ada
        databaseRef.child("Daftar Nama")
            .child(nama).addListenerForSingleValueEvent(dataListener)
    }





    private fun toastData(pesan : String){
        Toast.makeText(this, pesan, Toast.LENGTH_SHORT).show()
    }
}