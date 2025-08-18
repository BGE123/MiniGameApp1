package com.example.minigamesapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.runner.GameView

class RunnerActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(GameView(this))
    }
}
