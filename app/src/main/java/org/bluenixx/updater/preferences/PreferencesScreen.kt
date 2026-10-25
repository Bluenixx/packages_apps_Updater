/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.bluenixx.updater.preferences

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.SystemUpdate
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.bluenixx.updater.R
import org.bluenixx.updater.UpdaterApplication
import org.bluenixx.updater.data.CheckInterval
import org.bluenixx.updater.deviceinfo.DeviceInfoUtils
import org.bluenixx.updater.util.BatteryMonitor
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferencesScreen(onBack: () -> Unit = {}) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val application = remember(context) { context.applicationContext as UpdaterApplication }
    val repository = application.userPreferencesRepository
    val batteryMonitor = application.batteryMonitor
    val isABDevice = remember { DeviceInfoUtils.isABDevice }
    val showRecoveryUpdate = remember { installRecoveryScriptExists() }
    val scope = rememberCoroutineScope()

    val abPerfMode by repository.abPerfModeFlow.collectAsStateWithLifecycle(false)
    val batteryState by batteryMonitor.batteryState.collectAsStateWithLifecycle(batteryMonitor.currentBatteryState)
    val autoDelete by repository.autoDeleteFlow.collectAsStateWithLifecycle(true)
    val streamUpdates by repository.streamUpdatesFlow.collectAsStateWithLifecycle(true)
    val checkInterval by repository.checkIntervalFlow.collectAsStateWithLifecycle(CheckInterval.default)
    val meteredNetworkWarning by repository.meteredNetworkWarningFlow.collectAsStateWithLifecycle(true)
    val periodicCheckEnabled by repository.periodicCheckEnabledFlow.collectAsStateWithLifecycle(true)
    var recoveryUpdateEnabled by remember { mutableStateOf(repository.getRecoveryUpdateEnabled()) }
    var showIntervalDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                        Text(stringResource(R.string.updater_preferences_title), style = MaterialTheme.typography.titleLarge)
                        Text(
                            stringResource(R.string.updater_subtitle),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.0.sp,
                        )
                    }
                },
                navigationIcon = {
                    androidx.compose.material3.IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = stringResource(R.string.updater_back))
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Text(
                text = stringResource(R.string.updater_preferences_intro),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp),
            )

            PreferenceSection(
                title = stringResource(R.string.updater_preferences_sync),
                icon = { Icon(Icons.Outlined.Schedule, contentDescription = null) },
            ) {
                PreferenceSwitch(
                    title = stringResource(R.string.menu_auto_updates_check),
                    summary = stringResource(R.string.menu_auto_updates_check_summary),
                    checked = periodicCheckEnabled,
                    onCheckedChange = { value -> scope.launch { repository.setPeriodicCheckEnabled(value) } },
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f))
                ListItem(
                    modifier = Modifier.clickable(enabled = periodicCheckEnabled) { showIntervalDialog = true },
                    headlineContent = { Text(stringResource(R.string.updater_interval_title)) },
                    supportingContent = {
                        Text(
                            when (checkInterval) {
                                CheckInterval.DAILY -> stringResource(R.string.updater_interval_daily)
                                CheckInterval.WEEKLY -> stringResource(R.string.updater_interval_weekly)
                                CheckInterval.MONTHLY -> stringResource(R.string.updater_interval_monthly)
                            },
                        )
                    },
                    trailingContent = { Icon(Icons.Outlined.Tune, contentDescription = null) },
                    colors = androidx.compose.material3.ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                )
            }

            PreferenceSection(
                title = stringResource(R.string.updater_preferences_delivery),
                icon = { Icon(Icons.Outlined.SystemUpdate, contentDescription = null) },
            ) {
                if (isABDevice) {
                    PreferenceSwitch(
                        title = stringResource(R.string.menu_stream_updates),
                        summary = stringResource(R.string.menu_stream_updates_summary),
                        checked = streamUpdates,
                        onCheckedChange = { value -> scope.launch { repository.setStreamUpdates(value) } },
                    )
                } else {
                    PreferenceSwitch(
                        title = stringResource(R.string.menu_auto_delete_updates),
                        summary = stringResource(R.string.menu_auto_delete_updates_summary),
                        checked = autoDelete,
                        onCheckedChange = { value -> scope.launch { repository.setAutoDelete(value) } },
                    )
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f))
                PreferenceSwitch(
                    title = stringResource(R.string.menu_metered_network_warning),
                    summary = stringResource(R.string.menu_metered_network_warning_summary),
                    checked = meteredNetworkWarning,
                    onCheckedChange = { value -> scope.launch { repository.setMeteredNetworkWarning(value) } },
                )
                if (isABDevice) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f))
                    PreferenceSwitch(
                        title = stringResource(R.string.menu_ab_perf_mode),
                        summary = if (batteryState.isAcCharging) {
                            stringResource(R.string.menu_ab_perf_mode_summary_charging)
                        } else {
                            stringResource(R.string.menu_ab_perf_mode_summary)
                        },
                        checked = batteryState.isAcCharging || abPerfMode,
                        enabled = !batteryState.isAcCharging,
                        onCheckedChange = { value -> scope.launch { repository.setAbPerfMode(value) } },
                    )
                }
                if (showRecoveryUpdate) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f))
                    PreferenceSwitch(
                        title = stringResource(R.string.menu_update_recovery),
                        summary = stringResource(R.string.menu_update_recovery_summary),
                        checked = recoveryUpdateEnabled,
                        onCheckedChange = { value ->
                            recoveryUpdateEnabled = value
                            repository.setRecoveryUpdateEnabled(value)
                        },
                    )
                }
            }
        }
    }

    if (showIntervalDialog) {
        AlertDialog(
            onDismissRequest = { showIntervalDialog = false },
            title = { Text(stringResource(R.string.updater_interval_title)) },
            text = {
                Column {
                    CheckInterval.entries.forEach { interval ->
                        val label = when (interval) {
                            CheckInterval.DAILY -> stringResource(R.string.updater_interval_daily)
                            CheckInterval.WEEKLY -> stringResource(R.string.updater_interval_weekly)
                            CheckInterval.MONTHLY -> stringResource(R.string.updater_interval_monthly)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable {
                                scope.launch { repository.setCheckInterval(interval) }
                                showIntervalDialog = false
                            }.padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                selected = checkInterval == interval,
                                onClick = {
                                    scope.launch { repository.setCheckInterval(interval) }
                                    showIntervalDialog = false
                                },
                            )
                            Spacer(Modifier.size(10.dp))
                            Text(label, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showIntervalDialog = false }) {
                    Text(stringResource(R.string.updater_done))
                }
            },
        )
    }
}

@Composable
private fun PreferenceSection(
    title: String,
    icon: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 16.dp, bottomEnd = 28.dp, bottomStart = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                icon()
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f))
            content()
        }
    }
}

@Composable
private fun PreferenceSwitch(
    title: String,
    summary: String,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit,
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(summary) },
        trailingContent = {
            Switch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
        },
        colors = androidx.compose.material3.ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    )
}

private fun installRecoveryScriptExists() = File("/vendor/bin/install-recovery.sh").exists()
