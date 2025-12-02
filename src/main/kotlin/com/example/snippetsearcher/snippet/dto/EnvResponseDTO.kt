package com.example.snippetsearcher.snippet.dto

import java.util.UUID

data class EnvResponseDTO(
    val id: UUID,
    val ownerId: UUID,
    val key: String,
    val value: String,
)
