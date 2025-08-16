package com.example.minigamesapp

import android.content.Context
import android.graphics.Typeface
import android.view.Gravity
import android.widget.TextView

class Tile(context: Context) : TextView(context) {

    var value: Int = 0
        set(v) {
            field = v
            text = if (v > 0) v.toString() else ""
            setBackgroundColor(getTileColor(v))
        }

    init {
        textSize = 32f
        typeface = Typeface.DEFAULT_BOLD
        gravity = Gravity.CENTER
        setBackgroundColor(getTileColor(0))
    }

    private fun getTileColor(value: Int): Int {
        return when (value) {
            0 -> 0xFFCCC0B3.toInt()
            2 -> 0xFFEEE4DA.toInt()
            4 -> 0xFFEDE0C8.toInt()
            8 -> 0xFFF2B179.toInt()
            16 -> 0xFFF59563.toInt()
            32 -> 0xFFF67C5F.toInt()
            64 -> 0xFFF65E3B.toInt()
            128 -> 0xFFEDCF72.toInt()
            256 -> 0xFFEDCC61.toInt()
            512 -> 0xFFEDC850.toInt()
            1024 -> 0xFFEDC53F.toInt()
            2048 -> 0xFFEDC22E.toInt()
            else -> 0xFF3C3A32.toInt()
        }
    }
}