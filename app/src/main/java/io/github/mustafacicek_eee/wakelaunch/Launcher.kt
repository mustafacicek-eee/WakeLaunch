package io.github.mustafacicek_eee.wakelaunch

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log

object Launcher {
    const val TAG = "WakeLaunch"

    /** Seçili uygulamayı açar. Başarılıysa true döner. */
    fun launchTarget(ctx: Context): Boolean {
        val pkg = Prefs.targetPackage(ctx) ?: run {
            Log.w(TAG, "Hedef uygulama seçilmemiş")
            return false
        }
        val pm = ctx.packageManager
        val intent = pm.getLeanbackLaunchIntentForPackage(pkg)
            ?: pm.getLaunchIntentForPackage(pkg)
            ?: Prefs.targetActivity(ctx)?.let { act ->
                Intent(Intent.ACTION_MAIN).setComponent(ComponentName(pkg, act))
            }
        if (intent == null) {
            Log.e(TAG, "Başlatılamıyor (kaldırılmış olabilir): $pkg")
            return false
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
        return try {
            ctx.startActivity(intent)
            Log.i(TAG, "Başlatıldı: $pkg")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Başlatma hatası: $pkg", e)
            false
        }
    }
}
