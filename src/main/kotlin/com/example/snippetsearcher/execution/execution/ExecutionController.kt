package com.example.snippetsearcher.execution.execution

import com.example.snippetsearcher.execution.execution.dto.ExecutionResponseDTO
import com.example.snippetsearcher.execution.execution.dto.TestExecutionResponseDTO
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/execution")
class ExecutionController(
    private val service: ExecutionService,
) {

    @PostMapping("/{snippetId}")
    fun executeSnippet(@PathVariable snippetId: UUID): ResponseEntity<ExecutionResponseDTO> =
        ResponseEntity.ok(service.executeSnippet(snippetId))

    @PostMapping("/{snippetId}/tests/{testId}")
    fun executeTest(
        @PathVariable snippetId: UUID,
        @PathVariable testId: UUID,
    ): ResponseEntity<TestExecutionResponseDTO> =
        ResponseEntity.ok(service.executeTest(snippetId, testId))
}
