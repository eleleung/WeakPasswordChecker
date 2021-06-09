import org.junit.Test
import java.io.File

class CuckooFilterTest {
    @Test
    fun testFilter() {
        val filter = CuckooFilter()

        println(filter.findFingerprintSize(0.001, 6))
        println(filter.findCapacity(100000, 0.95))

        var errors = 0
        var success = 0
        val file = File("./src/test/kotlin/100000-weak-passwords.txt")

        file.forEachLine { if (!filter.insert(it)) errors += 1 else success += 1 }

        assert(errors == 0)

        val testFile = File("./src/test/kotlin/100000-weak-passwords.txt")
        for (i in 0 until 1) {
            var found = 0
            var notFound = 0

            testFile.forEachLine {
                if (filter.contains(it)) {
                    found += 1
                } else {
                    println(it)
                    notFound += 1
                }
            }

            assert(notFound == 0)
        }
    }
}