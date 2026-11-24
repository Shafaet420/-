package com.example

import com.example.model.RoundLogFormula
import com.example.model.TallySaveResult
import com.example.model.TimberCalculatorEngine
import com.example.model.WoodMode
import com.example.viewmodel.TimberViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI

class TimberCalculatorEngineTest {

    @Test
    fun roundLogHoppus_classicSawmillCase_returnsExactCft() {
        // 48" girth (4 ft), 10 ft length => (48 * 48 * 10) / 2304 = 10.0 CFT
        val res = TimberCalculatorEngine.calculateRoundLog(
            lengthFeetRaw = "10",
            lengthInchesRaw = "",
            girthFeetRaw = "",
            girthInchesRaw = "48",
            quantity = 1,
            formula = RoundLogFormula.HOPPUS,
            ratePerCftRaw = "1200"
        )
        assertTrue(res.isValid)
        assertEquals(10.0, res.perPieceCft, 0.0001)
        assertEquals(10.0, res.totalCft, 0.0001)
        assertEquals(12000.0, res.totalPrice ?: 0.0, 0.01)
    }

    @Test
    fun roundLogHoppus_mixedFeetAndInches_withBengaliDigits_returnsExactCft() {
        // Length: 8 ft 6 in = 8.5 ft; Girth: 3 ft 6 in = 42 in
        // (42 * 42 * 8.5) / 2304 = 14994 / 2304 = 6.5078125 CFT * 2 pieces = 13.015625 CFT
        val res = TimberCalculatorEngine.calculateRoundLog(
            lengthFeetRaw = "৮",
            lengthInchesRaw = "৬",
            girthFeetRaw = "৩",
            girthInchesRaw = "৬",
            quantity = 2,
            formula = RoundLogFormula.HOPPUS,
            ratePerCftRaw = ""
        )
        assertTrue(res.isValid)
        assertEquals(6.5078125, res.perPieceCft, 0.0001)
        assertEquals(13.015625, res.totalCft, 0.0001)
    }

    @Test
    fun roundLogTrueCylinder_returnsExpectedVolume() {
        // 48" girth (4 ft), 10 ft length => (4^2 * 10) / (4 * PI) = 40 / PI ≈ 12.7324 CFT
        val res = TimberCalculatorEngine.calculateRoundLog(
            lengthFeetRaw = "10",
            lengthInchesRaw = "0",
            girthFeetRaw = "4",
            girthInchesRaw = "0",
            quantity = 1,
            formula = RoundLogFormula.TRUE_CYLINDER,
            ratePerCftRaw = ""
        )
        assertTrue(res.isValid)
        assertEquals(40.0 / PI, res.totalCft, 0.0001)
    }

    @Test
    fun sawnTimber_classicPlank_returnsExactCft() {
        // Length 8 ft, Width 6 in, Thickness 3 in => (8 * 6 * 3) / 144 = 1.0 CFT
        val res = TimberCalculatorEngine.calculateSawnTimber(
            lengthFeetRaw = "8",
            lengthInchesRaw = "",
            widthFeetRaw = "",
            widthInchesRaw = "6",
            thicknessInchesRaw = "3",
            quantity = 5,
            ratePerCftRaw = "1500"
        )
        assertTrue(res.isValid)
        assertEquals(1.0, res.perPieceCft, 0.0001)
        assertEquals(5.0, res.totalCft, 0.0001)
        assertEquals(7500.0, res.totalPrice ?: 0.0, 0.01)
    }

    @Test
    fun sawnTimber_mixedFeetInchesWidthAndLength_returnsExactCft() {
        // Length: 10 ft 6 in = 10.5 ft; Width: 1 ft 2 in = 14 in; Thickness: 1.5 in
        // (10.5 * 14 * 1.5) / 144 = 220.5 / 144 = 1.53125 CFT
        val res = TimberCalculatorEngine.calculateSawnTimber(
            lengthFeetRaw = "10",
            lengthInchesRaw = "6",
            widthFeetRaw = "1",
            widthInchesRaw = "2",
            thicknessInchesRaw = "1.5",
            quantity = 1,
            ratePerCftRaw = ""
        )
        assertTrue(res.isValid)
        assertEquals(1.53125, res.totalCft, 0.0001)
    }

    @Test
    fun incompleteInputs_returnsInvalid() {
        val res = TimberCalculatorEngine.calculateSawnTimber(
            lengthFeetRaw = "10",
            lengthInchesRaw = "",
            widthFeetRaw = "",
            widthInchesRaw = "6",
            thicknessInchesRaw = "",
            quantity = 1,
            ratePerCftRaw = ""
        )
        assertFalse(res.isValid)
        assertEquals(0.0, res.totalCft, 0.0001)
    }

    @Test
    fun sanitizeNumericInput_normalizesLeadingZerosAndBengaliDigits() {
        assertEquals("5", TimberCalculatorEngine.sanitizeNumericInput("005"))
        assertEquals("0", TimberCalculatorEngine.sanitizeNumericInput("000"))
        assertEquals("0.5", TimberCalculatorEngine.sanitizeNumericInput("0.5"))
        assertEquals("12.75", TimberCalculatorEngine.sanitizeNumericInput("১২.৭.৫"))
        assertEquals("100.125", TimberCalculatorEngine.sanitizeNumericInput("100.125"))
    }

    @Test
    fun formatCft_preservesPrecisionForTinyAndUltraTinyPositiveVolumes() {
        // 1 ft * 1 in * 0.5 in / 144 = 0.003472 CFT -> must not display misleading "0.00"
        val res = TimberCalculatorEngine.calculateSawnTimber("1", "", "", "1", "0.5", 1, "")
        assertTrue(res.isValid)
        assertEquals("0.003", TimberCalculatorEngine.formatCft(res.totalCft, useBengaliDigits = false))
        assertEquals("০.০০৩", TimberCalculatorEngine.formatCft(res.totalCft, useBengaliDigits = true))

        // Ultra-tiny strip: 0.5 ft * 0.5 in * 0.25 in / 144 = 0.000434 CFT -> must not show "0.000"
        val ultraTiny = TimberCalculatorEngine.calculateSawnTimber("0.5", "", "", "0.5", "0.25", 1, "")
        assertTrue(ultraTiny.isValid)
        assertEquals("0.0004", TimberCalculatorEngine.formatCft(ultraTiny.totalCft, useBengaliDigits = false))

        assertEquals("10.00", TimberCalculatorEngine.formatCft(10.0, useBengaliDigits = false))
    }

    @Test
    fun formatCompactNumber_preservesThreeDecimalSutaFractionsWithoutRounding() {
        assertEquals("0.125", TimberCalculatorEngine.formatCompactNumber(0.125, useBengaliDigits = false))
        assertEquals("0.375", TimberCalculatorEngine.formatCompactNumber(0.375, useBengaliDigits = false))
        assertEquals("0.625", TimberCalculatorEngine.formatCompactNumber(0.625, useBengaliDigits = false))
        assertEquals("0.875", TimberCalculatorEngine.formatCompactNumber(0.875, useBengaliDigits = false))
        assertEquals("1.5", TimberCalculatorEngine.formatCompactNumber(1.5, useBengaliDigits = false))
        assertEquals("5", TimberCalculatorEngine.formatCompactNumber(5.0, useBengaliDigits = false))
    }

    @Test
    fun localizeDigits_convertsBidirectionally() {
        assertEquals("÷16", TimberCalculatorEngine.localizeDigits("÷১৬", useBengaliDigits = false))
        assertEquals("÷১৬", TimberCalculatorEngine.localizeDigits("÷16", useBengaliDigits = true))
    }

    @Test
    fun viewModel_presetClearsInches_andAtomicAdd_andUndoRestore() {
        val vm = TimberViewModel()
        vm.updateRoundLengthFeet("8")
        vm.updateRoundLengthInches("6")
        vm.applyRoundLengthPreset("10")

        assertEquals("10", vm.uiState.value.roundLengthFeet)
        assertEquals("", vm.uiState.value.roundLengthInches)

        vm.updateRoundGirthInches("48")
        vm.updateRatePerCft("1200")
        assertEquals(TallySaveResult.ADDED, vm.addCurrentToTally())
        // Rapid second tap after add must not duplicate
        assertEquals(TallySaveResult.NONE, vm.addCurrentToTally())
        assertEquals(1, vm.uiState.value.tallyEntries.size)

        // Add an unpriced sawn timber piece to verify partial pricing tracking
        vm.selectWoodMode(WoodMode.SAWN_TIMBER)
        vm.updateRatePerCft("")
        vm.updateSawnLengthFeet("8")
        vm.updateSawnWidthInches("6")
        vm.updateSawnThicknessInches("3")
        assertEquals(TallySaveResult.ADDED, vm.addCurrentToTally())

        val stateWithTwo = vm.uiState.value
        assertEquals(2, stateWithTwo.tallyEntries.size)
        assertEquals(1, stateWithTwo.pricedTallyCount)
        assertFalse(stateWithTwo.isUsingGlobalRate)
        assertEquals(12000.0, stateWithTwo.totalTallyPrice ?: 0.0, 0.01)

        // Verify chronological ordering: first added (round log) is index 0, second added (sawn) is index 1
        assertEquals(WoodMode.ROUND_LOG, stateWithTwo.tallyEntries[0].mode)
        assertEquals(WoodMode.SAWN_TIMBER, stateWithTwo.tallyEntries[1].mode)

        // Verify remove + undo restore preserves exact order & item
        val firstId = stateWithTwo.tallyEntries.first().id
        val removed = vm.removeTallyEntry(firstId)
        assertNotNull(removed)
        assertEquals(1, vm.uiState.value.tallyEntries.size)

        vm.restoreTallyEntry(removed!!)
        assertEquals(2, vm.uiState.value.tallyEntries.size)
        assertEquals(firstId, vm.uiState.value.tallyEntries.first().id)
    }

    @Test
    fun multiSizeAndMixedWoodChain_matchesExactCombinedCftAndSupportsEditing() {
        val vm = TimberViewModel()

        // 1) Sawn timber: (5 ft × 12 in × 1 in) × 15 pcs = 6.25 CFT
        vm.selectWoodMode(WoodMode.SAWN_TIMBER)
        vm.updateSawnLengthFeet("5")
        vm.updateSawnWidthInches("12")
        vm.updateSawnThicknessInches("1")
        vm.updateQuantityInput("১৫")
        assertEquals(15, vm.uiState.value.quantity)
        assertEquals(6.25, vm.uiState.value.currentResult.totalCft, 0.0001)
        assertEquals(TallySaveResult.ADDED, vm.addCurrentToTally())

        // 2) Sawn timber: (5 ft × 2.5 in × 1 in) × 45 pcs = 3.90625 CFT
        vm.updateSawnLengthFeet("5")
        vm.updateSawnWidthInches("2.5")
        vm.updateSawnThicknessInches("1")
        vm.updateQuantityInput("45")
        // Verify live combined total preview BEFORE adding the 2nd item!
        assertEquals(10.15625, vm.uiState.value.liveCombinedCft, 0.0001)
        assertEquals(60, vm.uiState.value.liveCombinedPieces)
        assertEquals(TallySaveResult.ADDED, vm.addCurrentToTally())

        // 3) Mix a Round Log into the exact same chain: (10 ft × 48 in girth) × 2 pcs = 20.0 CFT
        vm.selectWoodMode(WoodMode.ROUND_LOG)
        vm.updateRoundLengthFeet("10")
        vm.updateRoundGirthInches("48")
        vm.updateQuantityInput("2")
        assertEquals(TallySaveResult.ADDED, vm.addCurrentToTally())

        assertEquals(3, vm.uiState.value.tallyEntries.size)
        assertEquals(62, vm.uiState.value.totalTallyPieces)
        assertEquals(30.15625, vm.uiState.value.totalTallyCft, 0.0001)

        // 4) Edit the 45-piece entry to 50 pieces in place without creating a duplicate
        val secondEntry = vm.uiState.value.tallyEntries.first { it.quantity == 45 }
        vm.startEditingEntry(secondEntry)
        assertEquals(secondEntry.id, vm.uiState.value.editingEntryId)
        vm.updateQuantityInput("50")
        // Live combined total while editing replaces old 45 pcs with 50 pcs (delta = +5 * 12.5/144 = +0.4340277)
        assertEquals(30.590277, vm.uiState.value.liveCombinedCft, 0.001)
        assertEquals(TallySaveResult.UPDATED, vm.addCurrentToTally())

        assertEquals(3, vm.uiState.value.tallyEntries.size)
        assertEquals(67, vm.uiState.value.totalTallyPieces)
        assertEquals(30.590277, vm.uiState.value.totalTallyCft, 0.001)
    }

    @Test
    fun editMode_doesNotLeakSessionRate_preservesSutaPrecision_andRestoresDraftOnCancelAndModeSwitch() {
        val vm = TimberViewModel()

        // Add an UNPRICED 5-suta (0.625") sawn plank entry
        vm.selectWoodMode(WoodMode.SAWN_TIMBER)
        vm.updateSawnLengthFeet("8")
        vm.updateSawnWidthInches("12")
        vm.updateSawnThicknessInches("0.625")
        assertEquals(TallySaveResult.ADDED, vm.addCurrentToTally())

        // Now user types a session rate + unsaved draft for the next entry
        vm.updateRatePerCft("1500")
        vm.selectWoodName("সেগুন")
        vm.updateSawnLengthFeet("10")
        vm.updateSawnWidthInches("6")

        // Tap Edit on the first unpriced 0.625" entry
        val firstEntry = vm.uiState.value.tallyEntries.first()
        vm.startEditingEntry(firstEntry)

        // Must NOT inherit session rate "1500" and MUST preserve exact "0.625" (not "0.63")
        assertEquals("", vm.uiState.value.ratePerCft)
        assertEquals("0.625", vm.uiState.value.sawnThicknessInches)

        // Canceling edit restores the unsaved draft & session rate/wood cleanly
        vm.cancelEditing()
        assertNull(vm.uiState.value.editingEntryId)
        assertEquals("1500", vm.uiState.value.ratePerCft)
        assertEquals("সেগুন", vm.uiState.value.selectedWoodName)
        assertEquals("10", vm.uiState.value.sawnLengthFeet)
        assertEquals("6", vm.uiState.value.sawnWidthInches)

        // Start editing again, then switch to Round Log mode -> must safely exit edit mode
        vm.startEditingEntry(firstEntry)
        assertEquals(firstEntry.id, vm.uiState.value.editingEntryId)
        vm.selectWoodMode(WoodMode.ROUND_LOG)
        assertNull(vm.uiState.value.editingEntryId)
        assertEquals(WoodMode.ROUND_LOG, vm.uiState.value.woodMode)

        // Start editing again, then save -> unpriced item stays unpriced AND session rate is restored
        vm.startEditingEntry(firstEntry)
        vm.updateQuantityInput("2")
        assertEquals(TallySaveResult.UPDATED, vm.addCurrentToTally())
        val updatedEntry = vm.uiState.value.tallyEntries.first()
        assertNull(updatedEntry.ratePerCft)
        assertNull(updatedEntry.totalPrice)
        assertEquals(0.625, updatedEntry.thicknessInches, 0.00001)
        assertEquals("1500", vm.uiState.value.ratePerCft)
        assertEquals("সেগুন", vm.uiState.value.selectedWoodName)
    }

    @Test
    fun deletingCurrentlyEditedEntry_exitsEditModeAndClearsStaleLiveCombinedCft() {
        val vm = TimberViewModel()
        vm.selectWoodMode(WoodMode.ROUND_LOG)
        vm.updateRoundLengthFeet("10")
        vm.updateRoundGirthInches("48")
        assertEquals(TallySaveResult.ADDED, vm.addCurrentToTally())

        val entry = vm.uiState.value.tallyEntries.first()
        vm.startEditingEntry(entry)
        assertEquals(10.0, vm.uiState.value.liveCombinedCft, 0.001)

        // Deleting the entry being edited must clear edit state and drop liveCombinedCft to 0
        vm.removeTallyEntry(entry.id)
        assertNull(vm.uiState.value.editingEntryId)
        assertEquals(0, vm.uiState.value.tallyEntries.size)
        assertEquals(0.0, vm.uiState.value.liveCombinedCft, 0.001)
    }

    @Test
    fun shareableSummary_matchesChronologicalOrderAndReconcilesGlobalRate() {
        val vm = TimberViewModel()
        vm.toggleBengaliDigits() // switch to English digits for easy assertion

        // #1: Sawn timber (8 ft × 6 in × 3 in) × 2 = 2.00 CFT, old rate 1000 => ৳2000
        vm.selectWoodMode(WoodMode.SAWN_TIMBER)
        vm.updateSawnLengthFeet("8")
        vm.updateSawnWidthInches("6")
        vm.updateSawnThicknessInches("3")
        vm.updateQuantityInput("2")
        vm.updateRatePerCft("1000")
        assertEquals(TallySaveResult.ADDED, vm.addCurrentToTally())

        // #2: Round log (10 ft × 48 in) × 1 = 10.00 CFT, no individual rate
        vm.selectWoodMode(WoodMode.ROUND_LOG)
        vm.updateRoundLengthFeet("10")
        vm.updateRoundGirthInches("48")
        vm.updateRatePerCft("")
        assertEquals(TallySaveResult.ADDED, vm.addCurrentToTally())

        // Set global rate 1200 => Item #1 = ৳2400, Item #2 = ৳12000, Total = ৳14400
        vm.updateGlobalRate("1200")
        val summary = vm.buildShareableText()

        val idx1 = summary.indexOf("1. কাটা কাঠ")
        val idx2 = summary.indexOf("2. গোল গাছ")
        assertTrue("Expected #1 Sawn Timber before #2 Round Log:\n$summary", idx1 in 0 until idx2)
        assertTrue("Expected item #1 reconciled to ৳2400:\n$summary", summary.contains("(৳2400)"))
        assertTrue("Expected item #2 reconciled to ৳12000:\n$summary", summary.contains("(৳12000)"))
        assertTrue("Expected global rate line:\n$summary", summary.contains("গড় দর: ৳1200/সিএফটি"))
        assertTrue("Expected grand total ৳14400:\n$summary", summary.contains("৳14400"))
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    @Test
    fun offlinePersistence_restoresEntriesPreferencesOrderAndNextIdAcrossRestart() {
        val sharedRepo = com.example.data.InMemoryTallyRepository()
        val dispatcher = kotlinx.coroutines.test.UnconfinedTestDispatcher()

        // Session 1: Add 2 entries, change preferences, delete & undo entry #1
        val vm1 = TimberViewModel(repository = sharedRepo, persistenceDispatcher = dispatcher)
        vm1.toggleBengaliDigits() // false
        vm1.selectRoundFormula(RoundLogFormula.TRUE_CYLINDER)

        vm1.selectWoodMode(WoodMode.SAWN_TIMBER)
        vm1.updateSawnLengthFeet("5")
        vm1.updateSawnWidthInches("12")
        vm1.updateSawnThicknessInches("1")
        vm1.updateQuantityInput("15")
        assertEquals(TallySaveResult.ADDED, vm1.addCurrentToTally())

        vm1.updateSawnLengthFeet("5")
        vm1.updateSawnWidthInches("2.5")
        vm1.updateSawnThicknessInches("1")
        vm1.updateQuantityInput("45")
        assertEquals(TallySaveResult.ADDED, vm1.addCurrentToTally())

        vm1.updateGlobalRate("1100")

        // Delete item #1 then undo -> must retain original position #1 via sortOrder
        val firstEntry = vm1.uiState.value.tallyEntries.first()
        val removed = vm1.removeTallyEntry(firstEntry.id)
        assertNotNull(removed)
        vm1.restoreTallyEntry(removed!!)

        // Session 2 (simulated app restart): New ViewModel backed by same repository
        val vm2 = TimberViewModel(repository = sharedRepo, persistenceDispatcher = dispatcher)
        val restored = vm2.uiState.value

        assertEquals(2, restored.tallyEntries.size)
        assertEquals(15, restored.tallyEntries[0].quantity)
        assertEquals(45, restored.tallyEntries[1].quantity)
        assertFalse(restored.useBengaliDigits)
        assertEquals(RoundLogFormula.TRUE_CYLINDER, restored.roundFormula)
        assertEquals("1100", restored.globalRateInput)

        // Adding a 3rd item after restart must get a strictly higher unique ID & sortOrder
        vm2.selectWoodMode(WoodMode.ROUND_LOG)
        vm2.updateRoundLengthFeet("10")
        vm2.updateRoundGirthInches("48")
        assertEquals(TallySaveResult.ADDED, vm2.addCurrentToTally())

        val afterThird = vm2.uiState.value
        assertEquals(3, afterThird.tallyEntries.size)
        assertEquals(3, afterThird.tallyEntries.map { it.id }.toSet().size)
        assertEquals(WoodMode.ROUND_LOG, afterThird.tallyEntries[2].mode)
    }
}
