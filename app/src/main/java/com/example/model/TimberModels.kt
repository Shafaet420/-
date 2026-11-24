package com.example.model

enum class WoodMode {
    ROUND_LOG,
    SAWN_TIMBER
}

enum class RoundLogFormula {
    HOPPUS,
    TRUE_CYLINDER
}

enum class AppTab {
    CALCULATOR,
    TALLY,
    GUIDE
}

enum class TallySaveResult {
    NONE,
    ADDED,
    UPDATED
}

data class TimberMeasurementResult(
    val perPieceCft: Double = 0.0,
    val totalCft: Double = 0.0,
    val totalCbm: Double = 0.0,
    val totalPrice: Double? = null,
    val totalLengthFeet: Double = 0.0,
    val totalGirthInches: Double = 0.0,
    val totalWidthInches: Double = 0.0,
    val totalThicknessInches: Double = 0.0,
    val isValid: Boolean = false
)

data class TallyEntry(
    val id: Long,
    val mode: WoodMode,
    val woodName: String,
    val lengthFeet: Double,
    val lengthInches: Double,
    val girthFeet: Double = 0.0,
    val girthInches: Double = 0.0,
    val widthFeet: Double = 0.0,
    val widthInches: Double = 0.0,
    val thicknessInches: Double = 0.0,
    val quantity: Int,
    val formula: RoundLogFormula = RoundLogFormula.HOPPUS,
    val perPieceCft: Double,
    val totalCft: Double,
    val ratePerCft: Double?,
    val totalPrice: Double?,
    val sortOrder: Long = id
)

data class RemovedTallyItem(
    val entry: TallyEntry,
    val index: Int
)

/**
 * Snapshot of the user's unsaved calculator inputs taken right before editing an existing tally item,
 * so canceling edit restores their work and saving edit does not corrupt their session rate/wood type.
 */
data class CalculatorDraft(
    val woodMode: WoodMode,
    val roundFormula: RoundLogFormula,
    val roundLengthFeet: String,
    val roundLengthInches: String,
    val roundGirthFeet: String,
    val roundGirthInches: String,
    val sawnLengthFeet: String,
    val sawnLengthInches: String,
    val sawnWidthFeet: String,
    val sawnWidthInches: String,
    val sawnThicknessInches: String,
    val quantity: Int,
    val quantityInput: String,
    val ratePerCft: String,
    val selectedWoodName: String,
    val showPricingSection: Boolean
)

data class TimberUiState(
    val activeTab: AppTab = AppTab.CALCULATOR,
    val woodMode: WoodMode = WoodMode.ROUND_LOG,
    val roundFormula: RoundLogFormula = RoundLogFormula.HOPPUS,
    val useBengaliDigits: Boolean = true,

    // Round Log inputs
    val roundLengthFeet: String = "",
    val roundLengthInches: String = "",
    val roundGirthFeet: String = "",
    val roundGirthInches: String = "",

    // Sawn Timber inputs
    val sawnLengthFeet: String = "",
    val sawnLengthInches: String = "",
    val sawnWidthFeet: String = "",
    val sawnWidthInches: String = "",
    val sawnThicknessInches: String = "",

    // Shared inputs
    val quantity: Int = 1,
    val quantityInput: String = "1",
    val ratePerCft: String = "",
    val selectedWoodName: String = "",
    val showPricingSection: Boolean = false,
    val editingEntryId: Long? = null,
    val preEditDraft: CalculatorDraft? = null,

    // Live calculation output
    val currentResult: TimberMeasurementResult = TimberMeasurementResult(),

    // Session tally (stored in chronological order: #1, #2, #3...)
    val tallyEntries: List<TallyEntry> = emptyList(),
    val globalRateInput: String = ""
) {
    val totalTallyCft: Double
        get() = tallyEntries.sumOf { it.totalCft }

    val totalTallyCbm: Double
        get() = TimberCalculatorEngine.cftToCbm(totalTallyCft)

    val totalTallyPieces: Int
        get() = tallyEntries.sumOf { it.quantity }

    val roundLogTallyCft: Double
        get() = tallyEntries.filter { it.mode == WoodMode.ROUND_LOG }.sumOf { it.totalCft }

    val sawnTimberTallyCft: Double
        get() = tallyEntries.filter { it.mode == WoodMode.SAWN_TIMBER }.sumOf { it.totalCft }

    /**
     * Live running total including saved entries plus the currently typed measurement (if valid).
     * When editing an existing entry, replaces that entry's CFT with the live edited value.
     */
    val liveCombinedCft: Double
        get() {
            val baseCft = if (editingEntryId != null) {
                tallyEntries.filterNot { it.id == editingEntryId }.sumOf { it.totalCft }
            } else {
                totalTallyCft
            }
            return baseCft + if (currentResult.isValid) currentResult.totalCft else 0.0
        }

    val liveCombinedPieces: Int
        get() {
            val basePieces = if (editingEntryId != null) {
                tallyEntries.filterNot { it.id == editingEntryId }.sumOf { it.quantity }
            } else {
                totalTallyPieces
            }
            return basePieces + if (currentResult.isValid) quantity else 0
        }

    val effectiveGlobalRate: Double?
        get() = TimberCalculatorEngine.parsePositiveDouble(globalRateInput)
            .takeIf { it > 0.0 && totalTallyCft > 0.0 }

    val isUsingGlobalRate: Boolean
        get() = effectiveGlobalRate != null

    val pricedTallyCount: Int
        get() = tallyEntries.count { it.totalPrice != null }

    val totalTallyPrice: Double?
        get() {
            val globalRate = effectiveGlobalRate
            if (globalRate != null) {
                return totalTallyCft * globalRate
            }
            val pricedItems = tallyEntries.mapNotNull { it.totalPrice }
            return if (pricedItems.isNotEmpty()) pricedItems.sum() else null
        }
}
