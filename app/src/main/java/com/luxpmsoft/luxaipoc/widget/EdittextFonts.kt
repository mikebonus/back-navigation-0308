package com.luxpmsoft.luxaipoc.widget

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Typeface
import android.util.AttributeSet
import android.widget.EditText
import com.luxpmsoft.luxaipoc.R

@SuppressLint("AppCompatCustomView")
class EdittextFonts(context: Context?, attrs: AttributeSet) : EditText(context, attrs) {

    init {
        setCustomFont(context!!, attrs)
    }

    private fun setCustomFont(ctx: Context, attrs: AttributeSet) {
        val a = ctx.obtainStyledAttributes(attrs, R.styleable.EditTextPlus)
        val customFont = a.getString(R.styleable.EditTextPlus_editfont)
        setCustomFont(ctx, customFont)
        a.recycle()
    }

    fun setCustomFont(ctx: Context, asset: String?): Boolean {
        var typeface: Typeface? = null
        try {
            typeface = Typeface.createFromAsset(ctx.assets, "fonts/$asset")
            setTypeface(typeface)
        } catch (e: Exception) {
            return false
        }
        setTypeface(typeface)
        return true
    }
}