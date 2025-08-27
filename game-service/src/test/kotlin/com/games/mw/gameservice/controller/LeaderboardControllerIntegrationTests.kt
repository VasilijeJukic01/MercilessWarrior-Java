package com.games.mw.gameservice.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.games.mw.gameservice.IntegrationTestBase
import com.games.mw.gameservice.domain.account.settings.model.Settings
import com.games.mw.gameservice.domain.account.settings.repository.SettingsRepository
import com.games.mw.gameservice.util.JwtTestUtil
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.transaction.annotation.Transactional

@AutoConfigureMockMvc
@WireMockTest(httpPort = 8081)
@Transactional
class LeaderboardControllerIntegrationTests : IntegrationTestBase() {

    @Autowired private lateinit var mockMvc: MockMvc
    @Autowired private lateinit var objectMapper: ObjectMapper
    @Autowired private lateinit var settingsRepository: SettingsRepository

    companion object {
        @DynamicPropertySource
        @JvmStatic
        fun registerAuthServiceUrl(registry: DynamicPropertyRegistry) {
            registry.add("services.auth.url") { "http://localhost:8081" }
        }
    }

    @AfterEach
    fun tearDown() {
        settingsRepository.deleteAll()
        resetAllRequests()
    }

    @Test
    fun `getLeaderboard should return sorted list of users by level`() {
        val user1 = "player_high" to 1L
        val user2 = "player_low" to 2L
        val user3 = "player_mid" to 3L
        settingsRepository.saveAll(listOf(
            Settings(userId = user1.second, level = 20, exp = 100),
            Settings(userId = user2.second, level = 5, exp = 50),
            Settings(userId = user3.second, level = 15, exp = 75)
        ))

        val usernames = listOf(user1.first, user2.first, user3.first)
        val token = JwtTestUtil.generateToken(99L, "anyuser", listOf("USER"))

        stubFor(get(urlEqualTo("/auth/usernames"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(objectMapper.writeValueAsString(usernames))))

        stubFor(get(urlEqualTo("/auth/account/${user1.first}"))
            .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(user1.second.toString())))
        stubFor(get(urlEqualTo("/auth/account/${user2.first}"))
            .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(user2.second.toString())))
        stubFor(get(urlEqualTo("/auth/account/${user3.first}"))
            .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(user3.second.toString())))

        mockMvc.get("/leaderboard") {
            header("Authorization", "Bearer $token")
        }.andExpect {
            status { isOk() }
            content { contentType("application/json") }
            jsonPath("$", hasSize<Any>(3))
            jsonPath("$[0].username") { value(user1.first) }
            jsonPath("$[0].level") { value(20) }
            jsonPath("$[1].username") { value(user3.first) }
            jsonPath("$[1].level") { value(15) }
            jsonPath("$[2].username") { value(user2.first) }
            jsonPath("$[2].level") { value(5) }
        }
    }
}