package com.example.snippetsearcher.execution.snippet

import com.example.snippetsearcher.execution.snippet.dto.SnippetResponseDTO
import com.example.snippetsearcher.execution.snippet.dto.TestResponseDTO
import java.util.*

interface SnippetClient {
    fun getSnippetById(snippetId: UUID): SnippetResponseDTO
    fun getTestSnippetById(snippetId: UUID, testId: UUID): TestResponseDTO
}
