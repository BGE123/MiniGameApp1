package com.example.minigamesapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class TicTacToeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tic_tac_toe)
        supportActionBar?.title = "Tic Tac Toe"
    }
}