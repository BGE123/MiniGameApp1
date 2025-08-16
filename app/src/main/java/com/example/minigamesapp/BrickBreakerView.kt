package com.example.minigamesapp

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import kotlin.math.abs
import kotlin.math.min

class BrickBreakerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : SurfaceView(context, attrs), SurfaceHolder.Callback, Runnable {

    private var gameThread: Thread? = null
    @Volatile private var running = false

    // screen
    private var sw = 0f
    private var sh = 0f

    // paints
    private val bg = Paint().apply { color = Color.WHITE }
    private val ballPaint = Paint().apply { color = Color.RED; isAntiAlias = true }
    private val paddlePaint = Paint().apply { color = Color.BLACK }
    private val brickPaint = Paint().apply { color = Color.rgb(30, 144, 255) }
    private val hud = Paint().apply {
        color = Color.BLACK; textSize = 48f; isAntiAlias = true; typeface = Typeface.DEFAULT_BOLD
    }

    // ball
    private var bx = 0f
    private var by = 0f
    private var br = 18f
    private var vx = 6f
    private var vy = -7f

    // paddle
    private var pw = 220f
    private var ph = 26f
    private var px = 0f
    private var py = 0f

    // bricks
    data class Brick(var rect: RectF, var alive: Boolean = true)
    private val bricks = mutableListOf<Brick>()
    private var rows = 5
    private var cols = 7
    private var brickGap = 8f

    // state
    private var score = 0
    private var lives = 3
    private var gameOver = false

    init {
        holder.addCallback(this)
    }

    // --- Surface lifecycle ---
    override fun surfaceCreated(holder: SurfaceHolder) {
        sw = width.toFloat()
        sh = height.toFloat()
        setupLevel()
        resume()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        pause()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) = Unit

    // --- Thread / loop ---
    override fun run() {
        while (running) {
            val start = System.nanoTime()
            update()
            render()
            val tookMs = (System.nanoTime() - start) / 1_000_000
            val sleep = (16 - tookMs).coerceAtLeast(0L)
            try { Thread.sleep(sleep) } catch (_: InterruptedException) {}
        }
    }

    fun resume() {
        if (running) return
        running = true
        gameThread = Thread(this, "BrickBreakerThread").also { it.start() }
    }

    fun pause() {
        running = false
        gameThread?.join(200)
        gameThread = null
    }

    // --- Setup ---
    private fun setupLevel() {
        // paddle
        py = sh - 120f
        px = sw / 2f - pw / 2f

        // ball
        bx = sw / 2f
        by = sh * 0.65f
        vx = if (vx == 0f) 5f else vx
        vy = -abs(vy.takeIf { it != 0f } ?: 7f)

        buildBricks()
    }

    private fun buildBricks() {
        bricks.clear()
        val top = 120f
        val totalGapW = (cols + 1) * brickGap
        val bw = (sw - totalGapW) / cols
        val bh = 40f
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val left = brickGap + c * (bw + brickGap)
                val topY = top + r * (bh + brickGap)
                bricks += Brick(RectF(left, topY, left + bw, topY + bh))
            }
        }
    }

    // --- Update ---
    private fun update() {
        if (gameOver) return

        // move ball
        bx += vx
        by += vy

        // walls
        if (bx - br < 0) { bx = br; vx = -vx }
        if (bx + br > sw) { bx = sw - br; vx = -vx }
        if (by - br < 0) { by = br; vy = -vy }

        // bottom (lose life)
        if (by - br > sh) {
            lives--
            if (lives <= 0) {
                gameOver = true
            } else {
                bx = sw / 2f; by = sh * 0.65f; vx = 5f; vy = -7f
            }
        }

        // paddle collision (only when falling)
        val paddle = RectF(px, py, px + pw, py + ph)
        val ballRect = RectF(bx - br, by - br, bx + br, by + br)
        if (vy > 0 && RectF.intersects(paddle, ballRect)) {
            vy = -abs(vy)
            // add a little "spin" based on hit location
            val hitNorm = ((bx - px) / pw - 0.5f) // -0.5 .. +0.5
            vx += hitNorm * 4f
        }

        // brick collisions
        for (b in bricks) {
            if (!b.alive) continue
            if (RectF.intersects(b.rect, ballRect)) {
                b.alive = false
                score += 10

                // simple side resolution
                val overlapLeft = ballRect.right - b.rect.left
                val overlapRight = b.rect.right - ballRect.left
                val overlapTop = ballRect.bottom - b.rect.top
                val overlapBottom = b.rect.bottom - ballRect.top
                val minOverlap = min(min(overlapLeft, overlapRight), min(overlapTop, overlapBottom))
                when (minOverlap) {
                    overlapLeft -> vx = -abs(vx)
                    overlapRight -> vx = abs(vx)
                    overlapTop -> vy = -abs(vy)
                    else -> vy = abs(vy)
                }
                break
            }
        }

        // next level if cleared
        if (bricks.none { it.alive }) {
            rows = (rows + 1).coerceAtMost(8)
            vy -= 1f // slightly faster
            setupLevel()
        }
    }

    // --- Render ---
    private fun render() {
        val canvas = holder.lockCanvas() ?: return
        try {
            canvas.drawColor(Color.WHITE)

            // bricks
            bricks.forEach { if (it.alive) canvas.drawRect(it.rect, brickPaint) }

            // paddle
            canvas.drawRect(px, py, px + pw, py + ph, paddlePaint)

            // ball
            canvas.drawCircle(bx, by, br, ballPaint)

            // HUD
            canvas.drawText("Score: $score   Lives: $lives", 24f, 60f, hud)

            if (gameOver) {
                hud.textSize = 72f
                val text = "Game Over"
                val w = hud.measureText(text)
                canvas.drawText(text, sw / 2f - w / 2f, sh / 2f, hud)
                hud.textSize = 48f
                canvas.drawText("Tap to restart", sw / 2f - 150f, sh / 2f + 70f, hud)
            }
        } finally {
            holder.unlockCanvasAndPost(canvas)
        }
    }

    // --- Input ---
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (gameOver && event.action == MotionEvent.ACTION_DOWN) {
            score = 0; lives = 3; gameOver = false; setupLevel(); return true
        }
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                px = (event.x - pw / 2f).coerceIn(0f, sw - pw)
                return true
            }
        }
        return super.onTouchEvent(event)
    }
}
