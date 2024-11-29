package org.callcat

import android.app.Activity
import android.content.Intent
import android.os.Bundle

class Activity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Запускаем сервис
        val serviceIntent = Intent(this, Service::class.java)
        startService(serviceIntent)

        // Завершаем активность
        finish()
    }
}
