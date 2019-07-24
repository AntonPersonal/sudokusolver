package reader

import java.io.File
import java.io.InputStream
import kotlin.math.ceil

fun readFile(fileName : String) : MutableList<String> {
    val inputStream: InputStream = File(fileName).inputStream()

    val lineList = mutableListOf<String>()

    inputStream.bufferedReader().useLines { lines -> lines.forEach { lineList.add(it) } }

    return lineList
}

/**
 * Validates the file.
 *
 * Note: assumes a square sudoku.
 */
fun validateFile(sudokuAsString : MutableList<String>) : Boolean {

    // The size of the sudoku is determined by the length of the first row.
    val size = determineSudokuSize(sudokuAsString[0])

    if (sudokuAsString.size != size) {
        // Not square
        return false;
    }

    // All rows should have equal length. Divide by two due to the . seperator
    if (sudokuAsString.any({rowAsString -> ceil(rowAsString.length.toDouble() / 2.0).toInt() != size})) {
        return false
    }

    // All rows should contain only numbers or points
    return sudokuAsString.all({row -> row.matches(Regex("[0-9.]*"))})
}


/**
 * Determines the size of the sudoku based on a row as String
 * Every row is separated by a '.'
 * By using ceil, the last '.' is optional.
 */
fun determineSudokuSize(sudokuRowAsString : String) : Int = ceil(sudokuRowAsString.length.toDouble() / 2.0).toInt()