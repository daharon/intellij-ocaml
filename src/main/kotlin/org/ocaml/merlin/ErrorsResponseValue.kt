package org.ocaml.merlin

import kotlinx.serialization.*

//import org.ocaml.merlin.serializers.ErrorsResponseValueSerializer


interface ErrorsResponseValue : List<Error>, MerlinResponseValue
//@Serializable(with = ErrorsResponseValueSerializer::class)
//class ErrorsResponseValue : ArrayList<Error>(), MerlinResponseValue

/**
 * Merlin `error` response.
 */
@Serializable
data class Error(
    /**
     * Omitted if the error has no location.
     */
    val start: String?,
    /**
     * Omitted if the error has no location.
     */
    val end: String?,
    /**
     * Reflects whether Merlin was expecting such an error to be possible or not. Useful for debugging purposes.
     */
    val valid: Boolean,
    /**
     * The error description to be shown to the user.
     */
    val message: String,
    /**
     *  An experimental extension to put more detailed information about type errors,
     *  for instance the location of the field that mismatches between an interface and an implementation.
     */
    @SerialName("sub")
    val subError: List<SubError>,
    /**
     * A classification of the error.
     */
    val type: ErrorType
) {
    @Serializable
    enum class ErrorType {
        @SerialName("type") TYPE,
        @SerialName("parser") PARSER,
        @SerialName("lexer") LEXER,
        @SerialName("env") ENV,
        @SerialName("warning") WARNING,
        @SerialName("unknown") UNKNOWN
    }

    @Serializable
    data class SubError(
        val start: String,
        val end: String,
        val message: String
    )
}
