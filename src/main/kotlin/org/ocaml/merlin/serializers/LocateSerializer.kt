package org.ocaml.merlin.serializers


import kotlinx.serialization.*
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonInput
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

import org.ocaml.merlin.Locate3


/**
 * Custom serializer for the `locate` Merlin response, which may contain either
 * a JSON string, or two different formats of JSON object.
 */
object LocateSerializer : KSerializer<Locate3> {
    override val descriptor: SerialDescriptor = SerialDescriptor(Locate3::class.qualifiedName!!, PolymorphicKind.SEALED)

    override fun deserialize(decoder: Decoder): Locate3 {
        val input = decoder as? JsonInput
            ?: throw SerializationException("This class can be loaded only by Json")
        val element = input.decodeJson()
        return when (element) {
            is JsonPrimitive -> Locate3.NotFound(message = element.content)
            is JsonObject -> if ("file" in element) {
                input.json.fromJson(Locate3.FoundNotLocal.serializer(), element)
            } else {
                input.json.fromJson(Locate3.FoundLocal.serializer(), element)
            }
            // We're only expecting a string or an object from the locate command.
            is JsonArray -> Locate3.NotFound(message = element.content.toString())
        }
    }

    override fun serialize(encoder: Encoder, value: Locate3) {
        TODO("Not yet implemented")
    }
}
