package com.example.snippetsearcher.execution.event

import java.util.UUID

data class TestJobEvent(
    val testId: UUID,
    val snippetId: UUID,
    val ownerId: UUID,
    val language: String,
    val version: String,
    val inputs: Collection<String>,
    val outputs: Collection<String>,
)
