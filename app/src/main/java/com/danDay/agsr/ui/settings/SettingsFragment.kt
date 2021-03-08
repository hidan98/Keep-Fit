package com.danDay.agsr.ui.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.danDay.agsr.R
import com.google.android.material.bottomnavigation.BottomNavigationView


class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

        val context = preferenceManager.context
        val screen = preferenceManager.createPreferenceScreen(context)


        val switchGoal = SwitchPreferenceCompat(context).apply {
            key = "Goal"
            title = "Goal editing"
            summary = "Allow the editing of goals"

        }

        val switchHistory = SwitchPreferenceCompat(context).apply {
            key = "HistorySwitch"
            title = "Record History"
            summary = "Allow the recording of history"
        }


        screen.addPreference(switchGoal)
        screen.addPreference(switchHistory)
        preferenceScreen = screen
    }

}