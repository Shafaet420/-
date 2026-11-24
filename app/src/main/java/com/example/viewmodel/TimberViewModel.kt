package com.example.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.InMemoryTallyRepository
import com.example.data.OfflineTallyRepository
import com.example.data.TallyRepository
import com.example.model.AppTab
import com.example.model.CalculatorDraft
import com.example.model.RemovedTallyItem
import com.example.model.RoundLogFormula
import com.example.model.TallyEntry
import com.example.model.TallySaveResult
import com.example.model.TimberCalculatorEngine
import com.example.model.TimberUiState
import com.example.model.WoodMode
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger

class TimberViewModel(
    private val repository: TallyRepository = InMemoryTallyRepository(),
    persistenceDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _uiState = MutableStateFlow(TimberUiState())
    val uiState: StateFlow<TimberUiState> = _uiState.asStateFlow()

    private val persistenceScope = CoroutineScope(SupervisorJob() + persistenceDispatcher)
    private val writeQueue = Channel<suspend () -> Unit>(Channel.UNLIMITED)
    private val pendingEntryWrites = AtomicInteger(0)
    private val pendingPrefWrites = AtomicInteger(0)

    private var nextEntryId = 1L
    private var nextSortOrder = 1L

    init {
        // Process all repository mutations strictly in FIFO order so rapid add/delete/undo
        // or preference updates never race out of order.
        persistenceScope.launch {
            for (action in writeQueue) {
                runCatching { action() }
            }
        }

        persistenceScope.launch {
            repository.observeEntries().collect { persistedEntries ->
                if (pendingEntryWrites.get() > 0) return@collect
                _uiState.update { state ->
                    val maxId = persistedEntries.maxOfOrNull { it.id } ?: 0L
                    val maxOrder = persistedEntries.maxOfOrNull { it.sortOrder } ?: 0L
                    if (maxId >= nextEntryId) nextEntryId = maxId + 1L
                    if (maxOrder >= nextSortOrder) nextSortOrder = maxOrder + 1L
                    state.copy(tallyEntries = persistedEntries)
                }
            }
        }

        persistenceScope.launch {
            repository.observePreferences().collect { prefs ->
                if (pendingPrefWrites.get() > 0) return@collect
                _uiState.update { state ->
                    // Preserve the entry's formula while actively editing an existing Round Log entry
                    val effectiveFormula = if (state.editingEntryId != null) {
                        state.roundFormula
                    } else {
                        prefs.roundFormula
                    }
                    recalculate(
                        state.copy(
                            useBengaliDigits = prefs.useBengaliDigits,
                            roundFormula = effectiveFormula,
                            globalRateInput = prefs.globalRateInput
                        )
                    )
                }
            }
        }
    }

    override fun onCleared() {
        writeQueue.close()
        persistenceScope.cancel()
        super.onCleared()
    }

    private fun enqueueEntryWrite(block: suspend () -> Unit) {
        pendingEntryWrites.incrementAndGet()
        val offered = writeQueue.trySend {
            try {
                block()
            } finally {
                pendingEntryWrites.decrementAndGet()
            }
        }.isSuccess
        if (!offered) {
            pendingEntryWrites.decrementAndGet()
        }
    }

    private fun enqueuePrefWrite(block: suspend () -> Unit) {
        pendingPrefWrites.incrementAndGet()
        val offered = writeQueue.trySend {
            try {
                block()
            } finally {
                pendingPrefWrites.decrementAndGet()
            }
        }.isSuccess
        if (!offered) {
            pendingPrefWrites.decrementAndGet()
        }
    }

    fun selectTab(tab: AppTab) {
        _uiState.update { it.copy(activeTab = tab) }
    }

    fun selectWoodMode(mode: WoodMode) {
        _uiState.update { state ->
            if (state.woodMode == mode) return@update state
            // Switching wood modes while editing exits edit mode safely so an entry of one type
            // is never accidentally overwritten with a different wood type.
            val base = if (state.editingEntryId != null) {
                restoreFromDraftOrReset(state).copy(woodMode = mode)
            } else {
                state.copy(woodMode = mode)
            }
            recalculate(base)
        }
    }

    fun selectRoundFormula(formula: RoundLogFormula) {
        var shouldPersist = false
        _uiState.update { state ->
            shouldPersist = state.editingEntryId == null
            recalculate(state.copy(roundFormula = formula))
        }
        if (shouldPersist) {
            enqueuePrefWrite { repository.setRoundFormula(formula) }
        }
    }

    fun toggleBengaliDigits() {
        var nextValue = true
        _uiState.update { state ->
            nextValue = !state.useBengaliDigits
            state.copy(useBengaliDigits = nextValue)
        }
        enqueuePrefWrite { repository.setUseBengaliDigits(nextValue) }
    }

    fun togglePricingSection() {
        _uiState.update { it.copy(showPricingSection = !it.showPricingSection) }
    }

    fun updateRoundLengthFeet(raw: String) {
        val clean = TimberCalculatorEngine.sanitizeNumericInput(raw)
        _uiState.update { state -> recalculate(state.copy(roundLengthFeet = clean)) }
    }

    fun applyRoundLengthPreset(feet: String) {
        val clean = TimberCalculatorEngine.sanitizeNumericInput(feet)
        _uiState.update { state ->
            recalculate(state.copy(roundLengthFeet = clean, roundLengthInches = ""))
        }
    }

    fun updateRoundLengthInches(raw: String) {
        val clean = TimberCalculatorEngine.sanitizeNumericInput(raw)
        _uiState.update { state -> recalculate(state.copy(roundLengthInches = clean)) }
    }

    fun updateRoundGirthFeet(raw: String) {
        val clean = TimberCalculatorEngine.sanitizeNumericInput(raw)
        _uiState.update { state -> recalculate(state.copy(roundGirthFeet = clean)) }
    }

    fun updateRoundGirthInches(raw: String) {
        val clean = TimberCalculatorEngine.sanitizeNumericInput(raw)
        _uiState.update { state -> recalculate(state.copy(roundGirthInches = clean)) }
    }

    fun updateSawnLengthFeet(raw: String) {
        val clean = TimberCalculatorEngine.sanitizeNumericInput(raw)
        _uiState.update { state -> recalculate(state.copy(sawnLengthFeet = clean)) }
    }

    fun updateSawnLengthInches(raw: String) {
        val clean = TimberCalculatorEngine.sanitizeNumericInput(raw)
        _uiState.update { state -> recalculate(state.copy(sawnLengthInches = clean)) }
    }

    fun updateSawnWidthFeet(raw: String) {
        val clean = TimberCalculatorEngine.sanitizeNumericInput(raw)
        _uiState.update { state -> recalculate(state.copy(sawnWidthFeet = clean)) }
    }

    fun updateSawnWidthInches(raw: String) {
        val clean = TimberCalculatorEngine.sanitizeNumericInput(raw)
        _uiState.update { state -> recalculate(state.copy(sawnWidthInches = clean)) }
    }

    fun updateSawnThicknessInches(raw: String) {
        val clean = TimberCalculatorEngine.sanitizeNumericInput(raw)
        _uiState.update { state -> recalculate(state.copy(sawnThicknessInches = clean)) }
    }

    fun updateQuantityInput(raw: String) {
        val digitsOnly = TimberCalculatorEngine.localizeDigits(raw, useBengaliDigits = false)
            .filter { it in '0'..'9' }
            .take(4)
        val normalized = digitsOnly.trimStart('0')
        val parsedQty = normalized.toIntOrNull()?.coerceIn(1, 9999) ?: 1
        _uiState.update { state ->
            recalculate(
                state.copy(
                    quantityInput = normalized,
                    quantity = parsedQty
                )
            )
        }
    }

    fun incrementQuantity() {
        _uiState.update { state ->
            val next = (state.quantity + 1).coerceAtMost(9999)
            recalculate(state.copy(quantity = next, quantityInput = next.toString()))
        }
    }

    fun decrementQuantity() {
        _uiState.update { state ->
            val next = (state.quantity - 1).coerceAtLeast(1)
            recalculate(state.copy(quantity = next, quantityInput = next.toString()))
        }
    }

    fun updateRatePerCft(raw: String) {
        val clean = TimberCalculatorEngine.sanitizeNumericInput(raw, maxLength = 9)
        _uiState.update { state -> recalculate(state.copy(ratePerCft = clean)) }
    }

    fun selectWoodName(name: String) {
        _uiState.update { state ->
            val nextName = if (state.selectedWoodName == name) "" else name
            state.copy(selectedWoodName = nextName)
        }
    }

    fun updateGlobalRate(raw: String) {
        val clean = TimberCalculatorEngine.sanitizeNumericInput(raw, maxLength = 9)
        _uiState.update { it.copy(globalRateInput = clean) }
        enqueuePrefWrite { repository.setGlobalRateInput(clean) }
    }

    fun resetCurrentInputs() {
        _uiState.update { state ->
            recalculate(clearActiveModeInputs(state).copy(editingEntryId = null, preEditDraft = null))
        }
    }

    fun startEditingEntry(entry: TallyEntry) {
        val fmt: (Double) -> String = { v ->
            if (v > 0.0) TimberCalculatorEngine.formatCompactNumber(v, useBengaliDigits = false) else ""
        }
        _uiState.update { state ->
            // Capture the pre-edit draft only once so switching directly from editing item A to B
            // still preserves the user's original unsaved draft and session rate/wood.
            val draft = state.preEditDraft ?: captureDraft(state)
            val shouldExpandPricing = state.showPricingSection ||
                entry.ratePerCft != null ||
                entry.woodName.isNotBlank()

            val updated = when (entry.mode) {
                WoodMode.ROUND_LOG -> state.copy(
                    activeTab = AppTab.CALCULATOR,
                    woodMode = WoodMode.ROUND_LOG,
                    roundFormula = entry.formula,
                    roundLengthFeet = fmt(entry.lengthFeet),
                    roundLengthInches = fmt(entry.lengthInches),
                    roundGirthFeet = fmt(entry.girthFeet),
                    roundGirthInches = fmt(entry.girthInches),
                    quantity = entry.quantity,
                    quantityInput = entry.quantity.toString(),
                    ratePerCft = entry.ratePerCft?.let(fmt) ?: "",
                    selectedWoodName = entry.woodName,
                    showPricingSection = shouldExpandPricing,
                    editingEntryId = entry.id,
                    preEditDraft = draft
                )

                WoodMode.SAWN_TIMBER -> state.copy(
                    activeTab = AppTab.CALCULATOR,
                    woodMode = WoodMode.SAWN_TIMBER,
                    sawnLengthFeet = fmt(entry.lengthFeet),
                    sawnLengthInches = fmt(entry.lengthInches),
                    sawnWidthFeet = fmt(entry.widthFeet),
                    sawnWidthInches = fmt(entry.widthInches),
                    sawnThicknessInches = fmt(entry.thicknessInches),
                    quantity = entry.quantity,
                    quantityInput = entry.quantity.toString(),
                    ratePerCft = entry.ratePerCft?.let(fmt) ?: "",
                    selectedWoodName = entry.woodName,
                    showPricingSection = shouldExpandPricing,
                    editingEntryId = entry.id,
                    preEditDraft = draft
                )
            }
            recalculate(updated)
        }
    }

    fun cancelEditing() {
        _uiState.update { state ->
            if (state.editingEntryId == null) return@update state
            recalculate(restoreFromDraftOrReset(state))
        }
    }

    /**
     * Atomically adds a new entry or updates the currently edited entry and persists it offline.
     * Returns [TallySaveResult.ADDED], [TallySaveResult.UPDATED], or [TallySaveResult.NONE].
     */
    fun addCurrentToTally(): TallySaveResult {
        var saveResult = TallySaveResult.NONE
        var persistedEntry: TallyEntry? = null

        _uiState.update { state ->
            val res = state.currentResult
            if (!res.isValid) {
                saveResult = TallySaveResult.NONE
                persistedEntry = null
                return@update state
            }

            val existing = state.editingEntryId?.let { editId ->
                state.tallyEntries.firstOrNull { it.id == editId }
            }
            val isUpdate = existing != null
            val rate = TimberCalculatorEngine.parsePositiveDouble(state.ratePerCft).takeIf { it > 0.0 }

            val targetId = if (isUpdate) {
                existing!!.id
            } else {
                val maxId = state.tallyEntries.maxOfOrNull { it.id } ?: 0L
                val id = maxOf(nextEntryId, maxId + 1L)
                nextEntryId = id + 1L
                id
            }
            val targetSortOrder = if (isUpdate) {
                existing!!.sortOrder
            } else {
                val maxOrder = state.tallyEntries.maxOfOrNull { it.sortOrder } ?: 0L
                val order = maxOf(nextSortOrder, maxOrder + 1L)
                nextSortOrder = order + 1L
                order
            }

            val entry = when (state.woodMode) {
                WoodMode.ROUND_LOG -> TallyEntry(
                    id = targetId,
                    mode = WoodMode.ROUND_LOG,
                    woodName = state.selectedWoodName,
                    lengthFeet = TimberCalculatorEngine.parsePositiveDouble(state.roundLengthFeet),
                    lengthInches = TimberCalculatorEngine.parsePositiveDouble(state.roundLengthInches),
                    girthFeet = TimberCalculatorEngine.parsePositiveDouble(state.roundGirthFeet),
                    girthInches = TimberCalculatorEngine.parsePositiveDouble(state.roundGirthInches),
                    quantity = state.quantity,
                    formula = state.roundFormula,
                    perPieceCft = res.perPieceCft,
                    totalCft = res.totalCft,
                    ratePerCft = rate,
                    totalPrice = res.totalPrice,
                    sortOrder = targetSortOrder
                )

                WoodMode.SAWN_TIMBER -> TallyEntry(
                    id = targetId,
                    mode = WoodMode.SAWN_TIMBER,
                    woodName = state.selectedWoodName,
                    lengthFeet = TimberCalculatorEngine.parsePositiveDouble(state.sawnLengthFeet),
                    lengthInches = TimberCalculatorEngine.parsePositiveDouble(state.sawnLengthInches),
                    widthFeet = TimberCalculatorEngine.parsePositiveDouble(state.sawnWidthFeet),
                    widthInches = TimberCalculatorEngine.parsePositiveDouble(state.sawnWidthInches),
                    thicknessInches = TimberCalculatorEngine.parsePositiveDouble(state.sawnThicknessInches),
                    quantity = state.quantity,
                    perPieceCft = res.perPieceCft,
                    totalCft = res.totalCft,
                    ratePerCft = rate,
                    totalPrice = res.totalPrice,
                    sortOrder = targetSortOrder
                )
            }

            // Store chronologically (#1, #2, #3...) so Inline Chain, Tally Screen, and Share/Copy bill all match
            val updatedList = if (isUpdate) {
                state.tallyEntries.map { if (it.id == targetId) entry else it }
            } else {
                state.tallyEntries + entry
            }

            saveResult = if (isUpdate) TallySaveResult.UPDATED else TallySaveResult.ADDED
            persistedEntry = entry

            val afterSave = if (isUpdate && state.preEditDraft != null) {
                clearActiveModeInputs(state).copy(
                    tallyEntries = updatedList,
                    roundFormula = state.preEditDraft.roundFormula,
                    ratePerCft = state.preEditDraft.ratePerCft,
                    selectedWoodName = state.preEditDraft.selectedWoodName,
                    showPricingSection = state.preEditDraft.showPricingSection,
                    editingEntryId = null,
                    preEditDraft = null
                )
            } else {
                clearActiveModeInputs(state).copy(
                    tallyEntries = updatedList,
                    editingEntryId = null,
                    preEditDraft = null
                )
            }
            recalculate(afterSave)
        }

        persistedEntry?.let { entryToSave ->
            enqueueEntryWrite { repository.upsertEntry(entryToSave) }
        }
        return saveResult
    }

    fun removeTallyEntry(id: Long): RemovedTallyItem? {
        var removed: RemovedTallyItem? = null
        _uiState.update { state ->
            val idx = state.tallyEntries.indexOfFirst { it.id == id }
            if (idx < 0) return@update state
            removed = RemovedTallyItem(entry = state.tallyEntries[idx], index = idx)
            val remaining = state.tallyEntries.filterNot { it.id == id }
            if (state.editingEntryId == id) {
                // Exit edit mode and restore prior draft so the deleted entry's dimensions don't linger
                recalculate(restoreFromDraftOrReset(state).copy(tallyEntries = remaining))
            } else {
                state.copy(tallyEntries = remaining)
            }
        }
        if (removed != null) {
            enqueueEntryWrite { repository.deleteEntry(id) }
        }
        return removed
    }

    fun restoreTallyEntry(removed: RemovedTallyItem) {
        var restored = false
        _uiState.update { state ->
            if (state.tallyEntries.any { it.id == removed.entry.id }) {
                restored = false
                return@update state
            }
            restored = true
            val updated = (state.tallyEntries + removed.entry)
                .sortedWith(compareBy({ it.sortOrder }, { it.id }))
            state.copy(tallyEntries = updated)
        }
        if (restored) {
            enqueueEntryWrite { repository.upsertEntry(removed.entry) }
        }
    }

    fun clearAllTally() {
        _uiState.update { state ->
            val base = if (state.editingEntryId != null) {
                restoreFromDraftOrReset(state)
            } else {
                state
            }
            recalculate(
                base.copy(
                    tallyEntries = emptyList(),
                    globalRateInput = "",
                    editingEntryId = null,
                    preEditDraft = null
                )
            )
        }
        enqueueEntryWrite { repository.clearAllEntries() }
        enqueuePrefWrite { repository.setGlobalRateInput("") }
    }

    fun buildShareableText(): String {
        return TimberCalculatorEngine.buildShareableSummary(_uiState.value)
    }

    private fun captureDraft(state: TimberUiState): CalculatorDraft {
        return CalculatorDraft(
            woodMode = state.woodMode,
            roundFormula = state.roundFormula,
            roundLengthFeet = state.roundLengthFeet,
            roundLengthInches = state.roundLengthInches,
            roundGirthFeet = state.roundGirthFeet,
            roundGirthInches = state.roundGirthInches,
            sawnLengthFeet = state.sawnLengthFeet,
            sawnLengthInches = state.sawnLengthInches,
            sawnWidthFeet = state.sawnWidthFeet,
            sawnWidthInches = state.sawnWidthInches,
            sawnThicknessInches = state.sawnThicknessInches,
            quantity = state.quantity,
            quantityInput = state.quantityInput,
            ratePerCft = state.ratePerCft,
            selectedWoodName = state.selectedWoodName,
            showPricingSection = state.showPricingSection
        )
    }

    private fun restoreFromDraftOrReset(state: TimberUiState): TimberUiState {
        val draft = state.preEditDraft
        return if (draft != null) {
            state.copy(
                woodMode = draft.woodMode,
                roundFormula = draft.roundFormula,
                roundLengthFeet = draft.roundLengthFeet,
                roundLengthInches = draft.roundLengthInches,
                roundGirthFeet = draft.roundGirthFeet,
                roundGirthInches = draft.roundGirthInches,
                sawnLengthFeet = draft.sawnLengthFeet,
                sawnLengthInches = draft.sawnLengthInches,
                sawnWidthFeet = draft.sawnWidthFeet,
                sawnWidthInches = draft.sawnWidthInches,
                sawnThicknessInches = draft.sawnThicknessInches,
                quantity = draft.quantity,
                quantityInput = draft.quantityInput,
                ratePerCft = draft.ratePerCft,
                selectedWoodName = draft.selectedWoodName,
                showPricingSection = draft.showPricingSection,
                editingEntryId = null,
                preEditDraft = null
            )
        } else {
            clearActiveModeInputs(state).copy(editingEntryId = null, preEditDraft = null)
        }
    }

    private fun clearActiveModeInputs(state: TimberUiState): TimberUiState {
        return when (state.woodMode) {
            WoodMode.ROUND_LOG -> state.copy(
                roundLengthFeet = "",
                roundLengthInches = "",
                roundGirthFeet = "",
                roundGirthInches = "",
                quantity = 1,
                quantityInput = "1"
            )

            WoodMode.SAWN_TIMBER -> state.copy(
                sawnLengthFeet = "",
                sawnLengthInches = "",
                sawnWidthFeet = "",
                sawnWidthInches = "",
                sawnThicknessInches = "",
                quantity = 1,
                quantityInput = "1"
            )
        }
    }

    private fun recalculate(state: TimberUiState): TimberUiState {
        val result = when (state.woodMode) {
            WoodMode.ROUND_LOG -> TimberCalculatorEngine.calculateRoundLog(
                lengthFeetRaw = state.roundLengthFeet,
                lengthInchesRaw = state.roundLengthInches,
                girthFeetRaw = state.roundGirthFeet,
                girthInchesRaw = state.roundGirthInches,
                quantity = state.quantity,
                formula = state.roundFormula,
                ratePerCftRaw = state.ratePerCft
            )

            WoodMode.SAWN_TIMBER -> TimberCalculatorEngine.calculateSawnTimber(
                lengthFeetRaw = state.sawnLengthFeet,
                lengthInchesRaw = state.sawnLengthInches,
                widthFeetRaw = state.sawnWidthFeet,
                widthInchesRaw = state.sawnWidthInches,
                thicknessInchesRaw = state.sawnThicknessInches,
                quantity = state.quantity,
                ratePerCftRaw = state.ratePerCft
            )
        }
        return state.copy(currentResult = result)
    }

    companion object {
        fun factory(context: Context): ViewModelProvider.Factory {
            val appContext = context.applicationContext
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(TimberViewModel::class.java)) {
                        return TimberViewModel(
                            repository = OfflineTallyRepository.getInstance(appContext)
                        ) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
                }
            }
        }
    }
}
