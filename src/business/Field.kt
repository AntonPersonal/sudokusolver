package business

class Field(initialValue: Int, length : Int, coordinates : IntArray) {
    /**
     * Contains the value of this field. Filled in the constructor when [possibleValues] obtains length 1
     */
    private var value : Int = initialValue

    fun getValue() : Int {
       return value
    }

    /**
     * Represents the position of the field.
     * Used to write the result to a (two-dimensional) grid
     * For now, assumes the first two values in [coordinates] are  x-y
     */
    val coordinates : IntArray = coordinates

    /**
     * The possible values this Field can have.
     */
    private var possibleValues = if (initialValue == 0)  MutableList(length, {it -> it+1}) else MutableList(0, {it -> it+1})

    fun getPossibleValues() : List<Int> {
        return possibleValues
    }

    /**
     * Indicates if this Field has a value.
     */
    fun hasValue() : Boolean = value != 0

    /**
     * Contains the sudokuSets in which this field is present.
     */
    private val sudokuSets = mutableListOf<SudokuSet>()

    fun getSudokuSets() : List<SudokuSet> {
        return sudokuSets.toList()
    }

    fun dropPossibleValue(valuetoDrop : Int) {

        if (hasValue()) {
            // This field already has a value.
            return
        }

        if (!possibleValues.remove(valuetoDrop)) {
            // The value was already removed once before
            return
        }



        if (possibleValues.size != 1) {
            // Still multiple possible values.
            return
        }


        // There is now only one possible value.
        // This must be te value of this field. SudokuSet it.
        fillValue(possibleValues[0])
    }

    fun fillValue(value : Int) {
        this.value = value
        // There are no more possible values for this field:
        possibleValues.clear()
        // Verify that the model is still consistent: it is possible that this value has just been set at a different Field
        // but the call to this field to drop that value has not occurred yet.
        // This can only happen if the provided puzzle is invalid, or if there is a bug in the code.
        if (!checkValueForConsistency()) throw IllegalStateException("Forced to set duplicate value in a SudokuSet: filled " + value + " at field: " + coordinates[0] + "-"+ coordinates[1] + "\n " )
        // Notify the sudokuSets that contain this field to drop this value as a possible value.
        notifySetsToDropPossibleValueForFields()
    }

    private fun checkValueForConsistency() : Boolean {
        // There may never be an other field in any of the sudokuSets with the same value.
        return sudokuSets.none({ sudokuSet: SudokuSet -> sudokuSet.containsValueOfField(this)})
    }

    /**
     * Notifies all Sets that contain this field to drop the [value] of this field as a possible value.
     */
    private fun notifySetsToDropPossibleValueForFields() {
        sudokuSets.forEach({set -> set.dropPossibleValue(value)})
    }

    /**
     * Adds a set to the Field.
     */
    fun addSet(sudokuSetToAdd : SudokuSet) {
        sudokuSets.add(sudokuSetToAdd)
    }

    /**
     * Adds sudokuSets to the Field.
     */
    fun addSets(setsToAdd : List<SudokuSet>){
        sudokuSets.addAll(setsToAdd)
    }

    /**
     * Indicates if this field contains the provided [sudokuSet]
     */
    fun containsSet(sudokuSet : SudokuSet) : Boolean {
        return sudokuSets.contains(sudokuSet)
    }

    fun updatePossibleValues() {
        if (hasValue()) return

        val possibleValuesToEvaluate = possibleValues.toMutableList()
        possibleValuesToEvaluate.forEach({ value ->
            if (sudokuSets.any({ sudokuSet -> sudokuSet.hasFieldWithValue(value)})) {
                dropPossibleValue(value)
            }
        })
    }
}