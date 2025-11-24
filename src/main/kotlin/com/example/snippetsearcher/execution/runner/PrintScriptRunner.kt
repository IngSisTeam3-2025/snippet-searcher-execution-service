package com.example.snippetsearcher.execution.runner

import RunnerResult
import com.example.snippetsearcher.execution.execution.util.StringReader
import com.example.snippetsearcher.execution.runner.util.ExecutionOutputWriter
import com.example.snippetsearcher.execution.runner.util.ExecutionTimer
import com.example.snippetsearcher.execution.runner.util.InMemoryDiagnosticReporter
import com.example.snippetsearcher.execution.runner.util.ListEnvReader
import com.example.snippetsearcher.execution.runner.util.ListInputReader
import interpreter.InterpreterRunner
import interpreter.PrintScriptInterpreter
import io.reader.env.EnvReader
import io.reader.input.InputReader
import lexer.PrintScriptLexer
import org.springframework.stereotype.Service
import parser.PrintScriptParser
import validator.PrintScriptValidator

@Service
class PrintScriptRunner : LanguageRunner {

    override fun supports(language: String): Boolean =
        language.equals("printscript", ignoreCase = true)

    override fun run(
        code: String,
        version: String,
        inputs: Collection<String>,
        envs: Map<String, String>,
    ): RunnerResult {
        val source = StringReader(code)
        val inputReader: InputReader = ListInputReader(inputs)
        val envReader: EnvReader = ListEnvReader(envs)
        val outputWriter = ExecutionOutputWriter()
        val reporter = InMemoryDiagnosticReporter()

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
            input = inputReader,
            output = outputWriter,
            env = envReader,
            reporter = reporter,
        )

        val elapsed = timer.stop()

        return RunnerResult(
            success = !reporter.hasErrors(),
            output = outputWriter.getOutput(),
            diagnostics = reporter.getDiagnostics(),
            runtimeMs = elapsed,
        )
    }
}
