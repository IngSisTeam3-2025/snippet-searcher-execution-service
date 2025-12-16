package com.example.snippetsearcher.execution.dto

import com.example.snippetsearcher.execution.model.Status

data class ExecutionResponseDTO(
    val status: Status,
    val output: Collection<String>,
    val runtimeMs: Long,
)
