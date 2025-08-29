package com.games.mw.gameservice.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.games.mw.gameservice.IntegrationTestBase
import com.games.mw.gameservice.domain.account.settings.model.Settings
import com.games.mw.gameservice.domain.account.settings.repository.SettingsRepository
import com.games.mw.gameservice.domain.item.model.Item
import com.games.mw.gameservice.domain.item.repository.ItemMasterRepository
import com.games.mw.gameservice.domain.item.repository.ItemRepository
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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.transaction.annotation.Transactional

@Tag("integration")
@AutoConfigureMockMvc
@Transactional
class ShopControllerIntegrationTests : IntegrationTestBase() {

    @Autowired private lateinit var mockMvc: MockMvc
    @Autowired private lateinit var objectMapper: ObjectMapper
    @Autowired private lateinit var settingsRepository: SettingsRepository
    @Autowired private lateinit var itemRepository: ItemRepository
    @Autowired private lateinit var itemMasterRepository: ItemMasterRepository

    private lateinit var user: Settings
    private lateinit var token: String

    @BeforeEach
    fun setup() {
        user = settingsRepository.save(Settings(userId = 1L, coins = 100))
        token = JwtTestUtil.generateToken(1L, "shopuser", listOf("USER"))
    }

    @AfterEach
    fun tearDown() {
        itemRepository.deleteAll()
        settingsRepository.deleteAll()
    }

    @Test
    fun `getShopInventory should return available items`() {
        mockMvc.get("/shop/{shopId}", "DEFAULT_SHOP") {
            header("Authorization", "Bearer $token")
        }.andExpect {
            status { isOk() }
            jsonPath("$", hasSize<Any>(greaterThan(0)))
            jsonPath("$[0].itemId") { value("AETHERIUM_CRYSTAL") }
            jsonPath("$[0].stock") { value(3) }
            jsonPath("$[0].cost") { value(360) }
        }
    }

    @Test
    fun `buyItem should succeed with sufficient funds`() {
        val request = ShopTransactionRequest(userId = user.userId, username = "shopuser", itemId = "HEALTH_POTION", quantity = 2)

        mockMvc.post("/shop/buy") {
            header("Authorization", "Bearer $token")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isOk() }
            jsonPath("$.message") { value("Purchase successful") }
            jsonPath("$.updatedShopInventory[0].stock") { value(3) }
        }

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

        mockMvc.post("/shop/buy") {
            header("Authorization", "Bearer $brokeToken")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isBadRequest() }
            content { string("InsufficientFunds(message=Not enough coins.)") }
        }
    }

    @Test
    fun `sellItem should succeed when user has the item`() {
        val oreItem = itemMasterRepository.findById("IRON_ORE").get()
        itemRepository.save(Item(name = oreItem.itemId, amount = 10, settings = user))

        val request = ShopTransactionRequest(userId = user.userId, username = "shopuser", itemId = "IRON_ORE", quantity = 5)

        mockMvc.post("/shop/sell") {
            header("Authorization", "Bearer $token")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isOk() }
            jsonPath("$.message") { value("Sale successful") }
        }

        val updatedSettings = settingsRepository.findByUserId(user.userId)
        assertEquals(125, updatedSettings?.coins)
        val userItems = itemRepository.findBySettingsId(user.id!!)
        assertEquals(5, userItems[0].amount)
    }
}