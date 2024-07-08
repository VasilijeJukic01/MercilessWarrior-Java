package com.games.mw.gameservice.controller

import com.games.mw.gameservice.model.Perk
import com.games.mw.gameservice.service.PerkService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/perks")
class PerkController(
    private val perkService: PerkService
) {

    @GetMapping("/settings/{settingsId}")
    fun getPerksBySettingsId(@PathVariable settingsId: Long,
                             @RequestHeader("Authorization") token: String): ResponseEntity<List<Perk>> {
        val perks = perkService.getPerksBySettingsId(settingsId)

        return ResponseEntity.ok(perks)
    }

    @PostMapping("/")
    fun insertPerk(@RequestBody perk: Perk,
                   @RequestHeader("Authorization") token: String): ResponseEntity<Perk> {
        val newPerk = perkService.insertPerk(perk)

        return ResponseEntity.ok(newPerk)
    }

    @PutMapping("/{perkId}")
    fun updatePerk(@PathVariable perkId: Long,
                   @RequestBody perk: Perk,
                   @RequestHeader("Authorization") token: String): ResponseEntity<Perk> {
        val updatedPerk = perkService.updatePerk(perkId, perk)

        return if (updatedPerk != null) {
            ResponseEntity.ok(updatedPerk)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @DeleteMapping("/settings/{settingsId}")
    fun deleteBySettingsId(@PathVariable settingsId: Long,
                           @RequestHeader("Authorization") token: String): ResponseEntity<Void> {
        perkService.deleteBySettingsId(settingsId)

        return ResponseEntity.ok().build()
    }
}