package graymatter.sec.common.document

import com.fasterxml.jackson.databind.node.ObjectNode
import graymatter.sec.common.crypto.BinaryEncoding
import graymatter.sec.common.encodeBinary
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ParsingTest {

    @Test
    fun visiting() {
        val actual = mutableListOf<String>()
        val expected = """
            /server/host
            /server/adapters/0/protocol
            /server/adapters/0/port
            /server/adapters/1/protocol
            /server/adapters/1/port
            /server/adapters/1/keystore/path
            /server/adapters/1/keystore/format
            /server/adapters/1/keystore/password
        """.trimIndent().split(Regex("\\n"))
        visitNodePathsOf(sample()) {
            println(path)
            actual += path
        }

        assertEquals(expected, actual)
    }

    @Test
    fun testEditing() {

        val encodedPassword = "encoded".encodeToByteArray().encodeBinary(BinaryEncoding.Base16)

        val edited = visitNodePathsOf(sample()) {
            if (path.endsWith("/keystore/password")) {
                set(textNode(encodedPassword))
            }
        }

        println(edited.toPrettyString())

        val actual = edited.at("/server/adapters/1/keystore/password").textValue()

        assertEquals(encodedPassword, actual)

    }


    private fun sample() = jsonOf<ObjectNode>(
        """{
          "server": {
            "host": "localhost",
            "adapters": [
              {
                "protocol": "http",
                "port": 8080
              },
              {
                "protocol": "https",
                "port": 8774,
                "keystore": {
                  "path": "keystore.jks",
                  "format": "JKS",
                  "password": "w6niz798tqdhqfvu"
                }
              }
            ]
          }
        }""".trimIndent()
    )
}
