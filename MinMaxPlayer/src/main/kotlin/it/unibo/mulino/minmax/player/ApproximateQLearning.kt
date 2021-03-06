package it.unibo.mulino.minmax.player

class ApproximateQLearning<T, E>(private val alpha: () -> Double,
                                 private val discount: () -> Double,
                                 private val featureExtractors: Array<(T, E, T) -> Double>,
                                 val weights: DoubleArray = DoubleArray(featureExtractors.size, { 0.0 }),
                                 private val actionsFromState: (T) -> List<E>,
                                 private val explorationRate: Double,
                                 private val applyAction: (T, E) -> Pair<Double, T> // reward State
) {

    private fun qValue(state: T, agentAction: E, newState: T): Double {
        var value = 0.0
        for (i in featureExtractors.indices) {
            value += featureExtractors[i](state, agentAction, newState) * weights[i]
        }
        return value
    }

    fun think(state: T, actions: ArrayList<E>): ArrayList<E>/*List<Pair<E, Double>>*/ {
        //printActionValue(state)
        val nextActionAndValue = actions.map { Pair(it, qValue(state, it, applyAction(state, it).second)) }
                .sortedByDescending { it.second }.toMutableList()

        //val nextActionAndValue = actions
        //nextActionAndValue.sortBy { qValue(state, it, applyAction(state, it).second) }
        if (explorationRate > Math.random()) {
            val random = Math.round(Math.random() * nextActionAndValue.size).toInt() % nextActionAndValue.size
            val action = nextActionAndValue.get(random)
            val first = nextActionAndValue[0]
            nextActionAndValue[0] = action
            nextActionAndValue[random] = first
        }

        val alpha = alpha()
        // fine dei giochi
        if (nextActionAndValue.isEmpty()) return arrayListOf()

        if (alpha <= 0)
            return ArrayList(nextActionAndValue.map { it.first }) // no update

        val bestActionValue = nextActionAndValue.first()

        val reward = applyAction(state, bestActionValue.first)
        println("Reward : " + reward.first)
        val difference = (reward.first +
                (discount() * (getSortedAction(reward.second).firstOrNull()?.second ?: 0.0))) - bestActionValue.second


        // aggiorno i pesi
        for (index in weights.indices)
            weights[index] += alpha * difference * featureExtractors[index](state, bestActionValue.first, reward.second)

        return ArrayList(nextActionAndValue.map { it.first })
    }

    /*
    //debug
    private fun printActionValue(state: T) {
        val actions = actionsFromState(state)
        actions.sortedBy {
            val action = it
            val res = applyAction(state, it)
            var count = 0.0
            featureExtractors.forEachIndexed { index, f -> count += f(state, action, res.second) * weights[index] }
            count
        }.forEach {
            val action = it
            val res = applyAction(state, it)
            println("Action " + it + " - value " + qValue(state, it, res.second))
            featureExtractors.forEachIndexed { index, f ->
                println("f[$index] - ${f(state, action, res.second)} - ${f(state, action, res.second) * weights[index]}")
            }
            println("\n")
        }
    }*/

    private fun getSortedAction(state: T) = actionsFromState(state)
            .map { Pair(it, qValue(state, it, applyAction(state, it).second)) }
            .sortedByDescending { it.second }

}