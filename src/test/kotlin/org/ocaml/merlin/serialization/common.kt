package org.ocaml.merlin.serialization

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import org.ocaml.merlin.serializers.merlinModule


val json: Json by lazy {
    val config = JsonConfiguration.Stable
        .copy(ignoreUnknownKeys = true)
    Json(configuration = config, context = merlinModule)
}

