package com.games.mw.gameservice.controller

import com.games.mw.gameservice.model.Perk
import com.games.mw.gameservice.service.PerkService.PerkError
import com.games.mw.gameservice.service.PerkService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/perks")
class PerkController(
    private val perkService: PerkService
) {

    @GetMapping("/settings/{settingsId}")
    fun getPerksBySettingsId(@PathVariable settingsId: Long, @RequestHeader("Authorization") token: String): ResponseEntity<List<Perk>> {
        val perks = perkService.getPerksBySettingsId(settingsId)
        return ResponseEntity.ok(perks)
    }

    @PostMapping("/")
    fun insertPerk(@RequestBody perk: Perk, @RequestHeader("Authorization") token: String): ResponseEntity<*> {
        return perkService.insertPerk(perk).fold(
            { _ -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to insert perk.") },
            { newPerk -> ResponseEntity.ok(newPerk) }
        )
    }

    @PutMapping("/{perkId}")
    fun updatePerk(@PathVariable perkId: Long, @RequestBody perk: Perk, @RequestHeader("Authorization") token: String): ResponseEntity<*> {
        return perkService.updatePerk(perkId, perk).fold(
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
    fun deleteBySettingsId(@PathVariable settingsId: Long, @RequestHeader("Authorization") token: String): ResponseEntity<Void> {
        perkService.deleteBySettingsId(settingsId)
        return ResponseEntity.ok().build()
    }
}