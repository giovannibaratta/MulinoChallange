package it.unibo.mulino.minmax.player

import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime

fun main(args: Array<String>) {

    /**
     * STRUTTURA DEGLI ARRAY
     *    Se indice remove manca usiamo il valore -1
     *    ARRAY[INDICEFASE][INDICETO][INDICEREMOVE] -> Stringa
     *   ARRAY[INDICEFASE][INDICEFROM][INDICETO][INDICEREMOVE] -> Stringa
     *   ARRAY[INDICEFASE][INDICEFROM][INDICETO][INDICEREMOVE] -> Stringa
     */

    val azioniFase1ConRemove = MutableList<MutableList<String>>(24, { mutableListOf() })
    val azioniFase1SenzaRemove = MutableList<String>(24, { "" })
    val azioniFase2ConRemove = MutableList<MutableList<MutableList<String>>>(24, { mutableListOf() })
    val azioniFase2SenzaRemove = MutableList<MutableList<String>>(24, { mutableListOf() })
    val azioniFase3ConRemove = MutableList<MutableList<MutableList<String>>>(24, { mutableListOf() })
    val azioniFase3SenzaRemove = MutableList<MutableList<String>>(24, { mutableListOf() })
    val mapPosition = HashMap<Pair<Int, Int>, Boolean>()
    val mapPositionRemove = HashMap<Pair<Int, Int>, Boolean>()

    // azioni fase 1
    for (toVertex in 0 until 8) {
        for (toLevel in 0 until 3) {

            //debug
            mapPositionRemove.clear()

            val curPos = MutableList<String>(24, { "" })
            // Posizione from generata
            val toPosition = Pair(toVertex, toLevel)
            val toString = MulinoGame.toExternalPositions[toPosition]
            assert(toString != null, { toPosition })

            for (removeVertex in 0 until 8) {
                for (removeLevel in 0 until 3) {
                    // posizione remove generata
                    if (toVertex != removeVertex || toLevel != removeLevel) {
                        val removeString = MulinoGame.toExternalPositions[Pair(removeVertex, removeLevel)]
                        assert(removeString != null, { "$removeVertex,$removeLevel" })
                        curPos.add(removeVertex * 3 + removeLevel, "1$toString$removeString")
                        mapPositionRemove[Pair(removeVertex, removeLevel)] = true
                    }
                }
            }
            //println(mapPositionRemove.size)
            val toIndex = toVertex * 3 + toLevel
            assert(mapPosition[toPosition] == null)
            mapPosition[toPosition] = true
            azioniFase1SenzaRemove.add(toIndex, toString!!)
            //assert(azioniFase1ConRemove.getOrNull(toIndex) == null)
            azioniFase1ConRemove.add(toIndex, curPos)
        }
    }

    // azioni fase 2
    for (fromVertex in 0 until 8) {
        for (fromLevel in 0 until 3) {
            //azione from generata
            val toActionListConRemove = MutableList<MutableList<String>>(64, { mutableListOf() })
            val toActionListSenzaRemove = MutableList<String>(64, { "" })

            val fromPosition = Pair(fromVertex, fromLevel)
            val fromString = MulinoGame.toExternalPositions[fromPosition]
            for (toVertex in 0 until 8) {
                for (toLevel in 0 until 3) {
                    if (toVertex != fromVertex || toLevel != fromLevel) {
                        // Posizione from generata
                        val removeActionList = MutableList<String>(24, { "" })
                        val toPosition = Pair(toVertex, toLevel)
                        val toString = MulinoGame.toExternalPositions[toPosition]
                        assert(toString != null, { toPosition })

                        for (removeVertex in 0 until 8) {
                            for (removeLevel in 0 until 3) {
                                // posizione remove generata
                                if (((removeVertex != fromVertex) && (removeVertex != toVertex)) || ((removeLevel != fromLevel) && (removeLevel != toLevel))) {
                                    val removeString = MulinoGame.toExternalPositions[Pair(removeVertex, removeLevel)]
                                    assert(removeString != null, { "$removeVertex,$removeLevel" })
                                    removeActionList.add(removeVertex * 3 + removeLevel, "2$fromString$toString$removeString")
                                    mapPositionRemove[Pair(removeVertex, removeLevel)] = true
                                }
                            }
                        }

                        val toIndex = toVertex * 3 + toLevel
                        toActionListConRemove.add(toIndex, removeActionList)
                        toActionListSenzaRemove.add(toIndex, "2${fromString}${toString}")
                    }
                }
            }
            val fromIndex = fromVertex * 3 + fromLevel
            azioniFase2ConRemove.add(fromIndex, toActionListConRemove)
            azioniFase2SenzaRemove.add(fromIndex, toActionListSenzaRemove)
        }
    }

    // azioni fase 2
    for (fromVertex in 0 until 8) {
        for (fromLevel in 0 until 3) {
            //azione from generata
            val toActionListConRemove = MutableList<MutableList<String>>(64, { mutableListOf() })
            val toActionListSenzaRemove = MutableList<String>(64, { "" })

            val fromPosition = Pair(fromVertex, fromLevel)
            val fromString = MulinoGame.toExternalPositions[fromPosition]
            for (toVertex in 0 until 8) {
                for (toLevel in 0 until 3) {
                    if (toVertex != fromVertex || toLevel != fromLevel) {
                        // Posizione from generata
                        val removeActionList = MutableList<String>(24, { "" })
                        val toPosition = Pair(toVertex, toLevel)
                        val toString = MulinoGame.toExternalPositions[toPosition]
                        assert(toString != null, { toPosition })

                        for (removeVertex in 0 until 8) {
                            for (removeLevel in 0 until 3) {
                                // posizione remove generata
                                if (((removeVertex != fromVertex) && (removeVertex != toVertex)) || ((removeLevel != fromLevel) && (removeLevel != toLevel))) {
                                    val removeString = MulinoGame.toExternalPositions[Pair(removeVertex, removeLevel)]
                                    assert(removeString != null, { "$removeVertex,$removeLevel" })
                                    removeActionList.add(removeVertex * 3 + removeLevel, "3$fromString$toString$removeString")
                                    mapPositionRemove[Pair(removeVertex, removeLevel)] = true
                                }
                            }
                        }

                        val toIndex = toVertex * 3 + toLevel
                        toActionListConRemove.add(toIndex, removeActionList)
                        toActionListSenzaRemove.add(toIndex, "3${fromString}${toString}")
                    }
                }
            }
            val fromIndex = fromVertex * 3 + fromLevel
            azioniFase3ConRemove.add(fromIndex, toActionListConRemove)
            azioniFase3SenzaRemove.add(fromIndex, toActionListSenzaRemove)
        }
    }


    val generatedStructure = StringBuilder()
    val lb = System.lineSeparator()
    generatedStructure.append("// Generato il ${LocalDateTime.now()}$lb")
    generatedStructure.append("package it.unibo.mulino.minmax.player$lb")
    generatedStructure.append("object ActionsMapper{ $lb")
    generatedStructure.append("\t\t/**************************************$lb")
    generatedStructure.append(" \t\t* STRUTTURA GENERATA AUTOMATICAMENTE *$lb")
    generatedStructure.append(" \t\t*************************************/$lb$lb$lb")

    generatedStructure.append("\t\tval phase1ActionWithRemove = arrayOf($lb")
    for (toIndex in 0 until 24) {
        generatedStructure.append("\t\t\t\tarrayOf(")
        for (removeIndex in 0 until 24) {
            generatedStructure.append("\"${azioniFase1ConRemove[toIndex][removeIndex]}\"")
            if (removeIndex < 23)
                generatedStructure.append(", ")
        }
        generatedStructure.append(")")
        if (toIndex < 23)
            generatedStructure.append(",$lb")
        else
            generatedStructure.append("$lb\t\t)$lb")

    }

    generatedStructure.append("$lb$lb")

    // stampa actions 1 senza remove
    generatedStructure.append("\t\tval phase1ActionWithoutRemove = arrayOf( ")
    for (toIndex in 0 until 24) {
        generatedStructure.append("\"${azioniFase1SenzaRemove[toIndex]}\"")
        if (toIndex < 23)
            generatedStructure.append(", ")

    }
    generatedStructure.append(")$lb")

    // stampa fase 2 con remove
    generatedStructure.append("$lb$lb$lb")
    generatedStructure.append("\t\tval phase2ActionWithRemove = arrayOf($lb")

    /*
       arrayOf(

     */

    for (fromIndex in 0 until 24) {
        generatedStructure.append("\t\t\t\tarrayOf($lb")

        /*
            arrayOf(
                        arrayOf(

         */

        for (toIndex in 0 until 24) {

            generatedStructure.append("\t\t\t\t\tarrayOf(")

            /*
            arrayOf(
                        arrayOf(
                                        arrayOf(

            */
            if (!azioniFase2ConRemove[fromIndex][toIndex].isEmpty()) {
                for (removeIndex in 0 until 24) {
                    generatedStructure.append("\"${azioniFase2ConRemove[fromIndex][toIndex][removeIndex]}\"")
                    if (removeIndex < 23)
                        generatedStructure.append(", ")
                }
                if (toIndex < 23) {
                    generatedStructure.append("),$lb")
                } else
                    generatedStructure.append(")$lb")
            } else {
                generatedStructure.append("emptyArray<String>()")
                if (toIndex < 23) {
                    generatedStructure.append("),$lb")
                } else
                    generatedStructure.append(")$lb")
            }
        }
        generatedStructure.append("\t\t\t\t)")
        if (fromIndex < 23)
            generatedStructure.append(",$lb")
        else
            generatedStructure.append("$lb")

    }
    generatedStructure.append("\t\t)$lb")

    // stampa fase 2 senza remove
    generatedStructure.append("$lb$lb$lb")
    generatedStructure.append("\t\tval phase2ActionWithoutRemove = arrayOf($lb")

    for (fromIndex in 0 until 24) {
        // apro gli array interni
        generatedStructure.append("\t\t\tarrayOf( ")
        for (toIndex in 0 until 24) {
            generatedStructure.append("\"${azioniFase2SenzaRemove[fromIndex][toIndex]}\"")
            if (toIndex < 23)
                generatedStructure.append(", ")
        }
        // chiudo gli array interni
        if (fromIndex < 23)
            generatedStructure.append("),$lb")
        else
            generatedStructure.append(")$lb")
    }

    // chiudo array esterno
    generatedStructure.append("\t\t)$lb$lb")


    // stampa fase 3 con remove
    generatedStructure.append("$lb$lb$lb")
    generatedStructure.append("\t\tval phase3ActionWithRemove = arrayOf($lb")

    /*
       arrayOf(

     */

    for (fromIndex in 0 until 24) {
        generatedStructure.append("\t\t\t\tarrayOf($lb")

        /*
            arrayOf(
                        arrayOf(

         */

        for (toIndex in 0 until 24) {

            generatedStructure.append("\t\t\t\t\tarrayOf(")

            /*
            arrayOf(
                        arrayOf(
                                        arrayOf(

            */
            if (!azioniFase2ConRemove[fromIndex][toIndex].isEmpty()) {
                for (removeIndex in 0 until 24) {
                    generatedStructure.append("\"${azioniFase3ConRemove[fromIndex][toIndex][removeIndex]}\"")
                    if (removeIndex < 23)
                        generatedStructure.append(", ")
                }
                if (toIndex < 23) {
                    generatedStructure.append("),$lb")
                } else
                    generatedStructure.append(")$lb")
            } else {
                generatedStructure.append("emptyArray<String>()")
                if (toIndex < 23) {
                    generatedStructure.append("),$lb")
                } else
                    generatedStructure.append(")$lb")
            }
        }
        generatedStructure.append("\t\t\t\t)")
        if (fromIndex < 23)
            generatedStructure.append(",$lb")
        else
            generatedStructure.append("$lb")

    }
    generatedStructure.append("\t\t)$lb")

    // stampa fase 2 senza remove
    generatedStructure.append("$lb$lb$lb")
    generatedStructure.append("\t\tval phase3ActionWithoutRemove = arrayOf($lb")

    for (fromIndex in 0 until 24) {
        // apro gli array interni
        generatedStructure.append("\t\t\tarrayOf( ")
        for (toIndex in 0 until 24) {
            generatedStructure.append("\"${azioniFase3SenzaRemove[fromIndex][toIndex]}\"")
            if (toIndex < 23)
                generatedStructure.append(", ")
        }
        // chiudo gli array interni
        if (fromIndex < 23)
            generatedStructure.append("),$lb")
        else
            generatedStructure.append(")$lb")
    }

    // chiudo array esterno
    generatedStructure.append("\t\t)$lb$lb")


    generatedStructure.append("}$lb")


    val outFile = File("ActionMapper.kt")
    val os = FileOutputStream(outFile).bufferedWriter()

    os.write(generatedStructure.toString())
    os.close()


    println("Fine")
}