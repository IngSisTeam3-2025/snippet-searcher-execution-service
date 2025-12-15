package com.example.snippetsearcher.execution.dto

import com.example.snippetsearcher.execution.model.Status

data class TestExecutionResponseDTO(
    val status: Status,
    val errors: Collection<String>,
)
