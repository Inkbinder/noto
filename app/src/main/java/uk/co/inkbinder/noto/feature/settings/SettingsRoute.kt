package uk.co.inkbinder.noto.feature.settings

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import uk.co.inkbinder.noto.di.AppContainer
import uk.co.inkbinder.noto.domain.model.WeekStart

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsRoute(
    appContainer: AppContainer,
    modifier: Modifier = Modifier,
) {
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
}
