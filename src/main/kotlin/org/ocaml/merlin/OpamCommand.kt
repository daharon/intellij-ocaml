package org.ocaml.merlin

import java.io.File

object OpamCommand {

    fun processBuilder(basePath: String, vararg parameters: String): ProcessBuilder =
        ProcessBuilder()
            .directory(File(basePath))
            .command(
                "bash", "-c",
                makeCommandString(*parameters)
            )

    private fun makeCommandString(vararg parameters: String): String =
        parameters.joinToString(" ")
}