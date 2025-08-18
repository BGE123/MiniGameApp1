package com.example.minigamesapp   // ← use your real package

import android.content.res.Resources

// Convert dp to pixels
fun dp(value: Float): Float {
    return value * Resources.getSystem().displayMetrics.density
}

// Optionally, sp to px (for text sizes)
fun sp(value: Float): Float {
    return value * Resources.getSystem().displayMetrics.scaledDensity
}