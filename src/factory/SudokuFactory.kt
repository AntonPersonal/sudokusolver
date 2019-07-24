package factory

import business.Field
import business.Sudoku
import business.SudokuSet
import reader.determineSudokuSize

/**
 * Creates a new [Sudoku] from the input file.
 */
fun createSudokuFromInput(sudokuAsString: MutableList<String>, sudokuStructureAsString : MutableList<String>, hasDiagonal : Boolean) : Sudoku {

    if (sudokuAsString.isEmpty()) {
        throw IllegalStateException("Cannot create sudoku from empty file.")
    }

    // The length of a side of te sudoku is based on the length of the first row.
    val length: Int = determineSudokuSize(sudokuAsString[0])
    // Create the fields
    val fields = createFields(sudokuAsString, length)
    // Create the sets
    val sudokuSets = mutableSetOf<SudokuSet>()
    // Create the Sets based on the coordinates of the field, assuming two dimensions.
    (0 until length).forEach({
        sudokuSets.addAll(arrayOf(
                createSetForLine(length, fields, it, 0),
                createSetForLine(length, fields, it, 1)))
    })

    sudokuSets.addAll(createSetsForStructure(sudokuStructureAsString, length, fields))

    // Create the sudoku
    val sudoku = Sudoku(length, fields, sudokuSets.toSet())

    // Validate the sudoku
    if (!validateSudokuObjectModel(sudoku)) {
        throw IllegalStateException("Bug exists in SudokuFactory")
    }

    return sudoku
}

/**
 * Creates a set for a line (row, column).
 * @param length the length of the line
 * @param fields all the fields in the sudoku
 * @param lineNumber, the row/column number of the line to create the set for
 * @param dimension indicates if the line is created for a column {0} or a row (1).
 */
private fun createSetForLine(length : Int, fields : List<Field>, lineNumber : Int, dimension : Int) : SudokuSet {
    // Find all the fields that according to their coordinates are in this line
    val fieldsInRow = fields.filter { field -> field.coordinates[dimension] == lineNumber }
    // Create a sudoku set for the line
    val sudokuSet = SudokuSet(length, fieldsInRow.toSet())
    // Add the set to the setlist of each field that was added to the set.
    fieldsInRow.forEach({ field -> field.addSet(sudokuSet) })
    return sudokuSet
}

/**
 * Creates the immutable List of fields that correspond to this sudoku.
 */
private fun createFields(sudokuAsString: MutableList<String>, length: Int) : List<Field>{
    val fields = arrayListOf<Field>()
    // Create a field for every number in every String of the list
    sudokuAsString.forEachIndexed(
            { columnNumber, rowAsString ->
                // Iterate over the values in the row
                rowAsString.split(".").forEachIndexed(
                        { rowNumber, fieldValue ->
                            // Create a new field and add it to the row.
                            fields.add(Field(fieldValue.toInt(), length, intArrayOf(rowNumber, columnNumber)))
                        }
                )
            }
    )
    return fields.toList()
}

private fun createSetsForStructure(sudokuStructureAsString: MutableList<String>, length: Int, fields : List<Field>) : Set<SudokuSet> {
    // Create a list of a fieldlist based on the sudoku structure.
    val structureFieldList = MutableList(length, { mutableListOf<Field>()})

    // Iterate over all values in the structure.
    sudokuStructureAsString.forEachIndexed(
            { columnNumber, rowAsString ->
                // Iterate over the values in the row
                rowAsString.split(".").forEachIndexed(
                        { rowNumber, structureNumber ->

                            // A structure number cannot be higher than the length of the sudoku (a sudoku of length 9 should have 9 structures)
                            if (structureNumber.toInt() > length) throw IllegalStateException("A structure for this cannot contain more fields than possible values ")

                            // Find the field that is on te same coordinate as we are now.
                            val field = fields.find { field -> field.coordinates[0] == rowNumber && field.coordinates[1] == columnNumber}
                                    ?: throw IllegalStateException("There exists an error with the sudoku structure file. A field referenced there does not exist in the sudoku.")

                            // Add the field to the field list that corresponds to this structure number.
                            structureFieldList[structureNumber.toInt() -1].add(field)

                            // Verify that there will be no more than <length> fields in the structure
                            if (structureFieldList[structureNumber.toInt() -1].size > length) throw IllegalStateException("A structure for this cannot contain more fields than possible values ")
                        }
                )
            })

    // Create a set for each field list.
    val structureSetList = mutableSetOf<SudokuSet>()
    structureFieldList.forEach({fieldList ->
        val sudokuSet = SudokuSet(length, fieldList.toSet())
        structureSetList.add(sudokuSet)
        fieldList.forEach({ field -> field.addSet(sudokuSet) })
    })
    return structureSetList.toSet()
}

/**
 * Validates tat the suodoku object model is logically consistent
 * @return true if this is the case.
 */
private fun validateSudokuObjectModel(sudoku : Sudoku) : Boolean {
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