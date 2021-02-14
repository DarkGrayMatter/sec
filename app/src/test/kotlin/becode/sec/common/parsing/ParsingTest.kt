package becode.sec.common.parsing

import com.fasterxml.jackson.databind.node.ObjectNode
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ParsingTest {

    @Test
    fun findPaths() {
        val expectedString = "keystore.jks"
        val actual = sample.at("/server/adapters/")
        println(actual.toPrettyString())
    }

    @Test
    fun visiting() {
        visitNodePathsOf(sample) {
            println(path)
        }
    }


    @Language("JSON")
    private val sample = jsonOf<ObjectNode>(
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
