package it.unibo.mulino.minmax.player

import it.unibo.ai.didattica.mulino.actions.Phase1Action
import it.unibo.ai.didattica.mulino.actions.Phase2Action
import it.unibo.ai.didattica.mulino.actions.PhaseFinalAction
import it.unibo.ai.didattica.mulino.domain.State
import it.unibo.mulino.player.AIPlayer
import it.unibo.mulino.qlearning.player.QLearningPlayer
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.FileReader
import kotlin.math.roundToInt
import it.unibo.mulino.minmax.player.State as MinMaxState

class Trainer : AIPlayer {

    private val player = QLearningPlayerAlternative({ 0.05 })

    private fun convert(state: State, player: State.Checker): MinMaxState {
        /*
        val game = MulinoGame
        //val startTime = System.nanoTime()
        val diagonalsString = Array(8, { charArrayOf('e', 'e', 'e') })
        // mapping dello stato esterno
        for (position in state.board.keys) {
            val (vertex, level) = game.toInternalPositions[position]!!
            when (state.board[position]) {
                State.Checker.WHITE -> {
                    diagonalsString[vertex][level] = 'w'
                    //game.addPiece(clientState, position, State.Checker.WHITE)
                }
                State.Checker.BLACK -> {
                    diagonalsString[vertex][level] = 'b'
                    //game.addPiece(clientState, position, State.Checker.BLACK)
                }
            //else -> throw IllegalStateException("Nella board c'è un elemeno non valido")
            }
        }
        val diagonals: Array<CharArray> = Array(8, { index -> game.diagonals["${diagonalsString[index][0]}${diagonalsString[index][1]}${diagonalsString[index][2]}"]!! })
        val clientState = MinMaxState(playerType = player, board = diagonals, checkers = intArrayOf(state.whiteCheckers, state.blackCheckers), checkersOnBoard = intArrayOf(state.whiteCheckersOnBoard, state.blackCheckersOnBoard))
        clientState.checkers[0] = state.whiteCheckers
        clientState.checkers[1] = state.blackCheckers
        clientState.currentPhase = when {
            state.currentPhase == State.Phase.SECOND -> 2
            state.currentPhase == State.Phase.FINAL -> 3
            else -> 1
        }*/
        val game = MulinoGame
        val board = intArrayOf(0, 0)
        //val startTime = System.nanoTime()
        //val diagonalsString = Array(8, { charArrayOf('e','e','e')})
        // mapping dello stato esterno
        for (position in state.board.keys) {
            val intPosition = game.toInternalPositions[position]!!
            when (state.board[position]) {
                State.Checker.WHITE -> {
                    board[0] += it.unibo.mulino.minmax.player.State.position[intPosition]
                    //diagonalsString[vertex][level] = 'w'
                    //game.addPiece(clientState, position, State.Checker.WHITE)
                }
                State.Checker.BLACK -> {
                    //diagonalsString[vertex][level] = 'b'
                    board[1] += it.unibo.mulino.minmax.player.State.position[intPosition]
                    //game.addPiece(clientState, position, State.Checker.BLACK)
                }
            }
        }
        val whiteChecker = state.whiteCheckers
        val blackChecker = state.blackCheckers
        val phase = when {
            state.currentPhase == State.Phase.SECOND -> 2
            state.currentPhase == State.Phase.FINAL -> 3
            else -> 1
        }
        //val diagonals :Array<CharArray> = Array(8, {index->game.diagonals["${diagonalsString[index][0]}${diagonalsString[index][1]}${diagonalsString[index][2]}"]!!})
        val clientState = it.unibo.mulino.minmax.player.State(playerType = when (player) {
            State.Checker.WHITE -> MulinoGame.WHITE_PLAYER
            State.Checker.BLACK -> MulinoGame.BLACK_PLAYER
            else -> throw IllegalStateException("Player non valido")
        }, board = board, checkers = intArrayOf(whiteChecker, blackChecker), checkersOnBoard = intArrayOf(state.whiteCheckersOnBoard, state.blackCheckersOnBoard))

        return clientState
    }

    override fun playPhase1(state: State, playerType: State.Checker): Phase1Action {
        val clientState = convert(state, playerType)
        val resAction = when (Math.random() > 0.75) {
            true -> player.playPhase1(clientState, MulinoGame.getActions(clientState)).first()//.first
            false -> {
                val allAction = player.playPhase1(clientState, MulinoGame.getActions(clientState))
                allAction[((Math.random() * allAction.size).roundToInt()) % allAction.size]//.first
            }
        }
        println("Action " + resAction)
        //val resAction : String = player.playPhase1(clientState, MulinoGame.getActions(clientState)).first().first
        //val allAction = player.playPhase1(clientState, MulinoGame.getActions(clientState))
        //val resAction = allAction[(Math.random()*allAction.size).roundToInt()].first
        val action = Phase1Action()
        action.putPosition = resAction.substring(1, 3)
        if (resAction.length > 3)
            action.removeOpponentChecker = resAction.substring(3, 5)

        return action


    }

    override fun playPhase2(state: State, playerType: State.Checker): Phase2Action {
        val clientState = convert(state, playerType)
        //val resAction : String = player.playPhase1(clientState, MulinoGame.getActions(clientState)).first().first
        val resAction = when (Math.random() > 0.75) {
            true -> player.playPhase2(clientState, MulinoGame.getActions(clientState)).first()//.first
            false -> {
                val allAction = player.playPhase2(clientState, MulinoGame.getActions(clientState))
                allAction[((Math.random() * allAction.size).roundToInt()) % allAction.size]//.first
            }
        }
        println("Action " + resAction)
        val action = Phase2Action()
        action.from = resAction.substring(1, 3)
        action.to = resAction.substring(3, 5)
        if (resAction.length > 5)
            action.removeOpponentChecker = resAction.substring(5, 7)

        return action
    }

    override fun playPhaseFinal(state: State, playerType: State.Checker): PhaseFinalAction {
        val clientState = convert(state, playerType)
        //val resAction : String = player.playPhase1(clientState, MulinoGame.getActions(clientState)).first().first
        val resAction = when (Math.random() > 0.75) {
            true -> player.playPhase3(clientState, MulinoGame.getActions(clientState)).first()//.first
            false -> {
                val allAction = player.playPhase3(clientState, MulinoGame.getActions(clientState))
                allAction[((Math.random() * allAction.size).roundToInt()) % allAction.size]//.first
            }
        }
        println("Action " + resAction)
        val action = PhaseFinalAction()
        action.from = resAction.substring(1, 3)
        action.to = resAction.substring(3, 5)
        if (resAction.length > 5)
            action.removeOpponentChecker = resAction.substring(5, 7)


        return action
    }

    override fun matchStart() {
        load(true)
    }

    override fun matchEnd() {
        save()
    }

    fun load(noError: Boolean) {
        val saveFile = File("weightsMinMax")
        if (!saveFile.exists()) {
            if (noError)
                return
            throw IllegalStateException("File non esiste")
        }

        val isr = FileReader(saveFile)
        val lines = isr.readLines()
        isr.close()

        if (lines.size != (1 + player.phase1Weights.size + player.phase2Weights.size + player.phaseFinalWeights.size))
            throw IllegalStateException("File non valido")

        try {
            val versionRead = lines[0].toLong()
            if (versionRead != QLearningPlayer.version)
                throw IllegalStateException("File non valido")
            for (i in player.phase1Weights.indices)
                player.phase1Weights[i] = lines[1 + i].toDouble()
            for (i in player.phase2Weights.indices)
                player.phase2Weights[i] = lines[1 + i + player.phase1Weights.size].toDouble()
            for (i in player.phaseFinalWeights.indices)
                player.phaseFinalWeights[i] = lines[1 + i + player.phase1Weights.size + player.phase2Weights.size].toDouble()

        } catch (e: Exception) {
            throw IllegalStateException("File non valido")
        }

        println("Cariati \n")
        println("ph 1 " + player.phase1Weights.joinToString(", ", "[", "]"))
        println("ph 2 " + player.phase2Weights.joinToString(", ", "[", "]"))
        println("ph 3 " + player.phaseFinalWeights.joinToString(", ", "[", "]"))
    }

    fun save() {
        val saveFile = File("weightsMinMax")
        if (saveFile.exists()) {
            saveFile.delete()
        }
        saveFile.createNewFile()
        val os = BufferedOutputStream(FileOutputStream(saveFile)).bufferedWriter()
        os.write(QLearningPlayer.version.toString() + System.lineSeparator())
        player.phase1Weights.forEach { os.write(it.toString() + System.lineSeparator()) }
        player.phase2Weights.forEach { os.write(it.toString() + System.lineSeparator()) }
        player.phaseFinalWeights.forEach { os.write(it.toString() + System.lineSeparator()) }
        os.close()
    }
}