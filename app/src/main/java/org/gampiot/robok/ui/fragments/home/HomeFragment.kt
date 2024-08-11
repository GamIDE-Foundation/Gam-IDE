package org.gampiot.robok.ui.fragments.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.content.Intent

import androidx.fragment.app.Fragment

import com.google.android.material.transition.MaterialSharedAxis
import com.google.android.material.dialog.MaterialAlertDialogBuilder

import org.gampiot.robok.R
import org.gampiot.robok.databinding.FragmentHomeBinding
import org.gampiot.robok.feature.util.getDefaultPathFile
import org.gampiot.robok.feature.util.base.RobokFragment
import org.gampiot.robok.feature.res.Strings
import org.gampiot.robok.feature.settings.ui.fragment.SettingsFragment
import org.gampiot.robok.feature.terminal.TerminalActivity
import org.gampiot.robok.ui.fragments.project.template.ProjectTemplatesFragment
import org.gampiot.robok.ui.fragments.project.create.CreateProjectFragment
import org.gampiot.robok.ui.fragments.editor.EditorFragment
import org.gampiot.robok.ui.fragments.about.AboutFragment

import dev.trindadedev.lib.filepicker.model.DialogConfigs
import dev.trindadedev.lib.filepicker.model.DialogProperties

class HomeFragment (private val tansitionAxis : Int = MaterialSharedAxis.X) : RobokFragment(tansitionAxis) {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setFragmentLayoutResId(R.id.fragment_container)
        binding.createProject.setOnClickListener {
            openFragment(ProjectTemplatesFragment(MaterialSharedAxis.X))
        }
        
        binding.openProject.setOnClickListener {
            selectFolder()
        }
        
        binding.openSettings.setOnClickListener {
            openFragment(SettingsFragment(MaterialSharedAxis.X, R.id.fragment_container))
        }
        
        binding.openTerminal.setOnClickListener {
            startActivity(Intent(requireContext(), TerminalActivity::class.java))
        }
        
        binding.openEditor.setOnClickListener {
            openFragment(EditorFragment(MaterialSharedAxis.X))
        }
        
        binding.openAbout.setOnClickListener {
            openFragment(AboutFragment(MaterialSharedAxis.X))
        }
    }
    
    private fun selectFolder() {
        val properties = DialogProperties().apply {
             root = getDefaultPathFile()
             selection_mode = DialogConfigs.SINGLE_MODE
             selection_type = DialogConfigs.DIR_SELECT
        }
        
        val filePickerDialog = FilePicker(requireContext(), properties).apply {
             setTitle(getString(Strings.title_select_folder))
             setDialogSelectionListener { files ->
                  if (files != null && files.isNotEmpty()) {
                        val fileNames = files.joinToString("\n") { file ->
                             file.substringAfterLast('/')
                             
                        }
                  }
             }
        }
        filePickerDialog.show()
    }
    
    private fun onFolderSelect() {
        /* example to open the editor:
        * val fragment = EditorFragment.newInstance(path)
        * openFragment(fragment)
        */
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}