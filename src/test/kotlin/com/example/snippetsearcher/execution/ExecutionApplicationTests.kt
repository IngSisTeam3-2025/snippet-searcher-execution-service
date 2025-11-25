package com.example.snippetsearcher.execution

import com.example.snippetsearcher.execution.env.EnvClient
import com.example.snippetsearcher.execution.snippet.SnippetClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean

@SpringBootTest
@ActiveProfiles("test")
class ExecutionApplicationTests {

    @MockitoBean
    lateinit var envClient: EnvClient

    @MockitoBean
    lateinit var snippetClient: SnippetClient
}
