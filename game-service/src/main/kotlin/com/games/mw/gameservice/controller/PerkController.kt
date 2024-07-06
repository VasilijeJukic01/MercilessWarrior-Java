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
    fun getPerksBySettingsId(@PathVariable settingsId: Long): ResponseEntity<List<Perk>> {
        val perks = perkService.getPerksBySettingsId(settingsId)

        return ResponseEntity.ok(perks)
    }

    @GetMapping("/maxId")
    fun getMaxId(): ResponseEntity<Long> {
        val maxId = perkService.getMaxId()

        return if (maxId != null) {
            ResponseEntity.ok(maxId)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping("/")
    fun insertPerk(@RequestBody perk: Perk): ResponseEntity<Perk> {
        val newPerk = perkService.insertPerk(perk)

        return ResponseEntity.ok(newPerk)
    }

    @PutMapping("/{perkId}")
    fun updatePerk(@PathVariable perkId: Long, @RequestBody perk: Perk): ResponseEntity<Perk> {
        val updatedPerk = perkService.updatePerk(perkId, perk)

        return if (updatedPerk != null) {
            ResponseEntity.ok(updatedPerk)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @DeleteMapping("/settings/{settingsId}")
    fun deleteBySettingsId(@PathVariable settingsId: Long): ResponseEntity<Void> {
        perkService.deleteBySettingsId(settingsId)

        return ResponseEntity.ok().build()
    }
}