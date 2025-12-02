package com.example.snippetsearcher.execution.dto

data class ExecutionResponseDTO(
    val status: String,
    val output: Collection<String>,
    val runtimeMs: Long,
)
