package com.games.mw.gameservice.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.games.mw.gameservice.IntegrationTestBase
import com.games.mw.gameservice.domain.account.settings.model.Settings
import com.games.mw.gameservice.domain.account.settings.repository.SettingsRepository
import com.games.mw.gameservice.domain.item.model.Item
import com.games.mw.gameservice.domain.item.repository.ItemRepository
import com.games.mw.gameservice.util.JwtTestUtil
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
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
class ItemControllerIntegrationTests : IntegrationTestBase() {

    @Autowired private lateinit var mockMvc: MockMvc
    @Autowired private lateinit var objectMapper: ObjectMapper
    @Autowired private lateinit var settingsRepository: SettingsRepository
    @Autowired private lateinit var itemRepository: ItemRepository

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
        itemRepository.deleteAll()
        settingsRepository.deleteAll()
    }

    @Test
    fun `getMasterItemList should return the list of all master items`() {
        mockMvc.get("/items/master") {
            header("Authorization", "Bearer $userToken")
        }.andExpect {
            status { isOk() }
        }
    }

    @Test
    fun `getItemsBySettingsId should return items for the owner`() {
        itemRepository.save(Item(name = "Test Sword", settings = userSettings))

        mockMvc.get("/items/settings/{settingsId}", userSettings.id) {
            header("Authorization", "Bearer $userToken")
        }.andExpect {
            status { isOk() }
            jsonPath("$", hasSize<Any>(1))
            jsonPath("$[0].name") { value("Test Sword") }
        }
    }

    @Test
    fun `getItemsBySettingsId should return 403 Forbidden for non-owner`() {
        mockMvc.get("/items/settings/{settingsId}", userSettings.id) {
            header("Authorization", "Bearer $otherUserToken")
        }.andExpect {
            status { isForbidden() }
        }
    }

    @Test
    fun `insertItem should succeed for owner`() {
        val newItem = Item(name = "New Potion", amount = 5, settings = userSettings)

        mockMvc.post("/items/") {
            header("Authorization", "Bearer $userToken")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(newItem)
        }.andExpect {
            status { isOk() }
            jsonPath("$.name") { value("New Potion") }
        }

        assertEquals(1, itemRepository.findBySettingsId(userSettings.id!!).size)
    }

    @Test
    fun `updateItem should succeed for owner`() {
        val existingItem = itemRepository.save(Item(name = "Old Shield", settings = userSettings))
        val updatedItemData = Item(id = existingItem.id, name = "New Shield", amount = 2, settings = userSettings)

        mockMvc.put("/items/{itemId}", existingItem.id) {
            header("Authorization", "Bearer $userToken")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(updatedItemData)
        }.andExpect {
            status { isOk() }
            jsonPath("$.name") { value("New Shield") }
            jsonPath("$.amount") { value(2) }
        }
    }

    @Test
    fun `deleteBySettingsId should succeed for admin`() {
        itemRepository.save(Item(name = "ItemToDelete", settings = userSettings))

        mockMvc.delete("/items/settings/{settingsId}", userSettings.id!!) {
            header("Authorization", "Bearer $adminToken")
        }.andExpect {
            status { isOk() }
        }

        assertFalse(itemRepository.findById(userSettings.id!!).isPresent)
    }

    @Test
    fun `deleteBySettingsId should return 403 Forbidden for non-admin`() {
        mockMvc.delete("/items/settings/{settingsId}", userSettings.id!!) {
            header("Authorization", "Bearer $userToken")
        }.andExpect {
            status { isForbidden() }
        }
    }
}