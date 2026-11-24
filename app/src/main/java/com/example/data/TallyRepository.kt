package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.RoundLogFormula
import com.example.model.TallyEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.io.IOException

private val Context.kathHisabPrefs: DataStore<Preferences> by preferencesDataStore(
    name = "kath_hisab_prefs"
)

data class PersistedPreferences(
    val useBengaliDigits: Boolean = true,
    val roundFormula: RoundLogFormula = RoundLogFormula.HOPPUS,
    val globalRateInput: String = ""
)

interface TallyRepository {
    fun observeEntries(): Flow<List<TallyEntry>>
    fun observePreferences(): Flow<PersistedPreferences>

    suspend fun upsertEntry(entry: TallyEntry)
    suspend fun deleteEntry(id: Long)
    suspend fun clearAllEntries()

    suspend fun setUseBengaliDigits(useBengali: Boolean)
    suspend fun setRoundFormula(formula: RoundLogFormula)
    suspend fun setGlobalRateInput(rate: String)
}

/**
 * Production offline repository backed by Room SQLite (for structured tally rows)
 * and Jetpack DataStore Preferences (for lightweight user settings).
 */
class OfflineTallyRepository(
    private val tallyDao: TallyDao,
    private val dataStore: DataStore<Preferences>
) : TallyRepository {

    private object Keys {
        val USE_BENGALI_DIGITS = booleanPreferencesKey("use_bengali_digits")
        val ROUND_FORMULA = stringPreferencesKey("round_formula")
        val GLOBAL_RATE_INPUT = stringPreferencesKey("global_rate_input")
    }

    override fun observeEntries(): Flow<List<TallyEntry>> {
        return tallyDao.observeAll().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun observePreferences(): Flow<PersistedPreferences> {
        return dataStore.data
            .catch { cause ->
                if (cause is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw cause
                }
            }
            .map { prefs ->
                val formulaRaw = prefs[Keys.ROUND_FORMULA] ?: RoundLogFormula.HOPPUS.name
                val formula = runCatching { RoundLogFormula.valueOf(formulaRaw) }
                    .getOrDefault(RoundLogFormula.HOPPUS)
                PersistedPreferences(
                    useBengaliDigits = prefs[Keys.USE_BENGALI_DIGITS] ?: true,
                    roundFormula = formula,
                    globalRateInput = prefs[Keys.GLOBAL_RATE_INPUT] ?: ""
                )
            }
    }

    override suspend fun upsertEntry(entry: TallyEntry) {
        tallyDao.upsert(TallyEntryEntity.fromDomain(entry))
    }

    override suspend fun deleteEntry(id: Long) {
        tallyDao.deleteById(id)
    }

    override suspend fun clearAllEntries() {
        tallyDao.clearAll()
    }

    override suspend fun setUseBengaliDigits(useBengali: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.USE_BENGALI_DIGITS] = useBengali
        }
    }

    override suspend fun setRoundFormula(formula: RoundLogFormula) {
        dataStore.edit { prefs ->
            prefs[Keys.ROUND_FORMULA] = formula.name
        }
    }

    override suspend fun setGlobalRateInput(rate: String) {
        dataStore.edit { prefs ->
            prefs[Keys.GLOBAL_RATE_INPUT] = rate
        }
    }

    companion object {
        @Volatile
        private var instance: OfflineTallyRepository? = null

        fun getInstance(context: Context): OfflineTallyRepository {
            return instance ?: synchronized(this) {
                instance ?: run {
                    val appContext = context.applicationContext
                    val db = KathHisabDatabase.getInstance(appContext)
                    OfflineTallyRepository(
                        tallyDao = db.tallyDao(),
                        dataStore = appContext.kathHisabPrefs
                    ).also { instance = it }
                }
            }
        }
    }
}

/**
 * Lightweight in-memory repository used by default in pure JVM unit tests.
 */
class InMemoryTallyRepository(
    initialEntries: List<TallyEntry> = emptyList(),
    initialPreferences: PersistedPreferences = PersistedPreferences()
) : TallyRepository {

    private val entriesFlow = MutableStateFlow(initialEntries.sortedWith(compareBy({ it.sortOrder }, { it.id })))
    private val prefsFlow = MutableStateFlow(initialPreferences)

    override fun observeEntries(): Flow<List<TallyEntry>> = entriesFlow

    override fun observePreferences(): Flow<PersistedPreferences> = prefsFlow

    override suspend fun upsertEntry(entry: TallyEntry) {
        entriesFlow.update { current ->
            val without = current.filterNot { it.id == entry.id }
            (without + entry).sortedWith(compareBy({ it.sortOrder }, { it.id }))
        }
    }

    override suspend fun deleteEntry(id: Long) {
        entriesFlow.update { current -> current.filterNot { it.id == id } }
    }

    override suspend fun clearAllEntries() {
        entriesFlow.value = emptyList()
    }

    override suspend fun setUseBengaliDigits(useBengali: Boolean) {
        prefsFlow.update { it.copy(useBengaliDigits = useBengali) }
    }

    override suspend fun setRoundFormula(formula: RoundLogFormula) {
        prefsFlow.update { it.copy(roundFormula = formula) }
    }

    override suspend fun setGlobalRateInput(rate: String) {
        prefsFlow.update { it.copy(globalRateInput = rate) }
    }
}
