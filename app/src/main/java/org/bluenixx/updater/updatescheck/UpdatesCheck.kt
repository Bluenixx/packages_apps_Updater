/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.bluenixx.updater.updatescheck

import android.content.Context
import android.os.SystemClock
import android.text.format.DateFormat
import android.text.format.DateUtils
import androidx.annotation.RawRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieConstants
import kotlinx.coroutines.delay
import org.bluenixx.updater.R
import org.bluenixx.updater.ui.Lottie
import java.util.Date

private const val MIN_CHECKING_DURATION_MILLIS = 2_000L
private val AnimationSize = 136.dp

sealed interface UpdatesCheckState {
    data object Idle : UpdatesCheckState
    data object Checking : UpdatesCheckState
    data object NoInternet : UpdatesCheckState
    data object Error : UpdatesCheckState
}

data class UpdatesCheckModel(
    val state: UpdatesCheckState,
    val lastCheckedTimestamp: Long,
    val canCheckForUpdates: Boolean,
)

class UpdatesCheckUiState internal constructor(
    internal val displayedState: UpdatesCheckState,
) {
    val isStatusVisible = displayedState != UpdatesCheckState.Idle
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdatesCheck(
    model: UpdatesCheckModel,
    uiState: UpdatesCheckUiState,
    onCheckClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lastCheckedText = remember(model.lastCheckedTimestamp) {
        if (model.lastCheckedTimestamp <= 0L) {
            context.getString(R.string.updater_last_checked_never)
        } else {
            formatLastCheckedText(context, model.lastCheckedTimestamp)
        }
    }
    val colors = MaterialTheme.colorScheme

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(
            topStart = 30.dp,
            topEnd = 18.dp,
            bottomEnd = 30.dp,
            bottomStart = 18.dp,
        ),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerLow),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.updater_check_label),
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.primary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.35.sp,
                )
                Spacer(Modifier.weight(1f))
                if (uiState.displayedState == UpdatesCheckState.Idle) {
                    Text(
                        text = stringResource(R.string.updater_available_status),
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.tertiary,
                    )
                }
            }

            when (uiState.displayedState) {
                UpdatesCheckState.Idle -> {
                    Text(
                        text = stringResource(R.string.updater_check_idle_title),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = stringResource(R.string.updater_check_idle_summary),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurfaceVariant,
                    )
                }
                UpdatesCheckState.Checking -> StatusContent(
                    R.raw.sysupdater_progress,
                    R.string.updater_checking_title,
                    R.string.updater_checking_summary,
                )
                UpdatesCheckState.NoInternet -> StatusContent(
                    R.raw.sysupdater_error,
                    R.string.check_your_internet_connection,
                    R.string.updates_check_failed,
                    iterations = 1,
                )
                UpdatesCheckState.Error -> StatusContent(
                    R.raw.sysupdater_error,
                    R.string.updates_check_failed,
                    R.string.check_your_internet_connection,
                    iterations = 1,
                )
            }

            if (model.canCheckForUpdates && uiState.displayedState != UpdatesCheckState.Checking) {
                FilledTonalButton(
                    onClick = onCheckClick,
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = colors.secondaryContainer,
                        contentColor = colors.onSecondaryContainer,
                    ),
                ) {
                    Icon(Icons.Outlined.Refresh, contentDescription = null)
                    Spacer(Modifier.size(9.dp))
                    Text(stringResource(R.string.check_for_updates), fontWeight = FontWeight.SemiBold)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Outlined.History,
                    contentDescription = null,
                    tint = colors.onSurfaceVariant,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.size(7.dp))
                Text(
                    text = lastCheckedText,
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Start,
                )
            }
        }
    }
}

@Composable
private fun StatusContent(
    @RawRes animationResId: Int,
    @StringRes titleResId: Int,
    @StringRes summaryResId: Int,
    iterations: Int = LottieConstants.IterateForever,
) {
    val title = stringResource(titleResId)
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Lottie(
            resId = animationResId,
            modifier = Modifier.size(AnimationSize).semantics { contentDescription = title },
            iterations = iterations,
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(summaryResId),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

private fun rememberStateWithMinimumCheckingDuration(state: UpdatesCheckState): UpdatesCheckState {
    var displayedState by remember { mutableStateOf(state) }
    var checkingStartedAtMillis by remember { mutableLongStateOf(0L) }
    LaunchedEffect(state) {
        if (state == UpdatesCheckState.Checking) {
            checkingStartedAtMillis = SystemClock.elapsedRealtime()
            displayedState = state
            return@LaunchedEffect
        }
        if (displayedState == UpdatesCheckState.Checking) {
            val remaining = MIN_CHECKING_DURATION_MILLIS -
                (SystemClock.elapsedRealtime() - checkingStartedAtMillis)
            if (remaining > 0L) delay(remaining)
        }
        displayedState = state
    }
    return displayedState
}

@Composable
internal fun rememberUpdatesCheckUiState(state: UpdatesCheckState): UpdatesCheckUiState {
    val displayedState = rememberStateWithMinimumCheckingDuration(state)
    return remember(displayedState) { UpdatesCheckUiState(displayedState) }
}

private fun formatLastCheckedText(context: Context, timestampMillis: Long): String {
    val time = DateFormat.getTimeFormat(context).format(Date(timestampMillis))
    if (DateUtils.isToday(timestampMillis)) {
        return context.getString(R.string.header_last_updates_check_time, time)
    }
    val date = DateUtils.formatDateTime(
        context,
        timestampMillis,
        DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_ABBREV_MONTH or DateUtils.FORMAT_NO_YEAR,
    )
    return context.getString(R.string.header_last_updates_check, date, time)
}
