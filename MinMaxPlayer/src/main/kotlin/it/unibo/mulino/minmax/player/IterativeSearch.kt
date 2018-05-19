package it.unibo.mulino.minmax.player

import com.koloboke.collect.map.hash.HashIntDoubleMaps
import it.unibo.utils.FibonacciHeap
import java.util.*
import kotlin.concurrent.thread
import kotlin.math.max


typealias Player = Int
typealias Action = Int
typealias Actions = MutableList<Int>

class IterativeSearch(
        val game: Game,
        val utilMin: Double,
        val utilMax: Double,
        val eval: (State, Player) -> Double,
        val orderActions: (State, Actions, Player, Int) -> Actions,
        val maxTime: Int) {

    private var depthLimit = 0

    private fun incrementDepthLimit() = depthLimit++
    private fun hasSafeWinner(utilty: Double) = utilty <= utilMin || utilty >= utilMax

    private var exploredNode = 0
    private var timeout = false
    private var heuristicUsed = false
    private val cache = StateCache(4 * 4 * 1250 * 1000)
    private var cacheHit = 0
    private var cacheTotal = 0

    fun makeDecision(state: State): Action {
        thread {
            val startingTime: Long = System.currentTimeMillis()
            var sleptTime = 0L
            val endTime: Long = startingTime + 1000 * maxTime
            var difference = endTime - (sleptTime + startingTime)
            while (difference > 0) {
                try {
                    //val remaingSleepTime = 1000L * (maxTime - sleptTime * 1000)
                    //println("Inizio sleep ${System.currentTimeMillis()}, remaining $remaingSleepTime")
                    //Thread.sleep(remaingSleepTime)
                    Thread.sleep(1000L * (maxTime - sleptTime * 1000))
                    //println("Fine sleep ${System.currentTimeMillis()}")
                } catch (e: InterruptedException) {
                }
                sleptTime += System.currentTimeMillis() - (startingTime + sleptTime)
                difference = endTime - (sleptTime + startingTime)
            }
            timeout = true
        }
        val player = game.getPlayer(state)
        var actions: MutableList<Int> = orderActions(state, game.getActions(state), player, 0)
        depthLimit = 0
        heuristicUsed = false
        var logger: StringBuilder
        val actionValueMap: FibonacciHeap<Action>
        actionValueMap = FibonacciHeap<Action>()

        do {
            cacheTotal = 0
            cacheHit = 0
            exploredNode = 0
            incrementDepthLimit()
            logger = StringBuilder()
            logger.append("[Depth $depthLimit]\n")
            heuristicUsed = false
            var alpha = Double.NEGATIVE_INFINITY
            val beta = Double.POSITIVE_INFINITY
            for (action in actions) {
                val actionValue = minValue(game.getResult(state, action), player, alpha, beta, 1)
                if (timeout)
                    break
                actionValueMap.enqueue(action, -actionValue)
                logger.append("$action -> $actionValue ,")
                alpha = max(actionValue, alpha)
            }
            if (!timeout) {
                //lastBestAction = actionValueMap.min().value
                // ho completato tutto e non è scattato il timeout
                if (actionValueMap.size() > 0 && hasSafeWinner(actionValueMap.min().priority))
                    break
            } else {
                // sono andato in timeout, restituisco l'ultima migliore azione
                logger.append("\nCache Hit ${cacheHit} su ${cacheTotal}")
                logger.append("\nCache size : ${cache.size()}")
                logger.append("\nNodi esplorati : $exploredNode [NON COMPLETATO]")
                println(logger.toString())
                cache.clear()
                if (actionValueMap.isEmpty)
                    return actions[0]
                return actionValueMap.min().value
                        ?: throw IllegalStateException("Non sono riuscito a calcolare neanche una mossa")
            }

            logger.append("\nCache Hit ${cacheHit} su ${cacheTotal}")
            logger.append("\nCache size : ${cache.size()}")
            logger.append("\nNodi esplorati : $exploredNode ")
            println(logger.toString())
            actions = actionValueMap.dequeueAll()/*.toIntArray()*/
        } while (!timeout && heuristicUsed)
        cache.clear()
        return actionValueMap.min().value
    }

    // returns an utility value
    fun maxValue(state: State, player: Player, previousAlpha: Double, previousBeta: Double, depth: Int): Double {
        var alpha = previousAlpha
        exploredNode++
        //updateMetrics(depth)

        if (game.isTerminal(state) || depth >= depthLimit || timeout) {
            return evaluation(state, player, eval)
        } else {
            var value = Double.NEGATIVE_INFINITY
            for (action in orderActions(state, game.getActions(state), player, depth)) {
                value = max(value, minValue(game.getResult(state, action), player, alpha, previousBeta, depth + 1))
                if (value >= previousBeta)
                    return value
                alpha = Math.max(alpha, value)
            }
            return value
        }
    }

    private fun hash(state: State, player: Player): Int = Objects.hash(state, player)

    private inline fun evaluation(state: State, player: Player, eval: (State, Player) -> Double): Double {

        val hash = hash(state, player)
        val cached = cache.retrieve(hash)
        cacheTotal++
        if (cached != null) {
            cacheHit++
            return cached
        }

        var value = when (game.isTerminal(state)) {
            true -> game.getUtility(state, player)
            else -> {
                heuristicUsed = true
                (utilMax + utilMin) / 2
            }
        }
        value += eval(state, player)
        cache.insert(hash, value)
        return value
    }

    // returns an utility value
    fun minValue(state: State, player: Player, previousAlpha: Double, previousBeta: Double, depth: Int): Double {
        var beta = previousBeta
        exploredNode++
        if (game.isTerminal(state) || depth >= depthLimit || timeout) {
            return evaluation(state, player, eval)
        } else {
            var value = Double.POSITIVE_INFINITY
            for (action in orderActions(state, game.getActions(state), player, depth)) {
                value = Math.min(value, maxValue(game.getResult(state, action), //
                        player, previousAlpha, beta, depth + 1))
                if (value <= previousAlpha)
                    return value
                beta = Math.min(beta, value)
            }
            return value
        }
    }

    private class StateCache(val cacheLimit: Int) {

        private val map = HashIntDoubleMaps.newMutableMap(4 * 1000 * 1000).withDefault { Double.NEGATIVE_INFINITY }
        fun size() = map.size

        fun insert(state: Int, value: Double) {
            if (map.size > cacheLimit) {
                println("cleaning")
                for (i in 0 until 100000) {
                    map.remove(map.keys.first())
                }
            }
            map.put(state, value)
        }

        fun clear() {
            map.clear()
        }
        fun retrieve(state: Int): Double? = map.get(state)
    }

}