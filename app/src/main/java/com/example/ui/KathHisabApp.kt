package com.example.ui

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.Forest
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.model.AppTab
import com.example.model.TallySaveResult
import com.example.model.TimberCalculatorEngine
import com.example.ui.screens.CalculatorScreen
import com.example.ui.screens.GuideScreen
import com.example.ui.screens.TallyScreen
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.viewmodel.TimberViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KathHisabApp(
    providedViewModel: TimberViewModel? = null
) {
    val context = LocalContext.current
    val viewModel: TimberViewModel = providedViewModel ?: viewModel(
        factory = remember(context.applicationContext) {
            TimberViewModel.factory(context.applicationContext)
        }
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current

    val addedMessage = stringResource(R.string.tally_added_message)
    val updatedMessage = stringResource(R.string.tally_updated_message)
    val copiedMessage = stringResource(R.string.tally_copied_message)
    val deletedMessage = stringResource(R.string.tally_deleted_message)
    val undoLabel = stringResource(R.string.action_undo)
    val shareChooserTitle = stringResource(R.string.action_share_tally)
    val shareFallbackMessage = stringResource(R.string.tally_share_fallback_message)
    val toggleDigitsDesc = stringResource(R.string.action_toggle_digits)

    val handleAddToTally: () -> Unit = {
        when (viewModel.addCurrentToTally()) {
            TallySaveResult.ADDED -> {
                coroutineScope.launch {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    snackbarHostState.showSnackbar(addedMessage)
                }
            }
            TallySaveResult.UPDATED -> {
                coroutineScope.launch {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    snackbarHostState.showSnackbar(updatedMessage)
                }
            }
            TallySaveResult.NONE -> Unit
        }
    }

    val handleRemoveEntry: (Long) -> Unit = { entryId ->
        val removed = viewModel.removeTallyEntry(entryId)
        if (removed != null) {
            coroutineScope.launch {
                snackbarHostState.currentSnackbarData?.dismiss()
                val res = snackbarHostState.showSnackbar(
                    message = deletedMessage,
                    actionLabel = undoLabel,
                    duration = SnackbarDuration.Short
                )
                if (res == SnackbarResult.ActionPerformed) {
                    viewModel.restoreTallyEntry(removed)
                }
            }
        }
    }

    val handleCopySummary: () -> Unit = {
        if (state.tallyEntries.isNotEmpty()) {
            val text = viewModel.buildShareableText()
            clipboardManager.setText(AnnotatedString(text))
            coroutineScope.launch {
                snackbarHostState.currentSnackbarData?.dismiss()
                snackbarHostState.showSnackbar(copiedMessage)
            }
        }
    }

    val handleShareSummary: () -> Unit = {
        if (state.tallyEntries.isNotEmpty()) {
            val text = viewModel.buildShareableText()
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
            }
            val started = runCatching {
                context.startActivity(Intent.createChooser(intent, shareChooserTitle))
            }.isSuccess
            if (!started) {
                clipboardManager.setText(AnnotatedString(text))
                coroutineScope.launch {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    snackbarHostState.showSnackbar(shareFallbackMessage)
                }
            }
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 680.dp
        val isTwoPane = maxWidth >= 880.dp

        // Back press exits edit mode first, or returns from secondary tabs cleanly without swallowing back in two-pane mode
        val canReturnToCalculator = state.activeTab == AppTab.GUIDE || (!isTwoPane && state.activeTab == AppTab.TALLY)
        BackHandler(enabled = canReturnToCalculator || state.editingEntryId != null) {
            if (canReturnToCalculator) {
                viewModel.selectTab(AppTab.CALCULATOR)
            } else if (state.editingEntryId != null) {
                viewModel.cancelEditing()
            }
        }

        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                CenterAlignedTopAppBar(
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    ),
                    navigationIcon = {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier
                                .padding(start = 12.dp)
                                .size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.Forest,
                                    contentDescription = stringResource(R.string.app_name),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    },
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = stringResource(R.string.app_name),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = stringResource(R.string.app_subtitle),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    actions = {
                        Surface(
                            onClick = { viewModel.toggleBengaliDigits() },
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .minimumInteractiveComponentSize()
                                .semantics { contentDescription = toggleDigitsDesc }
                                .testTag("toggle_digits_button")
                        ) {
                            Text(
                                text = if (state.useBengaliDigits) "১২৩" else "123",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                )
            },
            bottomBar = {
                if (!isWideScreen) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 2.dp,
                        windowInsets = WindowInsets.navigationBars
                    ) {
                        NavigationBarItem(
                            selected = state.activeTab == AppTab.CALCULATOR,
                            onClick = { viewModel.selectTab(AppTab.CALCULATOR) },
                            icon = {
                                Icon(
                                    imageVector = if (state.activeTab == AppTab.CALCULATOR) {
                                        Icons.Rounded.Calculate
                                    } else {
                                        Icons.Outlined.Calculate
                                    },
                                    contentDescription = stringResource(R.string.nav_calculator)
                                )
                            },
                            label = { Text(stringResource(R.string.nav_calculator)) },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                selectedTextColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.testTag("nav_calculator_tab")
                        )

                        NavigationBarItem(
                            selected = state.activeTab == AppTab.TALLY,
                            onClick = { viewModel.selectTab(AppTab.TALLY) },
                            icon = {
                                BadgedBox(
                                    badge = {
                                        if (state.tallyEntries.isNotEmpty()) {
                                            Badge(
                                                containerColor = MaterialTheme.colorScheme.secondary,
                                                contentColor = MaterialTheme.colorScheme.onSecondary
                                            ) {
                                                Text(
                                                    text = TimberCalculatorEngine.formatInt(
                                                        state.tallyEntries.size,
                                                        state.useBengaliDigits
                                                    )
                                                )
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (state.activeTab == AppTab.TALLY) {
                                            Icons.Rounded.ReceiptLong
                                        } else {
                                            Icons.Outlined.ReceiptLong
                                        },
                                        contentDescription = stringResource(R.string.nav_tally)
                                    )
                                }
                            },
                            label = { Text(stringResource(R.string.nav_tally)) },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                selectedTextColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.testTag("nav_tally_tab")
                        )

                        NavigationBarItem(
                            selected = state.activeTab == AppTab.GUIDE,
                            onClick = { viewModel.selectTab(AppTab.GUIDE) },
                            icon = {
                                Icon(
                                    imageVector = if (state.activeTab == AppTab.GUIDE) {
                                        Icons.Rounded.MenuBook
                                    } else {
                                        Icons.Outlined.MenuBook
                                    },
                                    contentDescription = stringResource(R.string.nav_guide)
                                )
                            },
                            label = { Text(stringResource(R.string.nav_guide)) },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                selectedTextColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.testTag("nav_guide_tab")
                        )
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isWideScreen) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        val isCalcSelected = state.activeTab == AppTab.CALCULATOR ||
                            (isTwoPane && state.activeTab == AppTab.TALLY)
                        NavigationRailItem(
                            selected = isCalcSelected,
                            onClick = { viewModel.selectTab(AppTab.CALCULATOR) },
                            icon = {
                                Icon(
                                    imageVector = if (isCalcSelected) {
                                        Icons.Rounded.Calculate
                                    } else {
                                        Icons.Outlined.Calculate
                                    },
                                    contentDescription = stringResource(R.string.nav_calculator)
                                )
                            },
                            label = { Text(stringResource(R.string.nav_calculator)) },
                            colors = NavigationRailItemDefaults.colors(
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.testTag("rail_calculator_tab")
                        )
                        if (!isTwoPane) {
                            NavigationRailItem(
                                selected = state.activeTab == AppTab.TALLY,
                                onClick = { viewModel.selectTab(AppTab.TALLY) },
                                icon = {
                                    BadgedBox(
                                        badge = {
                                            if (state.tallyEntries.isNotEmpty()) {
                                                Badge {
                                                    Text(
                                                        TimberCalculatorEngine.formatInt(
                                                            state.tallyEntries.size,
                                                            state.useBengaliDigits
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = if (state.activeTab == AppTab.TALLY) {
                                                Icons.Rounded.ReceiptLong
                                            } else {
                                                Icons.Outlined.ReceiptLong
                                            },
                                            contentDescription = stringResource(R.string.nav_tally)
                                        )
                                    }
                                },
                                label = { Text(stringResource(R.string.nav_tally)) },
                                colors = NavigationRailItemDefaults.colors(
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                modifier = Modifier.testTag("rail_tally_tab")
                            )
                        }
                        NavigationRailItem(
                            selected = state.activeTab == AppTab.GUIDE,
                            onClick = { viewModel.selectTab(AppTab.GUIDE) },
                            icon = {
                                Icon(
                                    imageVector = if (state.activeTab == AppTab.GUIDE) {
                                        Icons.Rounded.MenuBook
                                    } else {
                                        Icons.Outlined.MenuBook
                                    },
                                    contentDescription = stringResource(R.string.nav_guide)
                                )
                            },
                            label = { Text(stringResource(R.string.nav_guide)) },
                            colors = NavigationRailItemDefaults.colors(
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.testTag("rail_guide_tab")
                        )
                    }
                    VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }

                if (isTwoPane && state.activeTab != AppTab.GUIDE) {
                    // Canonical Two-Pane Supporting Layout on Expanded Tablets
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        CalculatorScreen(
                            state = state,
                            onSelectMode = viewModel::selectWoodMode,
                            onSelectRoundFormula = viewModel::selectRoundFormula,
                            onRoundLengthFeetChange = viewModel::updateRoundLengthFeet,
                            onApplyRoundLengthPreset = viewModel::applyRoundLengthPreset,
                            onRoundLengthInchesChange = viewModel::updateRoundLengthInches,
                            onRoundGirthFeetChange = viewModel::updateRoundGirthFeet,
                            onRoundGirthInchesChange = viewModel::updateRoundGirthInches,
                            onSawnLengthFeetChange = viewModel::updateSawnLengthFeet,
                            onSawnLengthInchesChange = viewModel::updateSawnLengthInches,
                            onSawnWidthFeetChange = viewModel::updateSawnWidthFeet,
                            onSawnWidthInchesChange = viewModel::updateSawnWidthInches,
                            onSawnThicknessInchesChange = viewModel::updateSawnThicknessInches,
                            onQuantityInputChange = viewModel::updateQuantityInput,
                            onIncrementQty = viewModel::incrementQuantity,
                            onDecrementQty = viewModel::decrementQuantity,
                            onTogglePricing = viewModel::togglePricingSection,
                            onRateChange = viewModel::updateRatePerCft,
                            onSelectWoodName = viewModel::selectWoodName,
                            onAddToTally = handleAddToTally,
                            onCancelEditing = viewModel::cancelEditing,
                            onReset = viewModel::resetCurrentInputs,
                            onEditEntry = viewModel::startEditingEntry,
                            onRemoveEntry = handleRemoveEntry,
                            onCopySummary = handleCopySummary,
                            onShareSummary = handleShareSummary,
                            onOpenFullTally = { viewModel.selectTab(AppTab.TALLY) },
                            showInlineChain = false,
                            modifier = Modifier.weight(1.1f)
                        )
                        VerticalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant,
                            modifier = Modifier.width(1.dp)
                        )
                        TallyScreen(
                            state = state,
                            onEditEntry = viewModel::startEditingEntry,
                            onRemoveEntry = handleRemoveEntry,
                            onClearAll = viewModel::clearAllTally,
                            onGlobalRateChange = viewModel::updateGlobalRate,
                            onCopySummary = handleCopySummary,
                            onShareSummary = handleShareSummary,
                            onNavigateToCalculator = { viewModel.selectTab(AppTab.CALCULATOR) },
                            showGoToCalculatorButton = false,
                            modifier = Modifier.weight(0.9f)
                        )
                    }
                } else {
                    when (state.activeTab) {
                        AppTab.CALCULATOR -> CalculatorScreen(
                            state = state,
                            onSelectMode = viewModel::selectWoodMode,
                            onSelectRoundFormula = viewModel::selectRoundFormula,
                            onRoundLengthFeetChange = viewModel::updateRoundLengthFeet,
                            onApplyRoundLengthPreset = viewModel::applyRoundLengthPreset,
                            onRoundLengthInchesChange = viewModel::updateRoundLengthInches,
                            onRoundGirthFeetChange = viewModel::updateRoundGirthFeet,
                            onRoundGirthInchesChange = viewModel::updateRoundGirthInches,
                            onSawnLengthFeetChange = viewModel::updateSawnLengthFeet,
                            onSawnLengthInchesChange = viewModel::updateSawnLengthInches,
                            onSawnWidthFeetChange = viewModel::updateSawnWidthFeet,
                            onSawnWidthInchesChange = viewModel::updateSawnWidthInches,
                            onSawnThicknessInchesChange = viewModel::updateSawnThicknessInches,
                            onQuantityInputChange = viewModel::updateQuantityInput,
                            onIncrementQty = viewModel::incrementQuantity,
                            onDecrementQty = viewModel::decrementQuantity,
                            onTogglePricing = viewModel::togglePricingSection,
                            onRateChange = viewModel::updateRatePerCft,
                            onSelectWoodName = viewModel::selectWoodName,
                            onAddToTally = handleAddToTally,
                            onCancelEditing = viewModel::cancelEditing,
                            onReset = viewModel::resetCurrentInputs,
                            onEditEntry = viewModel::startEditingEntry,
                            onRemoveEntry = handleRemoveEntry,
                            onCopySummary = handleCopySummary,
                            onShareSummary = handleShareSummary,
                            onOpenFullTally = { viewModel.selectTab(AppTab.TALLY) },
                            showInlineChain = true,
                            modifier = Modifier.fillMaxSize()
                        )

                        AppTab.TALLY -> TallyScreen(
                            state = state,
                            onEditEntry = viewModel::startEditingEntry,
                            onRemoveEntry = handleRemoveEntry,
                            onClearAll = viewModel::clearAllTally,
                            onGlobalRateChange = viewModel::updateGlobalRate,
                            onCopySummary = handleCopySummary,
                            onShareSummary = handleShareSummary,
                            onNavigateToCalculator = { viewModel.selectTab(AppTab.CALCULATOR) },
                            modifier = Modifier.fillMaxSize()
                        )

                        AppTab.GUIDE -> GuideScreen(
                            useBengaliDigits = state.useBengaliDigits,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}
