package dev.trindade.robokide.ui.components.preferences

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupMenu

import androidx.annotation.NonNull

import dev.trindade.robokide.R
import dev.trindade.robokide.databinding.PreferenceBinding

class PreferencePopup @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding: PreferenceBinding = PreferenceBinding.inflate(LayoutInflater.from(context), this, true)
    val popupMenu: PopupMenu = PopupMenu(context, this)

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.PreferencePopup,
            0, 0
        ).apply {
            try {
                val title = getString(R.styleable.PreferencePopup_preferencePopupTitle) ?: ""
                val description = getString(R.styleable.PreferencePopup_preferencePopupDescription) ?: ""
                binding.preferenceTitle.text = title
                binding.preferenceDescription.text = description
            } finally {
                recycle()
            }
        }

        binding.preference.setOnClickListener { popupMenu.show() }
    }

    fun addPopupMenuItem(itemTitle: String) {
        popupMenu.menu.add(itemTitle)
    }

    fun setMenuListener(listener: PopupMenu.OnMenuItemClickListener) {
        popupMenu.setOnMenuItemClickListener(listener)
    }
}