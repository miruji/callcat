package org.callcat

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.IntentFilter
import android.util.Log

class Service : Service() {
    private lateinit var callReceiver: Receiver

    override fun onCreate() {
        // Логирование начала создания сервиса
        super.onCreate()
        Log.d("Service", "Service created")

        // Создание и регистрация BroadcastReceiver для отслеживания состояния звонков
        callReceiver = Receiver()
        val filter = IntentFilter("android.intent.action.PHONE_STATE")
        registerReceiver(callReceiver, filter) // Регистрация приемника для обработки событий звонков

        // Создание NotificationChannel, необходим для Android 8.0 и выше
        val channel = NotificationChannel(
            "foreground_service_channel", // Идентификатор канала
            "Foreground Service",            // Название канала
            NotificationManager.IMPORTANCE_DEFAULT // Важность канала
        )
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel) // Создание канала уведомлений

        // Создание уведомления для перевода сервиса в Foreground
        val notification = Notification.Builder(this, "foreground_service_channel")
            .setContentTitle("Call Event Service")           // Заголовок уведомления
            .setContentText("Service is running")            // Текст уведомления
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Иконка уведомления
            .build() // Создание уведомления

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
        // Отмена регистрации ресивера
        try {
            unregisterReceiver(callReceiver)
        } catch (e: IllegalArgumentException) {
            // Обработка случая, если ресивер не был зарегистрирован
            Log.d("Service", "Destroy error")
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null // Сервис не использует привязку
    }
}