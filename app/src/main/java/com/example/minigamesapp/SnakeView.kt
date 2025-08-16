package com.example.minigamesapp

import android.content.Context
import android.graphics.*
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.random.Random

class SnakeView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val snakePaint = Paint().apply { color = Color.GREEN }
    private val foodPaint = Paint().apply { color = Color.RED }
    private val bgPaint = Paint().apply { color = Color.WHITE }

    // score display paint
    private var score = 0
    private val scorePaint = Paint().apply {
        color = Color.BLACK
        textSize = 60f
        isAntiAlias = true
        typeface = Typeface.DEFAULT_BOLD
    }

    // grid and snake state
    private var cellSize = 40
    private var cols = 20
    private var rows = 30
    private val snake = ArrayDeque<Pair<Int, Int>>()

    // current and next movement direction
    private var direction = Pair(1, 0)
    private var nextDirection = Pair(1, 0)

    private var food = Pair(5, 5)

    // game loop
    private val handler = Handler(Looper.getMainLooper())
    private var running = false
    private var tickDelay = 200L

    private val tickRunnable = object : Runnable {
        override fun run() {
            update()
            invalidate()
            if (running) handler.postDelayed(this, tickDelay)
        }
    }

    init {
        resetGame()
    }

    private fun resetGame() {
        snake.clear()
        snake.addFirst(Pair(5, 5))
        snake.addLast(Pair(4, 5))
        snake.addLast(Pair(3, 5))
        direction = Pair(1, 0)
        nextDirection = Pair(1, 0)
        score = 0
        spawnFood()
    }

    fun resume() {
        if (!running) {
            running = true
            handler.post(tickRunnable)
        }
    }

    fun pause() {
        running = false
        handler.removeCallbacks(tickRunnable)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        cellSize = (w.coerceAtMost(h) / 20).coerceAtLeast(20)
        cols = w / cellSize
        rows = h / cellSize
        spawnFood()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // draw food
        drawCell(canvas, food.first, food.second, foodPaint)

        // draw snake
        for ((x, y) in snake) {
            drawCell(canvas, x, y, snakePaint)
        }

        // draw score
        canvas.drawText("Score: $score", 50f, 100f, scorePaint)
    }

    private fun drawCell(canvas: Canvas, cx: Int, cy: Int, paint: Paint) {
        val left = cx * cellSize.toFloat()
        val top = cy * cellSize.toFloat()
        canvas.drawRect(left, top, left + cellSize, top + cellSize, paint)
    }

    private fun update() {
        if (snake.isEmpty()) return

        // Apply nextDirection at the start of each tick
        direction = nextDirection

        val head = snake.first()
        val newHead = Pair(head.first + direction.first, head.second + direction.second)

        val nx = (newHead.first + cols) % cols
        val ny = (newHead.second + rows) % rows
        val headWrapped = Pair(nx, ny)

        if (snake.contains(headWrapped)) {
            resetGame()
            return
        }

        snake.addFirst(headWrapped)

        if (headWrapped == food) {
            score += 10
            spawnFood()
        } else {
            snake.removeLast()
        }
    }

    private fun spawnFood() {
        if (cols <= 1 || rows <= 1) {
            food = Pair(5, 5)
            return
        }
        var fx: Int
        var fy: Int
        do {
            fx = Random.nextInt(0, cols)
            fy = Random.nextInt(0, rows)
        } while (snake.contains(Pair(fx, fy)))
        food = Pair(fx, fy)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val head = snake.first()
            val headPxX = head.first * cellSize + cellSize / 2
            val headPxY = head.second * cellSize + cellSize / 2
            val dx = event.x - headPxX
            val dy = event.y - headPxY

            val newDir = if (kotlin.math.abs(dx) > kotlin.math.abs(dy)) {
                if (dx > 0) Pair(1, 0) else Pair(-1, 0)
            } else {
                if (dy > 0) Pair(0, 1) else Pair(0, -1)
            }

            // Prevent reversing direction instantly
            if (!(newDir.first == -direction.first && newDir.second == -direction.second)) {
                nextDirection = newDir
            }

            return true
        }
        return super.onTouchEvent(event)
    }
}
