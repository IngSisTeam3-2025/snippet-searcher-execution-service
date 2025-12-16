package com.example.snippetsearcher.asset

import java.util.UUID

interface AssetClient {
    fun getSnippetContent(snippetId: UUID): String
}
