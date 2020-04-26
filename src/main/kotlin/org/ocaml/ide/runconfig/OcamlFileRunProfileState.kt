package org.ocaml.ide.runconfig

import com.intellij.execution.configurations.CommandLineState
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.runners.ExecutionEnvironment
import java.io.File

/**
 * Created by sidharthkuruvila on 17/05/16.
 */

class OcamlFileRunProfileState(environment: ExecutionEnvironment, private val filename: String) : CommandLineState(environment) {

    override fun startProcess(): ProcessHandler {
        val commandLine = GeneralCommandLine("bash", "-c", buildAndRunCmd(environment, filename))
        environment.project.basePath?.run {
            commandLine.workDirectory = File(this)
        }
        return OSProcessHandler(commandLine)
    }

    private fun buildAndRunCmd(environment: ExecutionEnvironment, filepath: String) : String {
        val basePath = environment.project.basePath

        val relFilename = File(filepath).toRelativeString(File(basePath))

        if(!filepath.endsWith(".ml")) {
            throw IllegalStateException("Can only run ml files")
        }

        val outputFile = relFilename.toString().replace(".ml", ".native")
        return "pwd && ocamlbuild $outputFile && ./$outputFile"
    }
}