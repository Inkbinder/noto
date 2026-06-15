package uk.co.inkbinder.noto.feature.settings

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.app.TimePickerDialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import uk.co.inkbinder.noto.di.AppContainer
import uk.co.inkbinder.noto.domain.model.WeekStart

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsRoute(
    appContainer: AppContainer,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var showReminderTimePicker by remember { mutableStateOf(false) }
    val viewModel: SettingsViewModel = viewModel(
        factory = remember(appContainer) {
            viewModelFactory {
                initializer {
                    SettingsViewModel(appContainer.userPreferencesRepository)
                }
            }
        },
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        viewModel.setReminderEnabled(granted)
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Settings") })
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                ListItem(
                    headlineContent = { Text("Period prediction") },
                    supportingContent = { Text("Show the due / delayed banner on the home screen.") },
                    trailingContent = {
                        Switch(
                            checked = uiState.periodPredictionEnabled,
                            onCheckedChange = viewModel::setPredictionEnabled,
                        )
                    },
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("Period reminders") },
                    supportingContent = {
                        Text(
                            if (uiState.periodPredictionEnabled) {
                                "Show a notification 3, 2, 1, and 0 days before a predicted period."
                            } else {
                                "Turn period prediction back on to enable due reminders."
                            },
                        )
                    },
                    trailingContent = {
                        Switch(
                            checked = uiState.periodReminderEnabled,
                            onCheckedChange = { enabled ->
                                when {
                                    !enabled -> viewModel.setReminderEnabled(false)
                                    shouldRequestNotificationPermission(context) -> {
                                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                    else -> viewModel.setReminderEnabled(true)
                                }
                            },
                            enabled = uiState.periodPredictionEnabled,
                        )
                    },
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("Reminder time") },
                    supportingContent = {
                        Text(formatReminderTime(uiState.periodReminderMinutesAfterMidnight))
                    },
                    trailingContent = {
                        OutlinedButton(
                            onClick = { showReminderTimePicker = true },
                            enabled = uiState.periodPredictionEnabled,
                        ) {
                            Text("Edit")
                        }
                    },
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("Default cycle length") },
                    supportingContent = { Text("${uiState.defaultCycleLengthDays} days") },
                    trailingContent = {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = viewModel::decrementCycleLength) {
                                Text("-")
                            }
                            OutlinedButton(onClick = viewModel::incrementCycleLength) {
                                Text("+")
                            }
                        }
                    },
                )
            }
            item {
                Text(
                    text = "Week starts on",
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    WeekStart.entries.forEach { weekStart ->
                        val selected = uiState.weekStartsOn == weekStart
                        if (selected) {
                            FilledTonalButton(onClick = { viewModel.setWeekStart(weekStart) }) {
                                Text(weekStart.name.lowercase().replaceFirstChar(Char::titlecase))
                            }
                        } else {
                            OutlinedButton(onClick = { viewModel.setWeekStart(weekStart) }) {
                                Text(weekStart.name.lowercase().replaceFirstChar(Char::titlecase))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showReminderTimePicker) {
        ReminderTimePickerDialog(
            initialMinutesAfterMidnight = uiState.periodReminderMinutesAfterMidnight,
            onDismiss = { showReminderTimePicker = false },
            onConfirm = { minutesAfterMidnight ->
                viewModel.setReminderTime(minutesAfterMidnight)
                showReminderTimePicker = false
            },
        )
    }
}

private fun shouldRequestNotificationPermission(context: Context): Boolean =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) != PackageManager.PERMISSION_GRANTED

private fun formatReminderTime(minutesAfterMidnight: Int): String =
    LocalTime.of(minutesAfterMidnight / 60, minutesAfterMidnight % 60)
        .format(DateTimeFormatter.ofPattern("HH:mm"))

@Composable
private fun ReminderTimePickerDialog(
    initialMinutesAfterMidnight: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
) {
    val context = LocalContext.current
    val initialHour = initialMinutesAfterMidnight / 60
    val initialMinute = initialMinutesAfterMidnight % 60

    val dialog = remember(context, initialHour, initialMinute) {
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                onConfirm(hourOfDay * 60 + minute)
            },
            initialHour,
            initialMinute,
            true,
        ).apply {
            setOnDismissListener { onDismiss() }
        }
    }

    DisposableEffect(dialog) {
        dialog.show()
        onDispose {
            dialog.setOnDismissListener(null)
            dialog.dismiss()
        }
    }
}
