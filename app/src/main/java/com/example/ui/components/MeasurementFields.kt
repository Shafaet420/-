package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.TimberCalculatorEngine
import com.example.ui.theme.JetBrainsMonoFontFamily
import kotlin.math.abs

@Composable
fun FeetInchInputRow(
    label: String,
    feetValue: String,
    onFeetChange: (String) -> Unit,
    inchValue: String,
    onInchChange: (String) -> Unit,
    useBengaliDigits: Boolean,
    feetTestTag: String,
    inchTestTag: String,
    feetFocusRequester: FocusRequester? = null,
    inchFocusRequester: FocusRequester? = null,
    nextFocusRequester: FocusRequester? = null,
    lastImeAction: ImeAction = ImeAction.Next,
    onDone: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            NumericUnitField(
                value = feetValue,
                onValueChange = onFeetChange,
                unitLabel = stringResource(R.string.unit_feet),
                placeholder = TimberCalculatorEngine.toDisplayDigits("0", useBengaliDigits),
                imeAction = ImeAction.Next,
                onNext = { runCatching { inchFocusRequester?.requestFocus() } },
                onDone = {},
                modifier = Modifier
                    .weight(1f)
                    .then(if (feetFocusRequester != null) Modifier.focusRequester(feetFocusRequester) else Modifier)
                    .testTag(feetTestTag)
            )
            NumericUnitField(
                value = inchValue,
                onValueChange = onInchChange,
                unitLabel = stringResource(R.string.unit_inch),
                placeholder = TimberCalculatorEngine.toDisplayDigits("0", useBengaliDigits),
                imeAction = lastImeAction,
                onNext = { runCatching { nextFocusRequester?.requestFocus() } },
                onDone = onDone,
                modifier = Modifier
                    .weight(1f)
                    .then(if (inchFocusRequester != null) Modifier.focusRequester(inchFocusRequester) else Modifier)
                    .testTag(inchTestTag)
            )
        }
    }
}

@Composable
fun NumericUnitField(
    value: String,
    onValueChange: (String) -> Unit,
    unitLabel: String,
    placeholder: String,
    imeAction: ImeAction,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
    onNext: (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = MaterialTheme.typography.titleMedium.copy(
            fontFamily = JetBrainsMonoFontFamily,
            fontWeight = FontWeight.SemiBold
        ),
        placeholder = {
            Text(
                text = placeholder,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = JetBrainsMonoFontFamily
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        suffix = {
            Text(
                text = unitLabel,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal,
            imeAction = imeAction
        ),
        keyboardActions = KeyboardActions(
            onNext = if (onNext != null) {
                { onNext() }
            } else {
                null
            },
            onDone = { onDone() }
        ),
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        modifier = modifier
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuickPresetChipRow(
    presets: List<Pair<String, String>>,
    selectedValue: String,
    useBengaliDigits: Boolean,
    onSelect: (String) -> Unit,
    tagPrefix: String,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        val selectedNum = TimberCalculatorEngine.parsePositiveDouble(selectedValue)
        presets.forEach { (displayRaw, numericVal) ->
            val presetNum = TimberCalculatorEngine.parsePositiveDouble(numericVal)
            val isSelected = selectedValue.isNotEmpty() &&
                selectedNum > 0.0 &&
                abs(selectedNum - presetNum) < 1e-6
            val label = TimberCalculatorEngine.toDisplayDigits(displayRaw, useBengaliDigits)
            FilterChip(
                selected = isSelected,
                onClick = { onSelect(numericVal) },
                label = {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = MaterialTheme.colorScheme.outlineVariant
                ),
                modifier = Modifier.testTag("${tagPrefix}_${numericVal.replace('.', '_')}")
            )
        }
    }
}

@Composable
fun QuantityStepperRow(
    quantity: Int,
    quantityInput: String,
    useBengaliDigits: Boolean,
    onQuantityInputChange: (String) -> Unit,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    focusRequester: FocusRequester? = null,
    onDone: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.label_quantity),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${TimberCalculatorEngine.formatInt(quantity, useBengaliDigits)} ${stringResource(R.string.unit_piece)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = onDecrement,
                    enabled = quantity > 1,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("quantity_decrease_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Remove,
                        contentDescription = stringResource(R.string.action_decrease_qty)
                    )
                }

                OutlinedTextField(
                    value = quantityInput,
                    onValueChange = onQuantityInputChange,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    ),
                    placeholder = {
                        Text(
                            text = TimberCalculatorEngine.toDisplayDigits("1", useBengaliDigits),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = JetBrainsMonoFontFamily,
                                textAlign = TextAlign.Center
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { onDone() }
                    ),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .widthIn(min = 92.dp, max = 104.dp)
                        .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
                        .testTag("quantity_number_input")
                )

                IconButton(
                    onClick = onIncrement,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("quantity_increase_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = stringResource(R.string.action_increase_qty)
                    )
                }
            }
        }
    }
}
