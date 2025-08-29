package com.games.mw.gameservice.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.games.mw.gameservice.IntegrationTestBase
import com.games.mw.gameservice.domain.account.settings.model.Settings
import com.games.mw.gameservice.domain.account.settings.repository.SettingsRepository
import com.games.mw.gameservice.domain.perk.model.Perk
import com.games.mw.gameservice.domain.perk.repository.PerkRepository
import com.games.mw.gameservice.util.JwtTestUtil
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import org.springframework.transaction.annotation.Transactional

@Tag("integration")
@AutoConfigureMockMvc
@Transactional
class PerkControllerIntegrationTests : IntegrationTestBase() {

    @Autowired private lateinit var mockMvc: MockMvc
    @Autowired private lateinit var objectMapper: ObjectMapper
    @Autowired private lateinit var settingsRepository: SettingsRepository
    @Autowired private lateinit var perkRepository: PerkRepository

    private lateinit var userSettings: Settings
    private lateinit var otherUserSettings: Settings
    private lateinit var userToken: String
    private lateinit var otherUserToken: String
    private lateinit var adminToken: String

    @BeforeEach
    fun setup() {
        userSettings = settingsRepository.save(Settings(userId = 1L))
        otherUserSettings = settingsRepository.save(Settings(userId = 2L))
        userToken = JwtTestUtil.generateToken(1L, "user", listOf("USER"))
        otherUserToken = JwtTestUtil.generateToken(2L, "otheruser", listOf("USER"))
        adminToken = JwtTestUtil.generateToken(99L, "admin", listOf("ADMIN"))
    }

    @AfterEach
    fun tearDown() {
        perkRepository.deleteAll()
        settingsRepository.deleteAll()
    }

    @Test
    fun `getPerksBySettingsId should return perks for the owner`() {
        perkRepository.save(Perk(name = "Strength", settings = userSettings))

        mockMvc.get("/perks/settings/{settingsId}", userSettings.id) {
            header("Authorization", "Bearer $userToken")
        }.andExpect {
            status { isOk() }
            jsonPath("$", hasSize<Any>(1))
            jsonPath("$[0].name") { value("Strength") }
        }
    }

    @Test
    fun `getPerksBySettingsId should return 403 Forbidden for non-owner`() {
        mockMvc.get("/perks/settings/{settingsId}", userSettings.id) {
            header("Authorization", "Bearer $otherUserToken")
        }.andExpect {
            status { isForbidden() }
        }
    }

    @Test
    fun `insertPerk should succeed for owner`() {
        val newPerk = Perk(name = "Agility", settings = userSettings)

        mockMvc.post("/perks/") {
            header("Authorization", "Bearer $userToken")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(newPerk)
        }.andExpect {
            status { isOk() }
            jsonPath("$.name") { value("Agility") }
        }
        assertEquals(1, perkRepository.findBySettingsId(userSettings.id!!).size)
    }

    @Test
    fun `updatePerk should succeed for owner`() {
        val existingPerk = perkRepository.save(Perk(name = "OldPerk", settings = userSettings))
        val updatedPerkData = Perk(id = existingPerk.id, name = "NewPerk", settings = userSettings)

        mockMvc.put("/perks/{perkId}", existingPerk.id) {
            header("Authorization", "Bearer $userToken")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(updatedPerkData)
        }.andExpect {
            status { isOk() }
            jsonPath("$.name") { value("NewPerk") }
        }
    }

    @Test
    fun `deleteBySettingsId should succeed for admin`() {
        perkRepository.save(Perk(name = "PerkToDelete", settings = userSettings))

        mockMvc.delete("/perks/settings/{settingsId}", userSettings.id!!) {
            header("Authorization", "Bearer $adminToken")
        }.andExpect {
            status { isOk() }
        }
        assertEquals(0, perkRepository.findBySettingsId(userSettings.id!!).size)
    }

    @Test
    fun `deleteBySettingsId should return 403 Forbidden for non-admin`() {
        mockMvc.delete("/perks/settings/{settingsId}", userSettings.id!!) {
            header("Authorization", "Bearer $userToken")
        }.andExpect {
            status { isForbidden() }
        }
    }
}