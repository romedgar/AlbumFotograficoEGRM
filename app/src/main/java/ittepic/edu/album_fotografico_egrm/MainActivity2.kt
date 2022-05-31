package ittepic.edu.album_fotografico_egrm

import android.content.Intent
import android.graphics.BitmapFactory
import android.icu.util.GregorianCalendar
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import ittepic.edu.album_fotografico_egrm.databinding.ActivityMain2Binding
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class MainActivity2 : AppCompatActivity() {
    lateinit var binding: ActivityMain2Binding
    lateinit var imagen : Uri
    var listaArchivos = ArrayList<String>()
    var evento = ""
    var evento_id = ""



    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMain2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        evento = this.intent.extras!!.getString("evento")!!
        binding.cerrar.isEnabled = this.intent.extras!!.getBoolean("btncerrar")!!
        binding.enviar.isEnabled = this.intent.extras!!.getBoolean("btnesImg")!!
        binding.seleccionar.isEnabled = this.intent.extras!!.getBoolean("btnesImg")!!
        binding.ocultar.isEnabled = this.intent.extras!!.getBoolean("btnocultar")!!

        binding.evento.text = "Evento #:" + evento

        ObtenerID()
        cargarLista()



        binding.seleccionar.setOnClickListener {
            val galeria = Intent(Intent.ACTION_GET_CONTENT)
            galeria.type = "image/*"
            startActivityForResult(galeria, 2)
        }

        binding.enviar.setOnClickListener {
            var nombreImg = ""
            val cal = GregorianCalendar.getInstance()
            nombreImg = cal.get(Calendar.YEAR).toString()+
                    cal.get(Calendar.MONTH).toString()+
                    cal.get(Calendar.DAY_OF_MONTH).toString()+
                    cal.get(Calendar.HOUR).toString()+
                    cal.get(Calendar.MINUTE).toString()+
                    cal.get(Calendar.SECOND).toString()+
                    cal.get(Calendar.MILLISECOND).toString()+".jpg"

            val storageRef = FirebaseStorage.getInstance()
                .reference.child("imagenes/${evento}/${nombreImg}")

            storageRef.putFile(imagen)
                .addOnSuccessListener {
                    Toast.makeText(this, "Se subio el archivo", Toast.LENGTH_SHORT)
                        .show()
                     cargarLista()
                    binding.imagen.setImageBitmap(null)
                }
                .addOnFailureListener {
                    AlertDialog.Builder(this)
                        .setMessage(it.message)
                        .show()
                }
        }

        binding.cerrar.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Seguro?")
                .setMessage("No podrás volver a abrirlo")
                .setPositiveButton("Cerrar Evento"){d, i->
                    val baseRemota = FirebaseFirestore.getInstance()
                    baseRemota.collection("eventos")
                        .document(evento_id)
                        .update("estado",0)
                        .addOnSuccessListener {
                            Toast.makeText(this,"Se cerro correctamente",Toast.LENGTH_SHORT)
                                .show()
                            binding.ocultar.isEnabled = true
                            binding.cerrar.isEnabled = false
                            binding.seleccionar.isEnabled = false
                            binding.enviar.isEnabled = false
                        }
                        .addOnFailureListener {

                        }
                }
                .setNegativeButton("Salir"){d, i-> }
                .show()
        }

        binding.ocultar.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Seguro?")
                .setMessage("No podrás hacerlo visible de nuevo")
                .setPositiveButton("Ocultar Evento"){d, i->
                    val baseRemota = FirebaseFirestore.getInstance()
                    baseRemota.collection("eventos")
                        .document(evento_id)
                        .update("visible",0)
                        .addOnSuccessListener {
                            Toast.makeText(this,"Se oculto correctamente",Toast.LENGTH_SHORT)
                                .show()
                            binding.ocultar.isEnabled = false
                            binding.cerrar.isEnabled = false
                            binding.seleccionar.isEnabled = false
                            binding.enviar.isEnabled = false
                        }
                        .addOnFailureListener {

                        }
                }
                .setNegativeButton("Salir"){d, i-> }
                .show()
        }
    }

    private fun cargarLista() {
      val storageRef = FirebaseStorage.getInstance()
          .reference.child("imagenes/${evento}")

        storageRef.listAll()
            .addOnSuccessListener {
                listaArchivos.clear()
                it.items.forEach {
                    listaArchivos.add(it.name)
                }
                binding.lista.adapter = ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, listaArchivos)
                binding.lista.setOnItemClickListener { adapterView, view, i, l ->
                    cargarImagen(listaArchivos.get(i))
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == 2){
            imagen = data!!.data!!
            binding.imagen.setImageURI(imagen)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menuoculto, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.acerca->{
                AlertDialog.Builder(this)
                    .setTitle("Participantes")
                    .setMessage("Solo yo :( / Edgar Gerardo Rojas Medina #18401193")
                    .show()
            }
            R.id.sesion->{
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(this, MainActivity::class.java))
                finish()

            }
            R.id.salir->{

            }
        }
        return true
    }

    private fun cargarImagen(archivo: String){
        val storageRef = FirebaseStorage.getInstance()
            .reference.child("imagenes/${evento}/${archivo}")

        val archivoTemporal = File.createTempFile("imagenTemp","jpg")

        storageRef.getFile(archivoTemporal)
            .addOnSuccessListener {
                val mapBits = BitmapFactory.decodeFile(archivoTemporal.absolutePath)
                binding.imagen.setImageBitmap(mapBits)
            }
    }

    private fun ObtenerID(){
        val baseRemota = FirebaseFirestore.getInstance()
        baseRemota.collection("eventos").whereEqualTo("nombre_evento",evento)
            .get()
            .addOnSuccessListener {
                for (documento in it){
                    evento_id = documento.id
                    binding.descripcion.text = documento.getString("descripcion").toString()
                }
            }
            .addOnFailureListener {

            }
    }
}