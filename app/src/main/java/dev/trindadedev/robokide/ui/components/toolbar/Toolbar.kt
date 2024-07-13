package dev.trindadedev.robokide.ui.components.toolbar

import android.content.Context
import android.util.AttributeSet

import com.google.android.material.appbar.AppBarLayout

import dev.trindadedev.robokide.R

class Toolbar : AppBarLayout {

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        inflate(context, R.layout.robok_toolbar, this)
    }
}