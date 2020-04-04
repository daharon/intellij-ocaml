package org.ocaml.merlin.serializers

import kotlinx.serialization.*
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonInput
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

import org.ocaml.merlin.LocateResponseValue

class LocateResponseValueSerializer : KSerializer<LocateResponseValue> {

    override val descriptor: SerialDescriptor = SerialDescriptor(LocateResponseValue::class.qualifiedName!!, PolymorphicKind.SEALED)

    override fun deserialize(decoder: Decoder): LocateResponseValue {
        val input = decoder as? JsonInput
            ?: throw SerializationException("This class can be loaded only by Json")
        val element = input.decodeJson()
        return when (element) {
            is JsonPrimitive -> LocateResponseValue.NotFound(message = element.content)
            is JsonObject -> if ("file" in element) {
                input.json.fromJson(LocateResponseValue.FoundNotLocal.serializer(), element)
            } else {
                input.json.fromJson(LocateResponseValue.FoundLocal.serializer(), element)
            }
            // We're only expecting a string or an object from the locate command.
            is JsonArray -> LocateResponseValue.NotFound(message = element.content.toString())
        }
    }

    override fun serialize(encoder: Encoder, value: LocateResponseValue) {
        TODO("Not yet implemented")
    }
}