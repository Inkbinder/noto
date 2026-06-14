package uk.co.inkbinder.noto.feature.day

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import uk.co.inkbinder.noto.di.AppContainer
import uk.co.inkbinder.noto.domain.model.Tag

@Composable
fun DayDetailRoute(
    appContainer: AppContainer,
    dayKey: String,
    onBack: () -> Unit,
) {
    val parsedDate = remember(dayKey) { LocalDate.parse(dayKey) }
    val viewModel: DayDetailViewModel = viewModel(
        factory = remember(appContainer, parsedDate) {
            viewModelFactory {
                initializer {
                    DayDetailViewModel(parsedDate, appContainer.calendarRepository)
                }
            }
        },
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DayDetailScreen(
        uiState = uiState,
        onBack = onBack,
        onToggleTag = viewModel::toggleTag,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DayDetailScreen(
    uiState: DayDetailUiState,
    onBack: () -> Unit,
    onToggleTag: (String) -> Unit,
) {
    val date = uiState.date ?: return

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(date.format(DateTimeFormatter.ofPattern("EEEE, d MMMM")))
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                Text(
                    text = "Select all tags that apply to this day.",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            items(uiState.availableTags, key = Tag::id) { tag ->
                DayTagRow(
                    tag = tag,
                    selected = tag.id in uiState.selectedTagIds,
                    onToggle = { onToggleTag(tag.id) },
                )
            }
        }
    }
}

@Composable
private fun DayTagRow(
    tag: Tag,
    selected: Boolean,
    onToggle: () -> Unit,
) {
    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onToggle)
            .background(MaterialTheme.colorScheme.surfaceContainerLow),
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(colorFromHex(tag.colorHex)),
            )
        },
        headlineContent = {
            Text(tag.label)
        },
        supportingContent = {
            if (tag.isPeriodTag) {
                Text("Used for period prediction")
            }
        },
        trailingContent = {
            Checkbox(
                checked = selected,
                onCheckedChange = { onToggle() },
            )
        },
    )
}

private fun colorFromHex(hex: String): Color = runCatching {
    Color(android.graphics.Color.parseColor(hex))
}.getOrDefault(Color(0xFF6B7349))
