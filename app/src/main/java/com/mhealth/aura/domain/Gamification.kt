package com.mhealth.aura.domain

import com.mhealth.aura.data.db.entity.DoseLogEntity
import com.mhealth.aura.data.db.entity.MedicationEntity
import java.util.Calendar

data class SafetyBadge(
    val title: String,
    val description: String,
    val earned: Boolean
)

data class GamificationSummary(
    val safetyPoints: Int,
    val earnedBadges: Int,
    val totalBadges: Int,
    val badges: List<SafetyBadge>
)

object GamificationRules {
    fun calculate(
        medications: List<MedicationEntity>,
        logs: List<DoseLogEntity>
    ): GamificationSummary {
        val plannedDoses = medications.sumOf { medication ->
            courseDays(medication) * DoseSchedule.slotsFor(
                medication.frequency,
                medication.doseTimesCsv
            ).size
        }
        val uniqueLoggedSlots = logs
            .map { "${it.medicationId}:${startOfDay(it.dateMillis)}:${it.doseLabel}:${it.status}" }
            .distinct()
            .size
            .coerceAtMost(plannedDoses.coerceAtLeast(0))
        val taken = logs.count { it.status == "taken" }
        val recordedDays = logs.map { startOfDay(it.dateMillis) }.distinct().size
        val allPlannedLogged = plannedDoses > 0 && uniqueLoggedSlots >= plannedDoses

        val badges = listOf(
            SafetyBadge(
                title = "Schedule Builder",
                description = "Added at least one prescribed antibiotic schedule.",
                earned = medications.isNotEmpty()
            ),
            SafetyBadge(
                title = "First Dose Logged",
                description = "Recorded the first scheduled dose.",
                earned = logs.isNotEmpty()
            ),
            SafetyBadge(
                title = "Consistency Starter",
                description = "Logged doses on two different course days.",
                earned = recordedDays >= 2
            ),
            SafetyBadge(
                title = "Course Completer",
                description = "Every planned dose slot has been recorded as taken or missed.",
                earned = allPlannedLogged
            ),
            SafetyBadge(
                title = "AMR Safety Learner",
                description = "Review the AMR learning cards and quiz prompts before finishing the course.",
                earned = false
            )
        )

        val safeLoggedDosePoints = uniqueLoggedSlots * 5
        val schedulePoints = if (medications.isNotEmpty()) 20 else 0
        val consistencyPoints = if (recordedDays >= 2) 15 else 0
        val completionPoints = if (allPlannedLogged) 25 else 0
        val safetyPoints = schedulePoints + safeLoggedDosePoints + consistencyPoints + completionPoints

        return GamificationSummary(
            safetyPoints = safetyPoints,
            earnedBadges = badges.count { it.earned },
            totalBadges = badges.size,
            badges = badges
        )
    }

    fun safetyNote(taken: Int): String {
        return if (taken == 0) {
            "Points start after you record the prescribed schedule. Extra antibiotic doses never earn points."
        } else {
            "Points reward correct logging and learning only. Never take extra doses to maintain a badge."
        }
    }

    private fun courseDays(medication: MedicationEntity): Int {
        return ((((medication.endDateMillis - medication.startDateMillis)
            .coerceAtLeast(0L) / 86_400_000L) + 1).toInt()).coerceIn(1, 90)
    }

    private fun startOfDay(value: Long): Long = Calendar.getInstance().apply {
        timeInMillis = value
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}
