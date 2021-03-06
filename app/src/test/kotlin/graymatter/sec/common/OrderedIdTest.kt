package graymatter.sec.common

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class OrderedIdTest {

    private lateinit var orderedIdSequence:() -> Sequence<OrderedId>

    @BeforeAll
    fun setupAll() {
        orderedIdSequence = { generateSequence(::OrderedId) }
    }

    @Test
    fun testEncodeDecode() {
        val expected = orderedIdSequence().first()
        val given = "$expected"
        val actual = OrderedId.of(given)
        assertEquals(expected, actual)
    }

    @Test
    fun testIdsPreservesOrder() {
        val expected = orderedIdSequence().take(5).toList()
        val shuffled = expected.shuffled()
        assertFalse(expected == shuffled)
        val actual = shuffled.sorted()
        assertEquals(expected, actual)
    }

}
