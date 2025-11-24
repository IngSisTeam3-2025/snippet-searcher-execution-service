import model.diagnostic.Diagnostic

data class RunnerResult(
    val success: Boolean,
    val output: String,
    val diagnostics: List<Diagnostic>,
    val runtimeMs: Long,
)
