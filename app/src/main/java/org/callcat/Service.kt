package org.callcat

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat

class Service : Service() {
    override fun onCreate() {
        // Логирование начала создания сервиса
        super.onCreate()
        Log.d("Service", "Service created")

        // Создание канала для уведомления
        val CHANNEL_ID = "callcat_foreground_service_channel"
        val channel = NotificationChannel(
            CHANNEL_ID, // Идентификатор канала
            "Foreground Service for callcat", // Название канала
            NotificationManager.IMPORTANCE_HIGH // Важность канала
        )
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)

        // Создание уведомления
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("")
            .setContentText("").build()

        // Переводим сервис в Foreground с созданным уведомлением
        startForeground(1, notification) // Первый параметр - уникальный идентификатор уведомления
        // Логирование окончания создания сервиса
        Log.d("Service", "The service has been switched to background mode")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Сервис будет продолжать работать, пока не вызовем stopService() или stopSelf()
        return START_STICKY // Это гарантирует, что сервис будет перезапущен, если он уничтожится
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null // Сервис не использует привязку
    }
}