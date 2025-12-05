package com.example.snippetsearcher.execution.dto

data class TestExecutionResponseDTO(
    val status: String,
    val errors: Collection<String>,
    val runtimeMs: Long,
)
