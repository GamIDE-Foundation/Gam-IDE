package org.gampiot.robok.feature.settings.compose.viewmodels

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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import kotlinx.coroutines.launch

import org.gampiot.robok.feature.settings.compose.repositories.AppPreferencesRepository

class AppPreferencesViewModel(
    private val repo: AppPreferencesRepository
) : ViewModel() {
     val editorTheme = repo.editorTheme
     val editorTypeface = repo.editorTypeface
     val editorIsUseWordWrap = repo.editorIsUseWordWrap
     
     fun changeEditorTheme (value: Int) {
         viewModelScope.launch {
              repo.changeEditorTheme(value)
         }
     }
     
     fun changeEditorTypeface (value: Int) {
         viewModelScope.launch {
              repo.changeEditorTypeface(value)
         }
     }
     
     fun enableEditorWordWrap (value: Boolean) {
         viewModelScope.launch {
              repo.enableEditorWordWrap(value)
         }
     }
}