package com.games.mw.gameservice.service

import arrow.core.left
import arrow.core.right
import com.games.mw.gameservice.domain.account.AccountService
import com.games.mw.gameservice.domain.account.requests.AccountDataDTO
import com.games.mw.gameservice.domain.account.settings.SettingsService
import com.games.mw.gameservice.domain.account.settings.model.Settings
import com.games.mw.gameservice.domain.item.ItemService
import com.games.mw.gameservice.domain.item.model.Item
import com.games.mw.gameservice.domain.perk.PerkService
import com.games.mw.gameservice.domain.perk.model.Perk
import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import io.github.resilience4j.retry.Retry
import io.github.resilience4j.retry.RetryConfig
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpHeaders
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@ExtendWith(MockitoExtension::class)
class AccountServiceUnitTests {

    @Mock private lateinit var settingsService: SettingsService
    @Mock private lateinit var itemService: ItemService
    @Mock private lateinit var perkService: PerkService
    @Mock private lateinit var webClientBuilder: WebClient.Builder
    @Mock private lateinit var circuitBreakerRegistry: CircuitBreakerRegistry
    @Mock private lateinit var webClient: WebClient
    @Mock private lateinit var requestHeadersUriSpec: WebClient.RequestHeadersUriSpec<*>
    @Mock private lateinit var requestHeadersSpec: WebClient.RequestHeadersSpec<*>
    @Mock private lateinit var responseSpec: WebClient.ResponseSpec

    private lateinit var accountService: AccountService

    private val testUserId = 1L
    private val testUsername = "testuser"
    private val testToken = "Bearer jwttoken"
    private val testSettingsId = 10L

    @BeforeEach
    fun setUp() {
        val testRetry = Retry.of("testRetry", RetryConfig.custom<Any>().maxAttempts(1).build())
        val testCircuitBreaker = CircuitBreaker.of("authServiceGame", CircuitBreakerConfig.ofDefaults())
        whenever(circuitBreakerRegistry.circuitBreaker(eq("authServiceGame"))).thenReturn(testCircuitBreaker)

        accountService = AccountService(settingsService, itemService, perkService, webClientBuilder, testRetry, circuitBreakerRegistry)
    }

    private fun setupWebClientForFetchUserId() {
        whenever(webClientBuilder.baseUrl(any<String>())).thenReturn(webClientBuilder)
        whenever(webClientBuilder.build()).thenReturn(webClient)
        whenever(webClient.get()).thenReturn(requestHeadersUriSpec)
        whenever(requestHeadersUriSpec.uri(eq("/auth/account/$testUsername"))).thenReturn(requestHeadersSpec)
        whenever(requestHeadersSpec.header(eq(HttpHeaders.AUTHORIZATION), eq(testToken))).thenReturn(requestHeadersSpec)
        whenever(requestHeadersSpec.retrieve()).thenReturn(responseSpec)
    }


    @Test
    fun `getAccountData should return AccountDataDTO when all services succeed`() { runBlocking {
        // Arrange
        setupWebClientForFetchUserId()
        val settings = Settings(id = testSettingsId, userId = testUserId, coins = 100, level = 5)
        val items = listOf(Item(name = "Sword", amount = 1, equipped = 1, slotIndex = 0, settings = settings))
        val perks = listOf(Perk(name = "Strength", settings = settings))

        whenever(responseSpec.bodyToMono(any<ParameterizedTypeReference<Long>>())).thenReturn(Mono.just(testUserId))
        whenever(settingsService.getSettingsByUserId(testUserId)).thenReturn(settings.right())
        whenever(itemService.getItemsBySettingsId(testSettingsId)).thenAnswer { items }
        whenever(perkService.getPerksBySettingsId(testSettingsId)).thenAnswer { perks }

        // Act
        val result = accountService.getAccountData(testUsername, testToken)

        // Assert
        assertTrue(result.isRight(), "Expected result to be Right, but it was Left: ${result.leftOrNull()}")
        result.map {
            assertEquals(testUsername, it.username)
            assertEquals(testUserId, it.accountId)
            assertEquals(testSettingsId, it.settingsId)
            assertEquals(100, it.coins)
            assertEquals(5, it.level)
            assertEquals(listOf("Sword,1,1,0"), it.items)
            assertEquals(listOf("Strength"), it.perks)
        }
        verify(settingsService).getSettingsByUserId(testUserId)
        verify(itemService).getItemsBySettingsId(testSettingsId)
        verify(perkService).getPerksBySettingsId(testSettingsId)
    }}

    @Test
    fun `getAccountData should create settings if not found`() { runBlocking {
        // Arrange
        setupWebClientForFetchUserId()
        val newSettings = Settings(id = testSettingsId, userId = testUserId, coins = 0, level = 1)

        whenever(responseSpec.bodyToMono(any<ParameterizedTypeReference<Long>>())).thenReturn(Mono.just(testUserId))
        whenever(settingsService.getSettingsByUserId(testUserId)).thenReturn(SettingsService.SettingsError.SettingsNotFound.left())
        whenever(settingsService.insertSettings(any<Settings>())).thenReturn(newSettings.right())
        whenever(itemService.getItemsBySettingsId(testSettingsId)).thenReturn(emptyList())
        whenever(perkService.getPerksBySettingsId(testSettingsId)).thenReturn(emptyList())

        // Act
        val result = accountService.getAccountData(testUsername, testToken)

        // Assert
        assertTrue(result.isRight())
        result.map {
            assertEquals(testUsername, it.username)
            assertEquals(testUserId, it.accountId)
            assertEquals(testSettingsId, it.settingsId)
        }
        verify(settingsService).getSettingsByUserId(testUserId)
        verify(settingsService).insertSettings(argThat { userId == testUserId })
        verify(itemService).getItemsBySettingsId(testSettingsId)
        verify(perkService).getPerksBySettingsId(testSettingsId)
    }}

    @Test
    fun `getAccountData should return AuthServiceInteractionError if auth service fails`() = runBlocking {
        // Arrange
        setupWebClientForFetchUserId()
        val errorMessage = "Invalid token"

        whenever(responseSpec.bodyToMono(any<ParameterizedTypeReference<Long>>())).thenReturn(Mono.error(RuntimeException(errorMessage)))

        // Act
        val result = accountService.getAccountData(testUsername, testToken)

        // Assert
        assertTrue(result.isLeft())
        result.mapLeft {
            assertTrue(it is AccountService.AccountError.UnknownSource)
            assertEquals(errorMessage, (it as AccountService.AccountError.UnknownSource).throwable.message)
        }
        verifyNoInteractions(settingsService, itemService, perkService)
    }

    @Test
    fun `getAccountData should return SettingsOperationFailed if settings service fails on get`() = runBlocking {
        // Arrange
        setupWebClientForFetchUserId()

        whenever(responseSpec.bodyToMono(any<ParameterizedTypeReference<Long>>())).thenReturn(Mono.just(testUserId))
        whenever(settingsService.getSettingsByUserId(testUserId)).thenReturn(SettingsService.SettingsError.Unknown(RuntimeException("DB error")).left())

        // Act
        val result = accountService.getAccountData(testUsername, testToken)

        // Assert
        assertTrue(result.isLeft())
        result.mapLeft {
            assertTrue(it is AccountService.AccountError.SettingsOperationFailed)
        }
        verify(settingsService).getSettingsByUserId(testUserId)
        verifyNoInteractions(itemService, perkService)
    }

    @Test
    fun `updateAccountData should succeed when all services succeed`() { runBlocking {
        // Arrange
        val settings = Settings(id = testSettingsId, userId = testUserId, coins = 100, level = 5)
        val newAccountDataDTO = AccountDataDTO(
            username = testUsername,
            accountId = testUserId,
            settingsId = testSettingsId,
            spawn = 1,
            coins = 200,
            tokens = 5,
            level = 6,
            exp = 1000,
            items = listOf("NewItem,1,0,0"),
            perks = listOf("NewPerk")
        )

        whenever(settingsService.getSettingsByUserId(testUserId)).thenReturn(settings.right())
        whenever(settingsService.updateSettings(eq(testUserId), any<Settings>())).thenReturn(
            settings.copy(
                spawnId = newAccountDataDTO.spawn,
                coins = newAccountDataDTO.coins,
                tokens = newAccountDataDTO.tokens,
                level = newAccountDataDTO.level,
                exp = newAccountDataDTO.exp
            ).right()
        )
        whenever(itemService.deleteBySettingsId(testSettingsId)).thenReturn(Unit.right())
        whenever(itemService.insertItem(any<Item>())).thenReturn(Item().right())
        whenever(perkService.deleteBySettingsId(testSettingsId)).thenReturn(Unit.right())
        whenever(perkService.insertPerk(any<Perk>())).thenReturn(Perk().right())

        // Act
        val result = accountService.updateAccountData(newAccountDataDTO, testToken)

        // Assert
        assertTrue(result.isRight())
        verify(settingsService).getSettingsByUserId(testUserId)
        verify(settingsService).updateSettings(eq(testUserId), any<Settings>())
        verify(itemService).deleteBySettingsId(testSettingsId)
        verify(itemService).insertItem(argThat { name == "NewItem" && settings.id == testSettingsId })
        verify(perkService).deleteBySettingsId(testSettingsId)
        verify(perkService).insertPerk(argThat { name == "NewPerk" && settings.id == testSettingsId })
    }}

    @Test
    fun `updateAccountData should return SettingsOperationFailed if settings service fails on get`() = runBlocking {
        // Arrange
        val newAccountDataDTO = AccountDataDTO(
            username = testUsername,
            accountId = testUserId,
            settingsId = testSettingsId,
            spawn = 1,
            coins = 200,
            tokens = 5,
            level = 6,
            exp = 1000,
            items = emptyList(),
            perks = emptyList()
        )

        whenever(settingsService.getSettingsByUserId(testUserId)).thenReturn(SettingsService.SettingsError.SettingsNotFound.left())

        // Act
        val result = accountService.updateAccountData(newAccountDataDTO, testToken)

        // Assert
        assertTrue(result.isLeft())
        result.mapLeft {
            assertTrue(it is AccountService.AccountError.SettingsOperationFailed)
        }
        verify(settingsService).getSettingsByUserId(testUserId)
        verify(settingsService, never()).updateSettings(any(), any())
        verifyNoInteractions(itemService, perkService)
    }

    @Test
    fun `updateAccountData should return ItemOperationFailed if item delete fails`() = runBlocking {
        // Arrange
        val settings = Settings(id = testSettingsId, userId = testUserId, coins = 100, level = 5)
        val newAccountDataDTO = AccountDataDTO(
            username = testUsername,
            accountId = testUserId,
            settingsId = testSettingsId,
            spawn = 1,
            coins = 200,
            tokens = 5,
            level = 6,
            exp = 1000,
            items = listOf("NewItem,1,0,0"),
            perks = listOf("NewPerk")
        )

        whenever(settingsService.getSettingsByUserId(testUserId)).thenReturn(settings.right())
        whenever(settingsService.updateSettings(eq(testUserId), any<Settings>())).thenReturn(settings.copy(
            spawnId = newAccountDataDTO.spawn,
            coins = newAccountDataDTO.coins,
            tokens = newAccountDataDTO.tokens,
            level = newAccountDataDTO.level,
            exp = newAccountDataDTO.exp
        ).right())
        whenever(itemService.deleteBySettingsId(testSettingsId)).thenReturn(ItemService.ItemError.Unknown(RuntimeException("Item delete error")).left())

        // Act
        val result = accountService.updateAccountData(newAccountDataDTO, testToken)

        // Assert
        assertTrue(result.isLeft())
        result.mapLeft {
            assertTrue(it is AccountService.AccountError.ItemOperationFailed)
        }
        verify(settingsService).getSettingsByUserId(testUserId)
        verify(settingsService).updateSettings(eq(testUserId), any<Settings>())
        verify(itemService).deleteBySettingsId(testSettingsId)
        verify(itemService, never()).insertItem(any())
        verifyNoInteractions(perkService)
    }
}