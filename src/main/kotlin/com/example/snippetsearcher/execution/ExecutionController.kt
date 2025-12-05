package com.example.snippetsearcher.execution

import com.example.snippetsearcher.execution.dto.ExecutionRequestDTO
import com.example.snippetsearcher.execution.dto.ExecutionResponseDTO
import com.example.snippetsearcher.execution.dto.TestExecutionRequestDTO
import com.example.snippetsearcher.execution.dto.TestExecutionResponseDTO
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/execute")
class ExecutionController(
    private val service: ExecutionService,
) {

    @PostMapping
    fun executeSnippet(
        @RequestHeader("X-User-Id") userId: UUID,
        @RequestBody @Valid request: ExecutionRequestDTO,
    ): ResponseEntity<ExecutionResponseDTO> {
        return ResponseEntity.ok(service.executeSnippet(userId, request))
    }

    @PostMapping("/test")
    fun executeTest(
        @RequestHeader("X-User-Id") userId: UUID,
        @RequestBody @Valid request: TestExecutionRequestDTO,
    ): ResponseEntity<TestExecutionResponseDTO> {
        return ResponseEntity.ok(service.executeTest(userId, request))
    }
}
