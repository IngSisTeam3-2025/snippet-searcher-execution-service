package com.example.snippetsearcher.snippet.dto

import java.util.UUID

data class SnippetResponseDTO(
    val id: UUID,
    val ownerId: UUID,
    val name: String,
    val description: String?,
    val language: String,
    val version: String,
    val content: String,
    val inputs: Collection<String> = emptyList(),
)
