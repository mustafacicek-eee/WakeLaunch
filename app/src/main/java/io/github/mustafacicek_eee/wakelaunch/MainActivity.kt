package io.github.mustafacicek_eee.wakelaunch

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private lateinit var targetIcon: ImageView
    private lateinit var targetName: TextView
    private lateinit var swBoot: Switch
    private lateinit var swWake: Switch
    private lateinit var btnDelay: Button
    private lateinit var overlayStatus: TextView
    private lateinit var overlayHelp: TextView
    private lateinit var btnOverlay: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        targetIcon = findViewById(R.id.targetIcon)
        targetName = findViewById(R.id.targetName)
        swBoot = findViewById(R.id.swBoot)
        swWake = findViewById(R.id.swWake)
        btnDelay = findViewById(R.id.btnDelay)
        overlayStatus = findViewById(R.id.overlayStatus)
        overlayHelp = findViewById(R.id.overlayHelp)
        btnOverlay = findViewById(R.id.btnOverlay)

        findViewById<Button>(R.id.btnPick).setOnClickListener {
            startActivity(Intent(this, AppPickerActivity::class.java))
        }

        swBoot.setOnCheckedChangeListener { _, checked -> Prefs.setOnBoot(this, checked) }
        swWake.setOnCheckedChangeListener { _, checked ->
            Prefs.setOnWake(this, checked)
            WatchService.start(this, launchNow = false)
        }

        btnDelay.setOnClickListener {
            val all = Prefs.DELAYS
            val idx = all.indexOf(Prefs.delayMs(this))
            Prefs.setDelayMs(this, all[(idx + 1) % all.size])
            refresh()
        }

        btnOverlay.setOnClickListener { openOverlaySettings() }

        findViewById<Button>(R.id.btnTest).setOnClickListener {
            if (Prefs.targetPackage(this) == null) {
                Toast.makeText(this, R.string.pick_first, Toast.LENGTH_SHORT).show()
            } else if (!Launcher.launchTarget(this)) {
                Toast.makeText(this, R.string.launch_failed, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refresh()
        // Uygulama açıldığında servisi (gerekiyorsa) başlat
        WatchService.start(this, launchNow = false)
    }

    private fun refresh() {
        val pkg = Prefs.targetPackage(this)
        if (pkg == null) {
            targetIcon.setImageDrawable(null)
            targetName.setText(R.string.no_app)
        } else {
            try {
                val ai = packageManager.getApplicationInfo(pkg, 0)
                targetIcon.setImageDrawable(packageManager.getApplicationIcon(ai))
                targetName.text = packageManager.getApplicationLabel(ai)
            } catch (e: PackageManager.NameNotFoundException) {
                targetIcon.setImageDrawable(null)
                targetName.text = getString(R.string.app_missing, pkg)
            }
        }

        swBoot.isChecked = Prefs.onBoot(this)
        swWake.isChecked = Prefs.onWake(this)

        val sec = Prefs.delayMs(this) / 1000.0
        btnDelay.text = getString(R.string.delay_label, formatSec(sec))

        val ok = Settings.canDrawOverlays(this)
        overlayStatus.setText(if (ok) R.string.overlay_ok else R.string.overlay_missing)
        overlayStatus.setTextColor(if (ok) 0xFF4ADE80.toInt() else 0xFFFBBF24.toInt())
        overlayHelp.text = if (ok) "" else getString(R.string.overlay_help, packageName)
        btnOverlay.visibility = if (ok) View.GONE else View.VISIBLE
    }

    /** Dile uygun ondalık: TR "0,5", EN "0.5" */
    private fun formatSec(s: Double): String =
        java.text.NumberFormat.getNumberInstance().apply { maximumFractionDigits = 1 }.format(s)

    private fun openOverlaySettings() {
        val intents = listOf(
            Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")),
            Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
        )
        for (i in intents) {
            try {
                startActivity(i); return
            } catch (e: ActivityNotFoundException) {
            } catch (e: SecurityException) {
            }
        }
        Toast.makeText(this, R.string.overlay_settings_missing, Toast.LENGTH_LONG).show()
    }
}
