package com.example.apptelefonia

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.CallLog
import android.telephony.TelephonyManager
import android.telephony.SmsManager
import android.util.Log
import androidx.core.content.ContextCompat
import android.Manifest

class CallReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)

            Log.d("CallReceiver", "Estado de llamada: $state")

            if (context != null && state == TelephonyManager.EXTRA_STATE_IDLE) {
                Log.d("CallReceiver", "Llamada finalizada.")

                // Obtener el número de la última llamada
                val lastNumber = getLastCallNumber(context)
                Log.d("CallReceiver", "Último número detectado: $lastNumber")

                if (!lastNumber.isNullOrEmpty()) {
                    checkAndSendSms(context, lastNumber)
                }
            }
        }
    }

    private fun getLastCallNumber(context: Context): String? {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            Log.e("CallReceiver", "Permiso READ_CALL_LOG no concedido.")
            return null
        }

        val cursor = context.contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            arrayOf(CallLog.Calls.NUMBER),
            null, null,
            "${CallLog.Calls.DATE} DESC" // ✅ Eliminado "LIMIT 1"
        )

        return cursor?.use {
            if (it.moveToFirst()) {
                it.getString(it.getColumnIndexOrThrow(CallLog.Calls.NUMBER))
            } else {
                null
            }
        }
    }

    private fun checkAndSendSms(context: Context, incomingNumber: String) {
        val sharedPref = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val savedNumber = sharedPref.getString("saved_number", "") ?: ""
        val savedMessage = sharedPref.getString("saved_message", "") ?: ""

        if (incomingNumber == savedNumber && savedMessage.isNotEmpty()) {
            sendSms(context, savedNumber, savedMessage)
        }
    }

    private fun sendSms(context: Context, phoneNumber: String, message: String) {
        try {
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(phoneNumber, null, message, null, null)
            Log.d("CallReceiver", "Mensaje enviado a $phoneNumber: $message")
        } catch (e: Exception) {
            Log.e("CallReceiver", "Error al enviar el SMS: ${e.message}")
        }
    }
}
