package graymatter.sec.common

import org.hashids.Hashids
import java.util.*
import java.util.concurrent.atomic.AtomicLong

class OrderedId private constructor(
    private val a: Long,
    private val b: Long
) : Comparable<OrderedId> {

    constructor() : this(System.currentTimeMillis(), generateNextIdSeq.incrementAndGet() % Long.MAX_VALUE)

    private val value = hashIds.encode(a, b)

    override fun compareTo(other: OrderedId): Int {
        return comparable.compare(this, other)
    }

    override fun equals(other: Any?): Boolean {
        return when {
            other == null -> false
            other === this -> true
            other is OrderedId -> a == other.a && b == other.b
            else -> false
        }
    }

    override fun hashCode(): Int {
        return Objects.hash(a, b)
    }

    override fun toString(): String = value

    companion object {

        @JvmStatic
        fun of(string: String): OrderedId {
            return requireNotNull(tryParse(string))
        }

        private fun tryParse(string: String): OrderedId? {
            val (a,b) = hashIds.decode(string).takeIf { it.size == 2 } ?: return null
            return  OrderedId(a,b)
        }

        private val generateNextIdSeq = AtomicLong(0)
        val comparable = compareBy(OrderedId::a).thenBy(OrderedId::b)
        private val hashIds = Hashids("9939768AE510200E7C8735065FD95AD81A29D98B")
    }
}
