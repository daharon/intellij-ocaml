package org.ocaml.merlin

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

import org.ocaml.merlin.serializers.LocateResponseValueSerializer


@Serializable(LocateResponseValueSerializer::class)
sealed class LocateResponseValue : MerlinResponseValue {
    @Serializable
    data class NotFound(val message: String) : LocateResponseValue()
    @Serializable
    data class FoundLocal(@SerialName("pos") val position: String) : LocateResponseValue()
    @Serializable
    data class FoundNotLocal(
        val file: String,
        @SerialName("pos")
        val position: String
    ) : LocateResponseValue()
}
