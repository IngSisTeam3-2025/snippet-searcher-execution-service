package com.example.snippetsearcher.execution.execution.runner.util

import io.reader.env.EnvReader
import model.value.BooleanValue
import model.value.FloatValue
import model.value.IntegerValue
import model.value.StringValue
import model.value.Value
import type.option.Option

class ListEnvReader(
    private val envs: Map<String, String>,
) : EnvReader {

    override fun read(key: String): Option<Value> {
        val raw = envs[key] ?: return Option.None

        val parsed: Value = when {
            raw.equals("true", true) -> BooleanValue(true)
            raw.equals("false", true) -> BooleanValue(false)
            raw.toIntOrNull() != null -> IntegerValue(raw.toInt())
            raw.toFloatOrNull() != null -> FloatValue(raw.toFloat())
            else -> StringValue(raw)
        }

        return Option.Some(parsed)
    }
}
