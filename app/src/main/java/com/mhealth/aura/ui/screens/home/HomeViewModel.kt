package com.mhealth.aura.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mhealth.aura.data.db.entity.DoseLogEntity
import com.mhealth.aura.data.db.entity.MedicationEntity
import com.mhealth.aura.data.db.entity.UserEntity
import com.mhealth.aura.data.repository.DoseRepository
import com.mhealth.aura.data.repository.MedicationRepository
import com.mhealth.aura.data.repository.UserRepository
import com.mhealth.aura.domain.DoseSchedule
import com.mhealth.aura.domain.GamificationRules
import com.mhealth.aura.domain.GamificationSummary
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class HomeUiState(
    val user: UserEntity? = null,
    val medications: List<MedicationEntity> = emptyList(),
    val todayLogs: List<DoseLogEntity> = emptyList(),
    val totalTaken: Int = 0,
    val dayNumber: Int = 1,
    val adherencePct: Float = 0f,
    val courseProgressPct: Float = 0f,
    val streakDays: Int = 0,
    val gamification: GamificationSummary = GamificationRules.calculate(emptyList(), emptyList()),
    val todayLabel: String = ""
)

class HomeViewModel(
    private val userRepo: UserRepository,
    private val medicationRepo: MedicationRepository,
    private val doseRepo: DoseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                userRepo.user,
                medicationRepo.medications,
                doseRepo.allLogs,
                doseRepo.totalTaken
            ) { user, medications, logs, taken ->
                val now = System.currentTimeMillis()
                val sdf = SimpleDateFormat("EEEE, d MMM yyyy", Locale.getDefault())
                val primary = medications.firstOrNull { it.isActive } ?: medications.firstOrNull()
                val dayNum = if (primary != null) {
                    val diff = (now - primary.startDateMillis) / 86_400_000L
                    (diff + 1).toInt().coerceAtLeast(1)
                } else 1
                val totalDoses = medications.sumOf { medication ->
                    val days = (
                        ((medication.endDateMillis - medication.startDateMillis)
                            .coerceAtLeast(0L) / 86_400_000L) + 1
                        ).toInt()
                    days * DoseSchedule.slotsFor(
                        medication.frequency,
                        medication.doseTimesCsv
                    ).size
                }
                val primaryDays = primary?.let {
                    (((it.endDateMillis - it.startDateMillis).coerceAtLeast(0L) /
                        86_400_000L) + 1).toInt()
                } ?: 1
                val coursePct = if (primary != null) dayNum.toFloat() / primaryDays else 0f
                val adherence = if (totalDoses > 0) taken.toFloat() / totalDoses else 0f
                HomeUiState(
                    user = user,
                    medications = medications,
                    totalTaken = taken,
                    dayNumber = dayNum,
                    adherencePct = adherence.coerceIn(0f, 1f),
                    courseProgressPct = coursePct.coerceIn(0f, 1f),
                    streakDays = user?.adherenceStreakDays ?: 0,
                    gamification = GamificationRules.calculate(medications, logs),
                    todayLabel = sdf.format(Date(now)),
                    todayLogs = logs.filter { log ->
                        val cal = Calendar.getInstance()
                        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
                        log.dateMillis >= cal.timeInMillis
                    }
                )
            }.collect { _uiState.value = it }
        }
    }

    fun markDoseTaken(medication: MedicationEntity, label: String) {
        viewModelScope.launch {
            doseRepo.logDose(medication.id, medication.name, label, "taken")
        }
    }

    companion object {
        fun factory(
            userRepo: UserRepository,
            medicationRepo: MedicationRepository,
            doseRepo: DoseRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return HomeViewModel(userRepo, medicationRepo, doseRepo) as T
            }
        }
    }
}
