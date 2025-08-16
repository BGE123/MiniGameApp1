package com.example.minigamesapp

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AlertDialog
import kotlin.math.max
import kotlin.math.min

class TicTacToeView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val gridPaint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val xPaint = Paint().apply {
        color = Color.parseColor("#1565C0") // blue
        style = Paint.Style.STROKE
        isAntiAlias = true
        strokeCap = Paint.Cap.ROUND
    }

    private val oPaint = Paint().apply {
        color = Color.parseColor("#D32F2F") // red
        style = Paint.Style.STROKE
        isAntiAlias = true
        strokeCap = Paint.Cap.ROUND
    }

    private var cellSize = 0f
    private val board = Array(3) { Array(3) { "" } }
    private var currentPlayer = "X"
    private var gameOver = false

    /**
     * Controls how much of the available width the board should occupy.
     * Range: (0.0, 1.0] ; default 0.95 -> board uses 95% of available width.
     */
    var boardScale: Float = 0.95f
        set(value) {
            field = value.coerceIn(0.5f, 1.0f)
            requestLayout()
            invalidate()
        }

    // Optional inner padding (px) inside the view around the board
    var boardPaddingPx: Int = 0
        set(value) {
            field = max(0, value)
            requestLayout()
            invalidate()
        }

    // If you call setGridThicknessDp, this will be > 0 and take precedence over the auto-proportional thickness
    private var overrideGridThicknessPx: Float = 0f

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val availableWidth = (widthSize - paddingLeft - paddingRight - boardPaddingPx * 2).coerceAtLeast(0)
        val boardDesired = (availableWidth * boardScale).toInt()
        val desired = boardDesired + paddingLeft + paddingRight + boardPaddingPx * 2
        val finalWidth = resolveSize(desired, widthMeasureSpec)
        val finalHeight = finalWidth
        setMeasuredDimension(finalWidth, finalHeight)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        val totalPaddingX = paddingLeft + paddingRight + boardPaddingPx * 2
        val usable = (w - totalPaddingX).coerceAtLeast(1)
        val boardWidth = (usable * boardScale)
        cellSize = boardWidth / 3f

        // GRID: if override value set, use it (px). Otherwise compute proportional thickness.
        gridPaint.strokeWidth = if (overrideGridThicknessPx > 0f) {
            overrideGridThicknessPx
        } else {
            // proportional to cell size, clamped
            (cellSize * 0.08f).coerceIn(dpToPx(4f), dpToPx(20f))
        }

        // X/O strokes proportional (kept thicker relative to grid)
        xPaint.strokeWidth = (cellSize * 0.14f).coerceIn(dpToPx(4f), dpToPx(30f))
        oPaint.strokeWidth = (cellSize * 0.14f).coerceIn(dpToPx(4f), dpToPx(30f))
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val totalPaddingX = paddingLeft + paddingRight + boardPaddingPx * 2
        val usable = (width - totalPaddingX).coerceAtLeast(0)
        val boardWidth = usable * boardScale
        val left = paddingLeft + boardPaddingPx + (usable - boardWidth) / 2f
        val top = paddingTop + boardPaddingPx.toFloat()

        // draw grid lines (only the inner lines, outer frame implied)
        for (i in 1 until 3) {
            val x = left + i * cellSize
            canvas.drawLine(x, top, x, top + boardWidth, gridPaint)
            val y = top + i * cellSize
            canvas.drawLine(left, y, left + boardWidth, y, gridPaint)
        }

        // draw marks
        for (r in 0 until 3) {
            for (c in 0 until 3) {
                val mark = board[r][c]
                if (mark == "X") drawX(canvas, r, c, left, top)
                else if (mark == "O") drawO(canvas, r, c, left, top)
            }
        }
    }

    private fun drawX(canvas: Canvas, row: Int, col: Int, originX: Float, originY: Float) {
        val padding = cellSize * 0.16f
        val startX = originX + col * cellSize + padding
        val startY = originY + row * cellSize + padding
        val endX = originX + (col + 1) * cellSize - padding
        val endY = originY + (row + 1) * cellSize - padding
        canvas.drawLine(startX, startY, endX, endY, xPaint)
        canvas.drawLine(startX, endY, endX, startY, xPaint)
    }

    private fun drawO(canvas: Canvas, row: Int, col: Int, originX: Float, originY: Float) {
        val padding = cellSize * 0.16f
        val cx = originX + col * cellSize + cellSize / 2f
        val cy = originY + row * cellSize + cellSize / 2f
        val radius = (cellSize / 2f) - padding
        canvas.drawCircle(cx, cy, radius, oPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN && !gameOver) {
            val totalPaddingX = paddingLeft + paddingRight + boardPaddingPx * 2
            val usable = (width - totalPaddingX).coerceAtLeast(0)
            val boardWidth = usable * boardScale
            val originX = paddingLeft + boardPaddingPx + (usable - boardWidth) / 2f
            val originY = paddingTop + boardPaddingPx.toFloat()

            val x = event.x
            val y = event.y
            val relativeX = x - originX
            val relativeY = y - originY
            val col = (relativeX / cellSize).toInt()
            val row = (relativeY / cellSize).toInt()

            if (row in 0..2 && col in 0..2 && board[row][col].isEmpty()) {
                board[row][col] = currentPlayer
                if (checkWin(currentPlayer)) {
                    gameOver = true
                    showGameOverDialog("$currentPlayer Wins!")
                } else if (isBoardFull()) {
                    gameOver = true
                    showGameOverDialog("It's a Draw!")
                } else {
                    currentPlayer = if (currentPlayer == "X") "O" else "X"
                }
                invalidate()
            }
            return true
        }
        return super.onTouchEvent(event)
    }

    private fun checkWin(player: String): Boolean {
        for (i in 0..2) {
            if ((board[i][0] == player && board[i][1] == player && board[i][2] == player) ||
                (board[0][i] == player && board[1][i] == player && board[2][i] == player)
            ) return true
        }
        if ((board[0][0] == player && board[1][1] == player && board[2][2] == player) ||
            (board[0][2] == player && board[1][1] == player && board[2][0] == player)
        ) return true
        return false
    }

    private fun isBoardFull(): Boolean {
        for (r in 0..2) for (c in 0..2) if (board[r][c].isEmpty()) return false
        return true
    }

    private fun showGameOverDialog(message: String) {
        AlertDialog.Builder(context)
            .setTitle("Game Over")
            .setMessage(message)
            .setPositiveButton("Play Again") { _, _ -> resetGame() }
            .setCancelable(false)
            .show()
    }

    private fun resetGame() {
        for (r in 0..2) for (c in 0..2) board[r][c] = ""
        currentPlayer = "X"
        gameOver = false
        invalidate()
    }

    // -------------------------
    // Helpers / API for Activity
    // -------------------------

    private fun dpToPx(dp: Float): Float =
        dp * resources.displayMetrics.density

    /**
     * Set an absolute grid thickness in dp. Pass 0 to clear override and return to proportional sizing.
     */
    fun setGridThicknessDp(dp: Float) {
        if (dp <= 0f) {
            overrideGridThicknessPx = 0f
        } else {
            overrideGridThicknessPx = dpToPx(dp)
        }
        requestLayout()
        invalidate()
    }
}
