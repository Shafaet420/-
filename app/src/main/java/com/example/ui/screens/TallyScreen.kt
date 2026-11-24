package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Forest
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.RoundLogFormula
import com.example.model.TallyEntry
import com.example.model.TimberCalculatorEngine
import com.example.model.TimberUiState
import com.example.model.WoodMode
import com.example.ui.components.NumericUnitField
import com.example.ui.theme.HeroBrassAccent
import com.example.ui.theme.HeroCreamText
import com.example.ui.theme.HeroTeakEnd
import com.example.ui.theme.HeroTeakStart
import com.example.ui.theme.JetBrainsMonoFontFamily

@Composable
fun TallyScreen(
    state: TimberUiState,
    onEditEntry: (TallyEntry) -> Unit,
    onRemoveEntry: (Long) -> Unit,
    onClearAll: () -> Unit,
    onGlobalRateChange: (String) -> Unit,
    onCopySummary: () -> Unit,
    onShareSummary: () -> Unit,
    onNavigateToCalculator: () -> Unit,
    showGoToCalculatorButton: Boolean = true,
    modifier: Modifier = Modifier
) {
    val bn = state.useBengaliDigits
    val focusManager = LocalFocusManager.current
    var showClearAllDialog by remember { mutableStateOf(false) }

    if (showClearAllDialog) {
        AlertDialog(
            onDismissRequest = { showClearAllDialog = false },
            title = { Text(text = stringResource(R.string.dialog_clear_all_title)) },
            text = { Text(text = stringResource(R.string.dialog_clear_all_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearAllDialog = false
                        onClearAll()
                    },
                    modifier = Modifier.testTag("confirm_clear_all_button")
                ) {
                    Text(
                        text = stringResource(R.string.action_clear_all),
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showClearAllDialog = false },
                    modifier = Modifier.testTag("cancel_clear_all_button")
                ) {
                    Text(text = stringResource(R.string.action_cancel))
                }
            }
        )
    }

    if (state.tallyEntries.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.widthIn(max = 360.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(72.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.ReceiptLong,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }
                Text(
                    text = stringResource(R.string.tally_empty_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = stringResource(R.string.tally_empty_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                if (showGoToCalculatorButton) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = onNavigateToCalculator,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("empty_go_calculator_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Calculate,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = stringResource(R.string.tally_go_calculator))
                    }
                }
            }
        }
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .imePadding(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Grand Total Summary Banner
        item(key = "tally_summary_card") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.extraLarge)
                        .background(Brush.linearGradient(listOf(HeroTeakStart, HeroTeakEnd)))
                        .padding(18.dp)
                        .testTag("tally_summary_banner")
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.result_total_cft),
                                style = MaterialTheme.typography.labelLarge,
                                color = HeroBrassAccent
                            )
                            Surface(
                                shape = CircleShape,
                                color = HeroCreamText.copy(alpha = 0.16f)
                            ) {
                                Text(
                                    text = "${TimberCalculatorEngine.formatInt(state.totalTallyPieces, bn)} ${stringResource(R.string.unit_piece)}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = HeroCreamText,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        val formattedTotalCft = TimberCalculatorEngine.formatCft(state.totalTallyCft, bn)
                        val totalCftStyle = when {
                            formattedTotalCft.length >= 10 -> MaterialTheme.typography.displayMedium.copy(fontSize = 24.sp, lineHeight = 30.sp)
                            formattedTotalCft.length >= 7 -> MaterialTheme.typography.displayMedium.copy(fontSize = 28.sp, lineHeight = 34.sp)
                            else -> MaterialTheme.typography.displayMedium
                        }
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = formattedTotalCft,
                                style = totalCftStyle,
                                color = HeroCreamText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .weight(1f, fill = false)
                                    .testTag("tally_total_cft_text")
                            )
                            Text(
                                text = stringResource(R.string.unit_cft),
                                style = MaterialTheme.typography.titleMedium,
                                color = HeroBrassAccent,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                modifier = Modifier.padding(bottom = 5.dp)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "${stringResource(R.string.tally_round_count)}: ${TimberCalculatorEngine.formatCft(state.roundLogTallyCft, bn)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = HeroCreamText.copy(alpha = 0.85f)
                            )
                            Text(
                                text = "•",
                                style = MaterialTheme.typography.bodySmall,
                                color = HeroBrassAccent
                            )
                            Text(
                                text = "${stringResource(R.string.tally_sawn_count)}: ${TimberCalculatorEngine.formatCft(state.sawnTimberTallyCft, bn)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = HeroCreamText.copy(alpha = 0.85f)
                            )
                        }

                        state.totalTallyPrice?.let { price ->
                            val priceLabel = when {
                                state.isUsingGlobalRate -> stringResource(R.string.tally_global_rate_price_label)
                                state.pricedTallyCount < state.tallyEntries.size -> stringResource(
                                    R.string.tally_partial_price_label,
                                    TimberCalculatorEngine.formatInt(state.pricedTallyCount, bn),
                                    TimberCalculatorEngine.formatInt(state.tallyEntries.size, bn)
                                )
                                else -> stringResource(R.string.result_estimated_price)
                            }
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = HeroCreamText.copy(alpha = 0.14f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = priceLabel,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = HeroCreamText.copy(alpha = 0.85f)
                                    )
                                    Text(
                                        text = "৳ ${TimberCalculatorEngine.formatDecimal(price, 0, bn)}",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = HeroBrassAccent,
                                        modifier = Modifier.testTag("tally_total_price_text")
                                    )
                                }
                            }
                        }
                    }
                }

                // Global Rate Override & Copy/Share Actions
                NumericUnitField(
                    value = state.globalRateInput,
                    onValueChange = onGlobalRateChange,
                    unitLabel = "${stringResource(R.string.unit_taka)}/${stringResource(R.string.unit_cft)}",
                    placeholder = stringResource(R.string.tally_global_rate_short_hint),
                    imeAction = ImeAction.Done,
                    onDone = { focusManager.clearFocus() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("tally_global_rate_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onCopySummary,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .testTag("copy_tally_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ContentCopy,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.action_copy_tally),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Button(
                        onClick = onShareSummary,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .testTag("share_tally_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Share,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.action_share_tally),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${stringResource(R.string.tally_title)} (${TimberCalculatorEngine.formatInt(state.tallyEntries.size, bn)})",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    TextButton(
                        onClick = { showClearAllDialog = true },
                        modifier = Modifier.testTag("clear_all_tally_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteSweep,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.action_clear_all),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        itemsIndexed(
            items = state.tallyEntries,
            key = { _, item -> item.id }
        ) { index, entry ->
            TallyEntryCard(
                index = index + 1,
                entry = entry,
                effectiveGlobalRate = state.effectiveGlobalRate,
                isEditing = state.editingEntryId == entry.id,
                useBengaliDigits = bn,
                onEdit = { onEditEntry(entry) },
                onDelete = { onRemoveEntry(entry.id) },
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
            )
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TallyEntryCard(
    index: Int,
    entry: TallyEntry,
    effectiveGlobalRate: Double?,
    isEditing: Boolean,
    useBengaliDigits: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isRound = entry.mode == WoodMode.ROUND_LOG
    val badgeBg = if (isRound) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        MaterialTheme.colorScheme.primaryContainer
    }
    val badgeFg = if (isRound) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.onPrimaryContainer
    }

    val dimSummary = if (isRound) {
        val len = TimberCalculatorEngine.formatFeetInchesSummary(entry.lengthFeet, entry.lengthInches, useBengaliDigits)
        val girth = TimberCalculatorEngine.formatFeetInchesSummary(entry.girthFeet, entry.girthInches, useBengaliDigits)
        stringResource(R.string.dim_round_format, len, girth)
    } else {
        val len = TimberCalculatorEngine.formatFeetInchesSummary(entry.lengthFeet, entry.lengthInches, useBengaliDigits)
        val wid = TimberCalculatorEngine.formatFeetInchesSummary(entry.widthFeet, entry.widthInches, useBengaliDigits)
        val thk = "${TimberCalculatorEngine.formatCompactNumber(entry.thicknessInches, useBengaliDigits)}\""
        stringResource(R.string.dim_sawn_format, len, wid, thk)
    }
    val effectivePrice = if (effectiveGlobalRate != null) entry.totalCft * effectiveGlobalRate else entry.totalPrice

    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = if (isEditing) {
                MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.45f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = BorderStroke(
            width = if (isEditing) 1.5.dp else 1.dp,
            color = if (isEditing) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = modifier.testTag("tally_item_card_${entry.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, end = 8.dp, top = 12.dp, bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Top Row: Mode icon + #index + mode/badges + bold CFT
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = badgeBg,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isRound) Icons.Rounded.Forest else Icons.Rounded.GridView,
                            contentDescription = null,
                            tint = badgeFg,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                FlowRow(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "#${TimberCalculatorEngine.formatInt(index, useBengaliDigits)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (isRound) stringResource(R.string.mode_round_log) else stringResource(R.string.mode_sawn_timber),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (isRound && entry.formula == RoundLogFormula.TRUE_CYLINDER) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = TimberCalculatorEngine.localizeDigits(
                                    stringResource(R.string.badge_cylinder_formula),
                                    useBengaliDigits
                                ),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    if (entry.woodName.isNotBlank()) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.tertiaryContainer
                        ) {
                            Text(
                                text = entry.woodName,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = TimberCalculatorEngine.formatCft(entry.totalCft, useBengaliDigits),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(R.string.unit_cft),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Bottom Row: Full-width readable dimensions + qty/price on left, 48dp actions on right
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = dimSummary,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "${TimberCalculatorEngine.formatInt(entry.quantity, useBengaliDigits)} ${stringResource(R.string.unit_piece)}" +
                            (effectivePrice?.let { " • ৳${TimberCalculatorEngine.formatDecimal(it, 0, useBengaliDigits)}" } ?: ""),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("edit_tally_item_${entry.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = stringResource(R.string.action_edit_item),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("delete_tally_item_${entry.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = stringResource(R.string.action_delete_item),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
