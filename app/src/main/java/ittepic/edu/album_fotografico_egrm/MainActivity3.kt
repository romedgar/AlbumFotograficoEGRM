package ittepic.edu.album_fotografico_egrm

import android.content.Intent
import android.icu.util.GregorianCalendar
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import ittepic.edu.album_fotografico_egrm.databinding.ActivityMain3Binding
import java.util.*

class MainActivity3 : AppCompatActivity() {
    lateinit var binding: ActivityMain3Binding
    var estado = 1
    var visible = 1
    var owner = ""
    var btncerrar = false
    var btnocultar = false
    var btnesImg = false

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMain3Binding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.crearEvento.setOnClickListener {

            if (binding.nombreEvento.text.toString().equals("")){
                AlertDialog.Builder(this).setMessage("Ingrese un nombre de evento").show()
                return@setOnClickListener
            }
            var nombreEvento = ""
            val cal = GregorianCalendar.getInstance()

            nombreEvento = cal.get(Calendar.YEAR).toString()+
                    cal.get(Calendar.MONTH).toString()+
                    cal.get(Calendar.DAY_OF_MONTH).toString()+
                    cal.get(Calendar.HOUR).toString()+
                    cal.get(Calendar.MINUTE).toString()+
                    cal.get(Calendar.SECOND).toString()+
                    cal.get(Calendar.MILLISECOND).toString()

            val baseRemota = FirebaseFirestore.getInstance()
            var usuario = FirebaseAuth.getInstance().currentUser?.email.toString()

            val datos = hashMapOf(
                "nombre_evento" to nombreEvento,
                "owner" to usuario,
                "descripcion" to binding.nombreEvento.text.toString(),
                "estado" to 1,
                "visible" to 1
            )

            baseRemota.collection("eventos")
                .add(datos)
                .addOnSuccessListener {
                    btncerrar = true
                    btnocultar = false
                    btnesImg = true
                    otraVentana(nombreEvento)
                }
                .addOnFailureListener {
                    AlertDialog.Builder(this).setMessage(it.message).show()
                }
        }

        binding.entrar.setOnClickListener {
            if(binding.numEvento.text.toString() == ""){
                AlertDialog.Builder(this).setMessage("Favor de ingresar un número de evento")
                    .show()
            }else{
                var res = 0
                val baseRemota = FirebaseFirestore.getInstance()
                 baseRemota.collection("eventos").whereEqualTo("nombre_evento",binding.numEvento.text.toString())
                    .get()
                    .addOnSuccessListener {
                        for (documento in it){
                            res+=1
                            estado = documento.getLong("estado").toString().toInt()
                            visible = documento.getLong("visible").toString().toInt()
                            owner = documento.getString("owner").toString()
                        }
                        if (res>0){
                            if(visible == 0){
                                var usuario = FirebaseAuth.getInstance().currentUser?.email.toString()
                                if (usuario == owner){
                                    btncerrar = false
                                    btnocultar = false
                                    btnesImg = false
                                    otraVentana(binding.numEvento.text.toString())
                                }else{
                                    AlertDialog.Builder(this).setMessage("El evento ya no se encuentra disponible")
                                        .show()
                                }
                                return@addOnSuccessListener
                            }
                            if(estado == 0){
                                var usuario = FirebaseAuth.getInstance().currentUser?.email.toString()
                                if (usuario == owner){
                                    btncerrar = false
                                    btnocultar = true
                                    btnesImg = false
                                    otraVentana(binding.numEvento.text.toString())
                                }else{
                                    btncerrar = false
                                    btnocultar = false
                                    btnesImg = false
                                    otraVentana(binding.numEvento.text.toString())
                                }
                                return@addOnSuccessListener
                            }
                            if(visible == 1 && estado ==1){
                                var usuario = FirebaseAuth.getInstance().currentUser?.email.toString()
                                if (usuario == owner){
                                    btncerrar = true
                                    btnocultar = false
                                    btnesImg = true
                                    otraVentana(binding.numEvento.text.toString())
                                }else{
                                    btncerrar = false
                                    btnocultar = false
                                    btnesImg = true
                                    otraVentana(binding.numEvento.text.toString())
                                }
                                return@addOnSuccessListener
                            }



                        }else{
                            AlertDialog.Builder(this).setMessage("Numero de evento no encontrado")
                                .show()
                        }
                    }
                    .addOnFailureListener {
                    }
            }
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

    private fun otraVentana(numero: String) {
        val otraVentana = Intent(this, MainActivity2::class.java)
        otraVentana.putExtra("evento",numero)
        otraVentana.putExtra("btncerrar",btncerrar)
        otraVentana.putExtra("btnocultar",btnocultar)
        otraVentana.putExtra("btnesImg",btnesImg)
        startActivity(otraVentana)
        finish()
    }

}