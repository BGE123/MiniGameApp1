package com.example.minigamesapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class RunnerActivity : AppCompatActivity() {

    private lateinit var gameView: GameView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // create view instance so we can set listener before showing
        gameView = GameView(this)
        setContentView(gameView)

        gameView.onGameOver = { score, highScore ->
            runOnUiThread {
                AlertDialog.Builder(this)
                    .setTitle("Game Over")
                    .setMessage("Score: $score\nHigh score: $highScore")
                    .setPositiveButton("Restart") { _, _ ->
                        gameView.restart()
                    }
                    .setNegativeButton("Menu") { _, _ ->
                        // go back to main menu
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }
                    .setCancelable(false)
                    .show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        gameView.resume()
    }

    override fun onPause() {
        super.onPause()
        gameView.pause()
    }
}
