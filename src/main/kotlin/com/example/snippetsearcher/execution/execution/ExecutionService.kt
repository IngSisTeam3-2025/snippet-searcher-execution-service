package com.example.snippetsearcher.execution.execution

import com.example.snippetsearcher.execution.execution.dto.ExecutionResponseDTO
import com.example.snippetsearcher.execution.execution.dto.TestExecutionResponseDTO
import com.example.snippetsearcher.execution.execution.util.StringOutputWriter
import com.example.snippetsearcher.execution.execution.util.StringReader
import com.example.snippetsearcher.execution.snippet.SnippetClient
import interpreter.InterpreterRunner
import interpreter.PrintScriptInterpreter
import lexer.PrintScriptLexer
import org.springframework.stereotype.Service
import parser.PrintScriptParser
import validator.PrintScriptValidator
import java.util.*

@Service
class ExecutionService(
    private val client: SnippetClient
) {
    private val lexer = PrintScriptLexer()
    private val parser = PrintScriptParser()
    private val validator = PrintScriptValidator()
    private val interpreter = PrintScriptInterpreter()
    private val runner = InterpreterRunner(lexer, parser, validator, interpreter)

    fun executeSnippet(snippetId: UUID): ExecutionResponseDTO {
        val snippet = client.getSnippetById(snippetId)
        val source = StringReader(snippet.content)
        val input = null
        val output = StringOutputWriter()
        val env = null
        val reporter = null
        runner.run(snippet.version, source)
    }

    fun executeTest(snippetId: UUID, testId: UUID): TestExecutionResponseDTO {
        val snippet = client.getSnippetById(snippetId)
        val source = StringReader(snippet.content)
        val input = null
        val output = StringOutputWriter()
        val env = null
        val reporter = null
        runner.run(snippet.version, source)

    }
}
