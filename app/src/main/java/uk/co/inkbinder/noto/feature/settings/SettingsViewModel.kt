package uk.co.inkbinder.noto.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import uk.co.inkbinder.noto.data.preferences.UserPreferencesRepository
import uk.co.inkbinder.noto.domain.model.WeekStart

class SettingsViewModel(
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {
    val uiState = userPreferencesRepository.userPreferences.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = uk.co.inkbinder.noto.domain.model.UserPreferences(),
    )

    fun setPredictionEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setPredictionEnabled(enabled)
        }
    }

    fun incrementCycleLength() {
        viewModelScope.launch {
            val current = uiState.value.defaultCycleLengthDays
            userPreferencesRepository.setDefaultCycleLengthDays(current + 1)
        }
    }

    fun decrementCycleLength() {
        viewModelScope.launch {
            val current = uiState.value.defaultCycleLengthDays
            userPreferencesRepository.setDefaultCycleLengthDays(current - 1)
        }
    }

    fun setWeekStart(weekStart: WeekStart) {
        viewModelScope.launch {
            userPreferencesRepository.setWeekStartsOn(weekStart)
        }
    }
}

