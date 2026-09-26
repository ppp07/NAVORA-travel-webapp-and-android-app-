package com.navora.app.model

enum class TripStatus {
    InProgress,
    Upcoming,
    Completed,
}

enum class ActivityKind {
    Meal,
    Place,
    Transit,
    Experience,
}

enum class DestinationTone {
    Blue,
    Lavender,
    Peach,
    Mint,
    Coral,
}

data class TripActivity(
    val id: String,
    val time: String,
    val title: String,
    val place: String,
    val duration: String,
    val kind: ActivityKind,
    val note: String,
    val reason: String = "",
    val conflict: String? = null,
    val alternativeTitle: String? = null,
    val alternativePlace: String? = null,
    val alternativeNote: String? = null,
    val alternativeReason: String? = null,
)

data class ItineraryDay(
    val dayNumber: Int,
    val dateLabel: String,
    val title: String,
    val activities: List<TripActivity>,
    val recommendation: String?,
)

data class Trip(
    val id: String,
    val title: String,
    val destination: String,
    val country: String,
    val startDate: String,
    val endDate: String,
    val travelers: Int,
    val budget: String,
    val styles: List<String>,
    val interests: List<String>,
    val status: TripStatus,
    val progress: Float,
    val tone: DestinationTone,
    val routeSummary: String = "",
    val origin: String = "Mumbai",
    val preferences: TripPreferences = TripPreferences(),
    val days: List<ItineraryDay>,
    val adaptationApplied: Boolean = false,
)

data class TripDraft(
    val destination: String,
    val startDate: String,
    val endDate: String,
    val startEpochDay: Long,
    val endEpochDay: Long,
    val travelers: Int,
    val styles: List<String>,
    val budget: String,
    val interests: List<String>,
    val notes: String,
    val origin: String = "Mumbai",
    val pace: String = "Balanced",
    val food: String = "Local cuisine",
    val accommodation: String = "Comfortable hotel",
    val mobility: String = "No special requirements",
    val accessibility: List<String> = emptyList(),
    val pacingNeeds: List<String> = emptyList(),
    val walking: String = "Moderate",
    val rest: String = "Balanced",
)

data class Destination(
    val id: String,
    val city: String,
    val country: String,
    val category: String,
    val summary: String,
    val tone: DestinationTone,
    val popular: Boolean,
    val recommended: Boolean,
    val tags: List<String> = emptyList(),
)

data class AdaptiveScenario(
    val id: String,
    val tripId: String,
    val dayNumber: Int,
    val headline: String,
    val changeLabel: String,
    val changeDetail: String,
    val adaptationDetail: String,
    val before: List<TripActivity>,
    val after: List<TripActivity>,
    val alternateAfter: List<TripActivity> = emptyList(),
    val selectedOption: Int = 0,
    val ripple: RippleEffect = RippleEffect(),
    val tradeOffs: List<TradeOff> = emptyList(),
    val options: List<List<TripActivity>> = emptyList(),
    val unchanged: List<String> = emptyList(),
    val affectedTitle: String = "",
    val eventLabel: String = "",
    val accessibilityNote: String? = null,
    val applied: Boolean = false,
)

enum class TripEvent(val label: String) {
    HeavyRain("Heavy rain"),
    ExtremeHeat("Extreme heat"),
    Storm("Storm"),
    Snowfall("Snowfall"),
    FlightDelay("Flight delayed"),
    TrainDelay("Train delayed"),
    Traffic("Heavy traffic"),
    RoadClosure("Road closure"),
    AttractionClosed("Attraction closed"),
    Crowded("Attraction crowded"),
    FeelingSick("I'm feeling sick"),
    LowEnergy("I'm low on energy"),
    MoreRest("I need more rest"),
    SpendLess("I want to spend less"),
    SpendMore("I want to spend more"),
    MissedActivity("I missed an activity"),
    SlowerDay("I want a slower day"),
    AddActivities("I want to add more"),
}

data class RippleEffect(
    val activities: Int = 1,
    val travelLinks: Int = 2,
    val meals: Int = 1,
    val eveningPlans: Int = 1,
)

data class TradeOff(
    val label: String,
    val cost: String,
    val travel: String,
    val comfort: String,
    val summary: String,
)

data class TripPreferences(
    val pace: String = "Balanced",
    val interests: List<String> = emptyList(),
    val food: String = "Local cuisine",
    val budget: String = "Moderate",
    val accommodation: String = "Comfortable hotel",
    val mobility: String = "No special requirements",
    val accessibility: List<String> = emptyList(),
    val pacingNeeds: List<String> = emptyList(),
    val walking: String = "Moderate",
    val rest: String = "Balanced",
) {
    val adventure: Boolean
        get() = (pace == "Fast-paced" || pace == "Packed" || pace == "Fast") &&
            interests.contains("Adventure") && interests.contains("Nature")

    val avoidStairs: Boolean
        get() = "Avoid stairs" in accessibility ||
            mobility == "Limited walking" ||
            mobility == "Wheelchair-friendly options preferred"

    val shorterWalks: Boolean
        get() = mobility != "No special requirements" || avoidStairs

    val vegetarian: Boolean
        get() = food == "Vegetarian" || food == "Vegan"

    val relaxed: Boolean
        get() = pace == "Relaxed" || "Fewer activities per day" in pacingNeeds
}

data class TravelerProfile(
    val name: String,
    val email: String = "",
    val phone: String = "",
    val city: String,
    val birthDate: String = "",
    val language: String = "English",
    val budget: String,
    val style: String,
    val interests: List<String>,
    val food: String = "Local cuisine",
    val accommodation: String = "Comfortable hotel",
    val transport: String = "Combination",
    val activityLevel: String = "Moderate",
    val socialStyle: String = "Couple",
    val priorities: List<String> = emptyList(),
    val mobility: String = "No special requirements",
    val accessibility: List<String> = emptyList(),
    val pacingNeeds: List<String> = emptyList(),
    val walking: String = "Moderate",
    val rest: String = "Balanced",
    val favoriteDestinations: List<String> = emptyList(),
    val notificationsEnabled: Boolean,
) {
    fun toPreferences(): TripPreferences = TripPreferences(
        pace = style,
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

    fun dna(): List<Pair<String, Float>> {
        fun level(name: String, active: Boolean, high: Float, low: Float) = name to if (active) high else low
        return listOf(
            level("Food", "Food" in interests || food == "Local cuisine" || food == "Street food", 0.92f, 0.28f),
            level("Culture", "Culture" in interests || "History" in interests || "Architecture" in interests, 0.84f, 0.22f),
            level("Adventure", "Adventure" in interests || style == "Fast-paced", 0.78f, 0.3f),
            level("Relaxation", style == "Relaxed" || "Wellness" in interests || "Relaxation" in priorities, 0.7f, 0.42f),
        )
    }
}

data class DemoSnapshot(
    val trips: List<Trip>,
    val destinations: List<Destination>,
    val scenarios: List<AdaptiveScenario>,
    val profile: TravelerProfile,
)
