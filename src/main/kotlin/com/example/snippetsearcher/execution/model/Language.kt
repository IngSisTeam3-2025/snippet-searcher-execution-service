package com.example.snippetsearcher.execution.model

interface Language {
    val name: String
}

data object PrintScript : Language {
    override val name: String = "printscript"
}
