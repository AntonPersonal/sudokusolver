package writer

import business.Sudoku

fun writeSudoku(sudoku : Sudoku) {
    (0 until sudoku.size).forEach({rowNumber ->
        val valuesInRow = mutableListOf<Int>()
        (0 until sudoku.size).forEach({columnNumber ->
            val field = sudoku.fields.find {field -> field.coordinates[0] == columnNumber && field.coordinates[1] == rowNumber} ?: throw IllegalStateException("field not found")
            valuesInRow.add(field.getValue())
        })
        println(valuesInRow.joinToString("."))
    })
}