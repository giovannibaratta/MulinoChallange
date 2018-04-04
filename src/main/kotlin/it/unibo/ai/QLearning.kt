package it.unibo.ai

class ApproximateQLearning<T,E : Enum<*>>(private val alpha : Double,
                                          private val discount : Double,
                                          private val featureExtractors : Array<(T,E) -> Double>,
                                          val weights : Array<Double> = Array(featureExtractors.size,{0.0}),
                                          private val actionsFromState : (T) -> List<E>,
                                          private val applyAction : (T, E) -> Pair<Double,T>){

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

// TESTING
enum class Actions{
    NORTH
}

// tra le feature c'è una qlearning basata sui pesi attuali della ricerca
data class PacmanState(val s : Int)

fun main(args : Array<String>){
    val q = ApproximateQLearning<PacmanState, Actions>(0.003992016,
            1.0,
            arrayOf({
                ps, a->
                if(a==Actions.NORTH)
                    0.5
                else
                    0.0
            },{
                ps, a->
                if(a==Actions.NORTH)
                    1.0
                else
                    0.0
            }),
            arrayOf(4.0,-1.0), { s -> if(s.s == 1)
                    listOf(Actions.NORTH)
    else
    emptyList()},{ps,a->Pair(-500.0,PacmanState(2))})
    println(q.weights.contentDeepToString())
    q.thinkAndExecute(PacmanState(1))
    println(q.weights.contentDeepToString())
}