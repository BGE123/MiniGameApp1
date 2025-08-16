package com.example.minigamesapp

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class MainMenuActivity1 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu1)

        val cardMatchOption = findViewById<LinearLayout>(R.id.cardMatchOption)
        cardMatchOption.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
        val snakeTile = findViewById<LinearLayout>(R.id.snakeGameOption) // or whatever id you used
        snakeTile.setOnClickListener {
            startActivity(Intent(this, SnakeGameActivity::class.java))
        }
        val ttt = findViewById<LinearLayout>(R.id.ticTacToeOption)
        ttt.setOnClickListener {
            startActivity(Intent(this, TicTacToeActivity::class.java))
        }
        val brickBreaker = findViewById<LinearLayout>(R.id.brickBreakerOption)
        brickBreaker.setOnClickListener {
            startActivity(Intent(this, BrickBreakerActivity::class.java))
        }
        val rps = findViewById<LinearLayout>(R.id.rockPaperScissorsOption)
        rps.setOnClickListener {
            startActivity(Intent(this, RockPaperScissorsActivity::class.java))
        }
        val sudoku = findViewById<LinearLayout>(R.id.sudokuOption)
        sudoku.setOnClickListener {
            startActivity(Intent(this, SudokuActivity::class.java))
        }
        val runner = findViewById<LinearLayout>(R.id.runnerOption)
        runner.setOnClickListener {
            startActivity(Intent(this, RunnerActivity::class.java))
        }
        val idleClicker = findViewById<LinearLayout>(R.id.idleClickerOption)
        idleClicker.setOnClickListener {
            startActivity(Intent(this, IdleClickerActivity::class.java))
        }
    }
}