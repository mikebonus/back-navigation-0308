package com.luxpmsoft.luxaipoc.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.widget.TextView
import com.luxpmsoft.luxaipoc.R

@SuppressLint("AppCompatCustomView")
class TextViewFonts(context: Context?, attrs: AttributeSet) : TextView(context, attrs) {

    init {
        setCustomFont(context!!, attrs)
    }
    private fun setCustomFont(ctx: Context, attrs: AttributeSet) {
        val a = ctx.obtainStyledAttributes(attrs, R.styleable.TextViewPlus)
        val customFont = a.getString(R.styleable.TextViewPlus_textfont)
        setCustomFont(ctx, customFont)
        a.recycle()
    }

    fun setCustomFont(ctx: Context, asset: String?): Boolean {
        try {
            val typeface = Typeface.createFromAsset(ctx.assets, "fonts/$asset")
            setTypeface(typeface)
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return true
    }

    fun getTypeface(ctx: Context, name: String): Typeface? {
        var typeface: Typeface? = null
        try {
            typeface = Typeface.createFromAsset(ctx.assets, "fonts/$name")
        } catch (e: Exception) {
        }
        return typeface
    }
}