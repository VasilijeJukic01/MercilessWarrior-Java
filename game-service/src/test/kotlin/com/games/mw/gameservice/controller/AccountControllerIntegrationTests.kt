package com.games.mw.gameservice.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.games.mw.gameservice.IntegrationTestBase
import com.games.mw.gameservice.domain.account.requests.AccountDataDTO
import com.games.mw.gameservice.domain.account.settings.model.Settings
import com.games.mw.gameservice.domain.account.settings.repository.SettingsRepository
import com.games.mw.gameservice.domain.item.model.Item
import com.games.mw.gameservice.domain.item.repository.ItemRepository
import com.games.mw.gameservice.domain.perk.model.Perk
import com.games.mw.gameservice.domain.perk.repository.PerkRepository
import com.games.mw.gameservice.util.JwtTestUtil
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.http.MediaType
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.reactive.server.WebTestClient

@Tag("integration")
@AutoConfigureWebTestClient
@WireMockTest(httpPort = 8081)
class AccountControllerIntegrationTests : IntegrationTestBase() {

    @Autowired private lateinit var webTestClient: WebTestClient
    @Autowired private lateinit var objectMapper: ObjectMapper
    @Autowired private lateinit var settingsRepository: SettingsRepository
    @Autowired private lateinit var itemRepository: ItemRepository
    @Autowired private lateinit var perkRepository: PerkRepository

    companion object {
        @DynamicPropertySource
        @JvmStatic
        fun registerAuthServiceUrl(registry: DynamicPropertyRegistry) {
            registry.add("services.auth.url") { "http://localhost:8081" }
        }
    }

    @BeforeEach
    fun setup() {
        itemRepository.deleteAll()
        perkRepository.deleteAll()
        settingsRepository.deleteAll()
    }

    @AfterEach
    fun tearDown() {
        itemRepository.deleteAll()
        perkRepository.deleteAll()
        settingsRepository.deleteAll()
    }

    @Test
    fun `getAccountData should return account details for authorized user`() {
        val userId = 1L
        val username = "testuser"
        val token = JwtTestUtil.generateToken(userId, username, listOf("USER"))

        val settings = settingsRepository.save(Settings(userId = userId, coins = 100, level = 5, exp = 500))
        itemRepository.save(Item(name = "Sword", amount = 1, equipped = 1, slotIndex = 0, settings = settings))
        perkRepository.save(Perk(name = "Strength", settings = settings))

        stubFor(get(urlEqualTo("/auth/account/$username"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(userId.toString())))

        webTestClient.get().uri("/game/account/{username}", username)
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.username").isEqualTo(username)
            .jsonPath("$.accountId").isEqualTo(userId)
            .jsonPath("$.settingsId").isEqualTo(settings.id!!)
            .jsonPath("$.coins").isEqualTo(100)
            .jsonPath("$.level").isEqualTo(5)
            .jsonPath("$.items[0]").isEqualTo("Sword,1,1,0")
            .jsonPath("$.perks[0]").isEqualTo("Strength")
    }

    @Test
    fun `getAccountData should return 403 Forbidden for unauthorized user`() {
        val requesterId = 1L
        val requesterUsername = "requester"
        val targetUsername = "target"
        val token = JwtTestUtil.generateToken(requesterId, requesterUsername, listOf("USER"))

        webTestClient.get().uri("/game/account/{username}", targetUsername)
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isForbidden
    }

    @Test
    fun `updateAccountData should update details and return 200 OK`() {
        val userId = 2L
        val username = "updateuser"
        val token = JwtTestUtil.generateToken(userId, username, listOf("USER"))

        val settings = settingsRepository.save(Settings(userId = userId, coins = 100, level = 5))

        val updateDto = AccountDataDTO(
            username = username,
            accountId = userId,
            settingsId = settings.id!!,
            spawn = 1,
            coins = 500,
            tokens = 10,
            level = 6,
            exp = 1200,
            items = listOf("Shield,1,1,1", "Potion,5,0,0"),
            perks = listOf("Toughness")
        )

        webTestClient.put().uri("/game/account")
            .header("Authorization", "Bearer $token")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(updateDto)
            .exchange()
            .expectStatus().isOk

        val updatedSettings = settingsRepository.findByUserId(userId)
        val updatedItems = itemRepository.findBySettingsId(settings.id!!)
        val updatedPerks = perkRepository.findBySettingsId(settings.id!!)

        assertEquals(500, updatedSettings?.coins)
        assertEquals(6, updatedSettings?.level)
        assertEquals(2, updatedItems.size)
        assertEquals("Shield", updatedItems.find { it.name == "Shield" }?.name)
        assertEquals(1, updatedPerks.size)
        assertEquals("Toughness", updatedPerks[0].name)
    }
}