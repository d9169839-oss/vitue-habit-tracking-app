package com.virtue.habittracker.presentation.programs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.virtue.habittracker.domain.model.program.ProgramActivityStatus
import com.virtue.habittracker.domain.model.program.ProgramCatalog
import com.virtue.habittracker.domain.model.program.ProgramCategory
import com.virtue.habittracker.domain.model.program.ProgramPreferences
import com.virtue.habittracker.domain.model.program.ProgramProgress
import com.virtue.habittracker.domain.model.program.ProgramStatus
import com.virtue.habittracker.domain.repository.ProgramRepository
import com.virtue.habittracker.domain.repository.PremiumEntitlementProvider
import com.virtue.habittracker.domain.usecase.program.StartProgramUseCase
import com.virtue.habittracker.domain.usecase.program.SetProgramActivityStatusUseCase
import com.virtue.habittracker.domain.usecase.program.SetProgramStatusUseCase
import com.virtue.habittracker.domain.usecase.program.DeleteProgramUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ProgramsUiState(
    val selectedCategory: ProgramCategory? = null,
    val programs: List<ProgramProgress> = emptyList(),
    val isPremium: Boolean = false,
    val isStarting: Boolean = false,
    val message: String? = null
)

@HiltViewModel
class ProgramsViewModel @Inject constructor(
    private val repository: ProgramRepository,
    private val startProgramUseCase: StartProgramUseCase,
    private val setActivityStatusUseCase: SetProgramActivityStatusUseCase,
    private val setProgramStatusUseCase: SetProgramStatusUseCase,
    private val deleteProgramUseCase: DeleteProgramUseCase,
    private val entitlementProvider: PremiumEntitlementProvider
) : ViewModel() {
    private val selectedCategory = MutableStateFlow<ProgramCategory?>(null)
    private val message = MutableStateFlow<String?>(null)
    private val isStarting = MutableStateFlow(false)

    val uiState: StateFlow<ProgramsUiState> = combine(
        repository.observePrograms(), selectedCategory, entitlementProvider.premiumEntitlement,
        message, isStarting
    ) { progress, category, premium, msg, starting ->
        ProgramsUiState(category, progress, premium, starting, msg)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProgramsUiState())

    val catalog = ProgramCatalog.all

    init { entitlementProvider.refreshEntitlement() }

    fun selectCategory(category: ProgramCategory?) { selectedCategory.value = category }
    fun clearMessage() { message.value = null }

    fun startProgram(templateId: String, startEpochDay: Long, preferences: ProgramPreferences) {
        viewModelScope.launch {
            isStarting.value = true
            message.value = null
            runCatching { startProgramUseCase(templateId, startEpochDay, preferences) }
                .onFailure { message.value = it.message ?: "Could not start this program." }
            isStarting.value = false
        }
    }

    fun setActivityStatus(activityId: String, status: ProgramActivityStatus) {
        viewModelScope.launch {
            runCatching { setActivityStatusUseCase(activityId, status) }
                .onFailure { message.value = "Progress was not saved. Please try again." }
        }
    }

    fun setEnrollmentStatus(enrollmentId: String, status: ProgramStatus) {
        viewModelScope.launch {
            runCatching { setProgramStatusUseCase(enrollmentId, status) }
                .onFailure { message.value = "Could not update the program status." }
        }
    }

    fun deleteEnrollment(enrollmentId: String) {
        viewModelScope.launch {
            runCatching { deleteProgramUseCase(enrollmentId) }
                .onFailure { message.value = "Could not remove this program." }
        }
    }
}
