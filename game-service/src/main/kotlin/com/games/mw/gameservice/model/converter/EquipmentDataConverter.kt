package com.games.mw.gameservice.model.converter

import com.fasterxml.jackson.databind.ObjectMapper
import com.games.mw.gameservice.requests.EquipmentDataDTO
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter(autoApply = true)
class EquipmentDataConverter(
    private val objectMapper: ObjectMapper
) : AttributeConverter<EquipmentDataDTO, String> {

    override fun convertToDatabaseColumn(attribute: EquipmentDataDTO?): String? {
        return attribute?.let {
            try {
                objectMapper.writeValueAsString(it)
            } catch (e: Exception) {
                null
            }
        }
    }

    override fun convertToEntityAttribute(dbData: String?): EquipmentDataDTO? {
        return dbData?.let {
            try {
                objectMapper.readValue(it, EquipmentDataDTO::class.java)
            } catch (e: Exception) {
                null
            }
        }
    }
}