package org.gampiot.robok.feature.res

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
import android.util.TypedValue

import androidx.annotation.AttrRes
import androidx.core.content.ContextCompat

typealias Strings = org.gampiot.robok.feature.res.R.string
typealias AndroidAttr = android.R.attr

class ResUtils(val context: Context) {
     
     fun getAttrColor(@AttrRes resId: Int): Int {
          val typedValue = TypedValue()
          context.theme.resolveAttribute(resId, typedValue, true)
          return if (typedValue.resourceId != 0) {
              ContextCompat.getColor(context, typedValue.resourceId)
          } else {
              typedValue.data
          }
     }
}

