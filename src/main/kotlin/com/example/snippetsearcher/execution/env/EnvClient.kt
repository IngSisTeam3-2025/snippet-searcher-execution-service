package com.example.snippetsearcher.execution.env

import com.example.snippetsearcher.execution.env.dto.EnvResponseDTO
import java.util.UUID

interface EnvClient {
    fun getEnvsByOwner(ownerId: UUID): Collection<EnvResponseDTO>
}
