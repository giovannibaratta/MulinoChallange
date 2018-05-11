package it.unibo.utils

class Cache<K, V> {
    var hits = 0
        private set
    var total = 0
        private set

    private val map = HashMap<K, V>()

    fun get(k: K): V? {
        val res = map.get(k)
        if (res != null)
            hits++
        total++
        return res
    }

    fun put(k: K, v: V) = map.put(k, v)
}