package org.ocaml.merlin

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter

/**
 * Wrapper class for the Merlin process.
 */
class Merlin(private val project: Project) {

    private val process by lazy {
        val rootDir = project.basePath ?: System.getProperty("user.home")
        OpamCommand
            .processBuilder(rootDir, "ocamlmerlin")
            .start()
    }
    private val writer by lazy { OutputStreamWriter(process.outputStream) }
    private val reader by lazy { BufferedReader(InputStreamReader(process.inputStream)) }

    companion object {
        private val LOG = Logger.getInstance(Merlin::class.java)
        private val objectMapper = ObjectMapper()
            .registerModule(KotlinModule())
            .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
    }

    /**
     * Stop the Merlin process.
     */
    fun close() {
        process.destroy()
    }

    fun tellSource(filename: String, source: CharSequence): Boolean {
        val request = """["tell", "start", "end", ${objectMapper.writeValueAsString(source)}]"""
        LOG.debug("Tell source request:  $request")
        return makeRequest(filename, request, object : TypeReference<Boolean>() {})
    }

    fun errors(filename: String): List<MerlinError> {
        val request = """["errors"]"""
        LOG.debug("Errors request:  $request")
        val response = makeRequest(filename, request, object : TypeReference<List<MerlinError>>() {})
        LOG.debug("Errors response:  $response")
        return response
    }

    fun expandPrefix(filename: String, prefix: String, position: Position): Completions {
        val request = """["expand", "prefix", ${objectMapper.writeValueAsString(prefix)}, "at", ${objectMapper.writeValueAsString(position)}]"""
        LOG.debug("Expand prefix request:  $request")
        val response = makeRequest(filename, request, object : TypeReference<Completions>() {})
        LOG.debug("Expand prefix response:  $response")
        return response
    }

    fun completePrefix(filename: String, prefix: String, position: Position): Completions {
        val request = """["complete", "prefix", ${objectMapper.writeValueAsString(prefix)}, "at", ${objectMapper.writeValueAsString(position)}]"""
        LOG.debug("Complete prefix request:  $request")
        val response = makeRequest(filename, request, object : TypeReference<Completions>() {})
        LOG.debug("Complete prefix response:  $response")
        return response
    }

    fun locate(filename: String, position: Position): LocateResponse {
        val request = """["locate", null, "ml", "at", ${objectMapper.writeValueAsString(position)}]"""
        LOG.debug("Locate request:  $request")
        val node = makeRequest(filename, request, object : TypeReference<JsonNode>() {})
        LOG.debug("Locate response:  $node")
        if(node.isTextual) {
            if(node.textValue() == "Already at definition point") {
                return LocatedAtPosition
            } else {
                return LocateFailed(node.textValue())
            }
        } else {
            if(node.get("file") == null) {
                return objectMapper.treeToValue(node, LocatedInCurrentFile::class.java)
            } else {
                return objectMapper.treeToValue(node, Located::class.java)
            }
        }
    }

    /**
     * Returns OCamldoc documentation as a [String], either for the given qualified identifier
     * or the one at the specified position.
     */
    fun document(filename: String, position: Position, identifier: String?): String {
//        val request = """["document", ${objectMapper.writeValueAsString(identifier)}, "at", ${objectMapper.writeValueAsString(position)}]"""
        val request = """["document", null, "at", ${objectMapper.writeValueAsString(position)}]"""
        LOG.debug("Document request:  $request")
        return makeRequest(filename, request, object : TypeReference<String>() {})
    }

    fun typeEnclosing(filename: String, position: Position): List<TypeDefinition> {
        val request = """["type", "enclosing", "at", ${objectMapper.writeValueAsString(position)}]"""
        LOG.debug("Type Enclosing request:  $request")
        val response = makeRequest(filename, request, object : TypeReference<List<TypeDefinition>>() {})
        LOG.debug("Type Enclosing response:  $response")
        return response
    }

    fun typeExpression(filename: String, text: String, position: Position): List<TypeDefinition> {
        val request = """["type", "expression", ${objectMapper.writeValueAsString(text)}, "at", ${objectMapper.writeValueAsString(position)}]"""
        LOG.debug("Type Expression request:  $request")
        val response = makeRequest(filename, request, object : TypeReference<List<TypeDefinition>>() {})
        LOG.debug("Type Expression response:  $response")
        return response
    }

//    TODO Not working on merlin, find an alternative.
//    fun dumpTokens(filename: String): List<Token> {
//        val request = """["dump", "tokens"]"""
//        return makeRequest(filename, request, object : TypeReference<List<Token>>() {})
//    }

    fun dumpBrowse(filename: String): List<BrowseNode> {
        val request = """["dump", "browse"]"""
        LOG.debug("Dump browse request:  $request")
        return makeRequest(filename, request, object : TypeReference<List<BrowseNode>>() {})
    }

    fun dumpBrowse2(filename: String): JsonNode {
        val request = """["dump", "browse"]"""
        LOG.debug("Dump browse #2 request:  $request")
        return makeRequest(filename, request, object : TypeReference<JsonNode>() {})
    }


    private fun <T> makeRequest(filename: String, query: String, clazz: TypeReference<T>): T {
        val request = """{"context": ["auto", ${objectMapper.writeValueAsString(filename)}],
            "query": $query
        }"""
        LOG.debug("Merlin raw request:  $request")
        writer.write(request)
        writer.write("\n")
        writer.flush()

        val response = reader.readLine()
        LOG.debug("Merlin raw response:  $response")
        val parsedResponse = extractResponse(objectMapper.readTree(response))
        return objectMapper.convertValue(parsedResponse, clazz)
    }

    private fun extractResponse(t: JsonNode): JsonNode {
        val responseType = t.get(0).textValue()
        when (responseType) {
            "return" -> return t.get(1)
            else -> {
                LOG.error("Request failed with ${t.get(0).asText()} response ${t.get(1).toString()}")
                throw RuntimeException(t.get(1).toString())
            }
        }
    }

}

data class MerlinError(val start: Position?, val end: Position?, val valid: Boolean,
                       val message: String, val type: String, val sub: List<JsonNode>)

data class Position(val line: Int, val col: Int) {
    companion object {
        fun fromPsiElement(element: PsiElement): Position {
            val lineColumn = StringUtil.offsetToLineColumn(element.containingFile.text, element.textOffset)
            return Position(
                line = lineColumn.line + 1,
                col = lineColumn.column + 1)
        }
    }
}

data class TellResponse(val cursor: Position, val marker: Boolean)

data class Token(val start: Position, val end: Position, val token: String)

data class BrowseNode(val start: Position, val end: Position, val ghost: Boolean,
                      val attrs: List<String>, val kind: String, val children: List<BrowseNode>)


data class Completions(val entries: List<CompletionEntry>, val context: JsonNode?)

data class CompletionEntry(val name: String, val kind: CompletionEntryKind, val desc: String, val info: String)

enum class CompletionEntryKind(name: String) {
    VALUE("value"),
    VARIANT("variant"),
    CONSTRUCTOR("constructor"),
    LABEL("label"),
    MODULE("module"),
    SIGNATURE("signature"),
    TYPE("type"),
    METHOD("method"),
    METHOD_CALL("#"),
    EXCEPTION("exn"),
    CLASS("class")
}

data class TypeDefinition(val start: Position, val end: Position, val type: String, val tail: TypeDefinitionTail)

enum class TypeDefinitionTail(name: String) {
    NO("no"),
    POSITION("position"),
    CALL("call")
}

object CompletionContext

interface LocateResponse

object LocatedAtPosition : LocateResponse
data class LocateFailed(val msg: String) : LocateResponse
data class LocatedInCurrentFile(val pos: Position) : LocateResponse
data class Located(val file: String, val pos: Position): LocateResponse
