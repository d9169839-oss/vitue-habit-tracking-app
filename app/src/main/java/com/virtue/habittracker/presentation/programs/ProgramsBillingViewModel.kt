package com.virtue.habittracker.presentation.programs

import androidx.lifecycle.ViewModel
import com.virtue.habittracker.data.billing.PremiumBillingManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * ViewModel orchestrating Google Play billing interactions for the Programs tab.
 *
 * Extracted into its own file to maintain Single Responsibility and keep UI files focused.
 */
@HiltViewModel
class ProgramsBillingViewModel @Inject constructor(
    val manager: PremiumBillingManager
) : ViewModel()
