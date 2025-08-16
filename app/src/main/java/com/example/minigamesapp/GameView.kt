package com.example.minigamesapp

import android.content.Context
import android.graphics.*
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import kotlin.random.Random

class GameView(context: Context) : SurfaceView(context), Runnable {

    @Volatile private var isPlaying = false
    private var thread: Thread? = null
    private val surfaceHolder: SurfaceHolder = holder
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var screenX = 0
    private var screenY = 0

    private lateinit var player: Player
    private val obstacles = mutableListOf<Obstacle>()

    private var background: Bitmap

    private var score = 0
    private var highScore = 0

    // spawning
    private var lastSpawnTime = System.currentTimeMillis()
    private var spawnInterval = Random.nextLong(900, 2200)

    // soundpool
    private var soundPool: SoundPool
    private var jumpSound = 0
    private var crashSound = 0

    // listener for activity
    var onGameOver: ((score: Int, highScore: Int) -> Unit)? = null

    init {
        val dm = resources.displayMetrics
        screenX = dm.widthPixels
        screenY = dm.heightPixels

        background = BitmapFactory.decodeResource(resources, R.drawable.background)
        background = Bitmap.createScaledBitmap(background, screenX, screenY, true)

        player = Player(context, screenX, screenY)

        // preload 1 obstacle
        obstacles.add(Obstacle(context, screenX, screenY))

        // SoundPool setup (API safe)
        soundPool = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            SoundPool.Builder()
                .setMaxStreams(4)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .build()
        } else {
            @Suppress("DEPRECATION")
            SoundPool(4, android.media.AudioManager.STREAM_MUSIC, 0)
        }
        try {
            jumpSound = soundPool.load(context, R.raw.jump, 1)
            crashSound = soundPool.load(context, R.raw.crash, 1)
        } catch (_: Exception) { /* missing resource handled at runtime */ }

        // load high score
        val prefs = context.getSharedPreferences("runner_prefs", Context.MODE_PRIVATE)
        highScore = prefs.getInt("high_score", 0)
    }

    override fun run() {
        while (isPlaying) {
            val start = System.currentTimeMillis()
            update()
            draw()
            val took = System.currentTimeMillis() - start
            val sleep = (17 - took).coerceAtLeast(2)
            try { Thread.sleep(sleep) } catch (_: InterruptedException) {}
        }
    }

    private fun update() {
        // spawn logic
        val now = System.currentTimeMillis()
        if (now - lastSpawnTime >= spawnInterval) {
            obstacles.add(Obstacle(context, screenX, screenY))
            lastSpawnTime = now
            spawnInterval = Random.nextLong(900, 2600)
            // limit count
            if (obstacles.size > 6) obstacles.removeAt(0)
        }

        player.update()

        val playerRect = player.getCollisionShape()

        val it = obstacles.iterator()
        while (it.hasNext()) {
            val ob = it.next()
            ob.update()

            // collision
            if (Rect.intersects(ob.getCollisionShape(), playerRect)) {
                // game over
                soundPool.play(crashSound, 1f, 1f, 1, 0, 1f)
                isPlaying = false
                saveHighScoreIfNeeded()
                post {
                    onGameOver?.invoke(score, highScore)
                }
                return
            }

            // off screen -> recycle and increase score
            if (ob.isOffScreen()) {
                score++
                // recycle instead of new allocation for smoother performance
                ob.reset(screenX)
            }
        }
    }

    private fun saveHighScoreIfNeeded() {
        if (score > highScore) {
            highScore = score
            val prefs = context.getSharedPreferences("runner_prefs", Context.MODE_PRIVATE)
            prefs.edit().putInt("high_score", highScore).apply()
        }
    }

    private fun draw() {
        if (!surfaceHolder.surface.isValid) return
        val canvas = surfaceHolder.lockCanvas()
        try {
            canvas.drawBitmap(background, 0f, 0f, paint)
            // draw player
            canvas.drawBitmap(player.bitmap, player.x.toFloat(), player.y.toFloat(), paint)

            // draw obstacles
            for (ob in obstacles) {
                canvas.drawBitmap(ob.bitmap, ob.x.toFloat(), ob.y.toFloat(), paint)
            }

            // HUD
            paint.color = Color.WHITE
            paint.textSize = 64f
            paint.typeface = Typeface.DEFAULT_BOLD
            canvas.drawText("Score: $score", 40f, 100f, paint)
            paint.textSize = 36f
            canvas.drawText("High: $highScore", 40f, 150f, paint)
        } finally {
            surfaceHolder.unlockCanvasAndPost(canvas)
        }
    }

    fun resume() {
        if (isPlaying) return
        isPlaying = true
        thread = Thread(this)
        thread?.start()
    }

    fun pause() {
        isPlaying = false
        try { thread?.join() } catch (_: InterruptedException) {}
        thread = null
    }

    fun restart() {
        score = 0
        obstacles.clear()
        obstacles.add(Obstacle(context, screenX, screenY))
        player = Player(context, screenX, screenY)
        lastSpawnTime = System.currentTimeMillis()
        spawnInterval = Random.nextLong(900, 2200)
        isPlaying = true
        thread = Thread(this)
        thread?.start()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            player.jump()
            try { soundPool.play(jumpSound, 1f, 1f, 1, 0, 1f) } catch (_: Exception) {}
        }
        return true
    }
}
