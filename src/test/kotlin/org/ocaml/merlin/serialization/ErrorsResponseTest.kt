package org.ocaml.merlin.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.list
import kotlinx.serialization.serializer
import org.junit.Test
import org.ocaml.merlin.ErrorsResponseValue
import org.ocaml.merlin.Error
import org.ocaml.merlin.LocateResponseValue
import org.ocaml.merlin.MerlinResponse
import kotlin.test.assertEquals


/**
 * Test de-serialization of Merlin `errors` responses.
 */
class ErrorsResponseTest {

    @Test
    fun `Errors response with no errors`() {
        val expected = emptyList<Error>()
        //val expected = ErrorsResponseValue()
        val response = """{
            "class": "return",
            "value": [],
            "notifications": []
        }""".trimIndent()

        val parsedResponse: MerlinResponse<ErrorsResponseValue> =
            json.parse(MerlinResponse.serializer(ErrorsResponseValue::class.serializer()), response)

        assertEquals(expected, parsedResponse.value)
    }
}