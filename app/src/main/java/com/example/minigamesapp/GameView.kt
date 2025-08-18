package com.example.minigamesapp

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class GameView(context: Context) : SurfaceView(context), SurfaceHolder.Callback {

    private val thread: GameThread
    private val paint = Paint()

    // Player variables
    private var playerX = 0f
    private var playerY = 0f
    private var playerSize = dp(50f)
    private var lane = 1 // 0 = left, 1 = middle, 2 = right

    // Jump + gravity
    private var jumping = false
    private var jumpVelocity = 0f
    private val gravity = dp(1f)

    // Sliding
    private var sliding = false
    private var slideTimer = 0

    // Obstacles
    private val obstacles = mutableListOf<RectF>()
    private var obstacleSpeed = dp(10f)

    // Gesture detection
    private val gestureDetector = GestureDetector(context, GestureListener())

    // Constants
    private val SWIPE_THRESHOLD = 100

    init {
        holder.addCallback(this)
        thread = GameThread(holder, this)
        isFocusable = true
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        thread.setRunning(true)
        thread.start()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        thread.setRunning(false)
        thread.join()
    }

    override fun surfaceChanged(
        holder: SurfaceHolder,
        format: Int,
        width: Int,
        height: Int
    ) {}

    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        return true
    }

    fun update() {
        // Update jump
        if (jumping) {
            playerY += jumpVelocity
            jumpVelocity += gravity
            if (playerY >= height - playerSize - dp(50f)) {
                playerY = height - playerSize - dp(50f)
                jumping = false
            }
        }

        // Update slide
        if (sliding) {
            slideTimer--
            if (slideTimer <= 0) {
                sliding = false
            }
        }

        // Update player X by lane
        playerX = when (lane) {
            0 -> width / 6f - playerSize / 2
            1 -> width / 2f - playerSize / 2
            else -> 5 * width / 6f - playerSize / 2
        }

        // Spawn obstacles
        if (Random.nextInt(100) < 2) {
            val laneX = when (Random.nextInt(3)) {
                0 -> width / 6f - playerSize / 2
                1 -> width / 2f - playerSize / 2
                else -> 5 * width / 6f - playerSize / 2
            }
            obstacles.add(RectF(laneX, -playerSize, laneX + playerSize, 0f))
        }

        // Move obstacles
        val iterator = obstacles.iterator()
        while (iterator.hasNext()) {
            val obstacle = iterator.next()
            obstacle.top += obstacleSpeed
            obstacle.bottom += obstacleSpeed

            // Remove if off screen
            if (obstacle.top > height) {
                iterator.remove()
                continue
            }

            // Collision detection
            val playerRect = if (sliding) {
                RectF(playerX, playerY + playerSize / 2, playerX + playerSize, playerY + playerSize)
            } else {
                RectF(playerX, playerY, playerX + playerSize, playerY + playerSize)
            }

            if (RectF.intersects(playerRect, obstacle)) {
                resetGame()
            }
        }
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        canvas.drawColor(Color.WHITE)

        // Draw player
        paint.color = Color.BLUE
        if (sliding) {
            canvas.drawRect(
                playerX,
                playerY + playerSize / 2,
                playerX + playerSize,
                playerY + playerSize,
                paint
            )
        } else {
            canvas.drawRect(
                playerX,
                playerY,
                playerX + playerSize,
                playerY + playerSize,
                paint
            )
        }

        // Draw obstacles
        paint.color = Color.RED
        for (obstacle in obstacles) {
            canvas.drawRect(obstacle, paint)
        }
    }

    private fun resetGame() {
        obstacles.clear()
        lane = 1
        jumping = false
        sliding = false
        playerY = height - playerSize - dp(50f)
    }

    inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        private val SWIPE_THRESHOLD = 100

        // onDown must match the non-null signature in many SDKs
        override fun onDown(e: MotionEvent): Boolean {
            return true
        }

        // Use the signature your IDE suggested: e1 nullable, e2 non-null
        override fun onFling(
            e1: MotionEvent?,    // nullable here (check your IDE message)
            e2: MotionEvent,     // non-null here
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            // if e1 is null we can't compute a meaningful fling — ignore
            if (e1 == null) return false

            val diffX = e2.x - e1.x
            val diffY = e2.y - e1.y

            if (kotlin.math.abs(diffX) > kotlin.math.abs(diffY)) {
                // horizontal swipe -> lane change
                if (diffX > SWIPE_THRESHOLD) {
                    lane = min(2, lane + 1) // move right
                } else if (diffX < -SWIPE_THRESHOLD) {
                    lane = max(0, lane - 1) // move left
                }
            } else {
                // vertical swipe -> jump / slide
                if (diffY < -SWIPE_THRESHOLD && !jumping) {
                    jumping = true
                    jumpVelocity = -dp(18f)   // or use a literal if dp() not available
                } else if (diffY > SWIPE_THRESHOLD && !sliding) {
                    sliding = true
                    slideTimer = 30
                }
            }
            return true
        }
    }

    // helper dp function
    private fun dp(value: Float): Float {
        return value * resources.displayMetrics.density
    }
}