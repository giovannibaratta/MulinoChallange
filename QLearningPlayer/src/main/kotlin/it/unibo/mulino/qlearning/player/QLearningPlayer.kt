package it.unibo.mulino.qlearning.player

import it.unibo.ai.didattica.mulino.actions.Phase1Action
import it.unibo.ai.didattica.mulino.actions.Phase2Action
import it.unibo.ai.didattica.mulino.actions.PhaseFinalAction
import it.unibo.mulino.ai.ApproximateQLearning
import it.unibo.mulino.player.AIPlayer
import it.unibo.mulino.qlearning.player.model.Action
import it.unibo.mulino.qlearning.player.model.ActionResult
import it.unibo.mulino.qlearning.player.model.Position
import it.unibo.mulino.qlearning.player.model.State
import it.unibo.mulino.qlearning.player.model.State.Type
import it.unibo.utils.Cache
import it.unibo.utils.SquareMatrix
import it.unibo.utils.filterCellIndexed
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.FileReader
import java.util.*
import kotlin.collections.List
import kotlin.collections.any
import kotlin.collections.count
import kotlin.collections.filter
import kotlin.collections.forEach
import kotlin.collections.indices
import kotlin.collections.joinToString
import kotlin.collections.mutableListOf
import it.unibo.ai.didattica.mulino.domain.State as ExternalState

class QLearningPlayer(private val save: Boolean = true) : AIPlayer {

    private val previousState = Stack<State>()
    private var numeroSimulazioni = 0


    //<editor-fold desc="Features">
    /*********************
     *      FEATURES     *
     *********************/

    // TODO("Numero di caselle adiacenti libere disponibili")

    private val bias = { _: State, _: Action, _: State -> 1.0 }
    private val chiudoUnMill = { oldState: State, action: Action, _: State ->
        when (oldState.simulateWithCache(action).mill) {
            true -> 0.3
            false -> 0.0
        }
    }
    private val winningState = { oldState: State, action: Action, _: State ->
        when (oldState.simulateWithCache(action).winState) {
            true -> 1.0
            false -> 0.0
        }
    }

    private val numeroPedineSpostabili = { _: State, _: Action, newState: State ->
        var count = 0
        newState.grid.forEachIndexed { xIndex, yIndex, value ->
            if (value == Type.WHITE) {
                count += newState.adjacent(Position(xIndex, yIndex), true).count { it.second == Type.EMPTY }
            }
        }
        count.toDouble() / 100
    }
    private val numeroPedineSpostabiliAvversario = { _: State, _: Action, newState: State ->
        var count = 0
        newState.grid.forEachIndexed { xIndex, yIndex, value ->
            if (value == Type.BLACK) {
                count += newState.adjacent(Position(xIndex, yIndex), true).count { it.second == Type.EMPTY }
            }
        }
        -count.toDouble() / 100
    }
    private val deniedEnemyMill = { oldState: State, action: Action, _: State ->
        val enemyType = when (oldState.isWhiteTurn) {
            true -> Type.BLACK
            false -> Type.WHITE
        }
        if (oldState.closeAMill(action.to.get(), enemyType).first)
            0.7
        else
            0.0
    }
    // solo fase 1 e 2
    private val rimuovoPedinaChePuoChiudereUnMill = { oldState: State, action: Action, newState: State ->
        var featureValue = 0.0
        if (action.remove.isPresent) {
            // posso rimuove pedina. Controllo che negli spazi adiacenti alla pedina da rimuovere si poteva formare un mill
            val enemyType = when (oldState.isWhiteTurn) {
                true -> Type.BLACK
                false -> Type.WHITE
            }

            if (newState.adjacent(action.remove.get(), true).any {
                        it.second == Type.EMPTY && newState.closeAMill(it.first, enemyType).first
                    })
                featureValue += 0.5
        }
        featureValue
    }
    private val mettoLaPedinaInUnPostoChePuoGenerareUnMill = { oldState: State, action: Action, newState: State ->
        val myType = when (oldState.isWhiteTurn) {
            true -> Type.WHITE
            false -> Type.BLACK
        }

        when (newState.adjacent(action.to.get()).any { it.second == myType }) {
            true -> 0.3 // ho altre mie pedine vicino
            false -> 0.0
        }
    }
    private val enemyOpenMorris = { oldState: State, _: Action, newState: State ->
        val enemyType = when (oldState.isWhiteTurn) {
            true -> Type.BLACK
            false -> Type.WHITE
        }
        var count = 0.0
        newState.grid.forEachIndexed { xIndex, yIndex, type ->
            if (type == Type.EMPTY && newState.closeAMill(Position(xIndex, yIndex), enemyType).first)
                count++
        }
        -count / 10
    }
    private val myOpenMorris = { oldState: State, _: Action, newState: State ->
        val myType = when (oldState.isWhiteTurn) {
            true -> Type.WHITE
            false -> Type.BLACK
        }
        var count = 0.0
        newState.grid.forEachIndexed { xIndex, yIndex, type ->
            if (type == Type.EMPTY && newState.closeAMill(Position(xIndex, yIndex), myType).first)
                count++
        }
        count / 10
    }
    private val enemyCanWin = { _: State, _: Action, newState: State ->

        val invertedState = newState.invert()

        val actions = when (invertedState.gamePhase()) {
            State.GamePhase.PHASE1 -> actionFromStatePhase1(invertedState)
            State.GamePhase.PHASE2 -> actionFromStatePhase2(invertedState)
            State.GamePhase.PHASE3 -> actionFromStatePhase3(invertedState)
        }

        if (actions.any { invertedState.simulateWithCache(it).winState }) -1.0
        else 0.0
    }
    private val struttureAdL = { oldState: State, _: Action, newState: State ->
        val myType = when (oldState.isWhiteTurn) {
            true -> Type.WHITE
            false -> Type.BLACK
        }

        newState.numberOfLBlocks(myType) / 10.0
    }

    private val parallelStructures = { oldState: State, _: Action, newState: State ->
        val myType = when (oldState.isWhiteTurn) {
            true -> Type.WHITE
            false -> Type.BLACK
        }

        newState.parallelStructures(myType) / 10.0
    }

    private val featuresPhase12: Array<(State, Action, State) -> Double> =
            arrayOf(
                    bias,
                    enemyCanWin,
                    chiudoUnMill,
                    deniedEnemyMill,
                    rimuovoPedinaChePuoChiudereUnMill,
                    mettoLaPedinaInUnPostoChePuoGenerareUnMill,
                    winningState,
                    numeroPedineSpostabili,
                    numeroPedineSpostabiliAvversario,
                    enemyOpenMorris,
                    myOpenMorris,
                    struttureAdL,
                    parallelStructures
            )

    private val featuresPhase3: Array<(State, Action, State) -> Double> =
            arrayOf(
                    bias,
                    enemyCanWin,
                    chiudoUnMill,
                    deniedEnemyMill,
                    mettoLaPedinaInUnPostoChePuoGenerareUnMill,
                    winningState,
                    numeroPedineSpostabili,
                    numeroPedineSpostabiliAvversario,
                    enemyOpenMorris,
                    myOpenMorris
            )
    //</editor-fold

    //<editor-fold desc="Pesi">
    /*********************
     *        PESI       *
     *********************/
    private val phase1Weights = Array(featuresPhase12.size, { 0.0 })
    private val phase2Weights = Array(featuresPhase12.size, { 0.0 })
    private val phaseFinalWeights = Array(featuresPhase3.size, { 0.0 })
    //</editor-fold desc="Features">

    //<editor-fold desc="Reward">
    /*********************
     *        REWARD     *
     *********************/
    // TODO("Aggiungere winningState reward")
    // TODO("Creazione strutture in parallelo")
    // TODO("Da struttura vicina a generazione di mill")
    private val phase1Reward: (State, Action, State) -> Double = { oldState, action, newState ->

        /*
            simulazioni sulle mosse dell'avversario
               actionFromStatePhase1(newState)
               per ogni mossa dell'avversario  se mi chiude devo verificare che
               lo stato generato non sia vincente per lui (kill o soffocamento)
         */

        //println(oldState.toString())
        //println(newState.toString())
        var reward = 0.0
        //val simulation = oldState.simulateWithCache(action)

        if (previousState.contains(newState))
            reward -= 25.0
        //if(previousState.peek().whiteBoardCount() > oldState)

        /*
        // reward sul numero nei pezzi
        if (newState.whiteBoardCount() > newState.blackBoardCount()) {
            reward += 0.5
        } else {
            reward -= 0.5
        }*/

        // reward sul mill
        when (action.remove.isPresent) {
            true -> reward += 1.0
            else -> reward += -0.2
        }

        val (playerType, enemyType) = when (oldState.isWhiteTurn) {
            true -> Pair(Type.WHITE, Type.BLACK)
            false -> Pair(Type.BLACK, Type.WHITE)
        }

        // reward per mill bloccato
        if (oldState.closeAMill(action.to.get(), enemyType).first)
            reward += 2.0

        // reward per strutture vicine
        if (newState.adjacent(action.to.get(), false).filter { it == playerType }.any())
            reward += 0.3
        else
            reward -= 0.1

        // reward partita persa per soffocamento
        if (!newState.playerCanMove(playerType)) {
            reward -= 25.0
        }

        // reward partita vinta per soffocamento
        if (!newState.playerCanMove(enemyType)) {
            reward += 25.0
        }

        // reward partita vinta per kill
        if (newState.blackBoardCount() <= 2 && newState.blackHandCount == 0)
            reward += 25.0


        val invertedState = newState.invert()

        val actions = when (invertedState.gamePhase()) {
            State.GamePhase.PHASE1 -> actionFromStatePhase1(invertedState)
            State.GamePhase.PHASE2 -> actionFromStatePhase2(invertedState)
            State.GamePhase.PHASE3 -> actionFromStatePhase3(invertedState)
        }

        if (actions.any { invertedState.simulateWithCache(it).winState }) reward -= 25.0

        reward
    }

    //</editor-fold desc="Reward">

    //<editor-fold desc="Generatore azioni">
    internal val actionFromStatePhase1: (State) -> List<Action> = {
        //println("Azione tipo 1")
        val state = it
        val actionList = mutableListOf<Action>()
        val (myType, enemyType) = when (state.isWhiteTurn) {
            true -> Pair(Type.WHITE, Type.BLACK)
            false -> Pair(Type.BLACK, Type.WHITE)
        }

        val possibleRemove = state.grid
                .filterCellIndexed { it == enemyType }
                .filter { !state.isAClosedMill(Position(it.first.first, it.first.second), enemyType).first }

        val emptyCell = it.grid.filterCellIndexed { it == Type.EMPTY }

        emptyCell.forEach {
            val toPos = Position(it.first.first, it.first.second)
            //var removePos = Optional.empty<Position>()
            if (state.closeAMill(toPos, myType).first) {
                // 1.a
                if (possibleRemove.isEmpty()) {
                    assert(state.isWhiteTurn)
                    if (state.blackBoardCount() == 0)
                        actionList.add(Action.buildPhase1(toPos, Optional.empty()))
                    else {
                        state.grid.forEachIndexed { rIndex, cIndex, value ->
                            if (value == enemyType)
                                actionList.add(Action.buildPhase1(toPos, Optional.of(Position(rIndex, cIndex))))
                        }
                    }
                } else
                    possibleRemove.forEach {
                        actionList.add(Action.buildPhase1(toPos, Optional.of(Position(it.first.first, it.first.second))))
                    }
            } else {
                // 1.b
                actionList.add(Action.buildPhase1(toPos, Optional.empty()))
            }
        }

        actionList
        /* per ogni cella vuota
            1.a) se mulino verifico le celle removibili
            1.b) se non è un mulino inserisco solo le celle vuote
        */
    }

    internal val actionFromStatePhase2: (State) -> List<Action> = {
        //println("Azione tipo 2")
        val state = it
        val actionList = mutableListOf<Action>()
        val (myType, enemyType) = when (state.isWhiteTurn) {
            true -> Pair(Type.WHITE, Type.BLACK)
            false -> Pair(Type.BLACK, Type.WHITE)
        }

        val possibleRemove = state.grid
                .filterCellIndexed { it == enemyType }
                .filter { !state.isAClosedMill(Position(it.first.first, it.first.second), it.second).first }

        it.grid.filterCellIndexed { it == myType }
                .forEach {
                    state.grid[it.first.first, it.first.second] = Type.EMPTY
                    val fromCell = Position(it.first.first, it.first.second)

                    state.adjacent(fromCell, true)
                            .filter { it.second == Type.EMPTY }
                            .forEach {
                                val toCell = it.first
                                if (!state.closeAMill(toCell, myType).first) {
                                    actionList.add(Action.buildPhase2(fromCell, toCell, Optional.empty()))
                                } else {
                                    if (possibleRemove.isEmpty()) {
                                        assert(state.isWhiteTurn)
                                        if (state.blackBoardCount() == 0)
                                            actionList.add(Action.buildPhase2(fromCell, toCell, Optional.empty()))
                                        else {
                                            state.grid.forEachIndexed { rIndex, cIndex, value ->
                                                if (value == enemyType)
                                                    actionList.add(Action.buildPhase2(fromCell, toCell, Optional.of(Position(rIndex, cIndex))))
                                            }
                                        }
                                        //actionList.add(Action.buildPhase2(fromCell, toCell, Optional.empty()))
                                    } else {
                                        possibleRemove.forEach {
                                            val removeCell = Position(it.first.first, it.first.second)
                                            actionList.add(Action.buildPhase2(fromCell, toCell, Optional.of(removeCell)))
                                        }
                                    }
                                }
                            }
                    state.grid[it.first.first, it.first.second] = myType
                }
        actionList
        /*
            Preparazione:
            calcolo le celle dell'avversario

            per ogni mia cella
            1) rimuovo la pedina
            2) la metto in una cella vuota adiacente
                2.a) se mulino verifico le celle removibili
                2.b) se non è un mulino inserisco solo le celle vuote
        */
    }

    internal val actionFromStatePhase3: (State) -> List<Action> = {
        //println("Azione tipo 3")
        val state = it
        val actionList = mutableListOf<Action>()
        val (myType, enemyType) = when (state.isWhiteTurn) {
            true -> Pair(Type.WHITE, Type.BLACK)
            false -> Pair(Type.BLACK, Type.WHITE)
        }

        val possibleRemove = state.grid
                .filterCellIndexed { it == enemyType }
                .filter { !state.isAClosedMill(Position(it.first.first, it.first.second), it.second).first }

        val emptyCell = it.grid.filterCellIndexed { it == Type.EMPTY }

        it.grid.filterCellIndexed { it == myType }
                .forEach {
                    state.grid[it.first.first, it.first.second] = Type.EMPTY
                    val fromCell = Position(it.first.first, it.first.second)
                    emptyCell.forEach {
                        val toCell = Position(it.first.first, it.first.second)
                        if (!state.closeAMill(toCell, myType).first) {
                            actionList.add(Action.buildPhase3(fromCell, toCell, Optional.empty()))
                        } else {
                            // chiuso mill
                            if (possibleRemove.isEmpty()) {
                                assert(state.isWhiteTurn)
                                if (state.blackBoardCount() == 0)
                                    actionList.add(Action.buildPhase3(fromCell, toCell, Optional.empty()))
                                else {
                                    state.grid.forEachIndexed { rIndex, cIndex, value ->
                                        if (value == enemyType)
                                            actionList.add(Action.buildPhase3(fromCell, toCell, Optional.of(Position(rIndex, cIndex))))
                                    }
                                }
                                //actionList.add(Action.buildPhase3(fromCell, toCell, Optional.empty()))
                            } else {
                                possibleRemove.forEach {
                                    val removeCell = Position(it.first.first, it.first.second)
                                    actionList.add(Action.buildPhase3(fromCell, toCell, Optional.of(removeCell)))
                                }
                            }
                        }
                    }
                    state.grid[it.first.first, it.first.second] = myType
                }
        actionList
        /*
            Preparazione:
            calcolo le celle vuote
            calcolo le celle dell'avversario

            per ogni mia cella
            1) rimuovo la pedina
            2) la metto in una cella vuota
                2.a) se mulino verifico le celle removibili
                2.b) se non è un mulino inserisco solo le celle vuote
        */
    }

    //</editor-fold desc="Features">

    private val applyAction: ((State, Action, State) -> Double) -> (State, Action) -> Pair<Double, State> = {
        val rewardFunction = it
        { state, action ->
            val newState = state.simulateWithCache(action).newState
            Pair(rewardFunction(state, action, newState), newState)
        }
    }

    private val discount = 0.99
    private val alpha = 0.00
    private val explorationRate = 0.0

    private val learnerPhase1 = ApproximateQLearning<State, Action>(/*{ 1.0/(numeroSimulazioni+1)*/{ alpha },
            { discount },
            explorationRate = { explorationRate },
            featureExtractors = featuresPhase12,
            weights = phase1Weights,
            actionsFromState = actionFromStatePhase1,
            applyAction = applyAction(phase1Reward))

    private val learnerPhase2 = ApproximateQLearning<State, Action>(/*{ 1.0/(numeroSimulazioni+1)*/{ alpha },
            { discount },
            explorationRate = { explorationRate },
            featureExtractors = featuresPhase12,
            weights = phase2Weights,
            actionsFromState = actionFromStatePhase2,
            applyAction = applyAction(phase1Reward))

    private val learnerPhase3 = ApproximateQLearning<State, Action>(/*{ 1.0/(numeroSimulazioni+1) }*/{ alpha },
            { discount },
            explorationRate = { explorationRate },
            weights = phaseFinalWeights,
            featureExtractors = featuresPhase3,
            actionsFromState = actionFromStatePhase3,
            applyAction = applyAction(phase1Reward))

    override fun playPhase1(state: ExternalState, playerType: ExternalState.Checker): Phase1Action {
        val internalState = normalize(state, playerType).remapToInternal()
        val action = learnerPhase1.think(internalState).rempapToExternalPhase1()
        //val weight = StringBuilder()
        //weight.append("[")
        //learnerPhase1.weights.forEach { weight.append("$it ,") }
        //weight.append("]")
        println("Phase 1 : ${learnerPhase1.weights.joinToString(", ", "[", "]")}")
        previousState.add(internalState)
        numeroSimulazioni++
        return action
    }


    override fun playPhase2(state: ExternalState, playerType: ExternalState.Checker): Phase2Action {
        val internalState = normalize(state, playerType).remapToInternal()
        val action = learnerPhase2.think(internalState).rempapToExternalPhase2()
        previousState.add(internalState)
        println("Phase 2 : ${learnerPhase2.weights.joinToString(", ", "[", "]")}")
        numeroSimulazioni++
        return action
    }

    override fun playPhaseFinal(state: ExternalState, playerType: ExternalState.Checker): PhaseFinalAction {
        val internalState = normalize(state, playerType).remapToInternal()
        val action = learnerPhase3.think(internalState).rempapToExternalPhaseFinal()
        previousState.add(internalState)
        println("Phase 3 : ${learnerPhase3.weights.joinToString(", ", "[", "]")}")
        numeroSimulazioni++
        return action
    }

    internal fun playPhase1(state: State) {
        learnerPhase1.think(state)

        //println("[alpha : ${alpha}] Phase 1 : ${learnerPhase1.weights.joinToString(", ", "[", "]")}")
        numeroSimulazioni++

    }

    internal fun playPhase2(state: State) {
        learnerPhase2.think(state)

        //println("[alpha : ${alpha}] Phase 2 : ${learnerPhase2.weights.joinToString(", ", "[", "]")}")
        numeroSimulazioni++
    }

    internal fun playPhase3(state: State) {
        learnerPhase3.think(state)
        //println("[alpha : ${alpha}] Phase 3 : ${learnerPhase3.weights.joinToString(", ", "[", "]")}")
        numeroSimulazioni++
    }

    internal fun printPar() {
        val sb = StringBuilder()
        sb.append("Stat :\n")
        sb.append("Alpha : ${alpha}\n")
        sb.append("Cache hit : ${(cache.hits.toDouble() / cache.total) * 100}%")
        sb.append("Weights 1 : ${learnerPhase1.weights.joinToString(", ", "[", "]")}\n")
        sb.append("Weights 2 : ${learnerPhase2.weights.joinToString(", ", "[", "]")}\n")
        sb.append("Weights 3 : ${learnerPhase3.weights.joinToString(", ", "[", "]")}\n")
        println(sb.toString())
    }

    private fun normalize(state: ExternalState, playerType: ExternalState.Checker): ExternalState =
            when (playerType) {
                ExternalState.Checker.WHITE -> state
                ExternalState.Checker.BLACK -> {
                    val newBoard = HashMap<String, ExternalState.Checker>()
                    state.board.forEach { key, value ->
                        newBoard.put(key, when (value) {
                            ExternalState.Checker.BLACK -> ExternalState.Checker.WHITE
                            ExternalState.Checker.WHITE -> ExternalState.Checker.BLACK
                            ExternalState.Checker.EMPTY -> ExternalState.Checker.EMPTY
                            null -> throw IllegalArgumentException("null checker type")
                        })
                    }
                    state.board = newBoard
                    val temp = state.whiteCheckers
                    state.whiteCheckers = state.blackCheckers
                    state.blackCheckers = temp
                    state
                }
                else -> throw IllegalArgumentException("Tipo di giocatore non valido")
            }

    private fun ExternalState.remapToInternal() =
            State(this, true)

    private fun Action.rempapToExternalPhase1(): Phase1Action {
        if (this.from.isPresent || !this.to.isPresent)
            throw IllegalStateException("La mossa non può essere convertita")
        val action = Phase1Action()
        action.putPosition = this.to.get().toExternal()
        if (this.remove.isPresent)
            action.removeOpponentChecker = this.remove.get().toExternal()
        return action
    }

    private fun Action.rempapToExternalPhase2(): Phase2Action {
        if (!this.from.isPresent || !this.to.isPresent)
            throw IllegalStateException("La mossa non può essere convertita")
        val action = Phase2Action()
        action.from = this.from.get().toExternal()
        action.to = this.to.get().toExternal()
        if (this.remove.isPresent)
            action.removeOpponentChecker = this.remove.get().toExternal()
        return action
    }

    private fun Action.rempapToExternalPhaseFinal(): PhaseFinalAction {
        if (!this.from.isPresent || !this.to.isPresent)
            throw IllegalStateException("La mossa non può essere convertita")
        val action = PhaseFinalAction()
        action.from = this.from.get().toExternal()
        action.to = this.to.get().toExternal()
        if (this.remove.isPresent)
            action.removeOpponentChecker = this.remove.get().toExternal()
        return action
    }

    private fun Position.toExternal(): String {
        val xPos: Char = 'a' + this.x
        val yPos: String = (this.y + 1).toString()
        return xPos + yPos
    }

    private fun State.simulateWithCache(action: Action): ActionResult {
        val key = Pair(this, action)
        var cached = cache.get(key)
        if (cached == null) {
            cached = this.simulateAction(action)
            cache.put(key, cached)
        }
        return cached
    }
    
    fun load(noError: Boolean) {
        val saveFile = File("weights")
        if (!saveFile.exists()) {
            if (noError)
                return
            throw IllegalStateException("File non esiste")
        }

        val isr = FileReader(saveFile)
        val lines = isr.readLines()
        isr.close()

        if (lines.size != (1 + 1 + phase1Weights.size + phase2Weights.size + phaseFinalWeights.size))
            throw IllegalStateException("File non valido")

        try {
            val versionRead = lines[0].toLong()
            numeroSimulazioni = lines[1].toInt()
            if (versionRead != version)
                throw IllegalStateException("File non valido")
            for (i in phase1Weights.indices)
                phase1Weights[i] = lines[2 + i].toDouble()
            for (i in phase2Weights.indices)
                phase2Weights[i] = lines[2 + i + phase1Weights.size].toDouble()
            for (i in phaseFinalWeights.indices)
                phaseFinalWeights[i] = lines[2 + i + phase1Weights.size + phase2Weights.size].toDouble()

        } catch (e: Exception) {
            throw IllegalStateException("File non valido")
        }

        println("Cariati \n")
        println("ph 1 " + phase1Weights.joinToString(", ", "[", "]"))
        println("ph 2 " + phase2Weights.joinToString(", ", "[", "]"))
        println("ph 3 " + phaseFinalWeights.joinToString(", ", "[", "]"))
    }

    private fun State.invert(): State {
        val newMatrix = SquareMatrix<Type>(this.grid.size, { x, y ->
            when (this.grid[x, y]) {
                Type.EMPTY -> Type.EMPTY
                Type.BLACK -> Type.WHITE
                Type.WHITE -> Type.BLACK
                Type.INVALID -> Type.INVALID
            }
        })
        return State(newMatrix, true, this.blackHandCount, this.whiteHandCount)
    }

    fun save() {
        val saveFile = File("weights")
        if (saveFile.exists()) {
            saveFile.delete()
        }
        saveFile.createNewFile()
        val os = BufferedOutputStream(FileOutputStream(saveFile)).bufferedWriter()
        os.write(version.toString() + System.lineSeparator())
        os.write("" + numeroSimulazioni + System.lineSeparator())
        phase1Weights.forEach { os.write(it.toString() + System.lineSeparator()) }
        phase2Weights.forEach { os.write(it.toString() + System.lineSeparator()) }
        phaseFinalWeights.forEach { os.write(it.toString() + System.lineSeparator()) }
        os.close()
    }

    companion object {
        const val version: Long = 3L
        private val cache = Cache<Pair<State, Action>, ActionResult>()
    }

    override fun matchStart() {
        load(true)
    }

    override fun matchEnd() {
        if (save)
            save()
    }
}