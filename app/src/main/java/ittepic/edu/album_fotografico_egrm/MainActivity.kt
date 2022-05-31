package ittepic.edu.album_fotografico_egrm

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import ittepic.edu.album_fotografico_egrm.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if(FirebaseAuth.getInstance().currentUser != null){
            //Sesion Activa
            invocarOtraVenatna()
        }

        binding.registrar.setOnClickListener {
            val authentication = FirebaseAuth.getInstance()
            authentication.createUserWithEmailAndPassword(
                binding.correo.text.toString(),
                binding.pass.text.toString()
            ).addOnCompleteListener {
                if(it.isSuccessful){
                    binding.correo.text.clear()
                    binding.pass.text.clear()
                    Toast.makeText(this, "Se creo", Toast.LENGTH_SHORT)
                        .show()
                    authentication.signOut()
                }else{
                    AlertDialog.Builder(this)
                        .setMessage("Error no se construyó")
                        .setTitle("Atención")
                        .show()
                }
            }
        }

        binding.acerca.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Participantes")
                .setMessage("Solo yo :( / Edgar Gerardo Rojas Medina #18401193")
                .show()
        }

        binding.entrar.setOnClickListener {
            val authentication = FirebaseAuth.getInstance()

            val dialogo = ProgressDialog(this)
            dialogo.setMessage("AUTENTICANDO USUARIO/ CONTRA")
            dialogo.setCancelable(false)
            dialogo.show()

            authentication.signInWithEmailAndPassword(
                binding.correo.text.toString(),binding.pass.text.toString()
            ).addOnCompleteListener {
             dialogo.dismiss()
             if(it.isSuccessful){
                 invocarOtraVenatna()
                 return@addOnCompleteListener
             }
                AlertDialog.Builder(this)
                    .setTitle("Error")
                    .setMessage("No coincide correo/pass")
                    .show()
            }
        }

        binding.changePass.setOnClickListener {
            val authentication = FirebaseAuth.getInstance()

            val dialogo = ProgressDialog(this)
            dialogo.setCancelable(false)
            dialogo.setMessage("SOLICITANDO RECUPERACION")
            dialogo.show()

            authentication.sendPasswordResetEmail(
                binding.correo.text.toString()
            ).addOnSuccessListener {
                AlertDialog.Builder(this)
                    .setMessage("Se envio el Correo, revisa tu bandeja de entrada")
                    .show()
            }
        }
    }

    private fun invocarOtraVenatna() {
        val otraVentana = Intent(this, MainActivity3::class.java)
        startActivity(otraVentana)
        finish()
    }
}