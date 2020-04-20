package org.ocaml.ide.runconfig

import com.intellij.execution.configurations.RunProfile
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.runners.DefaultProgramRunner
import com.intellij.openapi.diagnostic.Logger

/**
 * Created by sidharthkuruvila on 18/05/16.
 */
class OcamlRunner : DefaultProgramRunner() {
    companion object {
        private val LOG = Logger.getInstance(OcamlRunner::class.java)
        const val ID = "OcamlRunner"
    }
    override fun canRun(executorId: String, profile: RunProfile): Boolean {
        LOG.debug("Checking if ocaml runner can run for ${OcamlFileRunConfiguration::class.simpleName}")
        return DefaultRunExecutor.EXECUTOR_ID == executorId && profile is OcamlFileRunConfiguration
    }

    override fun getRunnerId(): String = ID
}