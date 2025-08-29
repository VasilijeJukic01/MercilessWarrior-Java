package com.games.mw.gameservice.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.games.mw.gameservice.IntegrationTestBase
import com.games.mw.gameservice.domain.account.settings.model.Settings
import com.games.mw.gameservice.domain.account.settings.repository.SettingsRepository
import com.games.mw.gameservice.util.JwtTestUtil
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import org.springframework.transaction.annotation.Transactional

@Tag("integration")
@AutoConfigureMockMvc
@Transactional
class SettingsControllerIntegrationTests : IntegrationTestBase() {

    @Autowired private lateinit var mockMvc: MockMvc
    @Autowired private lateinit var objectMapper: ObjectMapper
    @Autowired private lateinit var settingsRepository: SettingsRepository

    private val userId = 1L
    private val otherUserId = 2L
    private lateinit var userToken: String
    private lateinit var otherUserToken: String

    @BeforeEach
    fun setup() {
        settingsRepository.deleteAll()
        userToken = JwtTestUtil.generateToken(userId, "user", listOf("USER"))
        otherUserToken = JwtTestUtil.generateToken(otherUserId, "otheruser", listOf("USER"))
    }

    @AfterEach
    fun tearDown() {
        settingsRepository.deleteAll()
    }

    @Test
    fun `getSettingsByUserId should return settings for owner`() {
        settingsRepository.save(Settings(userId = userId, coins = 100))
        mockMvc.get("/settings/{userId}", userId) {
            header("Authorization", "Bearer $userToken")
        }.andExpect {
            status { isOk() }
            jsonPath("$.userId") { value(userId) }
            jsonPath("$.coins") { value(100) }
        }
    }

    @Test
    fun `getSettingsByUserId should return 403 Forbidden for non-owner`() {
        settingsRepository.save(Settings(userId = otherUserId, coins = 100))
        mockMvc.get("/settings/{userId}", otherUserId) {
            header("Authorization", "Bearer $userToken")
        }.andExpect {
            status { isForbidden() }
        }
    }

    @Test
    fun `getSettingsByUserId should return 404 Not Found if settings do not exist`() {
        mockMvc.get("/settings/{userId}", userId) {
            header("Authorization", "Bearer $userToken")
        }.andExpect {
            status { isNotFound() }
        }
    }

    @Test
    fun `insertEmptySettings should create settings without authentication`() {
        val newUserId = 12345L
        mockMvc.post("/settings/empty/{userId}", newUserId)
            .andExpect {
                status { isOk() }
                jsonPath("$.userId") { value(newUserId) }
                jsonPath("$.level") { value(1) }
            }

        val newSettings = settingsRepository.findByUserId(newUserId)
        assertNotNull(newSettings)
        assertEquals(1, newSettings?.level)
    }

    @Test
    fun `updateSettings should succeed for owner`() {
        settingsRepository.save(Settings(userId = userId, level = 5))
        val updatedSettings = Settings(userId = userId, coins = 500, level = 6)

        mockMvc.put("/settings/{userId}", userId) {
            header("Authorization", "Bearer $userToken")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(updatedSettings)
        }.andExpect {
            status { isOk() }
            jsonPath("$.coins") { value(500) }
            jsonPath("$.level") { value(6) }
        }
    }

    @Test
    fun `updateSettings should return 403 Forbidden for non-owner`() {
        settingsRepository.save(Settings(userId = otherUserId))
        val updatedSettings = Settings(userId = otherUserId, coins = 500)

        mockMvc.put("/settings/{userId}", otherUserId) {
            header("Authorization", "Bearer $userToken")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(updatedSettings)
        }.andExpect {
            status { isForbidden() }
        }
    }
}