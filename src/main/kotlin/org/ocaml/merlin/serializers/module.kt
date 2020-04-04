package org.ocaml.merlin.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.list
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer
import org.ocaml.merlin.CompletePrefixResponseValue
import org.ocaml.merlin.Error
import org.ocaml.merlin.ErrorsResponseValue
import org.ocaml.merlin.LocateResponseValue
import org.ocaml.merlin.MerlinResponseValue


val merlinModule = SerializersModule {
    polymorphic<MerlinResponseValue> {
        LocateResponseValue::class with LocateResponseValueSerializer()
        CompletePrefixResponseValue::class with CompletePrefixResponseValue.serializer()
        ErrorsResponseValue::class with List::class.serializer() as KSerializer<ErrorsResponseValue>
    }
}