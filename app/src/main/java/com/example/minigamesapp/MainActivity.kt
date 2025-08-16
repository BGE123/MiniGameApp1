package com.example.minigamesapp

import android.app.AlertDialog
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.gridlayout.widget.GridLayout

class MainActivity : ComponentActivity() {

    private val symbols = mutableListOf(
        "🍎","🍎","🍌","🍌","🍇","🍇","🍓","🍓",
        "🥝","🥝","🍍","🍍","🍑","🍑","🍒","🍒"
    )
    private val buttons = mutableListOf<Button>()
    private var firstIndex: Int? = null
    private var secondIndex: Int? = null
    private var isBusy = false
    private val gridSize = 4

    private var score = 0
    private lateinit var scoreText: TextView
    private lateinit var timerText: TextView
    private var timer: CountDownTimer? = null
    private var timeLeft = 60

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        scoreText = findViewById(R.id.scoreText)
        timerText = findViewById(R.id.timerText)

        findViewById<Button>(R.id.resetButton).setOnClickListener { resetGame() }
        findViewById<Button>(R.id.endButton).setOnClickListener { showGameOverDialog() }

        startGame()
    }

    private fun startGame() {
        symbols.shuffle()
        buttons.clear()
        firstIndex = null
        secondIndex = null
        score = 0
        updateScore()

        val gridLayout = findViewById<GridLayout>(R.id.gridLayout)
        gridLayout.removeAllViews()
        gridLayout.rowCount = gridSize
        gridLayout.columnCount = gridSize

        for (i in symbols.indices) {
            val button = Button(this).apply {
                text = "❓"
                textSize = 24f
                gravity = Gravity.CENTER
            }

            val params = GridLayout.LayoutParams().apply {
                width = 0
                height = 0
                rowSpec = GridLayout.spec(i / gridSize, 1f)
                columnSpec = GridLayout.spec(i % gridSize, 1f)
                setMargins(8, 8, 8, 8)
            }
            button.layoutParams = params

            button.setOnClickListener {
                if (!isBusy && button.text == "❓") {
                    revealCard(i)
                }
            }

            buttons.add(button)
            gridLayout.addView(button)
        }

        startTimer()
    }

    private fun revealCard(index: Int) {
        val btn = buttons[index]
        flipCard(btn, symbols[index])
        onCardClicked(index)
    }

    private fun onCardClicked(index: Int) {
        if (firstIndex == null) {
            firstIndex = index
        } else if (secondIndex == null && index != firstIndex) {
            secondIndex = index
            checkMatch()
        }
    }

    private fun checkMatch() {
        val first = firstIndex ?: return
        val second = secondIndex ?: return

        if (symbols[first] != symbols[second]) {
            isBusy = true
            score -= 1
            updateScore()
            Handler(Looper.getMainLooper()).postDelayed({
                flipCard(buttons[first], "❓")
                flipCard(buttons[second], "❓")
                isBusy = false
            }, 1000)
        } else {
            score += 5
            updateScore()
            buttons[first].isEnabled = false
            buttons[second].isEnabled = false

            if (buttons.all { !it.isEnabled }) {
                timer?.cancel()
                showWinDialog()
            }
        }

        firstIndex = null
        secondIndex = null
    }

    private fun flipCard(button: Button, newText: String) {
        val scaleDown = ObjectAnimator.ofFloat(button, "scaleX", 1f, 0f)
        val scaleUp = ObjectAnimator.ofFloat(button, "scaleX", 0f, 1f)
        scaleDown.duration = 150
        scaleUp.duration = 150

        scaleDown.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                button.text = newText
                scaleUp.start()
            }
        })
        scaleDown.start()
    }

    private fun updateScore() {
        scoreText.text = "Score: $score"
    }

    private fun startTimer() {
        timer?.cancel()
        timeLeft = 60
        timerText.text = "Time: $timeLeft"

        timer = object : CountDownTimer(timeLeft * 1000L, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeft--
                timerText.text = "Time: $timeLeft"
            }

            override fun onFinish() {
                timerText.text = "Time: 0"
                showGameOverDialog()
            }
        }.start()
    }

    private fun disableAllButtons() {
        for (button in buttons) {
            button.isEnabled = false
        }
    }

    private fun resetGame() {
        timer?.cancel()
        startGame()
    }

    private fun showGameOverDialog() {
        timer?.cancel()
        disableAllButtons()
        AlertDialog.Builder(this)
            .setTitle("Game Over")
            .setMessage("Your score: $score")
            .setPositiveButton("OK") { _, _ -> }
            .show()
    }

    private fun showWinDialog() {
        AlertDialog.Builder(this)
            .setTitle("You Win!")
            .setMessage("Your score: $score")
            .setPositiveButton("OK") { _, _ -> }
            .show()
    }
}