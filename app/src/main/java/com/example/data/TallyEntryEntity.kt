package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.RoundLogFormula
import com.example.model.TallyEntry
import com.example.model.WoodMode

@Entity(tableName = "tally_entries")
data class TallyEntryEntity(
    @PrimaryKey val id: Long,
    val sortOrder: Long,
    val mode: String,
    val woodName: String,
    val lengthFeet: Double,
    val lengthInches: Double,
    val girthFeet: Double,
    val girthInches: Double,
    val widthFeet: Double,
    val widthInches: Double,
    val thicknessInches: Double,
    val quantity: Int,
    val formula: String,
    val perPieceCft: Double,
    val totalCft: Double,
    val ratePerCft: Double?,
    val totalPrice: Double?
) {
    fun toDomain(): TallyEntry {
        val parsedMode = runCatching { WoodMode.valueOf(mode) }.getOrDefault(WoodMode.ROUND_LOG)
        val parsedFormula = runCatching { RoundLogFormula.valueOf(formula) }.getOrDefault(RoundLogFormula.HOPPUS)
        return TallyEntry(
            id = id,
            mode = parsedMode,
            woodName = woodName,
            lengthFeet = lengthFeet,
            lengthInches = lengthInches,
            girthFeet = girthFeet,
            girthInches = girthInches,
            widthFeet = widthFeet,
            widthInches = widthInches,
            thicknessInches = thicknessInches,
            quantity = quantity.coerceAtLeast(1),
            formula = parsedFormula,
            perPieceCft = perPieceCft,
            totalCft = totalCft,
            ratePerCft = ratePerCft,
            totalPrice = totalPrice,
            sortOrder = sortOrder
        )
    }

    companion object {
        fun fromDomain(entry: TallyEntry): TallyEntryEntity {
            return TallyEntryEntity(
                id = entry.id,
                sortOrder = entry.sortOrder,
                mode = entry.mode.name,
                woodName = entry.woodName,
                lengthFeet = entry.lengthFeet,
                lengthInches = entry.lengthInches,
                girthFeet = entry.girthFeet,
                girthInches = entry.girthInches,
                widthFeet = entry.widthFeet,
                widthInches = entry.widthInches,
                thicknessInches = entry.thicknessInches,
                quantity = entry.quantity.coerceAtLeast(1),
                formula = entry.formula.name,
                perPieceCft = entry.perPieceCft,
                totalCft = entry.totalCft,
                ratePerCft = entry.ratePerCft,
                totalPrice = entry.totalPrice
            )
        }
    }
}
