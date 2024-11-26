package org.callcat

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.IntentFilter
import android.os.Build

class Service : Service() {
    private lateinit var flightModeReceiver: Receiver

    override fun onCreate() {
        super.onCreate()

        // Создание и регистрация BroadcastReceiver для получения SMS
        flightModeReceiver = Receiver()
        val filter = IntentFilter("android.provider.Telephony.SMS_RECEIVED")
        registerReceiver(flightModeReceiver, filter)

        // Создаем NotificationChannel (необходим для Android 8.0 и выше)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "foreground_service_channel",
                "Foreground Service",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        // Создаем уведомление для Foreground
        val notification = Notification.Builder(this, "foreground_service_channel")
            .setContentTitle("SMS Receiver Service")
            .setContentText("Service is running")
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Убедитесь, что у вас есть иконка
            .build()

        // Переводим сервис в Foreground
        startForeground(1, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Отмена регистрации ресивера
        try {
            unregisterReceiver(flightModeReceiver)
        } catch (e: IllegalArgumentException) {
            // Обработка случая, если ресивер не был зарегистрирован
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null // Сервис не использует привязку
    }
}
