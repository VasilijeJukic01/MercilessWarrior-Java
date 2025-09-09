package com.games.mw.gameservice.domain.perk

import com.games.mw.gameservice.domain.perk.model.Perk
import com.games.mw.gameservice.domain.perk.PerkService.PerkError
import kotlinx.coroutines.reactor.mono
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/perks")
class PerkController(
    private val perkService: PerkService
) {

    @GetMapping("/settings/{settingsId}")
    fun getPerksBySettingsId(@PathVariable settingsId: Long): Mono<ResponseEntity<List<Perk>>> = mono {
        ResponseEntity.ok(perkService.getPerksBySettingsId(settingsId))
    }

    @PostMapping("/")
    fun insertPerk(@RequestBody perk: Perk): Mono<ResponseEntity<*>> = mono {
        perkService.insertPerk(perk).fold(
            { _ -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to insert perk.") },
            { newPerk -> ResponseEntity.ok(newPerk) }
        )
    }

    @PutMapping("/{perkId}")
    fun updatePerk(@PathVariable perkId: Long, @RequestBody perk: Perk): Mono<ResponseEntity<*>> = mono {
        perkService.updatePerk(perkId, perk).fold(
            { error ->
                when (error) {
                    is PerkError.PerkNotFound -> ResponseEntity.notFound().build<Perk>()
                    is PerkError.Unknown -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update perk.")
                }
            },
            { updatedPerk -> ResponseEntity.ok(updatedPerk) }
        )
    }

    @DeleteMapping("/settings/{settingsId}")
    fun deleteBySettingsId(@PathVariable settingsId: Long): Mono<ResponseEntity<*>> = mono {
        perkService.deleteBySettingsId(settingsId).fold(
            { ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete perks.") },
            { ResponseEntity.ok().build<Void>() }
        )
    }
}