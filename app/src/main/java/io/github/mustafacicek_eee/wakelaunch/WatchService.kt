package io.github.mustafacicek_eee.wakelaunch

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat

/**
 * Bekleme modundan uyanmayı (ACTION_SCREEN_ON) dinler. Bu sinyal yalnızca
 * dinamik kayıtla alınabildiği için ön planda çalışan bir servis gerekir.
 * "Uyanınca aç" kapalıysa servis işini bitirip kendini durdurur.
 */
class WatchService : Service() {

    companion object {
        private const val EXTRA_LAUNCH = "launch"
        private const val EXTRA_BOOT = "boot"
        private const val CHANNEL_ID = "watch"
        private const val NOTIF_ID = 1

        fun start(ctx: Context, launchNow: Boolean, boot: Boolean = false) {
            if (!launchNow && !Prefs.onWake(ctx)) {
                ctx.stopService(Intent(ctx, WatchService::class.java))
                return
            }
            val i = Intent(ctx, WatchService::class.java)
                .putExtra(EXTRA_LAUNCH, launchNow)
                .putExtra(EXTRA_BOOT, boot)
            try {
                ContextCompat.startForegroundService(ctx, i)
            } catch (e: Exception) {
                Log.e(Launcher.TAG, "Servis başlatılamadı", e)
            }
        }
    }

    private val handler = Handler(Looper.getMainLooper())
    private var registered = false

    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context, i: Intent) {
            if (i.action == Intent.ACTION_SCREEN_ON && Prefs.onWake(c)) {
                schedule(Prefs.delayMs(c), stopAfter = false)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        goForeground()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val wake = Prefs.onWake(this)
        if (wake && !registered) {
            registerReceiver(screenReceiver, IntentFilter(Intent.ACTION_SCREEN_ON))
            registered = true
        } else if (!wake && registered) {
            unregisterReceiver(screenReceiver)
            registered = false
        }

        if (intent?.getBooleanExtra(EXTRA_LAUNCH, false) == true) {
            // Tam açılışta sistem zaten hazır: gecikme yok
            val delay = if (intent.getBooleanExtra(EXTRA_BOOT, false)) 0L else Prefs.delayMs(this)
            schedule(delay, stopAfter = !wake)
        } else if (!wake) {
            stopSelf()
        }
        return if (wake) START_STICKY else START_NOT_STICKY
    }

    private fun schedule(delayMs: Long, stopAfter: Boolean) {
        handler.removeCallbacksAndMessages(null)
        handler.postDelayed({
            Launcher.launchTarget(this)
            if (stopAfter) stopSelf()
        }, delayMs)
    }

    private fun goForeground() {
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, getString(R.string.notif_channel), NotificationManager.IMPORTANCE_MIN)
        )
        val n: Notification = Notification.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.notif_text))
            .setSmallIcon(R.drawable.ic_notif)
            .setOngoing(true)
            .build()
        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(NOTIF_ID, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(NOTIF_ID, n)
        }
    }

    override fun onDestroy() {
        if (registered) unregisterReceiver(screenReceiver)
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
