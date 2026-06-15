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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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

private fun colorFromHex(hex: String): Color = runCatching {
    Color(android.graphics.Color.parseColor(hex))
}.getOrDefault(Color(0xFF6B7349))
