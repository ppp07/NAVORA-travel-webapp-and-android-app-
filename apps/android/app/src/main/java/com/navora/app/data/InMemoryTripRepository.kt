package com.navora.app.data

import com.navora.app.model.AdaptiveScenario
import com.navora.app.model.DemoSnapshot
import com.navora.app.model.Destination
import com.navora.app.model.DestinationTone
import com.navora.app.model.TravelerProfile
import com.navora.app.model.Trip
import com.navora.app.model.TripEvent
import com.navora.app.model.TripDraft
import com.navora.app.model.TripPreferences
import com.navora.app.model.TripStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate

class InMemoryTripRepository : TripRepository {
    private val trips = mutableListOf<Trip>()
    private val scenarios = mutableListOf<AdaptiveScenario>()
    private var profile = TravelerProfile(
        name = "Aarav",
        email = "aarav.mehta@gmail.com",
        phone = "+91 98200 44118",
        city = "Mumbai",
        birthDate = "12 Mar 1997",
        language = "English",
        budget = "Moderate",
        style = "Balanced",
        interests = listOf("Food", "Culture", "Local experiences"),
        food = "Local cuisine",
        accommodation = "Comfortable hotel",
        transport = "Train",
        activityLevel = "Moderate",
        socialStyle = "Couple",
        priorities = listOf("Local experiences", "Maximum comfort"),
        mobility = "Prefer shorter walking distances",
        walking = "Minimal",
        rest = "Frequent breaks",
        accessibility = listOf("Avoid stairs"),
        pacingNeeds = listOf("Longer activity windows"),
        favoriteDestinations = listOf("Goa", "Jaipur", "Paris"),
        notificationsEnabled = true,
    )
    private var nextId = 1
    private val destinations = seedDestinations()
    private val _snapshot = MutableStateFlow(currentSnapshot())
    override val snapshot: StateFlow<DemoSnapshot> = _snapshot.asStateFlow()

    init {
        trips += plannedTrip(
            id = "goa",
            title = "Goa Getaway",
            destination = "Goa",
            start = LocalDate.of(2026, 9, 24),
            end = LocalDate.of(2026, 9, 27),
            travelers = 2,
            budget = "Balanced",
            styles = listOf("Food", "Culture"),
            interests = listOf("Food", "Culture", "Local experiences"),
            status = TripStatus.InProgress,
            progress = 0.62f,
            tone = DestinationTone.Peach,
            routeSummary = "Panaji • North Goa • South Goa",
        )
        trips += plannedTrip(
            id = "jaipur",
            title = "Jaipur long weekend",
            destination = "Jaipur",
            start = LocalDate.of(2026, 11, 12),
            end = LocalDate.of(2026, 11, 14),
            travelers = 2,
            budget = "Value",
            styles = listOf("Culture"),
            interests = listOf("Forts", "Markets"),
            status = TripStatus.Upcoming,
            progress = 0.1f,
            tone = DestinationTone.Coral,
            routeSummary = "Amber • Old City • Johari Bazaar",
        )
        trips += plannedTrip(
            id = "kerala",
            title = "Kerala, already taken",
            destination = "Kerala",
            start = LocalDate.of(2026, 8, 2),
            end = LocalDate.of(2026, 8, 6),
            travelers = 2,
            budget = "Comfortable",
            styles = listOf("Relaxation"),
            interests = listOf("Backwaters"),
            status = TripStatus.Completed,
            progress = 1f,
            tone = DestinationTone.Mint,
            routeSummary = "Kochi • Alleppey • Munnar",
        )
        trips += plannedTrip(
            id = "manali",
            title = "Manali in December",
            destination = "Manali",
            start = LocalDate.of(2026, 12, 18),
            end = LocalDate.of(2026, 12, 22),
            travelers = 3,
            budget = "Balanced",
            styles = listOf("Nature"),
            interests = listOf("Hills"),
            status = TripStatus.Upcoming,
            progress = 0.05f,
            tone = DestinationTone.Blue,
            routeSummary = "Old Manali • Solang • Mall Road",
        )
        publish()
    }

    override fun generateTrip(draft: TripDraft): Trip {
        val id = "trip-$nextId"
        nextId += 1
        val match = destinations.firstOrNull {
            it.city.equals(draft.destination.trim(), ignoreCase = true)
        }
        val city = match?.city ?: draft.destination.trim()
        val start = LocalDate.ofEpochDay(draft.startEpochDay)
        val end = LocalDate.ofEpochDay(draft.endEpochDay)
        val trip = plannedTrip(
            id = id,
            title = if (city.equals("Goa", true)) "Goa Getaway" else "$city, ${draft.styles.firstOrNull() ?: "your pace"}",
            destination = city,
            start = start,
            end = end,
            travelers = draft.travelers,
            budget = draft.budget,
            styles = draft.interests.ifEmpty { draft.styles }.take(3),
            interests = draft.interests.ifEmpty { listOf("Culture") },
            status = TripStatus.Upcoming,
            progress = 0.08f,
            tone = match?.tone ?: DestinationTone.Blue,
            routeSummary = if (city.equals("Goa", true)) "Panaji • North Goa • South Goa" else city,
            preferences = draft.toPreferences(),
            origin = draft.origin.ifBlank { "Mumbai" },
            country = match?.country ?: "India",
        )
        trips.add(0, trip)
        publish()
        return trip
    }

    override fun applyAdaptation(tripId: String) {
        val scenarioIndex = scenarios.indexOfFirst { it.tripId == tripId && !it.applied }
        val tripIndex = trips.indexOfFirst { it.id == tripId }
        if (scenarioIndex < 0 || tripIndex < 0) return
        val scenario = scenarios[scenarioIndex]
        val trip = trips[tripIndex]
        val chosen = scenario.options.getOrNull(scenario.selectedOption)
            ?: if (scenario.selectedOption == 0 || scenario.alternateAfter.isEmpty()) scenario.after else scenario.alternateAfter
        trips[tripIndex] = trip.copy(
            adaptationApplied = true,
            days = trip.days.map { day ->
                if (day.dayNumber != scenario.dayNumber) {
                    day
                } else {
                    day.copy(
                        activities = mergeKeptChoices(scenario.before, day.activities, chosen),
                        recommendation = scenario.adaptationDetail,
                    )
                }
            },
        )
        scenarios[scenarioIndex] = scenario.copy(applied = true)
        publish()
    }

    override fun selectTradeOff(tripId: String, option: Int) {
        val index = scenarios.indexOfFirst { it.tripId == tripId && !it.applied }
        if (index < 0) return
        val limit = (scenarios[index].options.size - 1).coerceAtLeast(0)
        scenarios[index] = scenarios[index].copy(selectedOption = option.coerceIn(0, limit))
        publish()
    }

    override fun chooseAlternative(tripId: String, activityId: String) {
        val tripIndex = trips.indexOfFirst { it.id == tripId }
        if (tripIndex < 0) return
        val trip = trips[tripIndex]
        trips[tripIndex] = trip.copy(
            days = trip.days.map { day ->
                day.copy(
                    activities = day.activities.map { activity ->
                        if (activity.id != activityId || activity.alternativeTitle == null) {
                            activity
                        } else {
                            activity.copy(
                                title = activity.alternativeTitle,
                                place = activity.alternativePlace ?: activity.place,
                                note = activity.alternativeNote ?: activity.note,
                                reason = activity.alternativeReason ?: activity.reason,
                                conflict = null,
                                alternativeTitle = null,
                            )
                        }
                    },
                )
            },
        )
        publish()
    }

    override fun adaptTo(tripId: String, eventName: String) {
        val tripIndex = trips.indexOfFirst { it.id == tripId }
        if (tripIndex < 0) return
        val event = TripEvent.entries.firstOrNull { it.name == eventName } ?: return
        val trip = trips[tripIndex].copy(adaptationApplied = false)
        trips[tripIndex] = trip
        scenarios.removeAll { it.tripId == tripId }
        scenarios += AdaptationEngine.plan(trip, event, trip.preferences)
        publish()
    }

    override fun updateProfile(profile: TravelerProfile) {
        this.profile = profile
        publish()
    }

    private fun plannedTrip(
        id: String,
        title: String,
        destination: String,
        start: LocalDate,
        end: LocalDate,
        travelers: Int,
        budget: String,
        styles: List<String>,
        interests: List<String>,
        status: TripStatus,
        progress: Float,
        tone: DestinationTone,
        routeSummary: String,
        preferences: com.navora.app.model.TripPreferences = profile.toPreferences(),
        origin: String = "Mumbai",
        country: String = "India",
    ): Trip {
        val days = ItineraryPlanner.buildDays(id, destination, start, end, preferences, origin)
        val trip = Trip(
            id = id,
            title = title,
            destination = destination,
            country = country,
            startDate = ItineraryPlanner.formatTripDate(start),
            endDate = ItineraryPlanner.formatTripDate(end),
            travelers = travelers,
            budget = budget,
            styles = styles,
            interests = interests,
            status = status,
            progress = progress,
            tone = tone,
            routeSummary = routeSummary,
            origin = origin,
            preferences = preferences,
            days = days,
        )
        scenarios += ItineraryPlanner.scenarioFor(trip, preferences)
        return trip
    }

    private fun mergeKeptChoices(
        before: List<com.navora.app.model.TripActivity>,
        live: List<com.navora.app.model.TripActivity>,
        proposed: List<com.navora.app.model.TripActivity>,
    ): List<com.navora.app.model.TripActivity> {
        val beforeById = before.associateBy { it.id }
        val liveById = live.associateBy { it.id }
        return proposed.map { next ->
            val earlier = beforeById[next.id]
            val current = liveById[next.id]
            if (current != null && earlier != null && current.title != earlier.title) current else next
        }
    }

    private fun publish() {
        _snapshot.value = currentSnapshot()
    }

    private fun currentSnapshot(): DemoSnapshot = DemoSnapshot(
        trips = trips.toList(),
        destinations = destinations,
        scenarios = scenarios.toList(),
        profile = profile,
    )

    private fun seedDestinations(): List<Destination> = listOf(
        Destination("goa", "Goa", "India", "Culture", "Panjim, the north beaches, Dudhsagar, and a last morning in the south.", DestinationTone.Peach, true, false, listOf("food", "culture", "beach")),
        Destination("jaipur", "Jaipur", "India", "Culture", "Amber, the old city, and bazaars that can move indoors.", DestinationTone.Coral, false, false, listOf("culture", "food", "history")),
        Destination("kerala", "Kerala", "India", "Nature", "Kochi, the backwaters, and room to slow the next day.", DestinationTone.Mint, false, false, listOf("nature", "food", "relaxation")),
        Destination("udaipur", "Udaipur", "India", "Culture", "Lake mornings and a palace that does not need a second transfer.", DestinationTone.Lavender, false, false, listOf("culture", "relaxation")),
        Destination("paris", "Paris", "France", "Culture", "Le Marais, the river, and meals that stay in one neighborhood.", DestinationTone.Lavender, false, false, listOf("culture", "food", "architecture")),
        Destination("tokyo", "Tokyo", "Japan", "Food", "Asakusa, Yanaka, and evenings that do not cross the city twice.", DestinationTone.Blue, true, false, listOf("food", "culture")),
        Destination("manali", "Manali", "India", "Adventure", "Old Manali and Solang, with an afternoon the weather can move.", DestinationTone.Blue, true, false, listOf("adventure", "nature")),
        Destination("rishikesh", "Rishikesh", "India", "Nature", "The ghats and a day that can stay on one bank of the river.", DestinationTone.Mint, false, false, listOf("nature", "adventure", "spiritual")),
        Destination("kashmir", "Kashmir", "India", "Nature", "Dal Lake and the gardens, paced for the weather.", DestinationTone.Blue, false, false, listOf("nature", "relaxation")),
        Destination("mumbai", "Mumbai", "India", "Culture", "The Gateway, Colaba, and Marine Drive as one walkable arrival day.", DestinationTone.Blue, true, false, listOf("city", "food")),
        Destination("dubai", "Dubai", "UAE", "City", "Creek, Al Fahidi, and a day that can stay indoors.", DestinationTone.Peach, true, false, listOf("city", "architecture")),
        Destination("singapore", "Singapore", "Singapore", "City", "Gardens, Chinatown, and short connections between them.", DestinationTone.Mint, true, false, listOf("city", "food")),
        Destination("bali", "Bali", "Indonesia", "Nature", "Ubud in the morning and the coast only if the day stays open.", DestinationTone.Peach, true, false, listOf("beach", "nature", "wellness")),
        Destination("bangkok", "Bangkok", "Thailand", "Food", "The river, the palace quarter, and meals close together.", DestinationTone.Coral, true, false, listOf("city", "food")),
        Destination("london", "London", "United Kingdom", "Culture", "The South Bank and one neighborhood, not the whole city.", DestinationTone.Blue, true, false, listOf("city", "culture")),
        Destination("switzerland", "Switzerland", "Switzerland", "Nature", "Lucerne and one lake day, with weather room in the afternoon.", DestinationTone.Mint, true, false, listOf("nature", "adventure")),
    )
}

private fun TripDraft.toPreferences() = TripPreferences(
    pace = pace,
    interests = interests,
    food = food,
    budget = budget,
    accommodation = accommodation,
    mobility = mobility,
    accessibility = accessibility,
    pacingNeeds = pacingNeeds,
    walking = walking,
    rest = rest,
)
