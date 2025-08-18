package com.example.minigamesapp

import android.animation.ObjectAnimator
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class IdleClickerActivity : AppCompatActivity() {

    private var score = 0
    private var autoClicks = 0

    private lateinit var scoreText: TextView
    private lateinit var clickButton: ImageButton
    private lateinit var upgradeButton: Button
    private lateinit var resetButton: Button
    private lateinit var assetLabel: TextView

    // handler for auto-click loop
    private val handler = Handler(Looper.getMainLooper())
    private val autoClickRunnable = object : Runnable {
        override fun run() {
            if (autoClicks > 0) {
                score += autoClicks
                updateScoreDisplay()
            }
            // schedule next tick in 1 second
            handler.postDelayed(this, 1000L)
        }
    }

    // thresholds and asset mapping
    private val thresholds = listOf(0, 10, 100, 1000) // score thresholds
    private val assetRes = listOf(
        R.drawable.click_asset_1,
        R.drawable.click_asset_2,
        R.drawable.click_asset_3,
        R.drawable.click_asset_4
    )
    private val assetNames = listOf(
        "Starter Fruit",
        "Juicy Fruit",
        "Golden Fruit",
        "Legendary Fruit"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_idle_clicker)

        scoreText = findViewById(R.id.scoreText)
        clickButton = findViewById(R.id.clickButton)
        upgradeButton = findViewById(R.id.upgradeButton)
        resetButton = findViewById(R.id.resetButton)
        assetLabel = findViewById(R.id.assetLabel)

        // set initial UI
        updateScoreDisplay()
        updateUpgradeButton()
        updateAsset()

        clickButton.setOnClickListener {
            // click animation
            val anim = ObjectAnimator.ofFloat(clickButton, "scaleX", 1f, 0.9f, 1f)
            anim.duration = 180
            anim.start()
            // keep Y scale locked same as X
            ObjectAnimator.ofFloat(clickButton, "scaleY", 1f, 0.9f, 1f).apply {
                duration = 180
                start()
            }

            // reward
            score += 1
            updateScoreDisplay()
            updateAsset() // change appearance if threshold crossed
            updateUpgradeButton()
        }

        upgradeButton.setOnClickListener {
            val cost = getUpgradeCost()
            if (score >= cost) {
                score -= cost
                autoClicks += 1
                updateScoreDisplay()
                updateUpgradeButton()
            } else {
                // optional: feedback
                AlertDialog.Builder(this)
                    .setMessage("You need $cost points to buy an auto-clicker.")
                    .setPositiveButton("OK", null)
                    .show()
            }
        }

        resetButton.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Reset progress?")
                .setMessage("This will reset score and upgrades.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Reset") { _, _ ->
                    score = 0
                    autoClicks = 0
                    updateScoreDisplay()
                    updateUpgradeButton()
                    updateAsset()
                }
                .show()
        }
    }

    override fun onResume() {
        super.onResume()
        // start auto-clicker
        handler.removeCallbacks(autoClickRunnable)
        handler.postDelayed(autoClickRunnable, 1000L)
    }

    override fun onPause() {
        super.onPause()
        // stop auto-click handler to avoid leaks
        handler.removeCallbacks(autoClickRunnable)
    }

    private fun updateScoreDisplay() {
        scoreText.text = "Score: $score (Auto: $autoClicks/s)"
    }

    private fun getUpgradeCost(): Int {
        // simple escalating cost: 10 * (autoClicks+1)
        return 10 * (autoClicks + 1)
    }

    private fun updateUpgradeButton() {
        val cost = getUpgradeCost()
        upgradeButton.text = "Buy Auto (Cost: $cost)"
    }

    private fun updateAsset() {
        // pick highest threshold satisfied
        var chosenIndex = 0
        for (i in thresholds.indices) {
            if (score >= thresholds[i]) chosenIndex = i
            else break
        }
        // clamp index to available assets
        chosenIndex = chosenIndex.coerceAtMost(assetRes.size - 1)

        // set drawable and label
        clickButton.setImageResource(assetRes[chosenIndex])
        assetLabel.text = assetNames.getOrNull(chosenIndex) ?: "Item"
    }
}
