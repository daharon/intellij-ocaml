package org.ocaml.merlin

import com.intellij.openapi.vfs.VirtualFile

data class ErrorsRequest(
    override val file: VirtualFile
) : MerlinRequest {
    override val command: String = "errors"
    override fun parameters(): Array<String> = arrayOf("-filename", file.name)
}