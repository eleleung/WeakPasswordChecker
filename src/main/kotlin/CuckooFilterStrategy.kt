import java.io.InputStream

interface CuckooFilterStrategy {
    fun insert(element: String): Boolean

    fun remove(element: String): Boolean

    fun contains(element: String): Boolean

    fun deserialize(input: InputStream): CuckooFilter
}