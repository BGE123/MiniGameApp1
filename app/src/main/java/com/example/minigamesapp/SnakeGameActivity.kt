package com.example.minigamesapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class SnakeGameActivity : AppCompatActivity() {

    private lateinit var gameView: SnakeView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // create the view and set it as content
        gameView = SnakeView(this)
        setContentView(gameView)
    }

    override fun onResume() {
        super.onResume()
        gameView.resume()   // start game loop
    }

    override fun onPause() {
        super.onPause()
        gameView.pause()    // stop game loop to avoid leaks
    }
}