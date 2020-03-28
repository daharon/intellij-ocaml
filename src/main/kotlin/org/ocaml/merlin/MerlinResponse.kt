package org.ocaml.merlin

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class MerlinResponse<T : MerlinResponseValue>(
    /**
     * The type of the Merlin response.
     */
    @SerialName("class")
    val clazz: MerlinResponseClass,

    /**
     * Messages that are to be reported to the user.
     */
    val notifications: List<String>,

    val value: T
) {
    @Serializable
    enum class MerlinResponseClass {
        @SerialName("return") RETURN,
        @SerialName("failure") FAILURE,
        @SerialName("error") ERROR,
        @SerialName("exception") EXCEPTION
    }
}
