package org.robok.engine.ui.screens.editor.components.drawer.filetree

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

import android.content.Context
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.viewinterop.AndroidView
import java.io.File
import org.robok.engine.core.filetree.interfaces.FileClickListener
import org.robok.engine.core.filetree.interfaces.FileObject
import org.robok.engine.core.filetree.model.Node
import org.robok.engine.core.filetree.provider.DefaultFileIconProvider
import org.robok.engine.core.filetree.provider.FileWrapper
import org.robok.engine.core.filetree.widget.FileTree as FileTreeView

@Composable
fun FileTree(
  path: String,
  onClick: (Node<FileObject>) -> Unit,
  modifier: Modifier = Modifier,
  state: FileTreeState,
) {
  val context = LocalContext.current
  val fileTreeFactory = remember {
    setFileTreeFactory(context = context, path = path, onClick = onClick, state = state)
  }
  AndroidView(factory = { fileTreeFactory }, modifier = modifier)
}

private fun setFileTreeFactory(
  context: Context,
  path: String,
  onClick: (Node<FileObject>) -> Unit,
  state: FileTreeState,
): FileTreeView {
  val fileObject = FileWrapper(File(path))
  val fileTree =
    FileTreeView(context).apply {
      loadFiles(fileObject)
      setOnFileClickListener(
        object : FileClickListener {
          override fun onClick(node: Node<FileObject>) {
            onClick(node)
          }
        }
      )
      setIconProvider(DefaultFileIconProvider(context))
    }
  state.fileTreeView = fileTree
  return fileTree
}

data class FileTreeState(var fileTreeView: FileTreeView? = null)

@Composable fun rememberFileTreeState() = remember { FileTreeState() }
