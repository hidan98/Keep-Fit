package com.danDay.agsr.ui.settings

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.danDay.agsr.R
import com.danDay.agsr.data.HistoryDao
import com.danDay.agsr.ui.MainActivity
import com.danDay.agsr.ui.home.HomeViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment: PreferenceFragmentCompat() {

    private val viewModel: SettingsViewModel by viewModels()
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

        val context = preferenceManager.context
        val screen = preferenceManager.createPreferenceScreen(context)

        val goalCategory = PreferenceCategory(context).apply {
            key= "goal_category"
            title= "Gaol"
        }
        screen.addPreference(goalCategory)

        val switchGoal = SwitchPreferenceCompat(context).apply {
            key = "Goal"
            title = "Goal editing"
            summary = "Allow the editing of goals"
            setDefaultValue(true)

        }
        screen.addPreference(switchGoal)

        val historyCategory = PreferenceCategory(context).apply {
            key = "history_category"
            title= "History"
        }
        screen.addPreference(historyCategory)

        val switchHistory = SwitchPreferenceCompat(context).apply {
            key = "HistorySwitch"
            title = "Record History"
            summary = "Allow the recording of history"
            setDefaultValue(true)
        }
        screen.addPreference(switchHistory)

        val historyDelete= Preference(context).apply {
            key = "HistoryDelete"
            title = "Delete History"
            summary = "Warning! This will delete your entire activity history!"
            isSelectable = true
        }
        historyDelete.setOnPreferenceClickListener{
            viewModel.deleteAll()
            true
        }
        screen.addPreference(historyDelete)

        val stepCategory = PreferenceCategory(context).apply {
            key="step_category"
            title= "Steps"
        }
        screen.addPreference(stepCategory)


        val automaticStep = SwitchPreferenceCompat(context).apply {
            key="automaticStep"
            title = "Allow Automatic step counting"
            summary ="Allow the applications to automaticaly update your steps"
            setDefaultValue(false)
        }
        screen.addPreference(automaticStep)

        preferenceScreen = screen




    }

    override fun onStart() {
        super.onStart()
        //(activity as MainActivity).supportActionBar.hide()
        val navBar: BottomNavigationView = requireActivity().findViewById(R.id.bottom_bar)
        navBar.visibility = View.GONE

        //requireActivity().findViewById(R.menu.options_menu)
    }

    override fun onStop() {
        super.onStop()
        //(activity as MainActivity).supportActionBar?.show()
        val navBar: BottomNavigationView = requireActivity().findViewById(R.id.bottom_bar)
        navBar.visibility = View.VISIBLE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)


    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.findItem(R.id.settingsFragment).setVisible(false)
        super.onPrepareOptionsMenu(menu)
    }



}