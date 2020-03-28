package org.ocaml.merlin.serialization

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import org.junit.Test

import org.ocaml.merlin.Locate3
import org.ocaml.merlin.MerlinResponse

import kotlin.test.assertEquals

/**
 * Test de-serialization of Merlin `locate` responses.
 */
class LocateResponseTest {

    private val json: Json by lazy {
        val config = JsonConfiguration.Stable
            .copy(ignoreUnknownKeys = true)
        Json(config)
    }

    @Test
    fun `Locate responds with an error`() {
        val expected = Locate3.NotFound("Not a valid identifier")
        val response = """{
            "class": "return",
            "value": "${expected.message}",
            "notifications":[]
        }""".trimIndent()

        val parsedResponse: MerlinResponse<Locate3> = json.parse(MerlinResponse.serializer(Locate3.serializer()), response)

        assert(parsedResponse.value is Locate3.NotFound)
        assertEquals(expected, parsedResponse.value)
    }

    @Test
    fun `Locate responds with entity located in same file`() {
        val response = """{
            "class": "return",
            "value": {
                "pos": "1:1"   
            },
            "notifications":[]
        }""".trimIndent()
        val expected = Locate3.FoundLocal(position = "1:1")

        val parsedResponse: MerlinResponse<Locate3> = json.parse(MerlinResponse.serializer(Locate3.serializer()), response)

        assert(parsedResponse.value is Locate3.FoundLocal)
        assertEquals(expected.position, (parsedResponse.value as Locate3.FoundLocal).position)
    }

    @Test
    fun `Locate responds with entity located in separate file`() {
        val response = """{
            "class": "return",
            "value": {
                "file": "/path/to/test.ml",
                "pos": "1:1"   
            },
            "notifications":[]
        }""".trimIndent()
        val expected = Locate3.FoundNotLocal(file = "/path/to/test.ml", position = "1:1")

        val parsedResponse: MerlinResponse<Locate3> = json.parse(MerlinResponse.serializer(Locate3.serializer()), response)

        assert(parsedResponse.value is Locate3.FoundNotLocal)
        assertEquals(expected.position, (parsedResponse.value as Locate3.FoundNotLocal).position)
        assertEquals(expected.file, (parsedResponse.value as Locate3.FoundNotLocal).file)
    }
}