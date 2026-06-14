package uk.co.inkbinder.noto.data.bootstrap

import kotlinx.coroutines.flow.first
import uk.co.inkbinder.noto.data.preferences.UserPreferencesRepository
import uk.co.inkbinder.noto.data.repository.TagRepository
import uk.co.inkbinder.noto.domain.model.Tag

class SeedDataInitializer(
    private val tagRepository: TagRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
) {
    suspend fun seedDefaultsIfNeeded() {
        val preferences = userPreferencesRepository.userPreferences.first()
        if (preferences.seededDefaultsVersion >= DEFAULTS_VERSION) return

        tagRepository.upsertDefaults(defaultTags)
        userPreferencesRepository.markDefaultsSeeded(DEFAULTS_VERSION)
    }

    private companion object {
        const val DEFAULTS_VERSION = 1

        val defaultTags = listOf(
            Tag(
                id = "bleeding",
                label = "Bleeding",
                colorHex = "#D86A6A",
                isPeriodTag = true,
                isArchived = false,
                sortOrder = 0,
            ),
            Tag(
                id = "bad_hangover",
                label = "Bad hangover",
                colorHex = "#D7A15C",
                isPeriodTag = false,
                isArchived = false,
                sortOrder = 1,
            ),
            Tag(
                id = "no_hangover",
                label = "No hangover",
                colorHex = "#6E8FD7",
                isPeriodTag = false,
                isArchived = false,
                sortOrder = 2,
            ),
            Tag(
                id = "dodgy_poops",
                label = "Dodgy poops",
                colorHex = "#9A9055",
                isPeriodTag = false,
                isArchived = false,
                sortOrder = 3,
            ),
            Tag(
                id = "lightheaded",
                label = "Lightheaded",
                colorHex = "#6FA8A4",
                isPeriodTag = false,
                isArchived = false,
                sortOrder = 4,
            ),
        )
    }
}

