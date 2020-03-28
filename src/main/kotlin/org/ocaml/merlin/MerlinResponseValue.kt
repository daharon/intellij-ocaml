package org.ocaml.merlin

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

import org.ocaml.merlin.serializers.LocateSerializer


/**
 * Super-class of all Merlin response objects.
 */
@Serializable
sealed class MerlinResponseValue

/**
 * Merlin `complete-prefix` response.
 */
@Serializable
data class CompletePrefix2(
    val context: List<Map<String, String>>?,
    val entries: List<Map<String, String>>
) : MerlinResponseValue()

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
) : MerlinResponseValue() {
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

/**
 * Merlin `locate` response.
 */
@Serializable(LocateSerializer::class)
sealed class Locate3 : MerlinResponseValue() {

    /**
     * The message provided by Merlin if the entity was not found.
     */
    @Serializable
    data class NotFound(val message: String) : Locate3()

    /**
     * The position of the entity when found within the same file.
     */
    @Serializable
    data class FoundLocal(@SerialName("pos") val position: String) : Locate3()

    /**
     * The filename and position of the located entity.
     */
    @Serializable
    data class FoundNotLocal(
        val file: String,
        @SerialName("pos")
        val position: String
    ) : Locate3()
}
