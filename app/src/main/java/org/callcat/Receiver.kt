package org.callcat

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.InputStream
import java.security.KeyStore
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import android.telephony.TelephonyManager

class Receiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == "android.intent.action.PHONE_STATE") {
            val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
            if (state == TelephonyManager.EXTRA_STATE_OFFHOOK) {
                sendNotificationToServer(context, "Call started")
            }
        }
    }

    private fun sendNotificationToServer(context: Context?, number: String) {
        Thread {
            try {
                val client = OkHttpClient.Builder()
                    .sslSocketFactory(createSslSocketFactory(context), createTrustManager(context))
                    .build()

                // Не забудь поменять ip перед тем как выложить :)
                val url = "https://here/notify?number=$number"
                val request = Request.Builder().url(url).build()

                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    Log.d("Receiver", "Соединение успешно!")
                } else {
                    Log.e("Receiver", "Ошибка при отправке запроса!")
                }
            } catch (e: Exception) {
                Log.w("Receiver", "Ошибка при отправке запроса: ${e.message}")
                // Ошибка игнорируется, только логируем
            }
        }.start()
    }

    private fun createSslSocketFactory(context: Context?): javax.net.ssl.SSLSocketFactory {
        val certInputStream: InputStream = context?.resources?.openRawResource(R.raw.cert)
            ?: throw IllegalArgumentException("Context is null or resource not found")

        val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
        keyStore.load(null, null)
        val certFactory = java.security.cert.CertificateFactory.getInstance("X.509")
        val cert = certFactory.generateCertificate(certInputStream)
        keyStore.setCertificateEntry("server-cert", cert)

        val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        trustManagerFactory.init(keyStore)

        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, trustManagerFactory.trustManagers, java.security.SecureRandom())

        return sslContext.socketFactory
    }

    private fun createTrustManager(context: Context?): javax.net.ssl.X509TrustManager {
        val certInputStream: InputStream = context?.resources?.openRawResource(R.raw.cert)
            ?: throw IllegalArgumentException("Context is null or resource not found")

        val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
        keyStore.load(null, null)

        val certFactory = java.security.cert.CertificateFactory.getInstance("X.509")
        val cert = certFactory.generateCertificate(certInputStream)
        keyStore.setCertificateEntry("server-cert", cert)

        val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        trustManagerFactory.init(keyStore)

        return trustManagerFactory.trustManagers[0] as javax.net.ssl.X509TrustManager
    }
}
