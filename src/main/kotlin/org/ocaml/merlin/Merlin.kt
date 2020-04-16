package org.ocaml.merlin

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter

/**
 * Created by sidharthkuruvila on 30/04/16.
 */
class Merlin(private val objectMapper: ObjectMapper, private val merlinProcess: Process) {

    companion object {
        private val LOG = Logger.getInstance(Merlin::class.java)

        private fun merlinInstance(project: Project): Merlin {
            val p = merlinProcess(project)
            val om = ObjectMapper()
            om.registerModule(KotlinModule())
            val m = Merlin(om, p)
            return m
        }

        private fun merlinProcess(project: Project): Process {
            val rootDir = project.basePath ?: System.getProperty("user.home")
            val pb = OpamCommand.processBuilder(rootDir, "ocamlmerlin")
            return pb.start()
        }

        fun newInstance(project: Project): Merlin {
            return merlinInstance(project)
        }
    }

    private val writer = OutputStreamWriter(merlinProcess.outputStream)
    private val reader = BufferedReader(InputStreamReader(merlinProcess.inputStream))

    fun tellSource(filename: String, source: CharSequence): Boolean {
        val request = """["tell", "start", "end", ${objectMapper.writeValueAsString(source)}]"""
        LOG.info("Tell source request:  $request")
        return makeRequest(filename, request, object : TypeReference<Boolean>() {})
    }

    fun errors(filename: String): List<MerlinError> {
        val request = """["errors"]"""
        LOG.info("Errors request:  $request")
        return makeRequest(filename, request, object : TypeReference<List<MerlinError>>() {})
    }

    fun complete(filename: String, prefix: String, position: Position): Completions {
        val request = """["expand", "prefix", ${objectMapper.writeValueAsString(prefix)}, "at", ${objectMapper.writeValueAsString(position)}]"""
        LOG.info("Complete request:  $request")
        return makeRequest(filename, request, object : TypeReference<Completions>() {})
    }

    fun locate(filename: String, position: Position): LocateResponse {
        val request = """["locate", null, "ml", "at", ${objectMapper.writeValueAsString(position)}]"""
        LOG.info("Locate request:  $request")
        val node = makeRequest(filename, request, object : TypeReference<JsonNode>() {})
        LOG.info("Locate response:  $node")
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
        LOG.info("Document request:  $request")
        return makeRequest(filename, request, object : TypeReference<String>() {})
    }

    fun typeEnclosing(filename: String, position: Position): String {
        val request = """["type", "enclosing", "at", ${objectMapper.writeValueAsString(position)}]"""
        LOG.info("Type Enclosing request:  $request")
        return makeRequest(filename, request, object : TypeReference<String>() {})
    }

//    TODO Not working on merlin, find an alternative.
//    fun dumpTokens(filename: String): List<Token> {
//        val request = """["dump", "tokens"]"""
//        return makeRequest(filename, request, object : TypeReference<List<Token>>() {})
//    }

    fun dumpBrowse(filename: String): List<BrowseNode> {
        val request = """["dump", "browse"]"""
        LOG.info("Dump browse request:  $request")
        return makeRequest(filename, request, object : TypeReference<List<BrowseNode>>() {})
    }

    fun dumpBrowse2(filename: String): JsonNode {
        val request = """["dump", "browse"]"""
        LOG.info("Dump browse #2 request:  $request")
        return makeRequest(filename, request, object : TypeReference<JsonNode>() {})
    }


    private fun <T> makeRequest(filename: String, query: String, c: TypeReference<T>): T {
        val request = """{"context": ["auto", ${objectMapper.writeValueAsString(filename)}],
            "query": $query
        }"""
        writer.write(request)
        writer.write("\n")
        writer.flush()

        val s = reader.readLine()
        val response = extractResponse(objectMapper.readTree(s))
        return objectMapper.convertValue(response, c);
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

data class Position(val line: Int, val col: Int)

data class TellResponse(val cursor: Position, val marker: Boolean)

data class Token(val start: Position, val end: Position, val token: String)

data class BrowseNode(val start: Position, val end: Position, val ghost: Boolean,
                      val attrs: List<String>, val kind: String, val children: List<BrowseNode>)


data class Completions(val entries: List<CompletionEntry>, val context: CompletionContext?)

data class CompletionEntry(val name: String, val kind: String, val desc: String, val info: String)


object CompletionContext

interface LocateResponse

object LocatedAtPosition : LocateResponse
data class LocateFailed(val msg: String) : LocateResponse
data class LocatedInCurrentFile(val pos: Position) : LocateResponse
data class Located(val file: String, val pos: Position): LocateResponse
