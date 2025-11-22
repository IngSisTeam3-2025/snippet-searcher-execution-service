package com.example.snippetsearcher.execution.snippet

import com.example.snippetsearcher.execution.snippet.dto.SnippetResponseDTO
import java.util.*

interface SnippetClient {
    fun getSnippetById(snippetId: UUID): SnippetResponseDTO
}