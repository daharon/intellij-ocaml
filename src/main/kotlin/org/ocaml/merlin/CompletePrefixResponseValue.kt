package org.ocaml.merlin

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

import org.ocaml.merlin.serializers.CompletePrefixResponseValueContextSerializer


@Serializable
data class CompletePrefixResponseValue(
    val context: Context?,
    val entries: List<Entry>
) : MerlinResponseValue {

    @Serializable(CompletePrefixResponseValueContextSerializer::class)
    data class Context(
        val argumentType: String,
        val labels: List<Label>
    )

    @Serializable
    data class Label(
        val name: String,
        val type: String
    )

    @Serializable
    data class Entry(
        val name: String,
        val kind: String,
        @SerialName("desc")
        val description: String,
        val info: String
    )
}
