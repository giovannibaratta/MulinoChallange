package it.unibo.mulino.minmax.player

data class ActionContainer(val from: Int, val to: Int, val remove: Int)

// TODO("Lanciare con asser per verificare che tutte le azioni siano codificate diversamente")
object ActionMapper {


    fun generateHashPh1(to: Int) = ((-1 * 31) * 31 + to) * 31 + -1
    fun generateHashPh1(to: Int, remove: Int) = (((-1 * 31) * 31 + to) * 31) + remove
    fun generateHashPh23(from: Int, to: Int) = (((from * 31) * 31 + to) * 31) + -1
    fun generateHashPh23(from: Int, to: Int, remove: Int) = (((from * 31) * 31 + to) * 31) + remove


    //fun generateHashPh1(to : Int) = Objects.hash (-1, to, -1)
    //fun generateHashPh1(to: Int, remove: Int) = Objects.hash(-1, to, remove)
    //fun generateHashPh23(from: Int, to: Int) = Objects.hash(from, to, -1)
    //fun generateHashPh23(from: Int, to: Int, remove: Int) = Objects.hash(from, to, remove)
    //fun generateHashPh3NoRemove(from : Int , to : Int) = Objects.hash(from, to,-1)
    //fun generateHashPh3(from : Int, to : Int, remove : Int) = Objects.hash(from, to, remove)

    val actionMap = HashMap<Int, ActionContainer>()

    init {
        //assert(false)
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
                        //if (toVertex != removeVertex || toLevel != removeLevel) {
                        val removePosition = removeVertex * 3 + removeLevel
                        val hashRemove = generateHashPh1(toPosition, removePosition)
                        assert(!actionMap.containsKey(hashRemove))
                        actionMap[hashRemove] = ActionContainer(-1, toPosition, removePosition)
                        //}
                    }
            }

        // azioni fase 2 e 3
        for (fromVertex in 0 until 8)
            for (fromLevel in 0 until 3) {
                //azione from generata
                val fromPosition = fromVertex * 3 + fromLevel

                for (toVertex in 0 until 8)
                    for (toLevel in 0 until 3) {
                        //if (toVertex != fromVertex || toLevel != fromLevel) {
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
                                //}
                            }

                        //}
                    }
            }
    }
}


fun main(args: Array<String>) {
    println(ActionMapper.actionMap.get(ActionMapper.generateHashPh1(10)))
}


//object ActionMapper {
//
//    /**
//     * STRUTTURA DEGLI ARRAY
//     *    Se indice remove manca usiamo il valore -1
//     *    ARRAY[INDICEFASE][INDICETO][INDICEREMOVE] -> Stringa
//     *   ARRAY[INDICEFASE][INDICEFROM][INDICETO][INDICEREMOVE] -> Stringa
//     *   ARRAY[INDICEFASE][INDICEFROM][INDICETO][INDICEREMOVE] -> Stringa
//     */
//
//    val azioniFase1ConRemove = Array(24, { Array(24, { "" }) })
//    val azioniFase1SenzaRemove = Array(24, { "" })
//    val azioniFase2ConRemove = Array(24, { Array(24, { Array(24, {"" }) }) })
//    val azioniFase2SenzaRemove = Array(24, { Array(24, { "" }) })
//    val azioniFase3ConRemove = Array(24, { Array(24, { Array(24, { "" }) }) })
//    val azioniFase3SenzaRemove = Array(24, { Array(24, { "" }) })
//
//    init {
//
//        // azioni fase 1
//        for (toVertex in 0 until 8)
//            for (toLevel in 0 until 3) {
//                // Posizione from generata
//                val toPosition = Pair(toVertex, toLevel)
//                val toString = MulinoGame.toExternalPositions[toPosition]
//                val toIndex = toVertex * 3 + toLevel
//                azioniFase1SenzaRemove[toIndex] = "1$toString"
//                for (removeVertex in 0 until 8)
//                    for (removeLevel in 0 until 3) {
//                        // posizione remove generata
//                        //if (toVertex != removeVertex || toLevel != removeLevel) {
//                            val removeString = MulinoGame.toExternalPositions[Pair(removeVertex, removeLevel)]
//                            azioniFase1ConRemove[toIndex][removeVertex * 3 + removeLevel] = "1$toString$removeString"
//                        //}
//                    }
//            }
//
//        // azioni fase 2 e 3
//        for (fromVertex in 0 until 8)
//            for (fromLevel in 0 until 3) {
//                //azione from generata
//                val fromPosition = Pair(fromVertex, fromLevel)
//                val fromString = MulinoGame.toExternalPositions[fromPosition]
//                val fromIndex = fromVertex * 3 + fromLevel
//
//                for (toVertex in 0 until 8)
//                    for (toLevel in 0 until 3) {
//                        //if (toVertex != fromVertex || toLevel != fromLevel) {
//                            // Posizione from generata
//                            val toPosition = Pair(toVertex, toLevel)
//                            val toString = MulinoGame.toExternalPositions[toPosition]
//                            val toIndex = toVertex * 3 + toLevel
//                            azioniFase2SenzaRemove[fromIndex][toIndex] = "2${fromString}${toString}"
//                            azioniFase3SenzaRemove[fromIndex][toIndex] = "3${fromString}${toString}"
//
//                            for (removeVertex in 0 until 8)
//                                for (removeLevel in 0 until 3) {
//                                    // posizione remove generata
//                                    //if (((removeVertex != fromVertex) && (removeVertex != toVertex)) || ((removeLevel != fromLevel) && (removeLevel != toLevel))) {
//                                    val removeString = MulinoGame.toExternalPositions[Pair(removeVertex, removeLevel)]
//                                    azioniFase2ConRemove[fromIndex][toIndex][removeVertex * 3 + removeLevel] = "2$fromString$toString$removeString"
//                                    azioniFase3ConRemove[fromIndex][toIndex][removeVertex * 3 + removeLevel] = "3$fromString$toString$removeString"
//                                    //}
//                                }
//
//                        //}
//                    }
//            }
//
//    }
//}