package org.robok.engine.ui.activities.editor

/*
 *  This file is part of Robok © 2024.
 *
 *  Robok is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Robok is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *   along with Robok.  If not, see <https://www.gnu.org/licenses/>.
 */

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.SparseArray
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.FileProvider
import androidx.core.util.forEach
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.tabs.TabLayout
import io.github.rosemoe.sora.lang.diagnostic.DiagnosticRegion
import java.io.File
import java.util.concurrent.CompletableFuture
import kotlinx.coroutines.Runnable
import org.robok.aapt2.compiler.CompilerTask
import org.robok.antlr4.java.AntlrListener
import org.robok.engine.Drawables
import org.robok.engine.Ids
import org.robok.engine.core.utils.UniqueNameBuilder
import org.robok.engine.databinding.ActivityEditorBinding
import org.robok.engine.feature.editor.EditorListener
import org.robok.engine.feature.editor.RobokCodeEditor
import org.robok.engine.feature.treeview.interfaces.FileClickListener
import org.robok.engine.feature.treeview.interfaces.FileObject
import org.robok.engine.feature.treeview.model.Node
import org.robok.engine.feature.treeview.provider.DefaultFileIconProvider
import org.robok.engine.feature.treeview.provider.FileWrapper
import org.robok.engine.manage.project.ProjectManager
import org.robok.engine.strings.Strings
import org.robok.engine.ui.activities.base.RobokActivity
import org.robok.engine.ui.activities.editor.diagnostic.DiagnosticFragment
import org.robok.engine.ui.activities.editor.event.EditorEvent
import org.robok.engine.ui.activities.editor.logs.LogsFragment
import org.robok.engine.ui.activities.editor.viewmodel.EditorViewModel
import org.robok.engine.ui.activities.modeling.ModelingActivity
import org.robok.engine.core.components.progress.DotProgressBar
import android.view.Menu
import android.view.MenuItem
import android.view.MenuInflater

class EditorActivity :
    RobokActivity(), TabLayout.OnTabSelectedListener, CompilerTask.OnCompileResult {

    private lateinit var projectManager: ProjectManager
    private var projectPath: String? = null
    
    private var diagnosticStatusDotProgress: DotProgressBar? = null
    
    private var diagnosticStatusImage: MenuItem? = null
    private var undo: MenuItem? = null
    private lateinit var redo: MenuItem? = null

    private var _binding: ActivityEditorBinding? = null
    private val binding
        get() = _binding!!

    private val handler = Handler(Looper.getMainLooper())
    private val diagnosticTimeoutRunnable = Runnable {
        diagnosticStatusImage?.setIcon(Drawables.ic_success_24)
        diagnosticStatusDotProgress?.visibility = View.INVISIBLE
        diagnosticStatusImage.isVisible = true
    }

    private val diagnosticStandTime: Long = 800

    private lateinit var antlrListener: AntlrListener
    private val editorViewModel by viewModels<EditorViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        isEdgeToEdge = false
        super.onCreate(savedInstanceState)
        _binding = ActivityEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val extras = intent.extras
        if (extras != null) {
            projectPath = extras.getString("projectPath")
            projectManager = ProjectManager(this@EditorActivity)
            projectPath?.let { projectManager.projectPath = File(it) }
        }

        configureScreen()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        diagnosticStatusImage = menu.findItem(Ids.diagnostic_status_image)
        undo = menu.findItem(Ids.undo)
        redo = menu.findItem(Ids.redo)
        val menuDotProgress = menu.findItem(Ids.menu_dotprogress)
        val view = menuDotProgress.actionView ?: return null
        diagnosticStatusDotProgress = view.findViewById(Ids.diagnostic_status_dot_progress)
        return super.onCreateOptionsMenu(menu, inflater)
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
           Ids.diagnostic_status_image -> {
               if (binding.drawerLayout.isDrawerOpen(GravityCompat.END)) {
                   binding.drawerLayout.closeDrawer(GravityCompat.END)
               } else {
                   binding.drawerLayout.openDrawer(GravityCompat.END)
               }
               binding.tabLayout.getTabAt(1)?.select()
               true
           }
           Ids.undo -> {
               getCurrentEditor()?.undo()
               updateUndoRedo()
               true
           }
           Ids.redo -> {
               getCurrentEditor()?.redo()
               updateUndoRedo()
               true
           }
           Ids.run_button -> {
               projectManager.build(this)
           }
           else ->
           super.onOptionsItemSelected(item)
        }
    }

    override fun onTabReselected(tab: TabLayout.Tab) {
        val pm = PopupMenu(this, tab.view)
        pm.menu.add(0, 0, 0, Strings.common_word_close)
        pm.menu.add(0, 1, 0, Strings.common_word_close_others)
        pm.menu.add(0, 2, 0, Strings.common_word_close_all)

        pm.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                0 -> editorViewModel.closeFile(tab.position)
                1 -> editorViewModel.closeOthers()
                2 -> editorViewModel.closeAll()
            }
            true
        }
        pm.show()
    }

    override fun onTabSelected(tab: TabLayout.Tab) {
        editorViewModel.setCurrentFile(tab.position)
    }

    override fun onTabUnselected(tab: TabLayout.Tab) {}

    override fun onCompileSuccess(signApk: File) {
        val context = this@EditorActivity

        val apkUri: Uri =
            FileProvider.getUriForFile(context, "${context.packageName}.provider", signApk)
        val intent =
            Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            Toast.makeText(
                    context,
                    getString(Strings.warning_project_installer_not_found),
                    Toast.LENGTH_SHORT,
                )
                .show()
        }
    }

    override fun onCompileError(error: String) {}

    private fun configureScreen() {
        configureTabLayout()
        configureToolbar()
        configureDrawer()
        configureEditor()
        configureFileTree()
        configureButtons()
        updateUndoRedo()
    }

    private fun configureButtons() {
        binding.openFilesButton.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    private fun configureTabLayout() {
        binding.tabLayout.addOnTabSelectedListener(
            object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    tab?.let {
                        when (it.text) {
                            getString(Strings.text_logs) -> {
                                supportFragmentManager
                                    .beginTransaction()
                                    .replace(
                                        Ids.drawer_editor_right_fragment_container,
                                        LogsFragment(),
                                    )
                                    .commit()
                            }

                            getString(Strings.text_diagnostic) -> {
                                supportFragmentManager
                                    .beginTransaction()
                                    .replace(
                                        Ids.drawer_editor_right_fragment_container,
                                        DiagnosticFragment(),
                                    )
                                    .commit()
                            }

                            else -> {}
                        }
                    }
                }

                override fun onTabReselected(tab: TabLayout.Tab?) {}

                override fun onTabUnselected(tab: TabLayout.Tab?) {}
            }
        )
    }

    private fun configureToolbar() {
        diagnosticStatusDotProgress?.startAnimation()
        binding.toolbar.setNavigationOnClickListener {
            if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                binding.drawerLayout.openDrawer(GravityCompat.START)
            }
        }
    }

    private fun configureDrawer() {
        binding.drawerLayout.setScrimColor(Color.TRANSPARENT)
        binding.drawerLayout.setDrawerElevation(0f)
        binding.drawerLayout.addDrawerListener(
            object : DrawerLayout.DrawerListener {
                var leftDrawerOffset = 0f
                var rightDrawerOffset = 0f

                override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                    val drawerWidth = drawerView.width
                    when (drawerView.id) {
                        Ids.navigation_view_left -> {
                            leftDrawerOffset = drawerWidth * slideOffset
                            binding.content.translationX = leftDrawerOffset
                        }

                        Ids.navigation_view_right -> {
                            rightDrawerOffset = drawerWidth * slideOffset
                            binding.content.translationX = -rightDrawerOffset
                        }
                    }
                }

                override fun onDrawerOpened(drawerView: View) {}

                override fun onDrawerClosed(drawerView: View) {
                    binding.content.translationX = 0f
                    leftDrawerOffset = 0f
                    rightDrawerOffset = 0f
                }

                override fun onDrawerStateChanged(newState: Int) {}
            }
        )
    }

    private fun configureEditor() {
        binding.tabs.addOnTabSelectedListener(this)
        observeViewModel()
    }

    private fun configureFileTree() {
        val fileObject = FileWrapper(File(projectPath!!))
        binding.fileTree.loadFiles(fileObject)
        binding.fileTree.setOnFileClickListener(
            object : FileClickListener {
                override fun onClick(node: Node<FileObject>) {
                    if (node.value.isDirectory()) return
                    handleFileExtension(node)
                }
            }
        )
        binding.fileTree.setIconProvider(DefaultFileIconProvider(this))
    }

    private fun handleFileExtension(node: Node<FileObject>) {
        val fileExtension = node.value.getName().substringAfterLast(".")
        val fileToOpen = File(node.value.getAbsolutePath())
        when (fileExtension) {
            "obj" ->
                startActivity(
                    Intent(this@EditorActivity, ModelingActivity::class.java)
                ) // open 3d modeling (todo: send args)
            else -> editorViewModel.openFile(fileToOpen) // open file on editor
        }
    }

    private fun updateUndoRedo() {
        getCurrentEditor()?.let { editor ->
            redo.isEnabled = editor.isCanRedo
            undo.isEnabled = editor.isCanUndo
        }
            ?: run {
                redo.isEnabled = false
                undo.isEnabled = false
            }
    }

    private fun observeViewModel() {
        editorViewModel.editorState.observe(this) { state ->
            val index = state.currentIndex
            binding.apply {
                val tab = tabs.getTabAt(index)
                if (tab != null && !tab.isSelected) {
                    tab.select()
                }
                binding.editorContainer.displayedChild = index
            }
        }

        editorViewModel.editorEvent.observe(this) { event ->
            when (event) {
                is EditorEvent.OpenFile -> openFile(event.file)
                is EditorEvent.CloseFile -> closeFile(event.index)
                is EditorEvent.CloseOthers -> closeOthers()
                is EditorEvent.CloseAll -> closeAll()
            }
        }

        editorViewModel.files.observe(this) { openedFiles ->
            val hasOpenedFiles = openedFiles.isNotEmpty()
            binding.apply {
                tabs.isVisible = hasOpenedFiles
                noContentLayout.isVisible = !hasOpenedFiles
            }
        }
    }

    private fun openFile(file: File) {
        val openedFileIndex = editorViewModel.indexOfFile(file)
        if (openedFileIndex >= 0) {
            editorViewModel.setCurrentFile(openedFileIndex)
            return
        }

        val index = editorViewModel.fileCount
        val editor = RobokCodeEditor(this, file)

        editorViewModel.addFile(file)
        binding.apply {
            editorContainer.addView(editor)
            tabs.addTab(tabs.newTab())
            drawerLayout.closeDrawer(GravityCompat.START)
        }
        editorViewModel.setCurrentFile(index)
        configureEditorListeners(editor)
        updateTabs()
    }

    private fun configureEditorListeners(editor: RobokCodeEditor) {
        val antlrListener =
            object : AntlrListener {
                override fun onDiagnosticStatusReceive(isError: Boolean) {
                    handler.removeCallbacks(diagnosticTimeoutRunnable)
                    if (isError) {
                        diagnosticStatusImage?.setIcon(Drawables.ic_error_24)
                    } else {
                        diagnosticStatusImage?.setIcon(Drawables.ic_success_24)
                    }
                    diagnosticStatusDotProgress?.visibility = View.INVISIBLE
                    diagnosticStatusImage.isVisible = false
                }

                override fun onDiagnosticReceive(
                    line: Int,
                    positionStart: Int,
                    positionEnd: Int,
                    msg: String,
                ) {
                    editor.addDiagnosticInEditor(
                        positionStart,
                        positionEnd,
                        DiagnosticRegion.SEVERITY_ERROR,
                        msg,
                    )
                    onDiagnosticStatusReceive(true)
                }
            }
        val editorListener =
            object : EditorListener {
                override fun onEditorTextChange() {
                    updateUndoRedo()
                    
                    diagnosticStatusDotProgress?.visibility = View.VISIBLE
                    diagnosticStatusImage.isVisible = false

                    handler.removeCallbacks(diagnosticTimeoutRunnable)
                    handler.postDelayed(diagnosticTimeoutRunnable, diagnosticStandTime)
                }
            }
        editor.setAntlrListener(antlrListener)
        editor.setEditorListener(editorListener)
        editor.reload()
    }

    private fun closeFile(index: Int) {
        if (index >= 0 && index < editorViewModel.fileCount) {
            getEditorAtIndex(index)?.release()
            editorViewModel.removeFile(index)
            binding.apply {
                tabs.removeTabAt(index)
                editorContainer.removeViewAt(index)
            }
            updateTabs()
        }
    }

    private fun closeOthers() {
        if (editorViewModel.currentFileIndex >= 0) {
            val file = editorViewModel.currentFile!!
            var index: Int = 0
            while (editorViewModel.fileCount > 1) {
                val editor = getEditorAtIndex(index) ?: continue

                if (file != editor.file) {
                    closeFile(index)
                } else {
                    index = 1
                }
            }
            editorViewModel.setCurrentFile(editorViewModel.indexOfFile(file))
        }
    }

    private fun closeAll() {
        for (i in 0 until editorViewModel.fileCount) {
            getEditorAtIndex(i)?.release()
        }

        editorViewModel.removeAllFiles()
        binding.apply {
            tabs.removeAllTabs()
            tabs.requestLayout()
            editorContainer.removeAllViews()
        }
    }

    private fun saveFile(index: Int) {
        /*lifecycleScope.launch {

        }*/
    }

    fun getCurrentEditor(): RobokCodeEditor? {
        return if (editorViewModel.currentFileIndex >= 0) {
            getEditorAtIndex(editorViewModel.currentFileIndex)
        } else null
    }

    private fun getEditorAtIndex(index: Int): RobokCodeEditor? {
        return binding.editorContainer.getChildAt(index) as? RobokCodeEditor
    }

    private fun updateTabs() {
        CompletableFuture.supplyAsync({
                val files = editorViewModel.openedFiles
                val dupliCount = mutableMapOf<String, Int>()
                val names = SparseArray<String>()
                val nameBuilder = UniqueNameBuilder<File>("", File.separator)

                files.forEach {
                    dupliCount[it.name] = (dupliCount[it.name] ?: 0) + 1
                    nameBuilder.addPath(it, it.path)
                }

                for (i in 0 until binding.tabs.tabCount) {
                    val file = files[i]
                    val count = dupliCount[file.name] ?: 0
                    val isModified = getEditorAtIndex(i)?.isModified() ?: false
                    val name = if (count > 1) nameBuilder.getShortPath(file) else file.name
                    names[i] = if (isModified) "*$name" else name
                }
                names
            })
            .whenComplete { result, error ->
                if (result == null || error != null) {
                    return@whenComplete
                }

                runOnUiThread {
                    result.forEach { index, name -> binding.tabs.getTabAt(index)?.text = name }
                }
            }
    }
}
