package org.ocaml.merlin.serializers

import kotlinx.serialization.*
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonInput
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

import org.ocaml.merlin.ErrorsResponseValue
import org.ocaml.merlin.Error

class ErrorsResponseValueSerializer : KSerializer<List<Error>> {
    override val descriptor: SerialDescriptor = SerialDescriptor(ErrorsResponseValue::class.qualifiedName!!)

    override fun deserialize(decoder: Decoder): List<Error> {
        val input = decoder as? JsonInput
            ?: throw SerializationException("This class can be loaded only by Json")
        val element = input.decodeJson()
        return when (element) {
            is JsonArray -> input.json.fromJson(ArrayList::class.serializer(), element)
            is JsonPrimitive -> input.json.fromJson(ArrayList::class.serializer(), element)
            is JsonObject -> input.json.fromJson(ArrayList::class.serializer(), element)
        } as List<Error>
    }

    override fun serialize(encoder: Encoder, value: List<Error>) {
        TODO("Not yet implemented")
    }

}

/*
class ErrorsResponseValueSerializer : KSerializer<ErrorsResponseValue> {
    override val descriptor: SerialDescriptor = SerialDescriptor(ErrorsResponseValue::class.qualifiedName!!)

    override fun deserialize(decoder: Decoder): ErrorsResponseValue {
        val input = decoder as? JsonInput
            ?: throw SerializationException("This class can be loaded only by Json")
        val element = input.decodeJson()
        return when (element) {
            is JsonArray -> input.json.fromJson(ArrayList::class.serializer(), element)
            is JsonPrimitive -> input.json.fromJson(ArrayList::class.serializer(), element)
            is JsonObject -> input.json.fromJson(ArrayList::class.serializer(), element)
        } as ErrorsResponseValue
    }

    override fun serialize(encoder: Encoder, value: ErrorsResponseValue) {
        TODO("Not yet implemented")
    }
}
*/