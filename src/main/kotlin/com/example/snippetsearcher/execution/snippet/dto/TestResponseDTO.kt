package com.example.snippetsearcher.execution.snippet.dto

import java.util.UUID

data class TestResponseDTO(
    val id: UUID,
    val name: String,
    val snippetId: UUID,
    val inputs: Collection<String>,
    val outputs: Collection<String>,
)
