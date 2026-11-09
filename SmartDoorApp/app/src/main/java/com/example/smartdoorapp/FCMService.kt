package com.example.smartdoorapp

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FCMService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val titulo = remoteMessage.notification?.title ?: "ALERTA DE SEGURIDAD"
        val cuerpo = remoteMessage.notification?.body ?: "Se detectó una anomalía en el sistema de incendios."

        val notificationHelper = NotificationHelper(applicationContext)
        notificationHelper.enviarAlertaIncendio(titulo, cuerpo)
    }
}