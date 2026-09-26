package com.navora.app.data

import com.navora.app.model.TripDraft
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class InMemoryTripRepositoryTest {
    @Test
    fun goaScenario_movesAnjunaIndoorsUntilApplied() {
        val repository = InMemoryTripRepository()
        val snapshot = repository.snapshot.value
        val goa = snapshot.trips.first { it.id == "goa" }
        val scenario = snapshot.scenarios.first { it.tripId == "goa" }
        val day = goa.days.first { it.dayNumber == scenario.dayNumber }

        assertEquals(4, goa.days.size)
        assertEquals("Aarav", snapshot.profile.name)
        assertEquals("Heavy rain.", scenario.changeLabel)
        assertEquals("Anjuna Beach", scenario.affectedTitle)
        assertEquals(scenario.before.map { it.title }, day.activities.map { it.title })
        assertTrue(scenario.before.any { it.title == "Anjuna Beach" })
        assertTrue(scenario.after.any { it.title == "Assagao indoor studio and cafe" })
        assertTrue(scenario.after.any { it.title == "Dinner" || it.title == "Vegetarian dinner" })
        assertTrue(scenario.unchanged.contains("Breakfast"))
        assertFalse(scenario.applied)

        repository.applyAdaptation("goa")
        val updated = repository.snapshot.value
        val updatedDay = updated.trips.first { it.id == "goa" }.days.first { it.dayNumber == 2 }
        assertEquals(scenario.after.map { it.title }, updatedDay.activities.map { it.title })
        assertTrue(updated.scenarios.first { it.tripId == "goa" }.applied)
        assertTrue(updated.trips.first { it.id == "goa" }.adaptationApplied)
    }

    @Test
    fun generateTrip_buildsOneDayPerSelectedDate() {
        val repository = InMemoryTripRepository()
        val fourDayStart = LocalDate.of(2027, 6, 10)
        val fourDays = repository.generateTrip(draft("Goa", fourDayStart, LocalDate.of(2027, 6, 13)))
        assertEquals(4, fourDays.days.size)
        assertEquals(listOf(1, 2, 3, 4), fourDays.days.map { it.dayNumber })
        assertEquals("Arrival and Panjim", fourDays.days[0].title)
        assertEquals("North Goa", fourDays.days[1].title)
        assertEquals("Dudhsagar", fourDays.days[2].title)
        assertEquals("South Goa and departure", fourDays.days[3].title)
        assertEquals("India", fourDays.country)
        assertTrue(fourDays.days[0].activities.any { it.title == "Mumbai → Madgaon" })
        assertTrue(fourDays.days.flatMap { it.activities }.none { it.note.contains("Demo", ignoreCase = true) })

        val tenDayStart = LocalDate.of(2027, 7, 1)
        val tenDays = repository.generateTrip(draft("Goa", tenDayStart, LocalDate.of(2027, 7, 10)))
        assertEquals(10, tenDays.days.size)
        assertEquals((1..10).toList(), tenDays.days.map { it.dayNumber })
    }

    @Test
    fun preferences_changeGoaAndAccessibilityCanBeResolved() {
        val repository = InMemoryTripRepository()
        val seeded = repository.snapshot.value.trips.first { it.id == "goa" }
        val fort = seeded.days[1].activities.first { it.title == "Fort Aguada" }
        assertEquals("May not match your preference to avoid stairs.", fort.conflict)

        repository.chooseAlternative("goa", fort.id)
        val replaced = repository.snapshot.value.trips.first { it.id == "goa" }.days[1].activities
            .first { it.id == fort.id }
        assertEquals("Sinquerim lower promenade", replaced.title)
        assertEquals(null, replaced.conflict)

        val adventure = repository.generateTrip(
            draft("Goa", LocalDate.of(2027, 8, 1), LocalDate.of(2027, 8, 4)).copy(
                pace = "Fast-paced",
                interests = listOf("Adventure", "Nature"),
                food = "No strong preference",
                mobility = "No special requirements",
                accessibility = emptyList(),
            ),
        )
        assertEquals("Candolim kayak", adventure.days[1].activities.first { it.time == "12:00" }.title)
        assertTrue(adventure.days[1].activities.none { it.conflict != null })
        assertEquals(4, adventure.days.size)
    }

    @Test
    fun events_followTheDestinationAndOnlyReplaceTheAffectedStop() {
        val repository = InMemoryTripRepository()
        val jaipur = repository.generateTrip(draft("Jaipur", LocalDate.of(2027, 11, 12), LocalDate.of(2027, 11, 14)))
        val heat = repository.snapshot.value.scenarios.first { it.tripId == jaipur.id }
        assertEquals("Extreme heat.", heat.changeLabel)
        assertFalse(heat.affectedTitle.contains("Anjuna"))
        assertTrue(heat.after.any { it.title.contains("City Palace") || it.title.contains("Museum") || it.title.contains("Bazaar") })
        assertTrue(heat.unchanged.size >= 2)

        repository.adaptTo(jaipur.id, "FeelingSick")
        val sick = repository.snapshot.value.scenarios.first { it.tripId == jaipur.id }
        assertEquals("I'm feeling sick.", sick.changeLabel)
        assertTrue(sick.after.any { it.title == "Rest at your stay" })
        assertTrue(sick.before.count { it.kind.name == "Meal" } == sick.after.count { it.title in sick.before.filter { a -> a.kind.name == "Meal" }.map { it.title } })

        val goa = repository.snapshot.value.trips.first { it.id == "goa" }
        repository.adaptTo(goa.id, "FlightDelay")
        val delay = repository.snapshot.value.scenarios.first { it.tripId == "goa" }
        assertEquals(1, delay.dayNumber)
        assertEquals("Flight delayed.", delay.changeLabel)
        assertFalse(delay.affectedTitle.contains("Anjuna"))
    }

    private fun draft(city: String, start: LocalDate, end: LocalDate) = TripDraft(
        destination = city,
        startDate = start.toString(),
        endDate = end.toString(),
        startEpochDay = start.toEpochDay(),
        endEpochDay = end.toEpochDay(),
        travelers = 2,
        styles = listOf("Coast"),
        budget = "Balanced",
        interests = listOf("Heritage"),
        notes = "",
    )
}
