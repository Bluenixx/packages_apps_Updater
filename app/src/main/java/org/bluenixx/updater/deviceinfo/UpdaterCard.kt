/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.bluenixx.updater.deviceinfo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.matchParentSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import org.bluenixx.updater.R

@Composable
fun UpdaterCard(
    buildVersion: String,
    androidVersion: String,
    buildDate: String,
    securityPatch: String,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(
        topStart = 34.dp,
        topEnd = 34.dp,
        bottomEnd = 14.dp,
        bottomStart = 34.dp,
    ),
) {
    val colors = MaterialTheme.colorScheme
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = colors.primaryContainer,
            contentColor = colors.onPrimaryContainer,
        ),
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            androidx.compose.foundation.Canvas(
                modifier = Modifier.matchParentSize(),
            ) {
                val accent = colors.primary.copy(alpha = 0.10f)
                drawCircle(
                    color = accent,
                    radius = size.width * 0.48f,
                    center = Offset(size.width * 0.96f, size.height * 0.12f),
                )
                drawCircle(
                    color = colors.tertiary.copy(alpha = 0.12f),
                    radius = size.width * 0.22f,
                    center = Offset(size.width * 0.92f, size.height * 0.82f),
                )
                rotate(28f, pivot = Offset(size.width * 0.88f, size.height * 0.32f)) {
                    drawRoundRect(
                        color = colors.onPrimaryContainer.copy(alpha = 0.06f),
                        topLeft = Offset(size.width * 0.69f, size.height * 0.10f),
                        size = Size(size.width * 0.30f, size.height * 0.18f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(80f, 80f),
                        style = Stroke(width = 2.dp.toPx()),
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 24.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(13.dp))
                            .background(colors.onPrimaryContainer.copy(alpha = 0.10f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CloudDownload,
                            contentDescription = null,
                            tint = colors.onPrimaryContainer,
                            modifier = Modifier.size(21.dp),
                        )
                    }
                    Spacer(Modifier.size(10.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                        Text(
                            text = stringResource(R.string.brand_name).uppercase(),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp,
                        )
                        Text(
                            text = stringResource(R.string.updater_device_section),
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.onPrimaryContainer.copy(alpha = 0.72f),
                            letterSpacing = 1.0.sp,
                        )
                    }
                }

                Spacer(Modifier.height(22.dp))
                Text(
                    text = buildVersion.ifBlank { stringResource(R.string.brand_name) },
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontSize = 40.sp,
                        lineHeight = 44.sp,
                        letterSpacing = (-1.9).sp,
                        fontWeight = FontWeight.Light,
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.updater_subtitle),
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.onPrimaryContainer.copy(alpha = 0.72f),
                    letterSpacing = 1.7.sp,
                )

                Spacer(Modifier.height(26.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    BuildInfo(
                        modifier = Modifier.weight(1f),
                        label = stringResource(R.string.updater_android_label),
                        value = androidVersion,
                    )
                    BuildInfo(
                        modifier = Modifier.weight(1f),
                        label = stringResource(R.string.updater_date_label),
                        value = buildDate,
                    )
                    BuildInfo(
                        modifier = Modifier.weight(1f),
                        label = stringResource(R.string.updater_patch_label),
                        value = securityPatch,
                    )
                }
            }
        }
    }
}

@Composable
private fun BuildInfo(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(17.dp))
            .background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.07f))
            .padding(horizontal = 11.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.68f),
            maxLines = 1,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
        )
    }
}
