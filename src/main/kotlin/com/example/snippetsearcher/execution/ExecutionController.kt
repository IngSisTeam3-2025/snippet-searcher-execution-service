package com.example.snippetsearcher.execution

import com.example.snippetsearcher.execution.dto.ExecutionResponseDTO
import com.example.snippetsearcher.execution.dto.TestExecutionResponseDTO
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/execute")
class ExecutionController(
    private val service: ExecutionService,
) {

    @PostMapping("/{snippetId}")
    fun executeSnippet(
        @RequestHeader("X-User-Id") userId: UUID,
        @PathVariable snippetId: UUID,
    ): ResponseEntity<ExecutionResponseDTO> {
        return ResponseEntity.ok(service.executeSnippet(userId, snippetId))
    }

    @PostMapping("/{snippetId}/tests/{testId}")
    fun executeTest(
        @RequestHeader("X-User-Id") userId: UUID,
        @PathVariable snippetId: UUID,
        @PathVariable testId: UUID,
    ): ResponseEntity<TestExecutionResponseDTO> {
        return ResponseEntity.ok(service.executeTest(snippetId, testId, userId))
    }
}
