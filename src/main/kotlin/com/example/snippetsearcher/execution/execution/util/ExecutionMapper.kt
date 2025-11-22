package com.example.snippetsearcher.execution.execution.util

import com.example.snippetsearcher.snippet.snippet.dto.SnippetRequestDTO
import com.example.snippetsearcher.snippet.snippet.dto.SnippetResponseDTO
import com.example.snippetsearcher.snippet.snippet.model.Snippet

object ExecutionMapper {
    fun toEntity(dto: SnippetRequestDTO): Snippet =
        Snippet(
            name = dto.name,
            description = dto.description,
            language = dto.language,
            version = dto.version,
            content = dto.content,
            valid = true,
            ownerId = dto.ownerId,
        )

    fun toResponse(entity: Snippet): SnippetResponseDTO =
        SnippetResponseDTO(
            id = entity.id!!,
            name = entity.name,
            description = entity.description,
            language = entity.language,
            version = entity.version,
            content = entity.content,
            valid = entity.valid,
            ownerId = entity.ownerId,
        )
}
