package com.example.apptelefonia

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val phoneNumberEditText: EditText = findViewById(R.id.phoneNumberEditText)
        val messageEditText: EditText = findViewById(R.id.messageEditText)
        val saveButton: Button = findViewById(R.id.saveButton)

        // Cargar datos guardados en SharedPreferences
        val sharedPref = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        phoneNumberEditText.setText(sharedPref.getString("saved_number", ""))
        messageEditText.setText(sharedPref.getString("saved_message", ""))

        saveButton.setOnClickListener {
            val phoneNumber = phoneNumberEditText.text.toString().trim()
            val message = messageEditText.text.toString().trim()

            if (phoneNumber.isNotEmpty() && message.isNotEmpty()) {
                // Guardar en SharedPreferences
                sharedPref.edit().apply {
                    putString("saved_number", phoneNumber)
                    putString("saved_message", message)
                    apply()
                }
                Toast.makeText(this, "Datos guardados correctamente", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Por favor, ingresa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }

        // Solicitar permisos necesarios
        requestPermissions()
    }

    private fun requestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.SEND_SMS,
            Manifest.permission.RECEIVE_BOOT_COMPLETED,
            Manifest.permission.READ_CALL_LOG
        )

        if (!permissions.all { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }) {
            ActivityCompat.requestPermissions(this, permissions, 1)
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 1) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                Toast.makeText(this, "Permisos concedidos", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Debes aceptar los permisos para que la app funcione", Toast.LENGTH_LONG).show()
            }
        }
    }
}
