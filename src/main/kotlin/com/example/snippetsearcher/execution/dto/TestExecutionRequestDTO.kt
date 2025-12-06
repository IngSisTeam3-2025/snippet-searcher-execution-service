package com.example.snippetsearcher.execution.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class TestExecutionRequestDTO(
    @field:NotBlank(message = "Content is required")
    val content: String,

    @field:NotBlank(message = "Language is required")
    val language: String,

    @field:NotBlank(message = "Version is required")
    val version: String,

    @field:NotNull(message = "Inputs are required")
    val inputs: Collection<String>,

    @field:NotNull(message = "Outputs are required")
    val outputs: Collection<String>,
)
