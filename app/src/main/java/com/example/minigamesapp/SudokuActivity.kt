package com.example.minigamesapp

import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.util.TypedValue
import android.view.Gravity
import android.widget.EditText
import android.widget.GridLayout
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.min

class SudokuActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sudoku)

        val sudokuGame = SudokuGame()
        // easier puzzle: removalCount = 30 (change if you want harder)
        val puzzle = sudokuGame.generatePuzzle(removalCount = 30)

        val gridLayout = findViewById<GridLayout>(R.id.sudokuGrid)
        gridLayout.removeAllViews()               // clear if reusing
        gridLayout.rowCount = 9
        gridLayout.columnCount = 9

        // compute cell size: use 90% of screen width or cap by height to keep comfortable layout
        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels
        // give some room for title + margins: use 0.9 of width and also cap by 0.7 of height
        val boardMaxWidth = min((screenWidth * 0.9).toInt(), (screenHeight * 0.7).toInt())
        val cellSizePx = boardMaxWidth / 9

        // common cell margin in px
        val marginPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2f, resources.displayMetrics).toInt()

        val oneDigitFilter = InputFilter.LengthFilter(1)

        for (r in 0 until 9) {
            for (c in 0 until 9) {
                val cell = EditText(this)

                // show value if prefilled
                if (puzzle[r][c] != 0) {
                    cell.setText(puzzle[r][c].toString())
                    cell.isEnabled = false
                    cell.setTextColor(resources.getColor(android.R.color.black, theme))
                } else {
                    cell.setText("")
                    cell.isEnabled = true
                }

                cell.setEms(1)
                cell.textSize = 18f
                cell.gravity = Gravity.CENTER
                cell.inputType = InputType.TYPE_CLASS_NUMBER
                cell.filters = arrayOf(oneDigitFilter)

                // apply cell background
                cell.setBackgroundResource(R.drawable.sudoku_cell_bg)

                // layout params — fixed square cell
                val params = GridLayout.LayoutParams(
                    GridLayout.spec(r, 1),
                    GridLayout.spec(c, 1)
                )
                params.width = cellSizePx
                params.height = cellSizePx
                params.setMargins(marginPx, marginPx, marginPx, marginPx)

                cell.layoutParams = params

                gridLayout.addView(cell)
            }
        }
    }
}
