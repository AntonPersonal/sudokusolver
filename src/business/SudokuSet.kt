package business

/**
 * A sudoku set contains [length] [Field]s that each need to have an unique [Field.value]
 *
 * In a sudoku, a set can represent a row, column, diagonal, area (usually 3x3), etc.
 */
class SudokuSet(private val length : Int, sudokuFields : Set<Field>) {

    /**
     * The fields present in this SudokuSet.
     */
    private val fields = sudokuFields

    fun getFields() : Set<Field> {
        return fields
    }

    /**
     * The values in this SudokuSet that still need to be distributed among its [fields] that contain no [Field.value] yet.
     */
    private var valuesToFill = MutableList(length, {it -> it+1})


    init {
        val valuesTaken = fields.map{field -> field.getValue()}
        valuesToFill = valuesToFill.filter { value -> !valuesTaken.contains(value) }.toMutableList()
    }

    fun getValuesToFill() : List<Int> {
        return valuesToFill.toList()
    }

    /**
     * Indicates if this SudokuSet only contains fields with a value.
     */
    fun isFilled() : Boolean = valuesToFill.isEmpty()

    /**
     * Drops the provided [valueToDrop] as a possible value among the fields in this set.
     * @param [ignoreFields] these fields will not have a value dropped.
     */
    fun dropPossibleValue(valueToDrop : Int, ignoreFields : Set<Field>) {

        if (!valuesToFill.remove(valueToDrop)) {
            // Value is already distributed in this set.
            // None of the fields will have this value as a possible value.
            return
        }

        // Make all fields that should not be ignored drop this value as a possible value.
        fields.filter { field -> !ignoreFields.contains(field) }.forEach({ field : Field -> field.dropPossibleValue(valueToDrop) })
    }

    /**
     * Drops the provided [valueToDrop] as a possible value among the fields in this set.
     */
    fun dropPossibleValue(valueToDrop : Int) {
        dropPossibleValue(valueToDrop, emptySet())
    }

    /**
     * Checks if this SudokuSet contains a Field with the same value as the given Field.
     */
    fun containsValueOfField(fieldToCheck : Field) : Boolean = fields.any(
            // Verify that there is no field that is not this field but has the same value.
            {field -> field != fieldToCheck && field.getValue() == fieldToCheck.getValue()})

    /**
     * Checks if this sudoku set contains a field with the given value.
     */
    fun hasFieldWithValue(valueToCheck : Int) : Boolean = fields.any({ field -> field.getValue() == valueToCheck})

    fun updatePossibleValues() {
        if (isFilled()) return

        val possibleValuesToEvaluate = valuesToFill.toMutableList()

        possibleValuesToEvaluate.forEach {

            // Check in how many fields the current value is a possible value.
            val fieldList = determineFieldsPossibleForValue(it)

            if (fieldList.size == 1) {
                // there is only one field in the set that can contain this value. This field must have this value.
                fieldList.elementAt(0).fillValue(it)
            } else {
                // Else, determine if the fields that can contain this value share sets
                val setsWithFieldsForSharedValue = determineSharedSetsForPossibleValue(it)

                if (setsWithFieldsForSharedValue.isNotEmpty() && fieldList.stream().allMatch{ field2 -> field2.getSudokuSets().containsAll(setsWithFieldsForSharedValue)}) {
                    // All fields that can have the given value, share exactly the same sets.
                    // This means, in that set, the value cannot be given to a different field:
                    setsWithFieldsForSharedValue.forEach{sudokuSet -> sudokuSet.dropPossibleValue(it, fieldList)}
                }
            }
        }
    }

    /**
     * Counts the nr of fields has can contain the possible value.
     */
    fun determineFieldsPossibleForValue(value : Int) : Set<Field> {
        return fields.filter{field -> field.getPossibleValues().contains(value)}.toSet()
    }

    /**
     * Determines the shared sets of the fields in this set that have the given value as a possible value.
     * It will exclude this Set.
     */
    fun determineSharedSetsForPossibleValue(value : Int) : Set<SudokuSet> {

        // Determine the fields that contain the value as a possible value.
        val fieldsWithValue  = determineFieldsPossibleForValue(value)

        // Determine all sets in which these fields exist.
        val sets : List<SudokuSet> = fieldsWithValue.map{ field -> field.getSudokuSets()}.flatten()

        // Group the sets and then filter the sets that exist more than once and are not this set (because we know the fields share this set).
        val map : Map<SudokuSet, Int> = sets.groupingBy { it }.eachCount().filter { entry -> entry.value > 1 && entry.key != this }
        // Finally, return the sets.
        return map.map { entry -> entry.key }.toSet()
    }

    /**
     * Finds all fields in this set that are in a naked pair (two fields that both have the same two possible values)
     */
    fun determineFieldsContainedInNakedPairs() : Set<Field> {
        val fieldsWithTwoPossibleValues = fields.filter { field -> field.getPossibleValues().size == 2 }

        val fieldsWithTheSamePossibleValues = mutableSetOf<Field>()

        fieldsWithTwoPossibleValues.forEach {
            field->
            val fieldWithSameValues : Field = fieldsWithTwoPossibleValues.find { field2 -> field.getPossibleValues() == field2.getPossibleValues() && field != field2 } ?: return@forEach
            fieldsWithTheSamePossibleValues.add(fieldWithSameValues)

        }
        return fieldsWithTheSamePossibleValues
    }
}