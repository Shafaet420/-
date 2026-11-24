package com.example.model

import java.util.Locale
import kotlin.math.PI

object TimberCalculatorEngine {

    private const val CFT_TO_CBM_FACTOR = 0.0283168466
    private const val HOPPUS_INCH_DIVISOR = 2304.0 // 16 * 144
    private const val SAWN_INCH_DIVISOR = 144.0

    private val bengaliDigitMap = mapOf(
        '0' to '০',
        '1' to '১',
        '2' to '২',
        '3' to '৩',
        '4' to '৪',
        '5' to '৫',
        '6' to '৬',
        '7' to '৭',
        '8' to '৮',
        '9' to '৯'
    )

    private val asciiDigitMap = bengaliDigitMap.entries.associate { (k, v) -> v to k }

    /**
     * Converts Bengali numerals (০-৯) to ASCII (0-9), filters to valid decimal input,
     * and normalizes redundant leading zeros (e.g. "005" -> "5", while preserving "0" and "0.5").
     */
    fun sanitizeNumericInput(raw: String, maxLength: Int = 7): String {
        val sb = StringBuilder()
        var hasDecimal = false
        for (ch in raw) {
            val normalized = asciiDigitMap[ch] ?: ch
            when {
                normalized in '0'..'9' -> {
                    if (sb.length == 1 && sb[0] == '0' && !hasDecimal) {
                        if (normalized == '0') continue
                        sb.setCharAt(0, normalized)
                    } else {
                        sb.append(normalized)
                    }
                }
                normalized == '.' && !hasDecimal -> {
                    hasDecimal = true
                    sb.append('.')
                }
            }
            if (sb.length >= maxLength) break
        }
        return sb.toString()
    }

    fun parsePositiveDouble(raw: String): Double {
        val cleaned = sanitizeNumericInput(raw, maxLength = 16)
        if (cleaned.isEmpty() || cleaned == ".") return 0.0
        val value = cleaned.toDoubleOrNull() ?: 0.0
        return if (value > 0.0 && value.isFinite()) value else 0.0
    }

    /**
     * Round Log (গোল গাছ) CFT calculation:
     * - Hoppus / Sawmill standard: (Girth_in² × Length_ft) / 2304  [equivalent to (Girth_ft / 4)² × Length_ft]
     * - True Cylinder volume: (Girth_in² × Length_ft) / (4π × 144)
     */
    fun calculateRoundLog(
        lengthFeetRaw: String,
        lengthInchesRaw: String,
        girthFeetRaw: String,
        girthInchesRaw: String,
        quantity: Int,
        formula: RoundLogFormula,
        ratePerCftRaw: String
    ): TimberMeasurementResult {
        val lengthFt = parsePositiveDouble(lengthFeetRaw)
        val lengthIn = parsePositiveDouble(lengthInchesRaw)
        val girthFt = parsePositiveDouble(girthFeetRaw)
        val girthIn = parsePositiveDouble(girthInchesRaw)

        val totalLengthFeet = lengthFt + (lengthIn / 12.0)
        val totalGirthInches = (girthFt * 12.0) + girthIn

        if (totalLengthFeet <= 0.0 || totalGirthInches <= 0.0) {
            return TimberMeasurementResult(
                totalLengthFeet = totalLengthFeet,
                totalGirthInches = totalGirthInches,
                isValid = false
            )
        }

        val perPieceCft = when (formula) {
            RoundLogFormula.HOPPUS ->
                (totalGirthInches * totalGirthInches * totalLengthFeet) / HOPPUS_INCH_DIVISOR

            RoundLogFormula.TRUE_CYLINDER ->
                (totalGirthInches * totalGirthInches * totalLengthFeet) / (4.0 * PI * SAWN_INCH_DIVISOR)
        }

        val safeQty = quantity.coerceAtLeast(1)
        val totalCft = perPieceCft * safeQty
        val rate = parsePositiveDouble(ratePerCftRaw)
        val totalPrice = if (rate > 0.0) totalCft * rate else null

        return TimberMeasurementResult(
            perPieceCft = perPieceCft,
            totalCft = totalCft,
            totalCbm = cftToCbm(totalCft),
            totalPrice = totalPrice,
            totalLengthFeet = totalLengthFeet,
            totalGirthInches = totalGirthInches,
            isValid = true
        )
    }

    /**
     * Sawn Timber (কাটা / চেরাই কাঠ) CFT calculation:
     * (Length_ft × Width_in × Thickness_in) / 144
     */
    fun calculateSawnTimber(
        lengthFeetRaw: String,
        lengthInchesRaw: String,
        widthFeetRaw: String,
        widthInchesRaw: String,
        thicknessInchesRaw: String,
        quantity: Int,
        ratePerCftRaw: String
    ): TimberMeasurementResult {
        val lengthFt = parsePositiveDouble(lengthFeetRaw)
        val lengthIn = parsePositiveDouble(lengthInchesRaw)
        val widthFt = parsePositiveDouble(widthFeetRaw)
        val widthIn = parsePositiveDouble(widthInchesRaw)
        val thicknessIn = parsePositiveDouble(thicknessInchesRaw)

        val totalLengthFeet = lengthFt + (lengthIn / 12.0)
        val totalWidthInches = (widthFt * 12.0) + widthIn

        if (totalLengthFeet <= 0.0 || totalWidthInches <= 0.0 || thicknessIn <= 0.0) {
            return TimberMeasurementResult(
                totalLengthFeet = totalLengthFeet,
                totalWidthInches = totalWidthInches,
                totalThicknessInches = thicknessIn,
                isValid = false
            )
        }

        val perPieceCft = (totalLengthFeet * totalWidthInches * thicknessIn) / SAWN_INCH_DIVISOR
        val safeQty = quantity.coerceAtLeast(1)
        val totalCft = perPieceCft * safeQty
        val rate = parsePositiveDouble(ratePerCftRaw)
        val totalPrice = if (rate > 0.0) totalCft * rate else null

        return TimberMeasurementResult(
            perPieceCft = perPieceCft,
            totalCft = totalCft,
            totalCbm = cftToCbm(totalCft),
            totalPrice = totalPrice,
            totalLengthFeet = totalLengthFeet,
            totalWidthInches = totalWidthInches,
            totalThicknessInches = thicknessIn,
            isValid = true
        )
    }

    fun cftToCbm(cft: Double): Double = cft * CFT_TO_CBM_FACTOR

    fun toDisplayDigits(input: String, useBengaliDigits: Boolean): String {
        return localizeDigits(input, useBengaliDigits)
    }

    /**
     * Bidirectionally converts digits between Bengali (০-৯) and ASCII (0-9)
     * so hardcoded resource strings also honor the user's numeral language preference.
     */
    fun localizeDigits(input: String, useBengaliDigits: Boolean): String {
        val sb = StringBuilder(input.length)
        for (ch in input) {
            val mapped = if (useBengaliDigits) {
                bengaliDigitMap[ch] ?: ch
            } else {
                asciiDigitMap[ch] ?: ch
            }
            sb.append(mapped)
        }
        return sb.toString()
    }

    fun formatCft(value: Double, useBengaliDigits: Boolean = true): String {
        val safe = if (value.isFinite() && value > 0.0) value else 0.0
        val decimals = when {
            safe > 0.0 && safe < 0.001 -> 4
            safe > 0.0 && safe < 0.01 -> 3
            else -> 2
        }
        return formatDecimal(safe, decimals, useBengaliDigits)
    }

    fun formatDecimal(value: Double, decimals: Int = 2, useBengaliDigits: Boolean = true): String {
        val safe = if (value.isFinite()) value else 0.0
        val formatted = String.format(Locale.US, "%.${decimals}f", safe)
        return localizeDigits(formatted, useBengaliDigits)
    }

    /**
     * Preserves up to 3 decimal places (trimming trailing zeros) so exact 1/8-inch suta
     * measurements like 0.125, 0.375, 0.625, 0.875 never get rounded or corrupted on display/edit.
     */
    fun formatCompactNumber(value: Double, useBengaliDigits: Boolean = true): String {
        if (!value.isFinite()) return localizeDigits("0", useBengaliDigits)
        val formatted = if (value % 1.0 == 0.0) {
            String.format(Locale.US, "%.0f", value)
        } else {
            String.format(Locale.US, "%.3f", value).trimEnd('0').trimEnd('.')
        }
        return localizeDigits(formatted, useBengaliDigits)
    }

    fun formatInt(value: Int, useBengaliDigits: Boolean = true): String {
        return localizeDigits(value.toString(), useBengaliDigits)
    }

    fun formatFeetInchesSummary(feet: Double, inches: Double, useBengaliDigits: Boolean): String {
        val parts = mutableListOf<String>()
        if (feet > 0.0) {
            parts.add("${formatCompactNumber(feet, useBengaliDigits)}'")
        }
        if (inches > 0.0) {
            parts.add("${formatCompactNumber(inches, useBengaliDigits)}\"")
        }
        return if (parts.isEmpty()) localizeDigits("0\"", useBengaliDigits) else parts.joinToString(" ")
    }

    fun buildShareableSummary(state: TimberUiState): String {
        val bn = state.useBengaliDigits
        val globalRate = state.effectiveGlobalRate
        val sb = StringBuilder()
        sb.appendLine("কাঠ হিসাব — সিএফটি (CFT) তালিকা")
        sb.appendLine("────────────────────────")
        state.tallyEntries.forEachIndexed { index, item ->
            val num = formatInt(index + 1, bn)
            val modeLabel = if (item.mode == WoodMode.ROUND_LOG) "গোল গাছ" else "কাটা কাঠ"
            val namePart = if (item.woodName.isNotBlank()) " (${item.woodName})" else ""
            val formulaPart = if (item.mode == WoodMode.ROUND_LOG && item.formula == RoundLogFormula.TRUE_CYLINDER) {
                " [${localizeDigits("÷4π", bn)}]"
            } else {
                ""
            }
            val dimPart = if (item.mode == WoodMode.ROUND_LOG) {
                val len = formatFeetInchesSummary(item.lengthFeet, item.lengthInches, bn)
                val girth = formatFeetInchesSummary(item.girthFeet, item.girthInches, bn)
                "দৈর্ঘ্য $len × বেড় $girth"
            } else {
                val len = formatFeetInchesSummary(item.lengthFeet, item.lengthInches, bn)
                val wid = formatFeetInchesSummary(item.widthFeet, item.widthInches, bn)
                val thk = "${formatCompactNumber(item.thicknessInches, bn)}\""
                "দৈর্ঘ্য $len × প্রস্থ $wid × পুরুত্ব $thk"
            }
            val qty = formatInt(item.quantity, bn)
            val cft = formatCft(item.totalCft, bn)
            sb.append("$num. $modeLabel$namePart$formulaPart: $dimPart | $qty পিস = $cft সিএফটি")
            val effectiveItemPrice = if (globalRate != null) item.totalCft * globalRate else item.totalPrice
            effectiveItemPrice?.let { price ->
                sb.append(" (৳${formatDecimal(price, 0, bn)})")
            }
            sb.appendLine()
        }
        sb.appendLine("────────────────────────")
        sb.appendLine("মোট সংখ্যা: ${formatInt(state.totalTallyPieces, bn)} পিস")
        sb.appendLine("মোট সিএফটি: ${formatCft(state.totalTallyCft, bn)} CFT (${formatDecimal(state.totalTallyCbm, 3, bn)} m³)")
        if (globalRate != null) {
            sb.appendLine("গড় দর: ৳${formatCompactNumber(globalRate, bn)}/সিএফটি")
        }
        state.totalTallyPrice?.let { price ->
            val priceLabel = when {
                state.isUsingGlobalRate -> "গড় দরে মোট দাম"
                state.pricedTallyCount < state.tallyEntries.size ->
                    "দর দেওয়া ${formatInt(state.pricedTallyCount, bn)}/${formatInt(state.tallyEntries.size, bn)} টির দাম"
                else -> "মোট আনুমানিক দাম"
            }
            sb.appendLine("$priceLabel: ৳${formatDecimal(price, 0, bn)}")
        }
        return sb.toString().trim()
    }
}
