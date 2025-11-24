package com.example.snippetsearcher.execution.snippet.dto

import java.util.*

data class SnippetResponseDTO(
    val id: UUID,
    val name: String,
    val description: String,
    val language: String,
    val version: String,
    val content: String,
    val ownerId: String,

    val inputs: Collection<String> = emptyList(),
)
