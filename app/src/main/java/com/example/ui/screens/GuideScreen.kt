package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.TimberCalculatorEngine
import com.example.ui.theme.JetBrainsMonoFontFamily

@Composable
fun GuideScreen(
    useBengaliDigits: Boolean,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    val sutaRows = listOf(
        "1 সুতা" to "0.125 ইঞ্চি",
        "2 সুতা" to "0.25 ইঞ্চি (¼\")",
        "3 সুতা" to "0.375 ইঞ্চি",
        "4 সুতা" to "0.50 ইঞ্চি (½\")",
        "5 সুতা" to "0.625 ইঞ্চি",
        "6 সুতা" to "0.75 ইঞ্চি (¾\")",
        "7 সুতা" to "0.875 ইঞ্চি",
        "8 সুতা" to "1.00 ইঞ্চি (1\")"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
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
            Text(
                text = stringResource(R.string.guide_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            // 1. Round Log Formula Card
            FormulaInfoCard(
                title = TimberCalculatorEngine.localizeDigits(stringResource(R.string.guide_round_title), useBengaliDigits),
                formulaMain = TimberCalculatorEngine.localizeDigits(stringResource(R.string.guide_round_formula), useBengaliDigits),
                formulaAlt = TimberCalculatorEngine.localizeDigits(stringResource(R.string.guide_round_alt), useBengaliDigits),
                description = TimberCalculatorEngine.localizeDigits(stringResource(R.string.guide_round_desc), useBengaliDigits),
                example = TimberCalculatorEngine.localizeDigits(stringResource(R.string.guide_round_example), useBengaliDigits),
                accentContainer = MaterialTheme.colorScheme.secondaryContainer,
                accentOnContainer = MaterialTheme.colorScheme.onSecondaryContainer
            )

            // 2. Sawn Timber Formula Card
            FormulaInfoCard(
                title = TimberCalculatorEngine.localizeDigits(stringResource(R.string.guide_sawn_title), useBengaliDigits),
                formulaMain = TimberCalculatorEngine.localizeDigits(stringResource(R.string.guide_sawn_formula), useBengaliDigits),
                formulaAlt = TimberCalculatorEngine.localizeDigits(stringResource(R.string.guide_sawn_alt), useBengaliDigits),
                description = TimberCalculatorEngine.localizeDigits(stringResource(R.string.guide_sawn_desc), useBengaliDigits),
                example = TimberCalculatorEngine.localizeDigits(stringResource(R.string.guide_sawn_example), useBengaliDigits),
                accentContainer = MaterialTheme.colorScheme.primaryContainer,
                accentOnContainer = MaterialTheme.colorScheme.onPrimaryContainer
            )

            // 3. Suta to Inch Chart
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = TimberCalculatorEngine.localizeDigits(stringResource(R.string.guide_suta_title), useBengaliDigits),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = TimberCalculatorEngine.localizeDigits(stringResource(R.string.guide_suta_desc), useBengaliDigits),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            sutaRows.forEachIndexed { idx, (suta, inch) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = TimberCalculatorEngine.localizeDigits(suta, useBengaliDigits),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = TimberCalculatorEngine.localizeDigits(inch, useBengaliDigits),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontWeight = FontWeight.Medium
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                if (idx < sutaRows.lastIndex) {
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                }
                            }
                        }
                    }
                }
            }

            // 4. Unit Conversions
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = TimberCalculatorEngine.localizeDigits(stringResource(R.string.guide_unit_title), useBengaliDigits),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "• ${TimberCalculatorEngine.localizeDigits(stringResource(R.string.guide_unit_ft_in), useBengaliDigits)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "• ${TimberCalculatorEngine.localizeDigits(stringResource(R.string.guide_unit_cft_cbm), useBengaliDigits)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "• ${TimberCalculatorEngine.localizeDigits(stringResource(R.string.guide_unit_cbm_cft), useBengaliDigits)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun FormulaInfoCard(
    title: String,
    formulaMain: String,
    formulaAlt: String,
    description: String,
    example: String,
    accentContainer: androidx.compose.ui.graphics.Color,
    accentOnContainer: androidx.compose.ui.graphics.Color
) {
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = accentContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = formulaMain,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = accentOnContainer
                    )
                    Text(
                        text = formulaAlt,
                        style = MaterialTheme.typography.bodySmall,
                        color = accentOnContainer.copy(alpha = 0.85f)
                    )
                }
            }

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = example,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }
    }
}
