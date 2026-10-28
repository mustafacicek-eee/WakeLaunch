package io.github.mustafacicek_eee.wakelaunch

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Açılış sinyallerini yakalar. LOCKED_BOOT_COMPLETED erken gelir; o anda
 * açılamazsa BOOT_COMPLETED'da tekrar denenir (açıksa yalnızca öne gelir).
 * MY_PACKAGE_REPLACED: güncellemeden sonra servisi yeniden başlatır.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.i(Launcher.TAG, "Sinyal: ${intent.action}")
        val isBoot = intent.action != Intent.ACTION_MY_PACKAGE_REPLACED
        WatchService.start(context, launchNow = isBoot && Prefs.onBoot(context), boot = true)
    }
}
