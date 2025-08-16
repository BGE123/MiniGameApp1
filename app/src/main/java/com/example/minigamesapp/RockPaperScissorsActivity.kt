package com.example.minigamesapp

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

class RockPaperScissorsActivity : AppCompatActivity() {

    private lateinit var scoreText: TextView
    private lateinit var resultText: TextView
    private lateinit var youChoiceText: TextView
    private lateinit var compChoiceText: TextView

    private var scoreYou = 0
    private var scoreComp = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rock_paper_scissors)

        scoreText = findViewById(R.id.scoreText)
        resultText = findViewById(R.id.resultText)
        youChoiceText = findViewById(R.id.youChoiceText)
        compChoiceText = findViewById(R.id.compChoiceText)

        val rockBtn = findViewById<Button>(R.id.rockButton)
        val paperBtn = findViewById<Button>(R.id.paperButton)
        val scissorsBtn = findViewById<Button>(R.id.scissorsButton)
        val resetBtn = findViewById<Button>(R.id.rpsResetButton)
        val icon = findViewById<ImageView>(R.id.rpsIcon)

        // Optional: set icon programmatically if you want
        // icon.setImageResource(R.drawable.ic_rps)

        rockBtn.setOnClickListener { playRound("rock") }
        paperBtn.setOnClickListener { playRound("paper") }
        scissorsBtn.setOnClickListener { playRound("scissors") }
        resetBtn.setOnClickListener { resetScores() }

        updateScoreText()
    }

    private fun playRound(playerKey: String) {
        val emojiMap = mapOf("rock" to "✊", "paper" to "✋", "scissors" to "✌️")
        val options = listOf("rock", "paper", "scissors")
        val computerKey = options[Random.nextInt(options.size)]

        youChoiceText.text = "You: ${emojiMap[playerKey]} ($playerKey)"
        compChoiceText.text = "CPU: ${emojiMap[computerKey]} ($computerKey)"

        when (determineWinner(playerKey, computerKey)) {
            "win" -> {
                resultText.text = "🎉 You win this round!"
                scoreYou++
            }
            "lose" -> {
                resultText.text = "😢 You lose this round."
                scoreComp++
            }
            "draw" -> {
                resultText.text = "🤝 It's a draw."
            }
        }
        updateScoreText()
    }

    private fun updateScoreText() {
        scoreText.text = "Score — You: $scoreYou  CPU: $scoreComp"
    }

    private fun resetScores() {
        scoreYou = 0
        scoreComp = 0
        youChoiceText.text = ""
        compChoiceText.text = ""
        resultText.text = "Pick Rock, Paper or Scissors"
        updateScoreText()
    }

    private fun determineWinner(player: String, computer: String): String {
        return when {
            player == computer -> "draw"
            (player == "rock" && computer == "scissors") ||
                    (player == "paper" && computer == "rock") ||
                    (player == "scissors" && computer == "paper") -> "win"
            else -> "lose"
        }
    }
}