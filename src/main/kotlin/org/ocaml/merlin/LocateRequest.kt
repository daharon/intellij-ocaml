package org.ocaml.merlin

import com.intellij.openapi.vfs.VirtualFile

data class LocateRequest(
    override val file: VirtualFile,
    val position: Position
) : MerlinRequest {
    override val command: String = "locate"
    override fun parameters(): Array<String> = arrayOf(
        "-filename",
        file.name,
        "-position",
        "${position.line}:${position.col}"
    )
}
