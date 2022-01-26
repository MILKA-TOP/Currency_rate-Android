package proj.stocks.fragments

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode
import androidx.preference.PreferenceFragmentCompat
import proj.stocks.MainActivity
import proj.stocks.R
import proj.stocks.util.SHARE_THEME


class SettingsFragment : PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }

    override fun onSharedPreferenceChanged(p0: SharedPreferences?, p1: String?) {
        if (p0 == null) return
        when (p1) {
            SHARE_THEME -> changeTheme(p0, p1)
        }

        val intent = Intent(activity as MainActivity, MainActivity::class.java)
        startActivity(intent)
        (activity as MainActivity).finish()
    }

    private fun changeTheme(p0: SharedPreferences, p1: String) {
        when (p0.getString(p1, "base")) {
            "base" -> setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            "light" -> setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "dark" -> setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
    }

    override fun onResume() {
        super.onResume()
        preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        preferenceManager.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        super.onPause()
    }
}