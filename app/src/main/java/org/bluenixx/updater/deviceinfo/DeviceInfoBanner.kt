/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.bluenixx.updater.deviceinfo

import android.content.res.Configuration
import android.icu.text.DateFormat
import android.icu.util.TimeZone
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import org.bluenixx.updater.deviceinfo.actions.DeviceInfoActionButtons
import org.bluenixx.updater.util.StringUtil
import java.util.Date

@Composable
fun DeviceInfoBanner(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val locale = remember(context, configuration.locales) { StringUtil.getCurrentLocale(context) }
    val buildVersion = remember { DeviceInfoUtils.buildVersion }
    val androidVersion = remember { DeviceInfoUtils.androidVersion }
    val buildDate = remember(locale) {
        DateFormat.getInstanceForSkeleton("MMMd", locale)
            .apply { timeZone = TimeZone.getTimeZone("UTC") }
            .format(Date(DeviceInfoUtils.buildDateTimestamp * 1000L))
    }
    val securityPatch = remember(locale) {
        StringUtil.formatSecurityPatch(context, DeviceInfoUtils.buildSecurityPatch)
    }

    DeviceInfoBanner(
        buildVersion = buildVersion,
        androidVersion = androidVersion,
        buildDate = buildDate,
        securityPatch = securityPatch,
        modifier = modifier,
    )
}

@Composable
fun DeviceInfoBanner(
    buildVersion: String,
    androidVersion: String,
    buildDate: String,
    securityPatch: String,
    modifier: Modifier = Modifier,
) {
    val uiMode = LocalConfiguration.current.uiMode
    val isTv = (uiMode and Configuration.UI_MODE_TYPE_MASK) == Configuration.UI_MODE_TYPE_TELEVISION

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        UpdaterCard(
            buildVersion = buildVersion,
            androidVersion = androidVersion,
            buildDate = buildDate,
            securityPatch = securityPatch,
            modifier = Modifier.fillMaxWidth(),
        )
        if (!isTv) {
            Spacer(modifier = Modifier.height(8.dp))
            DeviceInfoActionButtons()
        }
    }
}
