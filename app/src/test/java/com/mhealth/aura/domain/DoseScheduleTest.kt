package com.mhealth.aura.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class DoseScheduleTest {
    @Test
    fun supportedFrequenciesCreateExpectedDoseCounts() {
        assertEquals(1, DoseSchedule.slotsFor("OD").size)
        assertEquals(2, DoseSchedule.slotsFor("BD").size)
        assertEquals(3, DoseSchedule.slotsFor("TID").size)
        assertEquals(4, DoseSchedule.slotsFor("QID").size)
    }

    @Test
    fun unknownFrequencyFallsBackToTwiceDaily() {
        assertEquals(DoseSchedule.slotsFor("BD"), DoseSchedule.slotsFor("unknown"))
    }

    @Test
    fun twiceDailyUsesMorningAndEveningLabels() {
        assertEquals(
            listOf("Morning Dose", "Evening Dose"),
            DoseSchedule.slotsFor("BD").map(DoseSlot::label)
        )
    }

    @Test
    fun customTimesReplaceTheMatchingDoseSlots() {
        val slots = DoseSchedule.slotsFor("BD", "06:30,22:15")

        assertEquals(listOf(6, 22), slots.map(DoseSlot::hour))
        assertEquals(listOf(30, 15), slots.map(DoseSlot::minute))
    }

    @Test
    fun invalidCustomTimeFallsBackWithoutShiftingLaterSlots() {
        val slots = DoseSchedule.slotsFor("BD", "invalid,21:45")

        assertEquals(8, slots[0].hour)
        assertEquals(0, slots[0].minute)
        assertEquals(21, slots[1].hour)
        assertEquals(45, slots[1].minute)
    }
}
