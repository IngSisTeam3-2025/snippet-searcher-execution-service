package com.example.snippetsearcher.execution.execution.dto

data class ExecutionResponseDTO(
    val status: String,
    val output: String,
    val runtimeMs: Long,
)
