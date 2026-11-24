package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Forest
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import com.example.R
import com.example.model.RoundLogFormula
import com.example.model.TallyEntry
import com.example.model.TimberCalculatorEngine
import com.example.model.TimberUiState
import com.example.model.WoodMode
import com.example.ui.components.FeetInchInputRow
import com.example.ui.components.NumericUnitField
import com.example.ui.components.QuantityStepperRow
import com.example.ui.components.QuickPresetChipRow
import com.example.ui.components.WoodBlueprintCanvas
import com.example.ui.theme.HeroBrassAccent
import com.example.ui.theme.HeroCreamText
import com.example.ui.theme.HeroForestEnd
import com.example.ui.theme.HeroForestStart
import com.example.ui.theme.HeroTeakEnd
import com.example.ui.theme.HeroTeakStart
import com.example.ui.theme.JetBrainsMonoFontFamily

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CalculatorScreen(
    state: TimberUiState,
    onSelectMode: (WoodMode) -> Unit,
    onSelectRoundFormula: (RoundLogFormula) -> Unit,
    onRoundLengthFeetChange: (String) -> Unit,
    onApplyRoundLengthPreset: (String) -> Unit,
    onRoundLengthInchesChange: (String) -> Unit,
    onRoundGirthFeetChange: (String) -> Unit,
    onRoundGirthInchesChange: (String) -> Unit,
    onSawnLengthFeetChange: (String) -> Unit,
    onSawnLengthInchesChange: (String) -> Unit,
    onSawnWidthFeetChange: (String) -> Unit,
    onSawnWidthInchesChange: (String) -> Unit,
    onSawnThicknessInchesChange: (String) -> Unit,
    onQuantityInputChange: (String) -> Unit,
    onIncrementQty: () -> Unit,
    onDecrementQty: () -> Unit,
    onTogglePricing: () -> Unit,
    onRateChange: (String) -> Unit,
    onSelectWoodName: (String) -> Unit,
    onAddToTally: () -> Unit,
    onCancelEditing: () -> Unit,
    onReset: () -> Unit,
    onEditEntry: (TallyEntry) -> Unit,
    onRemoveEntry: (Long) -> Unit,
    onCopySummary: () -> Unit,
    onShareSummary: () -> Unit,
    onOpenFullTally: () -> Unit,
    showInlineChain: Boolean = true,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()
    val bn = state.useBengaliDigits

    val roundLenFtFocus = remember { FocusRequester() }
    val roundLenInFocus = remember { FocusRequester() }
    val roundGirthFtFocus = remember { FocusRequester() }
    val roundGirthInFocus = remember { FocusRequester() }

    val sawnLenFtFocus = remember { FocusRequester() }
    val sawnLenInFocus = remember { FocusRequester() }
    val sawnWidFtFocus = remember { FocusRequester() }
    val sawnWidInFocus = remember { FocusRequester() }
    val sawnThkInFocus = remember { FocusRequester() }
    val quantityFocus = remember { FocusRequester() }

    val woodPresets = listOf(
        "shegun" to stringResource(R.string.wood_shegun),
        "mehogoni" to stringResource(R.string.wood_mehogoni),
        "kathal" to stringResource(R.string.wood_kathal),
        "aam" to stringResource(R.string.wood_aam),
        "koroi" to stringResource(R.string.wood_koroi),
        "akashmoni" to stringResource(R.string.wood_akashmoni)
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .imePadding()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 600.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Mode Switcher (গোল গাছ vs কাটা কাঠ)
            WoodModeSwitcher(
                selectedMode = state.woodMode,
                onSelectMode = {
                    focusManager.clearFocus()
                    onSelectMode(it)
                }
            )

            // Active Edit Mode Banner
            AnimatedVisibility(visible = state.editingEntryId != null) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = stringResource(R.string.editing_banner_label),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                        TextButton(
                            onClick = onCancelEditing,
                            modifier = Modifier.testTag("cancel_edit_banner_button")
                        ) {
                            Text(text = stringResource(R.string.action_cancel))
                        }
                    }
                }
            }

            // Live Auto-Calculated Hero Result Card
            HeroCftResultCard(state = state)

            // Measurement Inputs Card
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    AnimatedContent(
                        targetState = state.woodMode,
                        transitionSpec = {
                            fadeIn(tween(180)) togetherWith fadeOut(tween(140))
                        },
                        label = "wood_mode_inputs"
                    ) { mode ->
                        when (mode) {
                            WoodMode.ROUND_LOG -> {
                                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                    FeetInchInputRow(
                                        label = stringResource(R.string.label_length),
                                        feetValue = state.roundLengthFeet,
                                        onFeetChange = onRoundLengthFeetChange,
                                        inchValue = state.roundLengthInches,
                                        onInchChange = onRoundLengthInchesChange,
                                        useBengaliDigits = bn,
                                        feetTestTag = "round_length_feet_input",
                                        inchTestTag = "round_length_inch_input",
                                        feetFocusRequester = roundLenFtFocus,
                                        inchFocusRequester = roundLenInFocus,
                                        nextFocusRequester = roundGirthFtFocus
                                    )

                                    QuickPresetChipRow(
                                        presets = listOf(
                                            "6'" to "6",
                                            "8'" to "8",
                                            "10'" to "10",
                                            "12'" to "12",
                                            "14'" to "14"
                                        ),
                                        selectedValue = if (state.roundLengthInches.isEmpty()) state.roundLengthFeet else "",
                                        useBengaliDigits = bn,
                                        onSelect = onApplyRoundLengthPreset,
                                        tagPrefix = "preset_round_len"
                                    )

                                    FeetInchInputRow(
                                        label = stringResource(R.string.label_girth),
                                        feetValue = state.roundGirthFeet,
                                        onFeetChange = onRoundGirthFeetChange,
                                        inchValue = state.roundGirthInches,
                                        onInchChange = onRoundGirthInchesChange,
                                        useBengaliDigits = bn,
                                        feetTestTag = "round_girth_feet_input",
                                        inchTestTag = "round_girth_inch_input",
                                        feetFocusRequester = roundGirthFtFocus,
                                        inchFocusRequester = roundGirthInFocus,
                                        nextFocusRequester = quantityFocus,
                                        lastImeAction = ImeAction.Next
                                    )

                                    RoundFormulaSelector(
                                        selected = state.roundFormula,
                                        useBengaliDigits = bn,
                                        onSelect = onSelectRoundFormula
                                    )
                                }
                            }

                            WoodMode.SAWN_TIMBER -> {
                                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                    FeetInchInputRow(
                                        label = stringResource(R.string.label_length),
                                        feetValue = state.sawnLengthFeet,
                                        onFeetChange = onSawnLengthFeetChange,
                                        inchValue = state.sawnLengthInches,
                                        onInchChange = onSawnLengthInchesChange,
                                        useBengaliDigits = bn,
                                        feetTestTag = "sawn_length_feet_input",
                                        inchTestTag = "sawn_length_inch_input",
                                        feetFocusRequester = sawnLenFtFocus,
                                        inchFocusRequester = sawnLenInFocus,
                                        nextFocusRequester = sawnWidFtFocus
                                    )

                                    FeetInchInputRow(
                                        label = stringResource(R.string.label_width),
                                        feetValue = state.sawnWidthFeet,
                                        onFeetChange = onSawnWidthFeetChange,
                                        inchValue = state.sawnWidthInches,
                                        onInchChange = onSawnWidthInchesChange,
                                        useBengaliDigits = bn,
                                        feetTestTag = "sawn_width_feet_input",
                                        inchTestTag = "sawn_width_inch_input",
                                        feetFocusRequester = sawnWidFtFocus,
                                        inchFocusRequester = sawnWidInFocus,
                                        nextFocusRequester = sawnThkInFocus
                                    )

                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Text(
                                            text = stringResource(R.string.label_thickness),
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        NumericUnitField(
                                            value = state.sawnThicknessInches,
                                            onValueChange = onSawnThicknessInchesChange,
                                            unitLabel = stringResource(R.string.unit_inch),
                                            placeholder = TimberCalculatorEngine.toDisplayDigits("0", bn),
                                            imeAction = ImeAction.Next,
                                            onNext = { runCatching { quantityFocus.requestFocus() } },
                                            onDone = { focusManager.clearFocus() },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .focusRequester(sawnThkInFocus)
                                                .testTag("sawn_thickness_inch_input")
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        QuickPresetChipRow(
                                            presets = listOf(
                                                "1\"" to "1",
                                                "1.5\"" to "1.5",
                                                "2\"" to "2",
                                                "3\"" to "3",
                                                "4\"" to "4"
                                            ),
                                            selectedValue = state.sawnThicknessInches,
                                            useBengaliDigits = bn,
                                            onSelect = onSawnThicknessInchesChange,
                                            tagPrefix = "preset_thickness"
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Direct-editable Quantity Stepper (e.g., 15, 45)
                    QuantityStepperRow(
                        quantity = state.quantity,
                        quantityInput = state.quantityInput,
                        useBengaliDigits = bn,
                        onQuantityInputChange = onQuantityInputChange,
                        onDecrement = onDecrementQty,
                        onIncrement = onIncrementQty,
                        focusRequester = quantityFocus,
                        onDone = { focusManager.clearFocus() }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    // Optional Price & Wood Species Expander
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(onClick = onTogglePricing)
                            .minimumInteractiveComponentSize()
                            .padding(horizontal = 4.dp, vertical = 6.dp)
                            .testTag("toggle_pricing_section"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Payments,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = stringResource(R.string.label_rate_and_wood),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Icon(
                            imageVector = if (state.showPricingSection) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    AnimatedVisibility(
                        visible = state.showPricingSection,
                        enter = expandVertically(spring(stiffness = Spring.StiffnessMediumLow)) + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            NumericUnitField(
                                value = state.ratePerCft,
                                onValueChange = onRateChange,
                                unitLabel = "${stringResource(R.string.unit_taka)}/${stringResource(R.string.unit_cft)}",
                                placeholder = TimberCalculatorEngine.toDisplayDigits("1200", bn),
                                imeAction = ImeAction.Done,
                                onDone = { focusManager.clearFocus() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("rate_per_cft_input")
                            )

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                woodPresets.forEach { (woodKey, woodLabel) ->
                                    val selected = state.selectedWoodName == woodLabel
                                    FilterChip(
                                        selected = selected,
                                        onClick = { onSelectWoodName(woodLabel) },
                                        label = {
                                            Text(
                                                text = woodLabel,
                                                style = MaterialTheme.typography.labelMedium
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                        ),
                                        modifier = Modifier.testTag("wood_chip_$woodKey")
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Action Buttons: Add & Next / Update + Reset
            val isEditing = state.editingEntryId != null
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        focusManager.clearFocus()
                        if (isEditing) onCancelEditing() else onReset()
                    },
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("reset_button")
                ) {
                    Icon(
                        imageVector = if (isEditing) Icons.Rounded.Close else Icons.Rounded.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(if (isEditing) R.string.action_cancel else R.string.action_reset),
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                Button(
                    onClick = {
                        focusManager.clearFocus()
                        onAddToTally()
                    },
                    enabled = state.currentResult.isValid,
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isEditing) {
                            MaterialTheme.colorScheme.tertiary
                        } else if (state.woodMode == WoodMode.ROUND_LOG) {
                            MaterialTheme.colorScheme.secondary
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .testTag("add_to_tally_button")
                ) {
                    Icon(
                        imageVector = if (isEditing) Icons.Rounded.CheckCircle else Icons.Rounded.AddCircle,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(if (isEditing) R.string.action_update_entry else R.string.action_add_to_tally),
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Inline Multi-Size Running Chain Card (+) right inside Calculator
            if (showInlineChain && state.tallyEntries.isNotEmpty()) {
                InlineRunningChainCard(
                    state = state,
                    onEditEntry = onEditEntry,
                    onRemoveEntry = onRemoveEntry,
                    onCopySummary = onCopySummary,
                    onShareSummary = onShareSummary,
                    onOpenFullTally = onOpenFullTally
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun WoodModeSwitcher(
    selectedMode: WoodMode,
    onSelectMode: (WoodMode) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            ModeTabButton(
                title = stringResource(R.string.mode_round_log),
                icon = Icons.Rounded.Forest,
                selected = selectedMode == WoodMode.ROUND_LOG,
                activeColor = MaterialTheme.colorScheme.secondary,
                activeContentColor = MaterialTheme.colorScheme.onSecondary,
                onClick = { onSelectMode(WoodMode.ROUND_LOG) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("mode_round_log_button")
            )
            ModeTabButton(
                title = stringResource(R.string.mode_sawn_timber),
                icon = Icons.Rounded.GridView,
                selected = selectedMode == WoodMode.SAWN_TIMBER,
                activeColor = MaterialTheme.colorScheme.primary,
                activeContentColor = MaterialTheme.colorScheme.onPrimary,
                onClick = { onSelectMode(WoodMode.SAWN_TIMBER) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("mode_sawn_timber_button")
            )
        }
    }
}

@Composable
private fun ModeTabButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    activeColor: Color,
    activeContentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        targetValue = if (selected) activeColor else Color.Transparent,
        animationSpec = tween(220),
        label = "mode_tab_bg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) activeContentColor else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(220),
        label = "mode_tab_fg"
    )

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = bgColor,
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .minimumInteractiveComponentSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(19.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun HeroCftResultCard(
    state: TimberUiState
) {
    val bn = state.useBengaliDigits
    val result = state.currentResult

    val startColor by animateColorAsState(
        targetValue = if (state.woodMode == WoodMode.ROUND_LOG) HeroForestStart else HeroTeakStart,
        animationSpec = tween(260),
        label = "hero_grad_start"
    )
    val endColor by animateColorAsState(
        targetValue = if (state.woodMode == WoodMode.ROUND_LOG) HeroForestEnd else HeroTeakEnd,
        animationSpec = tween(260),
        label = "hero_grad_end"
    )

    val animatedCft by animateFloatAsState(
        targetValue = if (result.isValid) result.totalCft.toFloat() else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium,
            visibilityThreshold = 0.0005f
        ),
        label = "animated_cft_value"
    )
    val snapTolerance = maxOf(0.002, result.totalCft * 1e-5)
    val displayCft = when {
        !result.isValid -> 0.0
        abs(animatedCft.toDouble() - result.totalCft) < snapTolerance -> result.totalCft
        else -> animatedCft.toDouble().coerceAtLeast(0.0)
    }
    val formattedHeroCft = TimberCalculatorEngine.formatCft(
        value = displayCft,
        useBengaliDigits = bn
    )
    val heroNumberStyle = when {
        formattedHeroCft.length >= 10 -> MaterialTheme.typography.displayLarge.copy(fontSize = 28.sp, lineHeight = 34.sp)
        formattedHeroCft.length >= 7 -> MaterialTheme.typography.displayLarge.copy(fontSize = 36.sp, lineHeight = 42.sp)
        else -> MaterialTheme.typography.displayLarge
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.extraLarge)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(startColor, endColor)
                )
            )
            .padding(20.dp)
            .testTag("hero_result_card")
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1.3f)) {
                    Surface(
                        shape = CircleShape,
                        color = HeroCreamText.copy(alpha = 0.14f)
                    ) {
                        Text(
                            text = if (state.woodMode == WoodMode.ROUND_LOG) {
                                stringResource(R.string.mode_round_log)
                            } else {
                                stringResource(R.string.mode_sawn_timber)
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = HeroBrassAccent,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = formattedHeroCft,
                            style = heroNumberStyle,
                            color = HeroCreamText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .weight(1f, fill = false)
                                .testTag("result_cft_text")
                        )
                        Text(
                            text = stringResource(R.string.unit_cft),
                            style = MaterialTheme.typography.titleMedium,
                            color = HeroBrassAccent,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }

                    if (state.quantity > 1 && result.isValid) {
                        val perPiece = TimberCalculatorEngine.formatCft(result.perPieceCft, bn)
                        Text(
                            text = "${stringResource(R.string.result_per_piece)}: $perPiece ${stringResource(R.string.unit_cft)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = HeroCreamText.copy(alpha = 0.80f)
                        )
                    }
                }

                // Interactive Blueprint Vector Illustration
                Box(
                    modifier = Modifier
                        .weight(0.9f)
                        .height(92.dp)
                ) {
                    WoodBlueprintCanvas(mode = state.woodMode)
                }
            }

            if (result.isValid) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HeroStatPill(
                        label = stringResource(R.string.unit_cbm),
                        value = "${TimberCalculatorEngine.formatDecimal(result.totalCbm, 3, bn)} m³",
                        modifier = Modifier.weight(1f)
                    )
                    if (result.totalPrice != null) {
                        HeroStatPill(
                            label = stringResource(R.string.result_estimated_price),
                            value = "৳ ${TimberCalculatorEngine.formatDecimal(result.totalPrice, 0, bn)}",
                            modifier = Modifier
                                .weight(1f)
                                .testTag("result_price_pill")
                        )
                    } else if (state.woodMode == WoodMode.ROUND_LOG) {
                        HeroStatPill(
                            label = stringResource(R.string.label_girth),
                            value = "${TimberCalculatorEngine.formatCompactNumber(result.totalGirthInches, bn)}\"",
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        HeroStatPill(
                            label = stringResource(R.string.label_length),
                            value = "${TimberCalculatorEngine.formatDecimal(result.totalLengthFeet, 2, bn)}'",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            } else {
                Text(
                    text = stringResource(R.string.result_enter_measurements),
                    style = MaterialTheme.typography.bodySmall,
                    color = HeroCreamText.copy(alpha = 0.78f)
                )
            }

            // Combined Grand Total Strip whenever 1+ sizes are already in the running calculation
            if (state.tallyEntries.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = HeroBrassAccent.copy(alpha = 0.18f),
                    border = BorderStroke(1.dp, HeroBrassAccent.copy(alpha = 0.45f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("hero_combined_total_strip")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.result_combined_total),
                                style = MaterialTheme.typography.labelMedium,
                                color = HeroBrassAccent,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${TimberCalculatorEngine.formatInt(state.liveCombinedPieces, bn)} ${stringResource(R.string.unit_piece)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = HeroCreamText.copy(alpha = 0.85f)
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = TimberCalculatorEngine.formatCft(state.liveCombinedCft, bn),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = HeroCreamText,
                                modifier = Modifier.testTag("hero_combined_cft_text")
                            )
                            Text(
                                text = stringResource(R.string.unit_cft),
                                style = MaterialTheme.typography.labelMedium,
                                color = HeroBrassAccent,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InlineRunningChainCard(
    state: TimberUiState,
    onEditEntry: (TallyEntry) -> Unit,
    onRemoveEntry: (Long) -> Unit,
    onCopySummary: () -> Unit,
    onShareSummary: () -> Unit,
    onOpenFullTally: () -> Unit
) {
    val bn = state.useBengaliDigits
    val globalRate = state.effectiveGlobalRate
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("inline_running_chain_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${stringResource(R.string.inline_chain_title)} (${TimberCalculatorEngine.formatInt(state.tallyEntries.size, bn)})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(
                    onClick = onOpenFullTally,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.testTag("inline_open_full_tally_button")
                ) {
                    Text(
                        text = stringResource(R.string.inline_chain_view_all),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            // Chronological order (#1 + #2 + #3...) matches Tally Screen and Share/Copy bill
            state.tallyEntries.forEachIndexed { idx, entry ->
                val isRound = entry.mode == WoodMode.ROUND_LOG
                val isBeingEdited = state.editingEntryId == entry.id
                val dimSummary = if (isRound) {
                    val len = TimberCalculatorEngine.formatFeetInchesSummary(entry.lengthFeet, entry.lengthInches, bn)
                    val girth = TimberCalculatorEngine.formatFeetInchesSummary(entry.girthFeet, entry.girthInches, bn)
                    stringResource(R.string.dim_round_format, len, girth)
                } else {
                    val len = TimberCalculatorEngine.formatFeetInchesSummary(entry.lengthFeet, entry.lengthInches, bn)
                    val wid = TimberCalculatorEngine.formatFeetInchesSummary(entry.widthFeet, entry.widthInches, bn)
                    val thk = "${TimberCalculatorEngine.formatCompactNumber(entry.thicknessInches, bn)}\""
                    stringResource(R.string.dim_sawn_format, len, wid, thk)
                }
                val effectivePrice = if (globalRate != null) entry.totalCft * globalRate else entry.totalPrice

                if (idx > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "+",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 1.dp)
                            )
                        }
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isBeingEdited) {
                        MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.55f)
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerLow
                    },
                    border = if (isBeingEdited) {
                        BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary)
                    } else {
                        null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("inline_chain_item_${entry.id}")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 12.dp, end = 6.dp, top = 10.dp, bottom = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Top Row: Index + Mode + Badges on left, CFT on right (zero squishing)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(end = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FlowRow(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "#${TimberCalculatorEngine.formatInt(idx + 1, bn)}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = if (isRound) stringResource(R.string.mode_round_log) else stringResource(R.string.mode_sawn_timber),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isRound) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                                )
                                if (isRound && entry.formula == RoundLogFormula.TRUE_CYLINDER) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.secondaryContainer
                                    ) {
                                        Text(
                                            text = TimberCalculatorEngine.localizeDigits(
                                                stringResource(R.string.badge_cylinder_formula),
                                                bn
                                            ),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
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
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Row(
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Text(
                                    text = TimberCalculatorEngine.formatCft(entry.totalCft, bn),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = stringResource(R.string.unit_cft),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 1.dp)
                                )
                            }
                        }

                        // Bottom Row: Full unclipped dimensions + qty/price on left, 48dp actions on right
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
                                    text = "× ${TimberCalculatorEngine.formatInt(entry.quantity, bn)} ${stringResource(R.string.unit_piece)}" +
                                        (effectivePrice?.let { " • ৳${TimberCalculatorEngine.formatDecimal(it, 0, bn)}" } ?: ""),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { onEditEntry(entry) },
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .testTag("inline_edit_item_${entry.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Edit,
                                        contentDescription = stringResource(R.string.action_edit_item),
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { onRemoveEntry(entry.id) },
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .testTag("inline_delete_item_${entry.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = stringResource(R.string.action_delete_item),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Grand Total Footer + Quick Copy / Share
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${stringResource(R.string.result_total_cft)} (${TimberCalculatorEngine.formatInt(state.totalTallyPieces, bn)} ${stringResource(R.string.unit_piece)})",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${TimberCalculatorEngine.formatCft(state.totalTallyCft, bn)} ${stringResource(R.string.unit_cft)}",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.testTag("inline_chain_total_cft")
                    )
                    state.totalTallyPrice?.let { price ->
                        Text(
                            text = "৳ ${TimberCalculatorEngine.formatDecimal(price, 0, bn)}",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = onCopySummary,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("inline_copy_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ContentCopy,
                            contentDescription = stringResource(R.string.action_copy_tally),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = onShareSummary,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("inline_share_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Share,
                            contentDescription = stringResource(R.string.action_share_tally),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroStatPill(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.Black.copy(alpha = 0.22f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = HeroCreamText.copy(alpha = 0.72f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.Bold
                ),
                color = HeroCreamText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun RoundFormulaSelector(
    selected: RoundLogFormula,
    useBengaliDigits: Boolean,
    onSelect: (RoundLogFormula) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selected == RoundLogFormula.HOPPUS,
                onClick = { onSelect(RoundLogFormula.HOPPUS) },
                label = {
                    Text(
                        text = TimberCalculatorEngine.localizeDigits(
                            stringResource(R.string.formula_hoppus),
                            useBengaliDigits
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("formula_hoppus_chip")
            )
            FilterChip(
                selected = selected == RoundLogFormula.TRUE_CYLINDER,
                onClick = { onSelect(RoundLogFormula.TRUE_CYLINDER) },
                label = {
                    Text(
                        text = TimberCalculatorEngine.localizeDigits(
                            stringResource(R.string.formula_cylinder),
                            useBengaliDigits
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("formula_cylinder_chip")
            )
        }
        Text(
            text = if (selected == RoundLogFormula.HOPPUS) {
                stringResource(R.string.formula_hoppus_hint)
            } else {
                stringResource(R.string.formula_cylinder_hint)
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
