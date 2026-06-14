package uk.co.inkbinder.noto.feature.tags

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import uk.co.inkbinder.noto.data.repository.TagRepository
import uk.co.inkbinder.noto.domain.model.Tag

data class TagsUiState(
    val tags: List<Tag> = emptyList(),
)

class TagsViewModel(tagRepository: TagRepository) : ViewModel() {
    val uiState = tagRepository.observeActiveTags()
        .map { tags -> TagsUiState(tags = tags) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TagsUiState(),
        )
}

