package com.games.mw.gameservice.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.games.mw.gameservice.IntegrationTestBase
import com.games.mw.gameservice.domain.account.settings.model.Settings
import com.games.mw.gameservice.domain.account.settings.repository.SettingsRepository
import com.games.mw.gameservice.domain.item.model.Item
import com.games.mw.gameservice.domain.item.repository.ItemMasterRepository
import com.games.mw.gameservice.domain.item.repository.ItemRepository
import com.games.mw.gameservice.domain.shop.repository.UserShopStockRepository
import com.games.mw.gameservice.domain.shop.transaction.requests.ShopTransactionRequest
import com.games.mw.gameservice.util.JwtTestUtil
import org.hamcrest.Matchers.greaterThan
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

@Tag("integration")
@AutoConfigureWebTestClient
class ShopControllerIntegrationTests : IntegrationTestBase() {

    @Autowired private lateinit var webTestClient: WebTestClient
    @Autowired private lateinit var objectMapper: ObjectMapper
    @Autowired private lateinit var settingsRepository: SettingsRepository
    @Autowired private lateinit var itemRepository: ItemRepository
    @Autowired private lateinit var userShopStockRepository: UserShopStockRepository
    @Autowired private lateinit var itemMasterRepository: ItemMasterRepository

    private lateinit var user: Settings
    private lateinit var token: String

    @BeforeEach
    fun setup() {
        userShopStockRepository.deleteAll()
        itemRepository.deleteAll()
        settingsRepository.deleteAll()

        user = settingsRepository.save(Settings(userId = 1L, coins = 100))
        token = JwtTestUtil.generateToken(1L, "shopuser", listOf("USER"))
    }

    @AfterEach
    fun tearDown() {
        userShopStockRepository.deleteAll()
        itemRepository.deleteAll()
        settingsRepository.deleteAll()
    }

    @Test
    fun `getShopInventory should return available items`() {
        webTestClient.get().uri("/shop/{shopId}", "DEFAULT_SHOP")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$").value(hasSize<Any>(greaterThan(0)))
            .jsonPath("$[0].itemId").isNotEmpty
            .jsonPath("$[0].stock").isNumber
            .jsonPath("$[0].cost").isNumber
    }

    @Test
    fun `buyItem should succeed with sufficient funds`() {
        val request = ShopTransactionRequest(userId = user.userId, username = "shopuser", itemId = "HEALTH_POTION", quantity = 2)

        webTestClient.post().uri("/shop/buy")
            .header("Authorization", "Bearer $token")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.message").isEqualTo("Purchase successful")
            .jsonPath("$.updatedShopInventory[?(@.itemId == 'HEALTH_POTION')].stock").isEqualTo(8)

        val updatedSettings = settingsRepository.findByUserId(user.userId)
        assertEquals(80, updatedSettings?.coins) // 100 - (2 * 10)
        val userItems = itemRepository.findBySettingsId(user.id!!)
        assertEquals(1, userItems.size)
        assertEquals(2, userItems[0].amount)
    }

    @Test
    fun `buyItem should fail with insufficient funds`() {
        val brokeUser = settingsRepository.save(Settings(userId = 2L, coins = 5))
        val brokeToken = JwtTestUtil.generateToken(2L, "brokeuser", listOf("USER"))
        val request = ShopTransactionRequest(userId = brokeUser.userId, username = "brokeuser", itemId = "HEALTH_POTION", quantity = 1)

        webTestClient.post().uri("/shop/buy")
            .header("Authorization", "Bearer $brokeToken")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isBadRequest
            .expectBody(String::class.java)
            .isEqualTo("InsufficientFunds(message=Not enough coins.)")
    }

    @Test
    fun `sellItem should succeed when user has the item`() {
        val oreItem = itemMasterRepository.findById("IRON_ORE").get()
        itemRepository.save(Item(name = oreItem.itemId, amount = 10, settings = user))

        val request = ShopTransactionRequest(userId = user.userId, username = "shopuser", itemId = "IRON_ORE", quantity = 5)

        webTestClient.post().uri("/shop/sell")
            .header("Authorization", "Bearer $token")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.message").isEqualTo("Sale successful")

        val updatedSettings = settingsRepository.findByUserId(user.userId)
        assertEquals(125, updatedSettings?.coins) // 100 + (5 * 5 sellValue)
        val userItems = itemRepository.findBySettingsId(user.id!!)
        assertEquals(5, userItems[0].amount)
    }
}