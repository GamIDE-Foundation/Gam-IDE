package org.robok.engine.feature.editor.languages.java.models

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
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *   along with Robok.  If not, see <https://www.gnu.org/licenses/>.
 */

import java.lang.reflect.Field
import java.lang.reflect.Method

data class MethodOrField(
  var result: String,
  var field: Field? = null,
  var method: Method? = null
) {
  constructor(result: String, field: Field) : this(result, field, null)
  constructor(result: String, method: Method) : this(result, null, method)
}