package io.github.mustafacicek_eee.wakelaunch

import android.app.Activity
import android.content.Intent
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast

/** TV'de başlatılabilen uygulamaları listeler; seçileni kaydeder. */
class AppPickerActivity : Activity() {

    private data class AppEntry(
        val label: String,
        val pkg: String,
        val activity: String,
        val icon: Drawable
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val apps = loadApps()
        val list = ListView(this).apply {
            setBackgroundColor(0xFF0B1220.toInt())
            dividerHeight = 0
            itemsCanFocus = false
            // Kumandayla gezinirken seçili satır belirgin olsun
            selector = getDrawable(R.drawable.bg_row)
            setDrawSelectorOnTop(false)
            setPadding(32, 24, 32, 24)
            clipToPadding = false
            adapter = Adapter(apps)
            setOnItemClickListener { _, _, pos, _ ->
                val a = apps[pos]
                Prefs.setTarget(this@AppPickerActivity, a.pkg, a.activity)
                Toast.makeText(
                    this@AppPickerActivity,
                    getString(R.string.ready, a.label),
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
        }
        setContentView(list)
        list.requestFocus()
    }

    private fun loadApps(): List<AppEntry> {
        val pm = packageManager
        val found = LinkedHashMap<String, ResolveInfo>()
        for (cat in listOf(Intent.CATEGORY_LEANBACK_LAUNCHER, Intent.CATEGORY_LAUNCHER)) {
            val q = Intent(Intent.ACTION_MAIN).addCategory(cat)
            for (ri in pm.queryIntentActivities(q, 0)) {
                val p = ri.activityInfo.packageName
                if (p != packageName && p !in found) found[p] = ri
            }
        }
        return found.values.map { ri ->
            AppEntry(
                label = ri.loadLabel(pm).toString(),
                pkg = ri.activityInfo.packageName,
                activity = ri.activityInfo.name,
                icon = ri.loadIcon(pm)
            )
        }.sortedBy { it.label.lowercase() }
    }

    private inner class Adapter(private val items: List<AppEntry>) : BaseAdapter() {
        override fun getCount() = items.size
        override fun getItem(position: Int) = items[position]
        override fun getItemId(position: Int) = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val v = convertView ?: LayoutInflater.from(parent.context)
                .inflate(R.layout.item_app, parent, false)
            val a = items[position]
            v.findViewById<ImageView>(R.id.icon).setImageDrawable(a.icon)
            v.findViewById<TextView>(R.id.label).text = a.label
            v.findViewById<TextView>(R.id.pkg).text = a.pkg
            return v
        }
    }
}
