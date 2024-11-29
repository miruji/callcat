package org.callcat

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class Receiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Получаем состояние звонка
        val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE) ?: return

        if (TelephonyManager.EXTRA_STATE_RINGING == state) {
            // Получаем экземпляр TelephonyManager
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            Log.d("Receiver", "Incoming call")

            // Регистрация TelephonyCallback для отслеживания состояния звонка
            telephonyManager.registerTelephonyCallback(
                context.mainExecutor,
                object : TelephonyCallback(), TelephonyCallback.CallStateListener {
                    override fun onCallStateChanged(state: Int) {
                        Log.d("Receiver", "onCallStateChanged: $state")
                        if (state == TelephonyManager.CALL_STATE_RINGING) {
                            // Для получения номера звонящего, используем метод getLine1Number
                            sendNotificationToServer();
                        }
                    }
                })
        }
    }

    private fun sendNotificationToServer() {
        Thread {
            try {
                // Создаем X.509 TrustManager, который доверяет всем сертификатам
                val trustManager = object : X509TrustManager {
                    override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                    override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                    override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                }

                // Создаем SSLContext с нашим TrustManager
                val sslContext = SSLContext.getInstance("TLS")
                sslContext.init(null, arrayOf<TrustManager>(trustManager), SecureRandom())

                // Создаем OkHttpClient с этим SSLContext
                val client = OkHttpClient.Builder()
                    .sslSocketFactory(sslContext.socketFactory, trustManager)
                    .hostnameVerifier { _, _ -> true }  // Игнорируем проверку хоста
                    .build()

                // Формируем URL для уведомления с номером и именем
                val url = "https://here:here/call" // Оставляем только URL без curl и -k
                val request = Request.Builder().url(url).build()

                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    Log.d("Receiver", "[OK]")
                } else {
                    Log.e("Receiver", "[ERROR] ${response.code}")
                }
            } catch (e: Exception) {
                Log.w("Receiver", "[ERROR] ${e.message}")
                // Ошибка игнорируется, только логируем
            }
        }.start()
    }
}
