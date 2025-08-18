package com.example.minigamesapp

import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.EditText
import android.widget.GridLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlin.math.min

class SudokuActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sudoku)

        val sudokuGame = SudokuGame()
        val puzzle = sudokuGame.generatePuzzle(removalCount = 30)

        val gridLayout = findViewById<GridLayout>(R.id.sudokuGrid)
        gridLayout.removeAllViews()
        gridLayout.rowCount = 9
        gridLayout.columnCount = 9

        // compute board size (90% of width, capped by 70% of height)
        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels
        val boardMaxWidth = min((screenWidth * 0.9).toInt(), (screenHeight * 0.7).toInt())

        // set GridLayout's measured size so children can be sized using it
        val layoutParams = gridLayout.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.width = boardMaxWidth
        layoutParams.height = boardMaxWidth
        gridLayout.layoutParams = layoutParams
        gridLayout.requestLayout()

        // cell size
        val cellSizePx = boardMaxWidth / 9

        // small margin in px between cells
        val marginPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 2f, resources.displayMetrics
        ).toInt()

        val oneDigitFilter = InputFilter.LengthFilter(1)

        val prefillColor = ContextCompat.getColor(this, android.R.color.black)
        val editableColor = ContextCompat.getColor(this, android.R.color.black)

        for (r in 0 until 9) {
            for (c in 0 until 9) {
                val cell = EditText(this)

                if (puzzle[r][c] != 0) {
                    cell.setText(puzzle[r][c].toString())
                    cell.isEnabled = false
                    cell.setTextColor(prefillColor)
                } else {
                    cell.setText("")
                    cell.isEnabled = true
                    cell.setTextColor(editableColor)
                }

                // 1-digit width
                cell.setEms(1)
                // text size in sp
                cell.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
                cell.gravity = Gravity.CENTER
                cell.inputType = InputType.TYPE_CLASS_NUMBER
                cell.filters = arrayOf(oneDigitFilter)

                // background (border) drawable — make sure this exists
                cell.setBackgroundResource(R.drawable.sudoku_cell_bg)

                // layout params: fixed square cell
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
