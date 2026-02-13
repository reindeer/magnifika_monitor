package ru.tarandro.magnifika.monitor

import android.app.*
import android.content.Intent
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import java.io.BufferedReader
import java.io.InputStreamReader

class MonitorService : Service() {

    private var wakeLock: PowerManager.WakeLock? = null
    private var logcatThread: Thread? = null
    private var logcatProcess: Process? = null
    private var isServiceRunning = false

    companion object {
        private const val CHANNEL_ID = "MonitorServiceChannel"
        private const val NOTIFICATION_ID = 1
        const val ACTION_CLICK_UNLOCK = "ru.tarandro.magnifika.monitor.ACTION_CLICK_UNLOCK"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun handleTrigger(logLine: String) {
        val zone = try {
            logLine.split("zone:")[1].trim().split(" ")[0].toInt()
        } catch (e: Exception) {
            return
        }
        val targetZone = AppSettings.getPrefs(this).getInt(AppSettings.DEFAULT_ZONE, 1)
        if (zone == targetZone) {
            Log.d(ACTION_CLICK_UNLOCK, "send unlock event")
            val intent = Intent(this, GpioAccessibilityService::class.java)
            intent.action = ACTION_CLICK_UNLOCK
            this.startService(intent)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (isServiceRunning) {
            return START_STICKY
        }
        isServiceRunning = true

        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MonitorService::Wakelock")
        wakeLock?.acquire()

        startLogcatMonitoring()

        val pendingIntent = PendingIntent.getService(this, 0, Intent(), PendingIntent.FLAG_IMMUTABLE)

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText("Monitoring for triggers...")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(NOTIFICATION_ID, notification)
        return START_STICKY
    }

    private fun startLogcatMonitoring() {
        logcatThread = Thread {
            try {
                Runtime.getRuntime().exec("logcat -c")
                logcatProcess = Runtime.getRuntime().exec("logcat -b main -s dnake_control:I")
                val bufferedReader = BufferedReader(InputStreamReader(logcatProcess?.inputStream))
                while (!Thread.currentThread().isInterrupted) {
                    val line = bufferedReader.readLine() ?: break
                    if (line.contains("__sys_gpio::process") && line.endsWith(" 2")) {
                        handleTrigger(line)
                    }
                }
            } catch (e: Exception) {
                Log.e(ACTION_CLICK_UNLOCK, e.toString())
            }
        }
        logcatThread?.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceRunning = false
        logcatThread?.interrupt()
        logcatProcess?.destroy()
        wakeLock?.release()
    }

    override fun onBind(intent: Intent?): IBinder? = null
    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(
            CHANNEL_ID, getString(R.string.app_name), NotificationManager.IMPORTANCE_HIGH
        ).apply {
            setSound(null, null)
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(serviceChannel)
    }
}
