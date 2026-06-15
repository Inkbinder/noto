package uk.co.inkbinder.noto.feature.tags

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import java.util.Locale
import uk.co.inkbinder.noto.di.AppContainer
import uk.co.inkbinder.noto.domain.model.Tag

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagsRoute(
    appContainer: AppContainer,
    modifier: Modifier = Modifier,
) {
    val viewModel: TagsViewModel = viewModel(
        factory = remember(appContainer) {
            viewModelFactory {
                initializer {
                    TagsViewModel(appContainer.tagRepository)
                }
            }
        },
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tags") },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = viewModel::openCreateTag,
                modifier = Modifier.semantics {
                    contentDescription = "Add tag"
                },
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            contentPadding = PaddingValues(bottom = 104.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                SectionTitle(title = "Active tags")
            }
            itemsIndexed(uiState.activeTags, key = { _, tag -> tag.id }) { index, tag ->
                ActiveTagCard(
                    tag = tag,
                    canMoveEarlier = index > 0,
                    canMoveLater = index < uiState.activeTags.lastIndex,
                    onEdit = { viewModel.openEditTag(tag) },
                    onMoveEarlier = { viewModel.moveTagEarlier(tag.id) },
                    onMoveLater = { viewModel.moveTagLater(tag.id) },
                    onArchive = { viewModel.archiveTag(tag.id) },
                )
            }
            if (uiState.archivedTags.isNotEmpty()) {
                item {
                    SectionTitle(title = "Archived tags")
                }
                itemsIndexed(uiState.archivedTags, key = { _, tag -> tag.id }) { _, tag ->
                    ArchivedTagCard(
                        tag = tag,
                        onEdit = { viewModel.openEditTag(tag) },
                        onRestore = { viewModel.restoreTag(tag.id) },
                        onDelete = { viewModel.deleteArchivedTag(tag.id) },
                    )
                }
            }
        }
    }

    uiState.editor?.let { editor ->
        TagEditorDialog(
            editor = editor,
            availableColors = uiState.availableColors,
            onDismiss = viewModel::dismissEditor,
            onLabelChange = viewModel::updateLabel,
            onColorSelected = viewModel::updateColor,
            onPeriodTagChanged = viewModel::updatePeriodTag,
            onSave = viewModel::saveEditor,
        )
    }
}

@Composable
private fun SectionTitle(
    title: String,
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
    )
}

@Composable
private fun ActiveTagCard(
    tag: Tag,
    canMoveEarlier: Boolean,
    canMoveLater: Boolean,
    onEdit: () -> Unit,
    onMoveEarlier: () -> Unit,
    onMoveLater: () -> Unit,
    onArchive: () -> Unit,
) {
    TagCard(
        tag = tag,
        statusLabel = null,
        trailingContent = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(
                    onClick = onMoveEarlier,
                    enabled = canMoveEarlier,
                ) {
                    Icon(Icons.Default.ArrowUpward, contentDescription = "Move ${tag.label} earlier")
                }
                IconButton(
                    onClick = onMoveLater,
                    enabled = canMoveLater,
                ) {
                    Icon(Icons.Default.ArrowDownward, contentDescription = "Move ${tag.label} later")
                }
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit ${tag.label}")
                }
                IconButton(onClick = onArchive) {
                    Icon(Icons.Default.Archive, contentDescription = "Archive ${tag.label}")
                }
            }
        },
    )
}

@Composable
private fun ArchivedTagCard(
    tag: Tag,
    onEdit: () -> Unit,
    onRestore: () -> Unit,
    onDelete: () -> Unit,
) {
    TagCard(
        tag = tag,
        statusLabel = "Archived",
        trailingContent = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit ${tag.label}")
                }
                IconButton(onClick = onRestore) {
                    Icon(Icons.Default.RestoreFromTrash, contentDescription = "Restore ${tag.label}")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.DeleteForever, contentDescription = "Delete ${tag.label}")
                }
            }
        },
    )
}

@Composable
private fun TagCard(
    tag: Tag,
    statusLabel: String?,
    trailingContent: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .background(colorFromHex(tag.colorHex)),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = tag.label,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                statusLabel?.let { label ->
                    StatusBadge(label = label)
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 8.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                trailingContent()
            }
        }
    }
}

@Composable
private fun StatusBadge(label: String) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun TagEditorDialog(
    editor: TagEditorUiState,
    availableColors: List<String>,
    onDismiss: () -> Unit,
    onLabelChange: (String) -> Unit,
    onColorSelected: (String) -> Unit,
    onPeriodTagChanged: (Boolean) -> Unit,
    onSave: () -> Unit,
) {
    var showCustomColorPicker by remember(editor.selectedColorHex) { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(editor.title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = editor.label,
                    onValueChange = onLabelChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Label") },
                    singleLine = true,
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Color",
                        style = MaterialTheme.typography.labelLarge,
                    )
                    availableColors.chunked(4).forEach { rowColors ->
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            rowColors.forEach { colorHex ->
                                ColorOption(
                                    colorHex = colorHex,
                                    selected = editor.selectedColorHex == colorHex,
                                    onClick = { onColorSelected(colorHex) },
                                )
                            }
                        }
                    }
                    OutlinedButton(onClick = { showCustomColorPicker = true }) {
                        Text("Custom color")
                    }
                    Text(
                        text = "Selected: ${editor.selectedColorHex}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                if (!editor.isArchived) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            Text(
                                text = "Use for period prediction",
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            Text(
                                text = "Selecting this will replace any other prediction tag.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Switch(
                            checked = editor.isPeriodTag,
                            onCheckedChange = onPeriodTagChanged,
                        )
                    }
                } else {
                    Text(
                        text = "Archived tags keep their history, but they cannot drive prediction until restored.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onSave,
                enabled = editor.canSave,
            ) {
                Text(if (editor.tagId == null) "Create" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )

    if (showCustomColorPicker) {
        CustomColorDialog(
            initialColorHex = editor.selectedColorHex,
            onDismiss = { showCustomColorPicker = false },
            onConfirm = { selectedColorHex ->
                onColorSelected(selectedColorHex)
                showCustomColorPicker = false
            },
        )
    }
}

@Composable
private fun ColorOption(
    colorHex: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        shape = CircleShape,
        color = colorFromHex(colorHex),
        tonalElevation = if (selected) 4.dp else 0.dp,
        border = BorderStroke(
            width = if (selected) 3.dp else 1.dp,
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outlineVariant
            },
        ),
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }
}

@Composable
private fun CustomColorDialog(
    initialColorHex: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    val initialRgb = remember(initialColorHex) { rgbFromHex(initialColorHex) }
    var red by remember(initialColorHex) { mutableIntStateOf(initialRgb.red) }
    var green by remember(initialColorHex) { mutableIntStateOf(initialRgb.green) }
    var blue by remember(initialColorHex) { mutableIntStateOf(initialRgb.blue) }
    var hexInput by remember(initialColorHex) {
        mutableStateOf(colorToHex(red = initialRgb.red, green = initialRgb.green, blue = initialRgb.blue))
    }
    val normalizedHex = remember(hexInput) { normalizeColorHexInput(hexInput) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Custom color") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    color = colorFromHex(colorToHex(red = red, green = green, blue = blue)),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                ) {}

                OutlinedTextField(
                    value = hexInput,
                    onValueChange = { input ->
                        hexInput = input.uppercase(Locale.US)
                        normalizeColorHexInput(input)?.let { normalized ->
                            val rgb = rgbFromHex(normalized)
                            red = rgb.red
                            green = rgb.green
                            blue = rgb.blue
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Hex color") },
                    supportingText = {
                        Text(
                            if (normalizedHex == null) {
                                "Use a 6-digit hex value like #D86A6A"
                            } else {
                                "Applied color: $normalizedHex"
                            },
                        )
                    },
                    singleLine = true,
                )

                RgbSlider(
                    label = "Red",
                    value = red,
                    onValueChange = { value ->
                        red = value
                        hexInput = colorToHex(red = red, green = green, blue = blue)
                    },
                )
                RgbSlider(
                    label = "Green",
                    value = green,
                    onValueChange = { value ->
                        green = value
                        hexInput = colorToHex(red = red, green = green, blue = blue)
                    },
                )
                RgbSlider(
                    label = "Blue",
                    value = blue,
                    onValueChange = { value ->
                        blue = value
                        hexInput = colorToHex(red = red, green = green, blue = blue)
                    },
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(normalizedHex ?: colorToHex(red = red, green = green, blue = blue)) },
                enabled = normalizedHex != null,
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
private fun RgbSlider(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = "$label: $value",
            style = MaterialTheme.typography.bodyMedium,
        )
        Slider(
            value = value.toFloat(),
            onValueChange = { next -> onValueChange(next.toInt()) },
            valueRange = 0f..255f,
        )
    }
}

private data class RgbColor(
    val red: Int,
    val green: Int,
    val blue: Int,
)

internal fun normalizeColorHexInput(input: String): String? {
    val sanitized = input.trim().removePrefix("#")
    if (sanitized.length != 6 || !sanitized.matches(Regex("[0-9A-Fa-f]{6}"))) {
        return null
    }

    return "#${sanitized.uppercase(Locale.US)}"
}

private fun rgbFromHex(hex: String): RgbColor {
    val colorInt = android.graphics.Color.parseColor(normalizeColorHexInput(hex) ?: TAG_COLOR_OPTIONS.first())
    return RgbColor(
        red = android.graphics.Color.red(colorInt),
        green = android.graphics.Color.green(colorInt),
        blue = android.graphics.Color.blue(colorInt),
    )
}

private fun colorToHex(red: Int, green: Int, blue: Int): String =
    String.format(Locale.US, "#%02X%02X%02X", red, green, blue)

private fun colorFromHex(hex: String): Color = runCatching {
    Color(android.graphics.Color.parseColor(hex))
}.getOrDefault(Color(0xFF6B7349))
