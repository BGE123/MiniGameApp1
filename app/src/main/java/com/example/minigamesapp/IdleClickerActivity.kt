package com.example.minigamesapp

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class IdleClickerActivity : AppCompatActivity() {

    private var score = 0
    private var autoClicks = 0

    private lateinit var scoreText: TextView
    private lateinit var clickButton: Button
    private lateinit var upgradeButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_idle_clicker)

        scoreText = findViewById(R.id.scoreText)
        clickButton = findViewById(R.id.clickButton)
        upgradeButton = findViewById(R.id.upgradeButton)

        clickButton.setOnClickListener {
            score++
            updateScore()
        }

        upgradeButton.setOnClickListener {
            if (score >= 10) {
                score -= 10
                autoClicks++
                updateScore()
            }
        }

        // Auto-clicker loop
        Thread {
            while (true) {
                Thread.sleep(1000) // every second
                runOnUiThread {
                    score += autoClicks
                    updateScore()
                }
            }
        }.start()
    }

    private fun updateScore() {
        scoreText.text = "Score: $score (Auto: $autoClicks/s)"
    }
}
