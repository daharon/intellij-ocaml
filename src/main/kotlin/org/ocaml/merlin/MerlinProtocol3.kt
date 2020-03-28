package org.ocaml.merlin

import com.fasterxml.jackson.databind.JsonNode
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.vfs.VirtualFile
import kotlinx.serialization.json.*
import kotlinx.serialization.serializer

import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStreamWriter


/*
private enum class MerlinCommand(val value: String) {
    ERRORS("errors"),
    TYPE_ENCLOSING("type-enclosing"),
}
*/

private sealed class MerlinCommand(open val file: VirtualFile) {
    /**
     * The Merlin command to execute.<br>
     *
     * [Merlin command reference](https://github.com/ocaml/merlin/blob/master/doc/dev/PROTOCOL.md#commands).
     */
    abstract val value: String
}

private data class Errors(override val file: VirtualFile) : MerlinCommand(file) {
    override val value = "errors"
}

private data class TypeEnclosing(
    override val file: VirtualFile,
    val position: Position
) : MerlinCommand(file) {
    override val value = "type-enclosing"
}

private data class CompletePrefix(
    override val file: VirtualFile,
    val position: Position,
    val prefix: String
) : MerlinCommand(file) {
    override val value = "complete-prefix"
}

private data class Locate(
    override val file: VirtualFile,
    val position: Position
) : MerlinCommand(file) {
    override val value = "locate"
}

/**
 * Merlin editor service process.
 *
 * [Using Merlin Protocol 3](https://github.com/ocaml/merlin/blob/master/doc/dev/PROTOCOL.md).
 */
class MerlinProtocol3 {

    private val basePath by lazy { System.getProperty("user.home") }
    /*
    private val merlinProcess: Process by lazy {
        val rootDir = System.getProperty("user.home")
        val pb = OpamCommand.processBuilder(rootDir, "ocamlmerlin", "server")
        pb.start()
    }
    private val merlinWriter by lazy { OutputStreamWriter(merlinProcess.outputStream) }
    private val merlinReader by lazy { BufferedReader(InputStreamReader(merlinProcess.inputStream)) }
    */

    companion object {
        private val log = Logger.getInstance(MerlinProtocol3::class.java)
        private val json: Json by lazy {
            val config = JsonConfiguration.Stable
                .copy(ignoreUnknownKeys = true)
            Json(config)
        }
    }

    /**
     * Completes an identifier that the user started to type. Returns a list of possible completions.
     *
     * [Merlin: complete-prefix](https://github.com/ocaml/merlin/blob/master/doc/dev/PROTOCOL.md#complete-prefix--position-position---doc-bool---prefix-string---types-bool-)
     */
    fun completePrefix(file: VirtualFile, prefix: String, position: Position) {
        log.info("Complete-prefix was hit!")
        val command = CompletePrefix(file, position, prefix)
        executeRequest<CompletePrefix2>(command)
    }

    /**
     * Returns a list of errors in current buffer.
     *
     * [Merlin: errors](https://github.com/ocaml/merlin/blob/master/doc/dev/PROTOCOL.md#errors)
     */
    fun errors(file: VirtualFile): List<Error> {
        log.info("Errors checker was hit!")
        val command = Errors(file)
        executeRequest<Error>(command)
        return emptyList()
    }

    /**
     * Finds the declaration of entity at the specified position.
     *
     * [Merlin: locate](https://github.com/ocaml/merlin/blob/master/doc/dev/PROTOCOL.md#locate---prefix-string---position-position---look-for-interfaceimplementation-)
     */
    fun locate(file: VirtualFile, position: Position): MerlinResponse<Locate3> {
        log.info("Locate was hit!")
        val command = Locate(file, position)
        return executeRequest<Locate3>(command)
    }

    private inline fun <reified T : MerlinResponseValue> executeRequest(command: MerlinCommand): MerlinResponse<T> {
        log.info("Processing Merlin command:  $command")
        val parameters: Array<String> = when (command) {
            is CompletePrefix -> arrayOf(
                "-filename",
                command.file.name,
                "-position",
                "${command.position.line}:${command.position.col}",
                "-types",
                "true"
            )
            is Errors -> arrayOf("-filename", command.file.name)
            is Locate -> arrayOf(
                "-filename",
                command.file.name,
                "-position",
                "${command.position.line}:${command.position.col}"
            )
            is TypeEnclosing -> arrayOf(
                "-filename",
                command.file.name,
                "-position",
                "${command.position.line}:${command.position.col}"
            )
        }
        log.info("Merlin parameters:  ${parameters.joinToString()}")
        val merlinProcess = processBuilder(command, *parameters)
            .start()
        log.info("Merlin process:  $merlinProcess")
        val merlinWriter = OutputStreamWriter(merlinProcess.outputStream)
        val merlinReader = BufferedReader(InputStreamReader(merlinProcess.inputStream))
        val fileReader = BufferedReader(InputStreamReader(command.file.inputStream))
        merlinWriter.write(fileReader.readText())
        merlinWriter.flush()
        merlinWriter.close()
        val merlinResponse = merlinReader.readText()
        log.info("Received the following response from Merlin:  $merlinResponse")

        fileReader.close()
        merlinReader.close()
        merlinProcess.waitFor()

        val parsedResponse: MerlinResponse<T> = json.parse(
            MerlinResponse.serializer(T::class.serializer()),
            merlinResponse
        )
        log.info("Parsed response from Merlin:  $parsedResponse")
        return parsedResponse


        /*
        val request = """{"context": ["auto", ${objectMapper.writeValueAsString(filename)}],
            "query": $query
        }"""
        merlinWriter.write(request)
        merlinWriter.write("\n")
        merlinWriter.flush()

        val s = merlinReader.readLine()
        val response = extractResponse(objectMapper.readTree(s))
        return objectMapper.convertValue(response, c);
        */
    }

    private fun extractResponse(t: JsonNode): JsonNode {
        val responseType = t.get(0).textValue()
        when (responseType) {
            "return" -> return t.get(1)
            else -> {
                log.error("Request failed with ${t.get(0).asText()} response ${t.get(1).toString()}")
                throw RuntimeException(t.get(1).toString())
            }
        }
    }

    private fun processBuilder(command: MerlinCommand, vararg parameters: String): ProcessBuilder =
        ProcessBuilder()
            .directory(File(basePath))
            .command(
                "bash", "-c",
                arrayOf("ocamlmerlin", "server", command.value, *parameters)
                    .joinToString(separator = " ")
            )
}