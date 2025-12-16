package com.example.snippetsearcher.execution

import com.example.snippetsearcher.execution.model.Status
import com.example.snippetsearcher.snippet.SnippetClient
import com.example.snippetsearcher.snippet.dto.EnvResponseDTO
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ExecutionIntegrationTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var snippetClient: SnippetClient

    @TestConfiguration
    class MockConfig {
        @Bean
        fun snippetClient(): SnippetClient = mockk(relaxed = true)
    }

    @BeforeEach
    fun setup() {
        clearMocks(snippetClient)
    }

    @Test
    fun `execute should run PrintScript code and return success`() {
        val userId = UUID.randomUUID()

        every { snippetClient.getAllEnvs(userId) } returns listOf(
            EnvResponseDTO(
                id = UUID.randomUUID(),
                ownerId = userId,
                key = "a",
                value = "10",
            ),
        )

        val request = """
            {
              "content": "let x: number = 5; println(x);",
              "language": "printscript",
              "version": "1.0",
              "inputs": []
            }
        """.trimIndent()

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/execute")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Id", userId.toString())
                .content(request),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value(Status.SUCCESS.name))
            .andExpect(jsonPath("$.output[0]").value("5"))
    }

    @Test
    fun `executeTest should return PASSED when output matches expected`() {
        val userId = UUID.randomUUID()
        val snippetId = UUID.randomUUID()
        val testId = UUID.randomUUID()

        every { snippetClient.getAllEnvs(userId) } returns emptyList()
        justRun { snippetClient.updateTestStatus(userId, snippetId, testId, Status.PASSED) }

        val request = """
            {
              "content": "println(5);",
              "language": "printscript",
              "version": "1.0",
              "inputs": [],
              "outputs": ["5"]
            }
        """.trimIndent()

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/execute/snippets/$snippetId/tests/$testId")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Id", userId.toString())
                .content(request),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value(Status.PASSED.name))
            .andExpect(jsonPath("$.errors").isEmpty)

        verify { snippetClient.updateTestStatus(userId, snippetId, testId, Status.PASSED) }
    }

    @Test
    fun `executeTest should return FAILED when output does not match expected`() {
        val userId = UUID.randomUUID()
        val snippetId = UUID.randomUUID()
        val testId = UUID.randomUUID()

        every { snippetClient.getAllEnvs(userId) } returns emptyList()
        justRun { snippetClient.updateTestStatus(userId, snippetId, testId, Status.FAILED) }

        val request = """
            {
              "content": "println(5);",
              "language": "printscript",
              "version": "1.0",
              "inputs": [],
              "outputs": ["999"]
            }
        """.trimIndent()

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/execute/snippets/$snippetId/tests/$testId")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Id", userId.toString())
                .content(request),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value(Status.FAILED.name))

        verify { snippetClient.updateTestStatus(userId, snippetId, testId, Status.FAILED) }
    }

    @Test
    fun `executeTest should return ERROR when interpreter reports errors`() {
        val userId = UUID.randomUUID()
        val snippetId = UUID.randomUUID()
        val testId = UUID.randomUUID()

        every { snippetClient.getAllEnvs(userId) } returns emptyList()

        val request = """
            {
              "content": "let x: number = 'wrong';",
              "language": "printscript",
              "version": "1.0",
              "inputs": [],
              "outputs": ["anything"]
            }
        """.trimIndent()

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/execute/snippets/$snippetId/tests/$testId")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Id", userId.toString())
                .content(request),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value(Status.ERROR.name))
            .andExpect(jsonPath("$.errors").isArray)

        verify(exactly = 0) { snippetClient.updateTestStatus(any(), any(), any(), any()) }
    }

    @Test
    fun `executeTestStateless should return PASSED when output matches expected`() {
        val userId = UUID.randomUUID()

        every { snippetClient.getAllEnvs(userId) } returns emptyList()

        val request = """
            {
              "content": "println(42);",
              "language": "printscript",
              "version": "1.0",
              "inputs": [],
              "outputs": ["42"]
            }
        """.trimIndent()

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/execute/test")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Id", userId.toString())
                .content(request),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value(Status.PASSED.name))
            .andExpect(jsonPath("$.errors").isEmpty)

        verify(exactly = 0) { snippetClient.updateTestStatus(any(), any(), any(), any()) }
    }

    @Test
    fun `executeTestStateless should return ERROR when interpreter reports errors`() {
        val userId = UUID.randomUUID()

        every { snippetClient.getAllEnvs(userId) } returns emptyList()

        val request = """
            {
              "content": "let x: number = 'wrong';",
              "language": "printscript",
              "version": "1.0",
              "inputs": [],
              "outputs": []
            }
        """.trimIndent()

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/execute/test")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Id", userId.toString())
                .content(request),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value(Status.ERROR.name))
            .andExpect(jsonPath("$.errors").isArray)

        verify(exactly = 0) { snippetClient.updateTestStatus(any(), any(), any(), any()) }
    }
}
