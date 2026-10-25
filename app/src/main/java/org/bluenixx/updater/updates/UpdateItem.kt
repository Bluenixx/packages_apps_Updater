/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.bluenixx.updater.updates

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.settingslib.spa.debug.UiModePreviews
import com.android.settingslib.spa.framework.theme.SettingsTheme
import org.bluenixx.updater.R
import org.bluenixx.updater.updates.action.UpdateAction
import org.bluenixx.updater.updates.action.UpdateActionType
import org.bluenixx.updater.updates.action.UpdateActions
import org.bluenixx.updater.updates.button.ActionBar
import org.bluenixx.updater.updates.button.ActionBarButton
import org.bluenixx.updater.updates.state.ProgressState
import org.bluenixx.updater.updates.state.UpdateItemState

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun UpdateItem(
    state: UpdateItemState,
    expanded: Boolean,
    onExpandToggle: (() -> Unit)?,
    onAction: (UpdateAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(
            topStart = 28.dp,
            topEnd = 16.dp,
            bottomEnd = 28.dp,
            bottomStart = 16.dp,
        ),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerLow),
    ) {
        key(state.downloadId) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (onExpandToggle != null) Modifier.clickable(onClick = onExpandToggle)
                        else Modifier
                    )
                    .padding(horizontal = 17.dp, vertical = 17.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(13.dp),
            ) {
                Column(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(17.dp))
                        .background(colors.primaryContainer),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        imageVector = if (state.isLocal) Icons.Outlined.Archive else Icons.Outlined.CloudDownload,
                        contentDescription = null,
                        tint = colors.onPrimaryContainer,
                        modifier = Modifier.size(23.dp),
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text(
                        text = state.buildDate,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = state.buildVersion,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurfaceVariant,
                    )
                    if (state.status.isNotEmpty()) {
                        Text(
                            text = state.status,
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.primary,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
                if (onExpandToggle != null) {
                    Icon(
                        imageVector = if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                        contentDescription = null,
                        tint = colors.onSurfaceVariant,
                    )
                }
            }
        }

        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 18.dp, end = 18.dp, bottom = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                HorizontalDivider(color = colors.outlineVariant.copy(alpha = 0.65f))
                when (val progress = state.progress) {
                    is ProgressState.Determinate -> {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "${progress.percent.toInt()}%",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Medium,
                                letterSpacing = (-1).sp,
                            )
                            Spacer(Modifier.weight(1f))
                            if (progress.eta.isNotEmpty()) {
                                Text(
                                    text = progress.eta,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = colors.onSurfaceVariant,
                                )
                            }
                        }
                        LinearProgressIndicator(
                            progress = (progress.percent / 100f).coerceIn(0f, 1f),
                            modifier = Modifier.fillMaxWidth().height(9.dp).clip(CircleShape),
                            color = colors.primary,
                            trackColor = colors.secondaryContainer,
                        )
                        if (progress.downloadedSize.isNotEmpty()) {
                            Text(
                                text = progress.downloadedSize,
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurfaceVariant,
                            )
                        }
                    }
                    ProgressState.Indeterminate -> {
                        Text(
                            text = state.status,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth().height(9.dp).clip(CircleShape),
                            color = colors.primary,
                            trackColor = colors.secondaryContainer,
                        )
                    }
                    null -> {
                        DetailLine(stringResource(R.string.list_update_size, state.fileSize))
                        if (state.androidUpdateInfo.isNotEmpty()) DetailLine(state.androidUpdateInfo)
                        if (state.securityUpdate.isNotEmpty()) {
                            DetailLine(stringResource(R.string.list_security_update, state.securityUpdate))
                        }
                        Text(
                            text = if (state.installNote == R.string.list_major_upgrade_recovery_install) {
                                stringResource(state.installNote, stringResource(R.string.brand_name))
                            } else {
                                stringResource(state.installNote)
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant,
                        )
                    }
                }
                ActionBar(
                    buttons = listOfNotNull(
                        state.actions.primary.toActionBarButton(context, onAction, isPrimary = true),
                        state.actions.secondary?.toActionBarButton(context, onAction),
                    ),
                    menuContent = if (state.actions.overflow.isEmpty()) null else {
                        {
                            state.actions.overflow.forEach { action ->
                                MenuItem(
                                    text = action.type.title(context),
                                    enabled = action.enabled,
                                ) { onAction(action) }
                            }
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun DetailLine(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

private fun UpdateAction.toActionBarButton(
    context: Context,
    onAction: (UpdateAction) -> Unit,
    isPrimary: Boolean = false,
): ActionBarButton = when (type) {
    UpdateActionType.PAUSE_DOWNLOAD,
    UpdateActionType.PAUSE_INSTALL -> ActionBarButton.Icon(
        imageVector = Icons.Outlined.Pause,
        contentDescription = type.title(context),
        enabled = enabled,
        onClick = { onAction(this) },
    )
    UpdateActionType.RESUME_DOWNLOAD,
    UpdateActionType.RESUME_INSTALL -> ActionBarButton.Icon(
        imageVector = Icons.Outlined.PlayArrow,
        contentDescription = type.title(context),
        enabled = enabled,
        onClick = { onAction(this) },
    )
    else -> if (isPrimary) {
        ActionBarButton.Tonal(
            text = type.title(context),
            enabled = enabled,
            onClick = { onAction(this) },
        )
    } else {
        ActionBarButton.Outlined(
            text = type.title(context),
            enabled = enabled,
            onClick = { onAction(this) },
        )
    }
}

@UiModePreviews
@Composable
private fun UpdateItemPreview() {
    SettingsTheme {
        UpdateItem(
            state = UpdateItemState(
                downloadId = "preview",
                buildDate = "Oct 25",
                buildVersion = "Bluenixx 17.0",
                status = "Downloading",
                isLocal = false,
                fileSize = "1.1 GB",
                androidUpdateInfo = "Android 17",
                securityUpdate = "Oct 2026",
                installNote = R.string.list_full_install,
                progress = ProgressState.Determinate(
                    percent = 65f,
                    downloadedSize = "715 MB of 1.1 GB",
                    eta = "3 min left",
                ),
                actions = UpdateActions(
                    primary = UpdateAction(type = UpdateActionType.PAUSE_DOWNLOAD),
                    secondary = UpdateAction(type = UpdateActionType.CANCEL_DOWNLOAD),
                    overflow = listOf(UpdateAction(type = UpdateActionType.VIEW_DOWNLOADS)),
                ),
            ),
            expanded = true,
            onExpandToggle = {},
            onAction = {},
        )
    }
}
