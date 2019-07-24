package sudokutest

import business.Sudoku

fun validateSudokuObjectModel(sudoku : Sudoku) : Boolean {
    sudoku.sets.forEach({
        set ->
        set.getFields().forEach(
                {
                    // The set that contains the field should be present in the sets of the field
                    field ->
                    if (!field.containsSet(set)) {
                        return false
                    }
                }
        )
    })
    return true
}