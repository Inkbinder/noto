package uk.co.inkbinder.noto.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import java.time.DayOfWeek
import java.time.Month
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import uk.co.inkbinder.noto.di.AppContainer
import uk.co.inkbinder.noto.domain.model.CalendarDaySummary
import uk.co.inkbinder.noto.domain.model.MonthOverview

private const val DAY_SLICE_FILL_ALPHA = 0.68f

@Composable
fun HomeRoute(
    appContainer: AppContainer,
    onOpenDay: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: HomeViewModel = viewModel(
        factory = remember(appContainer) {
            viewModelFactory {
                initializer {
                    HomeViewModel(appContainer.calendarRepository)
                }
            }
        },
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeScreen(
        uiState = uiState,
        onPreviousMonth = viewModel::showPreviousMonth,
        onNextMonth = viewModel::showNextMonth,
        onSelectMonth = viewModel::setMonth,
        onOpenDay = onOpenDay,
        modifier = modifier,
    )
}

@Composable
private fun HomeScreen(
    uiState: HomeUiState,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSelectMonth: (YearMonth) -> Unit,
    onOpenDay: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val monthOverview = uiState.monthOverview ?: return
    var showMonthPicker by remember(monthOverview.month) { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = RoundedCornerShape(24.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = monthOverview.bannerText,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                )
            }
        }

        MonthHeader(
            month = monthOverview.month,
            onPreviousMonth = onPreviousMonth,
            onNextMonth = onNextMonth,
            onOpenMonthPicker = { showMonthPicker = true },
        )

        CalendarGrid(
            monthOverview = monthOverview,
            onOpenDay = onOpenDay,
        )
    }

    if (showMonthPicker) {
        MonthYearPickerDialog(
            initialMonth = monthOverview.month,
            onDismiss = { showMonthPicker = false },
            onConfirm = { selectedMonth ->
                onSelectMonth(selectedMonth)
                showMonthPicker = false
            },
        )
    }
}

@Composable
private fun MonthHeader(
    month: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onOpenMonthPicker: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(Icons.Default.ChevronLeft, contentDescription = "Previous month")
        }
        Text(
            text = month.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable(onClick = onOpenMonthPicker)
                .semantics {
                    contentDescription = "Choose month"
                }
                .padding(horizontal = 12.dp, vertical = 8.dp),
        )
        IconButton(onClick = onNextMonth) {
            Icon(Icons.Default.ChevronRight, contentDescription = "Next month")
        }
    }
}

@Composable
private fun MonthYearPickerDialog(
    initialMonth: YearMonth,
    onDismiss: () -> Unit,
    onConfirm: (YearMonth) -> Unit,
) {
    var selectedYear by remember(initialMonth) { mutableStateOf(initialMonth.year) }
    var selectedMonth by remember(initialMonth) { mutableStateOf(initialMonth.month) }
    val monthChoices = remember { Month.entries.chunked(3) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose month") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = { selectedYear -= 1 }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous year")
                    }
                    Text(
                        text = selectedYear.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    IconButton(onClick = { selectedYear += 1 }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next year")
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    monthChoices.forEach { rowMonths ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            rowMonths.forEach { month ->
                                val selected = month == selectedMonth
                                if (selected) {
                                    FilledTonalButton(
                                        onClick = { selectedMonth = month },
                                        modifier = Modifier.weight(1f),
                                    ) {
                                        Text(monthPickerLabel(month))
                                    }
                                } else {
                                    OutlinedButton(
                                        onClick = { selectedMonth = month },
                                        modifier = Modifier.weight(1f),
                                    ) {
                                        Text(monthPickerLabel(month))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(YearMonth.of(selectedYear, selectedMonth)) },
            ) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

@Composable
private fun CalendarGrid(
    monthOverview: MonthOverview,
    onOpenDay: (String) -> Unit,
) {
    val weekDays = remember(monthOverview.weekStartsOn) {
        buildWeekdays(monthOverview.weekStartsOn)
    }

    Card(shape = RoundedCornerShape(20.dp)) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                weekDays.forEach { day ->
                    Text(
                        text = day.getDisplayName(TextStyle.SHORT, Locale.getDefault()).take(3),
                        style = MaterialTheme.typography.labelMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 8.dp),
                    )
                }
            }

            monthOverview.weeks.forEach { week ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    week.forEach { day ->
                        CalendarDayCell(
                            day = day,
                            onClick = { onOpenDay(day.date.toString()) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    day: CalendarDaySummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .padding(2.dp)
            .aspectRatio(0.72f)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .background(MaterialTheme.colorScheme.surface)
            .then(
                if (day.isToday) {
                    Modifier.border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(14.dp),
                    )
                } else {
                    Modifier
                },
            ),
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            day.visibleTagColors.forEach { colorHex ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(colorFromHex(colorHex).copy(alpha = DAY_SLICE_FILL_ALPHA)),
                )
            }
        }

        Text(
            text = day.date.dayOfMonth.toString(),
            style = MaterialTheme.typography.bodyMedium,
            color = if (day.inCurrentMonth) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp),
        )

        if (day.overflowCount > 0) {
            Surface(
                shape = CircleShape,
                tonalElevation = 2.dp,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(6.dp),
            ) {
                Text(
                    text = "+${day.overflowCount}",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                )
            }
        }
    }
}

private fun buildWeekdays(weekStartsOn: DayOfWeek): List<DayOfWeek> =
    List(7) { index -> weekStartsOn.plus(index.toLong()) }

private fun monthPickerLabel(month: Month): String =
    month.getDisplayName(TextStyle.SHORT, Locale.getDefault()).replace(".", "").take(3)

private fun colorFromHex(hex: String): Color = runCatching {
    Color(android.graphics.Color.parseColor(hex))
}.getOrDefault(Color(0xFF6B7349))
