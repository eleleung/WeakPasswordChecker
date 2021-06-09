import net.openhft.hashing.LongHashFunction
import java.io.InputStream
import kotlin.math.log

class CuckooFilter: CuckooFilterStrategy {
    companion object {
        private const val buckets = 106000
        private const val entriesPerBucket = 6
        private const val bitsPerEntry = 13

        private const val maxNumKicks = 500

        private val FP_HASH = LongHashFunction.metro(1553)
        private val STR_HASH = LongHashFunction.metro(1337)
    }

    val table = CuckooHashTable(buckets, entriesPerBucket, maxNumKicks)

    fun findCapacity(target: Int, loadFactor: Double = 0.95) = target / loadFactor

    fun findFingerprintSize(epsilon: Double, buckets: Int): Int {
        val fp = log(1/epsilon, 2.0) + log(2.0 * buckets, 2.0)

        return if (fp <= 0) {
            1
        } else {
            fp.toInt()
        }
    }

    private fun fingerprint(hash: Short, f: Int): Short {
        check(f > 0) { "f must be greater than 0" }

        return FP_HASH.hashShort(hash).ushr(51).toShort()
    }

    private inline fun <R> hashTableOp(str: String, out: (Short, Int, Int) -> R): R {
        val hash64 = STR_HASH.hashChars(str)
        val firstHash = hash64.toShort()
        val secondHash = hash64.ushr(48).toShort()

        val fp = fingerprint(secondHash, bitsPerEntry)

        val firstIx = index(firstHash.toInt())
        val altIx = altIndex(firstIx.toLong(), fp.toInt())

        return out(fp, firstIx, altIx)
    }

    override fun insert(element: String): Boolean = hashTableOp(element) { fp, first, alt ->
        if (table.put(fp, first) || table.put(fp, alt)) return true

        table.tryPut(fp, listOf(first, alt).shuffled().first()) { newFp, oldIx ->
            altIndex(oldIx.toLong(), newFp.toInt())
        }
    }

    override fun contains(element: String): Boolean = hashTableOp(element) { fp, first, alt ->
        table.hasEntry(fp, first) || table.hasEntry(fp, alt)
    }

    private fun index(hash: Int): Int {
        return Math.floorMod(hash, buckets)
    }

    private fun altIndex(hash: Long, fingerprint: Int): Int {
        return index(hash.xor(FP_HASH.hashInt(fingerprint)).toInt())
    }

    override fun remove(element: String): Boolean {
        if (!contains(element)) return false

        return hashTableOp(element) { fp, first, alt ->
            table.removeEntry(fp, first) || table.removeEntry(fp, alt)
        }
    }

    override fun deserialize(input: InputStream): CuckooFilter {
        val size = deserializeInt(input)
        for (i in 0 until size) deserializeBucket(input, i)

        return this
    }

    private fun deserializeBucket(input: InputStream, bucketNum: Int) {
        val bucketSize = deserializeInt(input)
        for (i in 0 until bucketSize) {
            val content = deserializeShort(input)
            val bucket = table.buckets[bucketNum]

            bucket[i] = content
        }
    }

    private fun deserializeInt(input: InputStream): Int {
        var result = 0

        repeat(4) {
            val next = input.read()

            check(next != -1) { "no bytes to read" }

            result = (result shl 8) or next
        }

        return result
    }

    private fun deserializeShort(input: InputStream): Short {
        var result = 0

        repeat(2) {
            val next = input.read()

            check(next != -1) { "no bytes to read" }

            result = (result shl 8) or next
        }

        return result.toShort()
    }
}