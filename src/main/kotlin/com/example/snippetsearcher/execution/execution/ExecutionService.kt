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
        val input = StringReader("")
        val output = StringOutputWriter()
        val env = mutableMapOf<String, Any>()
        val reporter = { msg: String -> println(msg) }

        runner.run(
            snippet.version,
            input,
            output,
            env,
            reporter
        )

        return ExecutionResponseDTO(
            output = output.toString(),
            env = env
        )
    }

    fun executeTest(snippetId: UUID, testId: UUID): TestExecutionResponseDTO {
        val snippet = client.getSnippetById(snippetId)

        val source = StringReader(snippet.content)
        val input = StringReader("")
        val output = StringOutputWriter()
        val env = mutableMapOf<String, Any>()
        val reporter = { msg: String -> println(msg) }

        runner.run(
            snippet.version,
            input,
            output,
            env,
            reporter
        )

        return TestExecutionResponseDTO(
            output = output.toString(),
            env = env,
            success = true
        )
    }
}