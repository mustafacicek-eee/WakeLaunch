package io.github.mustafacicek_eee.wakelaunch

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.ContextCompat

/**
 * Ayarlar "cihaz korumalı" depoda tutulur; böylece açılışın erken
 * (kilitli) aşamasında da okunabilir. Hiçbir veri cihazdan çıkmaz.
 */
object Prefs {
    private const val FILE = "wakelaunch"
    private const val K_PKG = "target_package"
    private const val K_ACT = "target_activity"
    private const val K_BOOT = "on_boot"
    private const val K_WAKE = "on_wake"
    private const val K_DELAY = "delay_ms"

    /** Seçilebilir gecikmeler (ms). */
    val DELAYS = longArrayOf(0L, 500L, 1500L, 3000L)

    private fun sp(ctx: Context): SharedPreferences {
        val dp = ContextCompat.createDeviceProtectedStorageContext(ctx) ?: ctx
        return dp.getSharedPreferences(FILE, Context.MODE_PRIVATE)
    }

    fun targetPackage(ctx: Context): String? = sp(ctx).getString(K_PKG, null)
    fun targetActivity(ctx: Context): String? = sp(ctx).getString(K_ACT, null)
    fun setTarget(ctx: Context, pkg: String, activity: String?) =
        sp(ctx).edit().putString(K_PKG, pkg).putString(K_ACT, activity).apply()

    fun onBoot(ctx: Context) = sp(ctx).getBoolean(K_BOOT, true)
    fun setOnBoot(ctx: Context, v: Boolean) = sp(ctx).edit().putBoolean(K_BOOT, v).apply()

    fun onWake(ctx: Context) = sp(ctx).getBoolean(K_WAKE, true)
    fun setOnWake(ctx: Context, v: Boolean) = sp(ctx).edit().putBoolean(K_WAKE, v).apply()

    fun delayMs(ctx: Context) = sp(ctx).getLong(K_DELAY, 500L)
    fun setDelayMs(ctx: Context, v: Long) = sp(ctx).edit().putLong(K_DELAY, v).apply()
}
