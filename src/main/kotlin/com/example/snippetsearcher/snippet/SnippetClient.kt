package com.example.snippetsearcher.snippet

import com.example.snippetsearcher.execution.model.Status
import com.example.snippetsearcher.snippet.dto.EnvResponseDTO
import java.util.UUID

interface SnippetClient {
    fun getAllEnvs(userId: UUID): Collection<EnvResponseDTO>
    fun updateTestStatus(userId: UUID, snippetId: UUID, testId: UUID, status: Status)
}
