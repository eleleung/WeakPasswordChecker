class CuckooHashTable(
    private val numBuckets: Int,
    private val numEntriesPerBucket: Int,
    private val maxNumKicks: Int
) {
    val buckets = Array<ShortArray>(numBuckets) { ShortArray(numEntriesPerBucket) { Short.MAX_VALUE } }

    fun put(fingerprint: Short, index: Int): Boolean {
        val bucket = buckets[index]

        for (i in 0 until numEntriesPerBucket) {
            if (bucket[i] == Short.MAX_VALUE) {
                bucket[i] = fingerprint
                return true
            }
        }
        return false
    }

    fun tryPut(fingerprint: Short, index: Int, altIndex: (newFp: Short, oldIndex: Int) -> Int): Boolean {
        for (i in 0 until maxNumKicks) {
            val entryIx = (0 until numEntriesPerBucket).random()
            val newFingerprint = buckets[index][entryIx]
            buckets[index][entryIx] = fingerprint

            val newIx = altIndex(newFingerprint, index)
            if (put(newFingerprint, newIx)) return true
        }
        return false
    }

    fun hasEntry(fingerprint: Short, index: Int): Boolean {
        val bucket = buckets[index]

        for (i in 0 until numEntriesPerBucket) {
            if (fingerprint == bucket[i]) {
                return true
            }
        }
        return false
    }

    fun removeEntry(fingerprint: Short, index: Int): Boolean {
        val bucket = buckets[index]

        for (i in 0 until numEntriesPerBucket) {
            if (fingerprint == bucket[i]) {
                bucket[i] = 0
                return true
            }
        }
        return false
    }

    fun reset() {
        for (bucket in buckets) {
            for (i in 0 until numEntriesPerBucket) {
                bucket[i] = 0
            }
        }
    }
}