package org.gampiot.robok.ui.fragments.settings

import android.os.Bundle

import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.annotation.IdRes 
import androidx.fragment.app.Fragment

import com.google.android.material.transition.MaterialSharedAxis

import org.gampiot.robok.R
import org.gampiot.robok.ui.fragments.settings.editor.SettingsEditorTopFragment
import org.gampiot.robok.ui.fragments.settings.about.AboutFragment
import org.gampiot.robok.feature.res.Strings
import org.gampiot.robok.ui.fragments.settings.base.RobokSettingsFragment
import org.gampiot.robok.ui.fragments.settings.base.RobokPreferenceFragment 
import org.gampiot.robok.feature.component.terminal.RobokTerminal

class SettingsFragment(): RobokSettingsFragment(
       settingsTitle = Strings.settings_about_title, 
       fragmentCreator = { SettingsTopFragment() }
   )

class SettingsTopFragment() : RobokPreferenceFragment() {

    private lateinit var terminal: RobokTerminal

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_top, rootKey)
        terminal = RobokTerminal(requireContext())
    }
    
    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        when (preference.key) {
            "settings_editor" -> {
                openFragment(R.id.setting_fragment_container, SettingsEditorTopFragment())
                return true
            }
            "settings_about" -> {
                openFragment(R.id.setting_fragment_container, AboutFragment())
                return true
            }
        }
        return super.onPreferenceTreeClick(preference)
    }
}
