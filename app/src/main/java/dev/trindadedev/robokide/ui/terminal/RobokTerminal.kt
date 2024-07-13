package dev.trindadedev.robokide.ui.terminal

import android.content.Context
import android.view.LayoutInflater
import android.widget.LinearLayout

import androidx.appcompat.app.AppCompatDelegate

import com.google.android.material.bottomsheet.BottomSheetDialog

import dev.trindadedev.robokide.R
import dev.trindadedev.robokide.ui.components.log.Log

class RobokTerminal(context: Context) : BottomSheetDialog(context) {

    public val terminal: LinearLayout
    private val bottomSheetView = LayoutInflater.from(context).inflate(R.layout.dialog_terminal, null)
    
    init {
        setContentView(bottomSheetView)
        setCancelable(true)
        terminal = bottomSheetView.findViewById(R.id.terminal)
    }
    
    fun addLog(value: String) {
        val log = Log(context, value)
        terminal.addView(log)
    }

    private val isDarkMode: Boolean
        get() = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES

    companion object {
        const val ERROR_COLOR = "#FF0000"
        const val WARNING_COLOR = "#FFC400"
        const val SUCCESS_COLOR = "#198754"
    }
}