package com.virtue.habittracker.domain.usecase.program

import com.virtue.habittracker.domain.model.program.ProgramCatalog
import com.virtue.habittracker.domain.model.program.ProgramPreferences
import com.virtue.habittracker.domain.repository.PremiumEntitlementProvider
import com.virtue.habittracker.domain.repository.ProgramRepository
import javax.inject.Inject

class StartProgramUseCase @Inject constructor(
    private val repository: ProgramRepository,
    private val entitlementProvider: PremiumEntitlementProvider
) {
    suspend operator fun invoke(templateId: String, startEpochDay: Long, preferences: ProgramPreferences): String {
        val template = ProgramCatalog.find(templateId) ?: error("This program is not available.")
        if (template.isPremium && !entitlementProvider.premiumEntitlement.value) {
            error("This program requires an active Vitue Premium subscription.")
        }
        return repository.startProgram(templateId, startEpochDay, preferences)
    }
}
