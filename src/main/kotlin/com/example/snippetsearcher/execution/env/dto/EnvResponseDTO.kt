package com.example.snippetsearcher.execution.env.dto

import java.util.UUID

data class EnvResponseDTO(
    val id: UUID,
    val ownerId: UUID,
    val key: String,
    val value: String,
)
