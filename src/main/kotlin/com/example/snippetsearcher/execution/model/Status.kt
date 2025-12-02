package com.example.snippetsearcher.execution.model

interface Status {
    val label: String
}

data object Success : Status {
    override val label: String = "success"
}

data object Error : Status {
    override val label: String = "error"
}

data object Passed : Status {
    override val label: String = "passed"
}

data object Failed : Status {
    override val label: String = "failed"
}
