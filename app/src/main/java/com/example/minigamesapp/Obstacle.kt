package com.example.minigamesapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import kotlin.random.Random

class Obstacle(context: Context, private var screenX: Int, private var screenY: Int) {
    var bitmap: Bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.obstacle)
    var x: Int = screenX + Random.nextInt(200, 1000)
    var y: Int = 0
    var width: Int = 0
    var height: Int = 0
    var speed: Int = Random.nextInt(10, 20)

    init {
        // scale obstacle
        val targetHeight = (screenY * 0.12f).toInt()
        val aspect = bitmap.width.toFloat() / bitmap.height
        val targetWidth = (targetHeight * aspect).toInt()
        bitmap = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
        width = bitmap.width
        height = bitmap.height
        val ground = (screenY * 0.78f).toInt()
        y = ground - height
    }

    fun update() {
        x -= speed
    }

    fun isOffScreen() = (x + width) < 0

    fun reset(screenX: Int) {
        this.screenX = screenX
        x = screenX + Random.nextInt(200, 1000)
        speed = Random.nextInt(10, 22)
    }

    fun getCollisionShape(): Rect = Rect(x, y, x + width, y + height)
}
