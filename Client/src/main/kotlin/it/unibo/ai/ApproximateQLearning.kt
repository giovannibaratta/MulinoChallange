package it.unibo.ai

class ApproximateQLearning<T, E>(private val alpha: () -> Double,
                                 private val discount: () -> Double,
                                 private val explorationRate: () -> Double = { 0.0 },
                                 private val featureExtractors : Array<(T,E) -> Double>,
                                 val weights : Array<Double> = Array(featureExtractors.size,{0.0}),
                                 private val actionsFromState : (T) -> List<E>,
                                 private val applyAction: (T, E) -> Pair<Double, T> // reward State
) {

    private fun qValue(state : T, agentAction : E) =
        featureExtractors.mapIndexed{index, f -> f(state, agentAction) * weights[index]}.sum()

    fun think(state: T): E {
        printActionValue(state)
        val nextActionAndValue = when (Math.random() < explorationRate()) {
            true -> getRandomACtion(state)
            false -> getNextBestAction(state)
        }
        // fine dei giochi
        if (nextActionAndValue == null)
            throw IllegalStateException("Non è possibile effettuare ulteriori mosse")
        val reward = applyAction(state, nextActionAndValue.first)
        println("Reward : " + reward.first)
        val difference = (reward.first + (discount() * (getNextBestAction(reward.second)?.second
                ?: 0.0))) - nextActionAndValue.second
        // aggiorno i pesi
        for (index in weights.indices)
            weights[index] += alpha() * difference * featureExtractors[index](state, nextActionAndValue.first)
        return nextActionAndValue.first
    }

    //debug
    private fun printActionValue(state: T) {
        val actions = actionsFromState(state)
        actions.forEach { println("Action " + it + " - value " + qValue(state, it)) }
    }

    private fun getNextBestAction(state : T)
            = actionsFromState(state)
                    .map { Pair(it, qValue(state,it)) }
                    .maxBy { it.second }

    private fun getRandomACtion(state: T): Pair<E, Double>? {
        val actions = actionsFromState(state)
        if (actions.size <= 0)
            return null
        val random = Math.random() * actions.size - 1
        return Pair(actions[random.toInt()], qValue(state, actions[random.toInt()]))
    }

}