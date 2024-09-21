package org.gampiot.robok.feature.treeview.util

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

import org.gampiot.robok.feature.treeview.interfaces.FileObject
import org.gampiot.robok.feature.treeview.model.Node

object Sorter {
    fun sort(root: FileObject): List<Node<FileObject>> {
        return root.listFiles()
            .sortedWith(compareBy<FileObject> { !it.isDirectory() }.thenBy { it.getName() })
            .map { Node(it) }
    }
}