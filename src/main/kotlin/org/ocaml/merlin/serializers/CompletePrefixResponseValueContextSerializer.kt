package org.ocaml.merlin.serializers

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import org.ocaml.merlin.CompletePrefixResponseValue

class CompletePrefixResponseValueContextSerializer : KSerializer<CompletePrefixResponseValue.Context?> {
    override val descriptor: SerialDescriptor = SerialDescriptor(CompletePrefixResponseValue.Context::class.qualifiedName!!)

    override fun deserialize(decoder: Decoder): CompletePrefixResponseValue.Context? {
        val input = decoder as? JsonInput
            ?: throw SerializationException("This class can be loaded only by Json")
        val element = input.decodeJson()
        return when (element) {
            is JsonNull -> null
            is JsonLiteral -> null
            is JsonObject -> null
            is JsonArray -> {
                // Ignoring the first element.
                val contextElement = element.last()
                input.json.fromJson(CompletePrefixResponseValue.Context.serializer(), contextElement)
            }
        }
    }

    override fun serialize(encoder: Encoder, value: CompletePrefixResponseValue.Context?) {
        TODO("Not yet implemented")
    }
}