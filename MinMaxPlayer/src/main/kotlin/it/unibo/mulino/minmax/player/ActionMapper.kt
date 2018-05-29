package it.unibo.mulino.minmax.player

/**
 * Rappresenta un'azione da eseguire sulla board, se il valore di [from] e/o [remove] sono
 * uguali a -1 significa che non sono presenti.
 */
data class ActionContainer(val from: Int, val to: Int, val remove: Int)

/**
 * Oggeto che permette di recuperare un [ActionContainer] a partire dall'hash di un'azione. Tramite le funzioni
 * [generateHashPh1] e [generateHashPh23] è possibile generare l'hash con cui accedere all' [actionMap]
 */
object ActionMapper {

    fun generateHashPh1(to: Int) = ((-1 * 31) * 31 + to) * 31 + -1
    fun generateHashPh1(to: Int, remove: Int) = (((-1 * 31) * 31 + to) * 31) + remove
    fun generateHashPh23(from: Int, to: Int) = (((from * 31) * 31 + to) * 31) + -1
    fun generateHashPh23(from: Int, to: Int, remove: Int) = (((from * 31) * 31 + to) * 31) + remove

    val actionMap = HashMap<Int, ActionContainer>()

    init {
        // azioni fase 1
        for (toVertex in 0 until 8)
            for (toLevel in 0 until 3) {
                // Posizione from generata
                val toPosition = toVertex * 3 + toLevel
                val hashTo = generateHashPh1(toPosition)
                assert(!actionMap.containsKey(hashTo))
                actionMap[hashTo] = ActionContainer(-1, toPosition, -1)
                for (removeVertex in 0 until 8)
                    for (removeLevel in 0 until 3) {
                        // posizione remove generata
                        val removePosition = removeVertex * 3 + removeLevel
                        val hashRemove = generateHashPh1(toPosition, removePosition)
                        assert(!actionMap.containsKey(hashRemove))
                        actionMap[hashRemove] = ActionContainer(-1, toPosition, removePosition)
                    }
            }

        // azioni fase 2 e 3
        for (fromVertex in 0 until 8)
            for (fromLevel in 0 until 3) {
                //azione from generata
                val fromPosition = fromVertex * 3 + fromLevel

                for (toVertex in 0 until 8)
                    for (toLevel in 0 until 3) {
                        // Posizione from generata
                        val toPosition = toVertex * 3 + toLevel
                        val hashFromTo = generateHashPh23(fromPosition, toPosition)
                        assert(!actionMap.containsKey(hashFromTo))
                        actionMap[hashFromTo] = ActionContainer(fromPosition, toPosition, -1)

                        for (removeVertex in 0 until 8)
                            for (removeLevel in 0 until 3) {
                                val removePosition = removeVertex * 3 + removeLevel
                                val hashFromToRemove = generateHashPh23(fromPosition, toPosition, removePosition)
                                assert(!actionMap.containsKey(hashFromToRemove))
                                actionMap[hashFromToRemove] = ActionContainer(fromPosition, toPosition, removePosition)
                            }
                    }
            }
    }
}