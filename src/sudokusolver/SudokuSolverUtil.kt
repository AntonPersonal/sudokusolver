package sudokusolver

import business.Field
import business.Sudoku
import business.SudokuSet

/**
 * Removes candidates using block and column row interaction.
 */
fun removeCandidatesUsingBlockColumnRowInteraction(sudoku : Sudoku) {
    val sudokuSets = sudoku.sets

    sudokuSets.forEach outer@ {
        set ->
        (1..sudoku.size).forEach inner@ {

            val fieldsWithPossibleValue : Set<Field> = set.determineFieldsPossibleForValue(it)

            if (fieldsWithPossibleValue.size == 2) {
                val setsWithSharedValues = set.determineSharedSetsForPossibleValue(it)
                if (setsWithSharedValues.isEmpty()) {
                    return@inner
                }

                setsWithSharedValues.forEach{set -> set.dropPossibleValue(it, fieldsWithPossibleValue)}
            }
        }
    }
}

/**
 * remove candidates using a naked sub set.
 */
fun removeCandidateUsingNakedSubSet(sudoku : Sudoku) {
    val sudokuSets = sudoku.sets

    sudokuSets.forEach { sudokuSet: SudokuSet ->

        // Find all fields in the naked pair.
        val fieldsWithTheSamePossibleValues = sudokuSet.determineFieldsContainedInNakedPairs()

        // Obtain the values in the naked pair
        val valuesInNakedPair = fieldsWithTheSamePossibleValues.map{ field -> field.getPossibleValues() }.flatten().toList()

        valuesInNakedPair.forEach {
                // Drop these values. Note that multiple naked pairs could be found in a single set.
                value ->  sudokuSet.dropPossibleValue(value, fieldsWithTheSamePossibleValues.filter { field -> field.getPossibleValues().contains(value)}.toSet())

    }


    }
}