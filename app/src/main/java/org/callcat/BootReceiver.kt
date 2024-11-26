package org.callcat

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.i("BootReceiver", "Service started after reboot")
            // Запускаем сервис после загрузки устройства
            val serviceIntent = Intent(context, Service::class.java)
            context?.startService(serviceIntent)
        }
    }
}
