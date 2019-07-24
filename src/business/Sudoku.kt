package business

class Sudoku constructor(sudokuSize: Int, sudokuFields : List<Field>,  sudokuSets : Set<SudokuSet>) {

    val size = sudokuSize

    /** A sudoku is nothing more that a collection of Sets in which holds that all values 0-9 can only exist once. */
    val sets = sudokuSets

    val fields = sudokuFields

    fun isComplete() = fields.all {field -> field.hasValue()}

    fun numberOfFieldsFilled() = fields.count { field -> field.hasValue() }
}