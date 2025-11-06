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
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

@Tag("integration")
@AutoConfigureWebTestClient
class ItemControllerIntegrationTests : IntegrationTestBase() {

    @Autowired private lateinit var webTestClient: WebTestClient
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
        webTestClient.get().uri("/items/master")
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isOk
    }

    @Test
    fun `getItemsBySettingsId should return items for the owner`() {
        itemRepository.save(Item(name = "Test Sword", settings = userSettings))

        webTestClient.get().uri("/items/settings/{settingsId}", userSettings.id!!)
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$").value(hasSize<Any>(1))
            .jsonPath("$[0].name").isEqualTo("Test Sword")
    }

    @Test
    fun `getItemsBySettingsId should return 403 Forbidden for non-owner`() {
        webTestClient.get().uri("/items/settings/{settingsId}", userSettings.id!!)
            .header("Authorization", "Bearer $otherUserToken")
            .exchange()
            .expectStatus().isForbidden
    }

    @Test
    fun `insertItem should succeed for owner`() {
        val newItem = Item(name = "New Potion", amount = 5, settings = userSettings)

        webTestClient.post().uri("/items/")
            .header("Authorization", "Bearer $userToken")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(newItem)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.name").isEqualTo("New Potion")

        assertEquals(1, itemRepository.findBySettingsId(userSettings.id!!).size)
    }

    @Test
    fun `updateItem should succeed for owner`() {
        val existingItem = itemRepository.save(Item(name = "Old Shield", settings = userSettings))
        val updatedItemData = Item(id = existingItem.id, name = "New Shield", amount = 2, settings = userSettings)

        webTestClient.put().uri("/items/{itemId}", existingItem.id!!)
            .header("Authorization", "Bearer $userToken")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(updatedItemData)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.name").isEqualTo("New Shield")
            .jsonPath("$.amount").isEqualTo(2)
    }

    @Test
    fun `deleteBySettingsId should succeed for admin`() {
        val itemToDelete = itemRepository.save(Item(name = "ItemToDelete", settings = userSettings))

        webTestClient.delete().uri("/items/settings/{settingsId}", userSettings.id!!)
            .header("Authorization", "Bearer $adminToken")
            .exchange()
            .expectStatus().isOk

        assertFalse(itemRepository.findById(itemToDelete.id!!).isPresent)
    }

    @Test
    fun `deleteBySettingsId should return 403 Forbidden for non-admin`() {
        webTestClient.delete().uri("/items/settings/{settingsId}", userSettings.id!!)
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isForbidden
    }
}