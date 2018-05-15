package it.unibo.mulino.minmax.player

fun main(args: Array<String>) {

    /**
     * STRUTTURA DEGLI ARRAY
     *    Se indice remove manca usiamo il valore -1
     *    ARRAY[INDICEFASE][INDICETO][INDICEREMOVE] -> Stringa
     *   ARRAY[INDICEFASE][INDICEFROM][INDICETO][INDICEREMOVE] -> Stringa
     *   ARRAY[INDICEFASE][INDICEFROM][INDICETO][INDICEREMOVE] -> Stringa
     */

    val azioniFase1ConRemove = MutableList<MutableList<String>>(32, { mutableListOf() })
    val azioniFase1SenzaRemove = MutableList<String>(32, { "" })
    val mapPosition = HashMap<Pair<Int, Int>, Boolean>()
    val mapPositionRemove = HashMap<Pair<Int, Int>, Boolean>()

    // azioni fase 1
    for (toVertex in 0 until 7) {
        for (toLevel in 0 until 3) {

            //debug
            mapPositionRemove.clear()

            val curPos = MutableList<String>(32, { "" })
            // Posizione from generata
            val toPosition = Pair(toVertex, toLevel)
            val toString = MulinoGame.toExternalPositions[toPosition]
            assert(toString != null, { toPosition })

            for (removeVertex in 0 until 7) {
                for (removeLevel in 0 until 3) {
                    // posizione remove generata
                    if (toVertex != removeVertex && toLevel != removeLevel) {
                        val removeString = MulinoGame.toExternalPositions[Pair(removeVertex, removeLevel)]
                        assert(removeString != null, { "$removeVertex,$removeLevel" })
                        curPos.add(removeVertex * 6 + removeLevel, "1$toString$removeString")
                        mapPositionRemove[Pair(removeVertex, removeLevel)] = true
                    }
                }
            }
            println(mapPositionRemove.size)
            val toIndex = toVertex * 6 + toLevel
            assert(mapPosition[toPosition] == null)
            mapPosition[toPosition] = true
            azioniFase1SenzaRemove.add(toIndex, toString!!)
            //assert(azioniFase1ConRemove.getOrNull(toIndex) == null)
            azioniFase1ConRemove.add(toIndex, curPos)
        }
    }
    println(mapPosition.size)
    //println(azioniFase1SenzaRemove.size)
    //println(azioniFase1ConRemove.size)
    //println(azioniFase1ConRemove[0].size)
    println("")
}