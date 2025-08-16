package com.example.minigamesapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.RectF

class Player(context: Context, screenX: Int, screenY: Int) {
    var bitmap: Bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.player)
    var x = (screenX * 0.15f).toInt()
    var y = 0
    private val groundY: Int
    private var velocityY = 0f
    private val gravity = 2f
    var isOnGround = true

    init {
        // scale player to reasonable size
        val targetHeight = (screenY * 0.14f).toInt()
        val aspect = bitmap.width.toFloat() / bitmap.height
        val targetWidth = (targetHeight * aspect).toInt()
        bitmap = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)

        groundY = (screenY * 0.78f).toInt() // ground baseline
        y = groundY - bitmap.height
    }

    fun update() {
        if (!isOnGround) {
            velocityY += gravity
            y = (y + velocityY).toInt()
            if (y >= groundY - bitmap.height) {
                y = groundY - bitmap.height
                velocityY = 0f
                isOnGround = true
            }
        }
    }

    fun jump() {
        if (isOnGround) {
            velocityY = -36f
            isOnGround = false
        }
    }

    fun getCollisionShape() = android.graphics.Rect(x, y, x + bitmap.width, y + bitmap.height)
}
