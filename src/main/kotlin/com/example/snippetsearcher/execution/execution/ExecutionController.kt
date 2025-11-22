package com.example.snippetsearcher.execution.execution

import com.example.snippetsearcher.execution.execution.dto.ExecutionResponseDTO
import com.example.snippetsearcher.execution.execution.dto.TestExecutionResponseDTO
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/execute")
class ExecutionController(
    private val service: ExecutionService
) {

    @GetMapping("/{snippetId}")
    fun executeSnippet(@PathVariable snippetId: UUID): ResponseEntity<ExecutionResponseDTO> {
        return ResponseEntity.ok(service.executeSnippet(snippetId))
    }

    @PostMapping("/{snippetId}/tests/{testId}")
    fun executeTest(
        @PathVariable snippetId: UUID,
        @PathVariable testId: UUID,
    ): ResponseEntity<TestExecutionResponseDTO> {
        return ResponseEntity.ok(service.executeTest(snippetId, testId))
    }
}
