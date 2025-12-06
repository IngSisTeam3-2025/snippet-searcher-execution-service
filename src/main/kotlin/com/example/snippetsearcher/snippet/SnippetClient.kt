package com.example.snippetsearcher.snippet

import com.example.snippetsearcher.snippet.dto.EnvResponseDTO
import java.util.UUID

interface SnippetClient {
    fun getAllEnvs(userId: UUID): Collection<EnvResponseDTO>
}
