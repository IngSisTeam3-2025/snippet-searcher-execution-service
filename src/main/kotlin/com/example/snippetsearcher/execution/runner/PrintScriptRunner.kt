package com.example.snippetsearcher.execution.runner

import RunnerResult
import com.example.snippetsearcher.execution.model.PrintScript
import com.example.snippetsearcher.execution.runner.util.BufferDiagnosticReporter
import com.example.snippetsearcher.execution.runner.util.BufferEnvReader
import com.example.snippetsearcher.execution.runner.util.BufferOutputWriter
import com.example.snippetsearcher.execution.runner.util.ExecutionTimer
import com.example.snippetsearcher.execution.runner.util.StringReader
import interpreter.InterpreterRunner
import interpreter.PrintScriptInterpreter
import io.reader.env.EnvReader
import io.reader.input.InputReader
import lexer.PrintScriptLexer
import org.springframework.stereotype.Component
import parser.PrintScriptParser
import validator.PrintScriptValidator

@Component
class PrintScriptRunner : SnippetRunner {

    override fun supports(language: String): Boolean =
        language.equals(PrintScript.name, ignoreCase = true)

    override fun run(
        code: String,
        version: String,
        inputs: Collection<String>,
        envs: Map<String, String>,
    ): RunnerResult {
        val source: InputReader = StringReader(code)
        val input: InputReader = com.example.snippetsearcher.execution.runner.util.BufferInputReader(inputs)
        val env: EnvReader = BufferEnvReader(envs)
        val output = BufferOutputWriter()
        val reporter = BufferDiagnosticReporter()

        val timer = ExecutionTimer()

        timer.start()

        val runner = InterpreterRunner(
            lexer = PrintScriptLexer(),
            parser = PrintScriptParser(),
            validator = PrintScriptValidator(),
            interpreter = PrintScriptInterpreter(),
        )

        runner.run(
            version = version,
            source = source,
            input = input,
            output = output,
            env = env,
            reporter = reporter,
        )

        val elapsed = timer.stop()

        return RunnerResult(
            success = !reporter.hasErrors(),
            output = output.getOutput(),
            diagnostics = reporter.getDiagnostics(),
            runtimeMs = elapsed,
        )
    }
}
