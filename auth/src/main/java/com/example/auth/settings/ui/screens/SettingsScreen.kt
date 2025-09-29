package com.example.auth.settings.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavBackStackEntry
import com.example.auth.settings.ui.viewmodel.AppLanguage
import com.example.auth.settings.ui.viewmodel.SettingsViewModel
import com.example.auth.R
import org.koin.androidx.compose.koinViewModel

@Composable
fun settingsScreen(backStackEntry: NavBackStackEntry) {
    val settingsVm: SettingsViewModel = koinViewModel(viewModelStoreOwner = backStackEntry)

    settingsHost(
        vm = settingsVm,
        onApplyWindowDays = { },
        onSubChanged = { inSub ->
            backStackEntry.savedStateHandle["settings_in_sub"] = inSub
        },
    )
}

@Composable
fun settingsHost(
    vm: SettingsViewModel,
    onApplyWindowDays: (Int) -> Unit = {},
    onSubChanged: (Boolean) -> Unit = {},
) {
    var sub by remember { mutableStateOf(SettingsSubScreen.List) }

    LaunchedEffect(sub) { onSubChanged(sub != SettingsSubScreen.List) }

    when (sub) {
        SettingsSubScreen.List ->
            settingsListScreen(
                vm = vm,
                onOpenLanguage = { sub = SettingsSubScreen.Language },
                onOpenNotificationWindow = { sub = SettingsSubScreen.Notification },
            )
        SettingsSubScreen.Language -> languageSettingsScreen(vm = vm, onBack = { sub = SettingsSubScreen.List })
        SettingsSubScreen.Notification ->
            notificationWindowScreen(
                vm = vm,
                onBack = { sub = SettingsSubScreen.List },
                onApplyWindowDays = { days -> onApplyWindowDays(days) },
            )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun settingsListScreen(
    vm: SettingsViewModel,
    onOpenLanguage: () -> Unit,
    onOpenNotificationWindow: () -> Unit,
) {
    val ui by vm.ui.collectAsState()

    Column(
        Modifier
            .padding(dimensionResource(R.dimen.mediumSpace))
            .fillMaxSize(),
    ) {
        settingRow(
            title = stringResource(R.string.settings_app_language),
            value =
                if (ui.language == AppLanguage.EN) {
                    stringResource(R.string.app_language_english)
                } else {
                    stringResource(R.string.app_language_arabic)
                },
            onClick = onOpenLanguage,
        )
        Divider()
        settingRow(
            title = stringResource(R.string.settings_notifications),
            value = stringResource(R.string.every_x_days, ui.window.days),
            onClick = onOpenNotificationWindow,
        )
    }
}

@Composable
fun settingRow(
    title: String,
    value: String,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = dimensionResource(R.dimen.smallSpace)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(dimensionResource(R.dimen.smallestSpace)))
            Text(
                value,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
        )
    }
}

@Composable
fun radioLine(
    label: String,
    selected: Boolean,
    onPick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onPick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = onPick)
        Spacer(Modifier.width(dimensionResource(R.dimen.smallSpace)))
        Text(label)
    }
}

enum class SettingsSubScreen { List, Language, Notification }
