package uk.co.inkbinder.noto.feature.tags

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uk.co.inkbinder.noto.data.repository.TagRepository
import uk.co.inkbinder.noto.domain.model.Tag

data class TagsUiState(
    val activeTags: List<Tag> = emptyList(),
    val archivedTags: List<Tag> = emptyList(),
    val editor: TagEditorUiState? = null,
    val availableColors: List<String> = TAG_COLOR_OPTIONS,
)

data class TagEditorUiState(
    val tagId: String? = null,
    val title: String,
    val label: String,
    val selectedColorHex: String,
    val isPeriodTag: Boolean,
    val isArchived: Boolean,
    val canSave: Boolean,
)

class TagsViewModel(
    private val tagRepository: TagRepository,
) : ViewModel() {
    private val editorState = MutableStateFlow<TagEditorDraft?>(null)

    val uiState = combine(
        tagRepository.observeAllTags(),
        editorState,
    ) { tags, editor ->
        TagsUiState(
            activeTags = tags.filterNot(Tag::isArchived),
            archivedTags = tags.filter(Tag::isArchived),
            editor = editor?.toUiState(),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TagsUiState(),
    )

    fun openCreateTag() {
        editorState.value = TagEditorDraft(
            title = "New tag",
            label = "",
            selectedColorHex = TAG_COLOR_OPTIONS.first(),
            isPeriodTag = false,
            isArchived = false,
        )
    }

    fun openEditTag(tag: Tag) {
        editorState.value = TagEditorDraft(
            tagId = tag.id,
            title = "Edit tag",
            label = tag.label,
            selectedColorHex = tag.colorHex,
            isPeriodTag = tag.isPeriodTag,
            isArchived = tag.isArchived,
        )
    }

    fun dismissEditor() {
        editorState.value = null
    }

    fun updateLabel(label: String) {
        editorState.update { editor ->
            editor?.copy(label = label)
        }
    }

    fun updateColor(colorHex: String) {
        editorState.update { editor ->
            editor?.copy(selectedColorHex = colorHex)
        }
    }

    fun updatePeriodTag(isPeriodTag: Boolean) {
        editorState.update { editor ->
            editor?.copy(isPeriodTag = isPeriodTag)
        }
    }

    fun saveEditor() {
        val editor = editorState.value ?: return
        if (editor.label.isBlank()) return

        viewModelScope.launch {
            tagRepository.saveTag(
                tagId = editor.tagId,
                label = editor.label,
                colorHex = editor.selectedColorHex,
                isPeriodTag = editor.isPeriodTag,
            )
            editorState.value = null
        }
    }

    fun archiveTag(tagId: String) {
        if (editorState.value?.tagId == tagId) {
            editorState.value = null
        }

        viewModelScope.launch {
            tagRepository.archiveTag(tagId)
        }
    }

    fun restoreTag(tagId: String) {
        viewModelScope.launch {
            tagRepository.restoreTag(tagId)
        }
    }

    fun moveTagEarlier(tagId: String) {
        viewModelScope.launch {
            tagRepository.moveTagEarlier(tagId)
        }
    }

    fun moveTagLater(tagId: String) {
        viewModelScope.launch {
            tagRepository.moveTagLater(tagId)
        }
    }

    private fun TagEditorDraft.toUiState(): TagEditorUiState = TagEditorUiState(
        tagId = tagId,
        title = title,
        label = label,
        selectedColorHex = selectedColorHex,
        isPeriodTag = isPeriodTag,
        isArchived = isArchived,
        canSave = label.isNotBlank(),
    )
}

private data class TagEditorDraft(
    val tagId: String? = null,
    val title: String,
    val label: String,
    val selectedColorHex: String,
    val isPeriodTag: Boolean,
    val isArchived: Boolean,
)

val TAG_COLOR_OPTIONS = listOf(
    "#D86A6A",
    "#D7A15C",
    "#9A9055",
    "#6FA8A4",
    "#6E8FD7",
    "#9C76D7",
    "#C770A7",
    "#7C8A97",
)
