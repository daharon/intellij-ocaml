package org.ocaml.merlin

import com.intellij.openapi.vfs.VirtualFile

interface MerlinRequest {
    /**
     * The Merlin command to execute.<br>
     *
     * [Merlin command reference](https://github.com/ocaml/merlin/blob/master/doc/dev/PROTOCOL.md#commands).
     */
    val command: String

    val file: VirtualFile

    /**
     * The parameters required by the [command] as an array of [String]s.
     */
    fun parameters(): Array<String>
}
