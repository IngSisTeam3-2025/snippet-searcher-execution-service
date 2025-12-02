package com.example.snippetsearcher.snippet

import com.example.snippetsearcher.snippet.dto.EnvResponseDTO
import com.example.snippetsearcher.snippet.dto.SnippetResponseDTO
import com.example.snippetsearcher.snippet.dto.TestResponseDTO
import java.util.UUID

interface SnippetClient {
    fun getSnippet(userId: UUID, snippetId: UUID): SnippetResponseDTO
    fun getSnippetTest(userId: UUID, snippetId: UUID, testId: UUID): TestResponseDTO
    fun getAllEnvs(userId: UUID): Collection<EnvResponseDTO>
}
