package com.games.mw.gameservice.repository

import com.games.mw.gameservice.model.ItemMaster
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ItemMasterRepository : JpaRepository<ItemMaster, String>