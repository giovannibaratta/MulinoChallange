package it.unibo.ai

class ApproximateQLearning<T, E>(private val alpha: Double,
                                 private val discount : Double,
                                 private val featureExtractors : Array<(T,E) -> Double>,
                                 val weights : Array<Double> = Array(featureExtractors.size,{0.0}),
                                 private val actionsFromState : (T) -> List<E>,
                                 private val applyAction: (T, E) -> Pair<Double, T> // reward State
) {

    private fun qValue(state : T, agentAction : E) =
        featureExtractors.mapIndexed{index, f -> f(state, agentAction) * weights[index]}.sum()

    fun thinkAndExecute(state : T) : Boolean{
        val nextActionAndValue = getNextBestAction(state)
        // fine dei giochi
        if(nextActionAndValue == null)
            return true
        val reward = applyAction(state, nextActionAndValue.first)
        val difference = (reward.first + (discount * (getNextBestAction(reward.second)?.second ?: 0.0))) - nextActionAndValue.second
        // aggiorno i pesi
        for (index in weights.indices)
            weights[index] += alpha * difference * featureExtractors[index](state, nextActionAndValue.first)
        return false
    }

    private fun getNextBestAction(state : T)
            = actionsFromState(state)
                    .map { Pair(it, qValue(state,it)) }
                    .maxBy { it.second }

}