package it.unibo.mulino.minmax.player

object ActionMapper {

    /**
     * STRUTTURA DEGLI ARRAY
     *    Se indice remove manca usiamo il valore -1
     *    ARRAY[INDICEFASE][INDICETO][INDICEREMOVE] -> Stringa
     *   ARRAY[INDICEFASE][INDICEFROM][INDICETO][INDICEREMOVE] -> Stringa
     *   ARRAY[INDICEFASE][INDICEFROM][INDICETO][INDICEREMOVE] -> Stringa
     */

    val azioniFase1ConRemove = Array(24, { Array(24, { "" }) })
    val azioniFase1SenzaRemove = Array(24, { "" })
    val azioniFase2ConRemove = Array(24, { Array(24, { Array(24, { "" }) }) })
    val azioniFase2SenzaRemove = Array(24, { Array(24, { "" }) })
    val azioniFase3ConRemove = Array(24, { Array(24, { Array(24, { "" }) }) })
    val azioniFase3SenzaRemove = Array(24, { Array(24, { "" }) })

    init {

        // azioni fase 1
        for (toVertex in 0 until 8)
            for (toLevel in 0 until 3) {
                // Posizione from generata
                val toPosition = Pair(toVertex, toLevel)
                val toString = MulinoGame.toExternalPositions[toPosition]
                val toIndex = toVertex * 3 + toLevel
                azioniFase1SenzaRemove[toIndex] = "1$toString"
                for (removeVertex in 0 until 8)
                    for (removeLevel in 0 until 3) {
                        // posizione remove generata
                        if (toVertex != removeVertex || toLevel != removeLevel) {
                            val removeString = MulinoGame.toExternalPositions[Pair(removeVertex, removeLevel)]
                            azioniFase1ConRemove[toIndex][removeVertex * 3 + removeLevel] = "1$toString$removeString"
                        }
                    }
            }

        // azioni fase 2 e 3
        for (fromVertex in 0 until 8)
            for (fromLevel in 0 until 3) {
                //azione from generata
                val fromPosition = Pair(fromVertex, fromLevel)
                val fromString = MulinoGame.toExternalPositions[fromPosition]
                val fromIndex = fromVertex * 3 + fromLevel

                for (toVertex in 0 until 8)
                    for (toLevel in 0 until 3)
                        if (toVertex != fromVertex || toLevel != fromLevel) {
                            // Posizione from generata
                            val toPosition = Pair(toVertex, toLevel)
                            val toString = MulinoGame.toExternalPositions[toPosition]
                            val toIndex = toVertex * 3 + toLevel
                            azioniFase2SenzaRemove[fromIndex][toIndex] = "2${fromString}${toString}"
                            azioniFase3SenzaRemove[fromIndex][toIndex] = "3${fromString}${toString}"

                            for (removeVertex in 0 until 8)
                                for (removeLevel in 0 until 3)
                                // posizione remove generata
                                    if (((removeVertex != fromVertex) && (removeVertex != toVertex)) || ((removeLevel != fromLevel) && (removeLevel != toLevel))) {
                                        val removeString = MulinoGame.toExternalPositions[Pair(removeVertex, removeLevel)]
                                        azioniFase2ConRemove[fromIndex][toIndex][removeVertex * 3 + removeLevel] = "2$fromString$toString$removeString"
                                        azioniFase3ConRemove[fromIndex][toIndex][removeVertex * 3 + removeLevel] = "3$fromString$toString$removeString"
                                    }

                        }
            }

    }
}