package sudokusolver

import business.Sudoku
import factory.createSudokuFromInput
import reader.readFile
import reader.validateFile
import writer.writeSudoku


/**
 * Solves a sudoku.
 */
fun main(args: Array<String>) {
    if (args.size != 2 && args.size != 3) {
        println("Incorrect number of arguments provided: expected two or three arguments: [filenameSudoku, filenameSudokuStructure, hasDiagonal (optional)")
        return
    }

    val sudokuAsString : MutableList<String> = readFile(args[0])

    val sudokuStructureAsString : MutableList<String> = readFile(args[1])
    val hasDiagonal = if (args.size == 3) args[2].toBoolean() else false


    if (!validateFile(sudokuAsString)) {
        println("the file content of the sudoku is not valid: every row should be of equal length and contain only [0-9] or '.'")
        return
    }

    if (!validateFile(sudokuStructureAsString)) {
        println("the file content of the sudoku structure is not valid: every row should be of equal length and contain only [0-9] or '.'")
        return
    }

    val sudoku = createSudokuFromInput(sudokuAsString, sudokuStructureAsString, hasDiagonal)

    solveSudoku(sudoku)
}

/**
 * Solves a sudoku.
 */
private fun solveSudoku(sudoku : Sudoku) {
    // Update the possible values on all fields.
    // This will trigger a chain reaction filling all the fields.
    try {
        while (!sudoku.isComplete()) {
            val numberOfFieldsFilled = sudoku.numberOfFieldsFilled()
            println("Fields filled: " + numberOfFieldsFilled)
            removeCandidates(sudoku)
            sudoku.fields.forEach({ field -> field.updatePossibleValues() })
            sudoku.sets.forEach({ set -> set.updatePossibleValues() })
            if (numberOfFieldsFilled == sudoku.numberOfFieldsFilled()) {
                // No more hints found.
                println("I am still too stupid, solved till " + numberOfFieldsFilled)
                writeSudoku(sudoku)
                return
            }
        }
    } catch (e : IllegalStateException) {
        writeSudoku(sudoku)
        print(e.message)
        return
    }

    println("succes:")
    writeSudoku(sudoku)
}

private fun removeCandidates(sudoku : Sudoku) {
    removeCandidateUsingNakedSubSet(sudoku)
    removeCandidatesUsingBlockColumnRowInteraction(sudoku)
}

