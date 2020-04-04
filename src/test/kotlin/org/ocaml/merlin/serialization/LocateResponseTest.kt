package org.ocaml.merlin.serialization

import org.junit.Test

import org.ocaml.merlin.LocateResponseValue
import org.ocaml.merlin.MerlinResponse

import kotlin.test.assertEquals

/**
 * Test de-serialization of Merlin `locate` responses.
 */
class LocateResponseTest {

    @Test
    fun `Locate responds with an error`() {
        val expected = LocateResponseValue.NotFound("Not a valid identifier")
        val response = """{
            "class": "return",
            "value": "${expected.message}",
            "notifications": []
        }""".trimIndent()

        val parsedResponse: MerlinResponse<LocateResponseValue> =
            json.parse(MerlinResponse.serializer(LocateResponseValue.serializer()), response)

        assert(parsedResponse.value is LocateResponseValue.NotFound)
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
        val expected = LocateResponseValue.FoundLocal(position = "1:1")

        val parsedResponse: MerlinResponse<LocateResponseValue> = json.parse(MerlinResponse.serializer(LocateResponseValue.serializer()), response)

        assert(parsedResponse.value is LocateResponseValue.FoundLocal)
        assertEquals(expected.position, (parsedResponse.value as LocateResponseValue.FoundLocal).position)
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
        val expected = LocateResponseValue.FoundNotLocal(file = "/path/to/test.ml", position = "1:1")

        val parsedResponse: MerlinResponse<LocateResponseValue> = json.parse(MerlinResponse.serializer(LocateResponseValue.serializer()), response)

        assert(parsedResponse.value is LocateResponseValue.FoundNotLocal)
        assertEquals(expected.position, (parsedResponse.value as LocateResponseValue.FoundNotLocal).position)
        assertEquals(expected.file, (parsedResponse.value as LocateResponseValue.FoundNotLocal).file)
    }
}