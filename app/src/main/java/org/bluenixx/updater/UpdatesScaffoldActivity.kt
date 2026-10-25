/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.bluenixx.updater

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.IconButton
import org.bluenixx.updater.controller.UpdaterController
import org.bluenixx.updater.data.Update
import org.bluenixx.updater.data.UpdateStatus
import org.bluenixx.updater.deviceinfo.DeviceInfoBanner
import org.bluenixx.updater.preferences.PreferencesActivity
import org.bluenixx.updater.ui.BluenixxTheme
import org.bluenixx.updater.updates.UpdateList
import org.bluenixx.updater.updates.action.AlertDialogState
import org.bluenixx.updater.updates.action.UpdateActionDialog
import org.bluenixx.updater.updates.action.UpdateActionHandler
import org.bluenixx.updater.updates.state.UpdateItemStateMapper
import org.bluenixx.updater.updatescheck.UpdatesCheck
import org.bluenixx.updater.updatescheck.UpdatesCheckModel
import org.bluenixx.updater.updatescheck.rememberUpdatesCheckUiState

abstract class UpdatesScaffoldActivity : ComponentActivity() {
    private val viewModel by viewModels<UpdatesViewModel>()
    private var activeUpdaterController: UpdaterController? by mutableStateOf(null)
    private var controllerStateVersion: Int by mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
    }

    protected fun setupCompose() {
        setContent {
            BluenixxTheme {
                val uiState by viewModel.uiState.collectAsState()
                UpdatesScaffoldContent(
                    uiState = uiState,
                    updaterController = activeUpdaterController,
                    controllerStateVersion = controllerStateVersion,
                    onRefreshClick = { onRefreshClick() },
                    onLocalUpdateClick = { onLocalUpdateClick() },
                    onPreferencesClick = {
                        startActivity(Intent(this@UpdatesScaffoldActivity, PreferencesActivity::class.java))
                    },
                    onControllerStateChanged = { notifyControllerStateChanged() },
                )
            }
        }
    }

    protected fun setUpdaterController(controller: UpdaterController?) {
        activeUpdaterController = controller
        notifyControllerStateChanged()
    }

    protected fun notifyControllerStateChanged() {
        controllerStateVersion++
    }

    open fun onRefreshClick() {}
    open fun onLocalUpdateClick() {}
    open fun exportUpdate(update: Update) {}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UpdatesScaffoldContent(
    uiState: UpdatesViewModel.UiState,
    updaterController: UpdaterController?,
    controllerStateVersion: Int,
    onRefreshClick: () -> Unit,
    onLocalUpdateClick: () -> Unit,
    onPreferencesClick: () -> Unit,
    onControllerStateChanged: () -> Unit,
) {
    val actionDialogState = remember { mutableStateOf<AlertDialogState?>(null) }
    actionDialogState.value?.let { dialog ->
        UpdateActionDialog(dialog = dialog, onDismiss = { actionDialogState.value = null })
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(
                            modifier = Modifier
                                .size(40.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(15.dp)),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CloudDownload,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(22.dp),
                            )
                        }
                        Spacer(Modifier.size(12.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                            Text(
                                text = stringResource(R.string.brand_name),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = stringResource(R.string.updater_subtitle),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 1.1.sp,
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onPreferencesClick) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = stringResource(R.string.menu_preferences),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { paddingValues ->
        if (isWideScreen()) {
            WideUpdatesScaffold(
                paddingValues = paddingValues,
                updatesCheckModel = uiState.updatesCheckModel,
                updates = uiState.updates,
                updaterController = updaterController,
                controllerStateVersion = controllerStateVersion,
                showDialog = { actionDialogState.value = it },
                onControllerStateChanged = onControllerStateChanged,
                onRefreshClick = onRefreshClick,
                onLocalUpdateClick = onLocalUpdateClick,
                onPreferencesClick = onPreferencesClick,
            )
        } else {
            NarrowUpdatesScaffold(
                paddingValues = paddingValues,
                updatesCheckModel = uiState.updatesCheckModel,
                updates = uiState.updates,
                updaterController = updaterController,
                controllerStateVersion = controllerStateVersion,
                showDialog = { actionDialogState.value = it },
                onControllerStateChanged = onControllerStateChanged,
                onRefreshClick = onRefreshClick,
                onLocalUpdateClick = onLocalUpdateClick,
                onPreferencesClick = onPreferencesClick,
            )
        }
    }
}

@Composable
private fun WideUpdatesScaffold(
    paddingValues: PaddingValues,
    updatesCheckModel: UpdatesCheckModel,
    updates: List<Update>,
    updaterController: UpdaterController?,
    controllerStateVersion: Int,
    showDialog: (AlertDialogState) -> Unit,
    onControllerStateChanged: () -> Unit,
    onRefreshClick: () -> Unit,
    onLocalUpdateClick: () -> Unit,
    onPreferencesClick: () -> Unit,
) {
    val layoutDirection = LocalLayoutDirection.current
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                top = paddingValues.calculateTopPadding(),
                start = paddingValues.calculateStartPadding(layoutDirection) + 12.dp,
                end = paddingValues.calculateEndPadding(layoutDirection) + 12.dp,
                bottom = paddingValues.calculateBottomPadding(),
            ),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState()),
        ) {
            UpdatesInformationPane()
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState()),
        ) {
            UpdatesActionPane(
                model = updatesCheckModel,
                updates = updates,
                updaterController = updaterController,
                controllerStateVersion = controllerStateVersion,
                showDialog = showDialog,
                onControllerStateChanged = onControllerStateChanged,
                onRefreshClick = onRefreshClick,
                onLocalUpdateClick = onLocalUpdateClick,
                onPreferencesClick = onPreferencesClick,
            )
        }
    }
}

@Composable
private fun NarrowUpdatesScaffold(
    paddingValues: PaddingValues,
    updatesCheckModel: UpdatesCheckModel,
    updates: List<Update>,
    updaterController: UpdaterController?,
    controllerStateVersion: Int,
    showDialog: (AlertDialogState) -> Unit,
    onControllerStateChanged: () -> Unit,
    onRefreshClick: () -> Unit,
    onLocalUpdateClick: () -> Unit,
    onPreferencesClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .verticalScroll(rememberScrollState()),
    ) {
        UpdatesInformationPane()
        UpdatesActionPane(
            model = updatesCheckModel,
            updates = updates,
            updaterController = updaterController,
            controllerStateVersion = controllerStateVersion,
            showDialog = showDialog,
            onControllerStateChanged = onControllerStateChanged,
            onRefreshClick = onRefreshClick,
            onLocalUpdateClick = onLocalUpdateClick,
            onPreferencesClick = onPreferencesClick,
        )
    }
}

@Composable
private fun UpdatesInformationPane(modifier: Modifier = Modifier) {
    DeviceInfoBanner(modifier = modifier)
}

@Composable
private fun UpdatesActionPane(
    model: UpdatesCheckModel,
    updates: List<Update>,
    updaterController: UpdaterController?,
    controllerStateVersion: Int,
    showDialog: (AlertDialogState) -> Unit,
    onControllerStateChanged: () -> Unit,
    onRefreshClick: () -> Unit,
    onLocalUpdateClick: () -> Unit,
    onPreferencesClick: () -> Unit,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val activity = context as UpdatesScaffoldActivity
    val networkMonitor = remember { (context.applicationContext as UpdaterApplication).networkMonitor }
    val networkState by networkMonitor.networkState.collectAsState(initial = networkMonitor.currentNetworkState)
    val userPreferencesRepository = remember {
        (context.applicationContext as UpdaterApplication).userPreferencesRepository
    }
    val streamUpdatesEnabled by userPreferencesRepository.streamUpdatesFlow.collectAsState(initial = true)
    val updateItems = remember(
        updates,
        updaterController,
        networkState,
        streamUpdatesEnabled,
        controllerStateVersion,
    ) {
        val controller = updaterController ?: return@remember emptyList()
        val mapper = UpdateItemStateMapper(context, controller, streamUpdatesEnabled)
        updates.mapNotNull { update ->
            controller.getUpdate(update.downloadId)?.let { mapper.map(it, networkState) }
        }
    }
    val actionHandler = remember(updaterController) {
        updaterController?.let { controller ->
            UpdateActionHandler(
                activity = activity,
                updaterController = controller,
                exportUpdate = { update -> activity.exportUpdate(update) },
                showDialog = showDialog,
            )
        }
    }
    val updatesCheckUiState = rememberUpdatesCheckUiState(model.state)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        UpdatesCheck(
            model = model,
            uiState = updatesCheckUiState,
            onCheckClick = onRefreshClick,
        )
        if (updateItems.isNotEmpty()) {
            Text(
                text = stringResource(R.string.updater_updates_section),
                modifier = Modifier.padding(start = 4.dp, top = 2.dp),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
        }
        UpdateList(
            items = updateItems,
            isUpdatesCheckStatusVisible = updatesCheckUiState.isStatusVisible,
            onAction = { action, downloadId ->
                val controller = updaterController ?: return@UpdateList
                val update = controller.getUpdate(downloadId) ?: return@UpdateList
                actionHandler?.perform(action, update)
                onControllerStateChanged()
            },
        )
        UpdatesFooter(
            onLocalUpdateClick = onLocalUpdateClick,
            onPreferencesClick = onPreferencesClick,
        )
    }
}

@Composable
private fun UpdatesFooter(
    onLocalUpdateClick: () -> Unit,
    onPreferencesClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 22.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        FilledTonalButton(
            onClick = onLocalUpdateClick,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(18.dp),
        ) {
            Icon(Icons.Outlined.FolderOpen, contentDescription = null)
            Spacer(Modifier.size(7.dp))
            Text(stringResource(R.string.updater_import_action), maxLines = 1)
        }
        FilledTonalButton(
            onClick = onPreferencesClick,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(18.dp),
        ) {
            Icon(Icons.Outlined.Settings, contentDescription = null)
            Spacer(Modifier.size(7.dp))
            Text(stringResource(R.string.updater_settings_action), maxLines = 1)
        }
    }
}

@Composable
private fun isWideScreen(): Boolean {
    val minWideScreenWidth = 600.dp
    val density = LocalDensity.current
    val windowSize = LocalWindowInfo.current.containerSize
    return with(density) { windowSize.width.toDp() >= minWideScreenWidth }
}
