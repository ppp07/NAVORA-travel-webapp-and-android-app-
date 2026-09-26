package com.navora.app.data

import com.navora.app.model.ActivityKind
import com.navora.app.model.AdaptiveScenario
import com.navora.app.model.ItineraryDay
import com.navora.app.model.RippleEffect
import com.navora.app.model.TradeOff
import com.navora.app.model.Trip
import com.navora.app.model.TripActivity
import com.navora.app.model.TripPreferences
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

internal object ItineraryPlanner {
    private val dayFormat = DateTimeFormatter.ofPattern("EEE d MMM")
    private val tripFormat = DateTimeFormatter.ofPattern("d MMM yyyy")

    fun formatTripDate(date: LocalDate): String = date.format(tripFormat)

    fun inclusiveDays(start: LocalDate, end: LocalDate): Int {
        return ChronoUnit.DAYS.between(start, end).toInt() + 1
    }

    fun buildDays(
        tripId: String,
        destination: String,
        start: LocalDate,
        end: LocalDate,
        preferences: TripPreferences,
        origin: String = "Mumbai",
    ): List<ItineraryDay> {
        val count = inclusiveDays(start, end)
        return List(count) { index ->
            val date = start.plusDays(index.toLong())
            if (destination.equals("Goa", ignoreCase = true)) {
                goaDay(tripId, index, count, date, preferences, origin)
            } else {
                cityDay(tripId, destination, index, count, date, preferences, origin)
            }
        }
    }

    fun scenarioFor(trip: Trip, preferences: TripPreferences): AdaptiveScenario {
        return AdaptationEngine.plan(trip, DestinationCatalog.defaultEvent(trip.destination), preferences)
    }

    private fun goaDay(
        tripId: String,
        index: Int,
        count: Int,
        date: LocalDate,
        preferences: TripPreferences,
        origin: String,
    ): ItineraryDay {
        val last = index == count - 1
        val (title, recommendation, activities) = when {
            index == 0 -> Triple(
                "Arrival and Panjim",
                "Stay on the Mandovi side tonight. North Goa starts tomorrow without a second transfer.",
                arrivalPanjim(tripId, preferences, origin),
            )
            last -> Triple(
                "South Goa and departure",
                "Palolem in the morning, then one ride back to Madgaon. Lunch stays in Margao so the departure is unhurried.",
                southAndDeparture(tripId, preferences),
            )
            index == 1 -> Triple(
                "North Goa",
                "The afternoon is the flexible part of the coast. Anjuna and Vagator can move if the weather turns, and dinner stays.",
                northGoa(tripId, preferences),
            )
            index == 2 && count > 3 -> Triple(
                "Dudhsagar",
                "Leave early. The waterfall only works if you are back toward South Goa before evening.",
                dudhsagar(tripId, preferences),
            )
            index == 3 && !last -> Triple(
                "A slower south coast day",
                "Palolem and Colva without a departure. The train home is on the last day.",
                southLeisure(tripId, preferences),
            )
            else -> extraGoa(tripId, index, preferences)
        }
        return ItineraryDay(
            dayNumber = index + 1,
            dateLabel = date.format(dayFormat),
            title = title,
            activities = activities,
            recommendation = recommendation,
        )
    }

    private fun arrivalPanjim(tripId: String, preferences: TripPreferences, origin: String): List<TripActivity> {
        val heritage = if (preferences.adventure) {
            act("$tripId-d1-fontainhas", "12:00", "Fontainhas, a short pass", "Fontainhas, Panaji", "40 min", ActivityKind.Place, "A quick look, then the coast. The longer heritage walk can wait.", "Fits a faster pace")
        } else {
            act("$tripId-d1-fontainhas", "12:00", "Fontainhas heritage walk", "Fontainhas, Panaji", "80 min", ActivityKind.Place, "Latin quarter lanes. Old Goa comes next, not another neighborhood.", "Matches your interest in local culture")
        }
        return listOf(
            act("$tripId-d1-rail", "08:15", "$origin → Madgaon", "Madgaon", "Arrival · 08:15 AM", ActivityKind.Transit, "Train from $origin, then a short ride into Panaji.", "Fits the way you like to arrive"),
            act("$tripId-d1-hotel", "10:00", "Hotel check-in", "Panaji", "40 min", ActivityKind.Transit, "Bags down in Panjim before Fontainhas, so the rest of the day is on foot.", "Matches your stay preference"),
            act("$tripId-d1-breakfast", "10:45", mealName(preferences, "Breakfast in Panjim", "Vegetarian breakfast in Panjim"), "Fontainhas", "50 min", ActivityKind.Meal, "A short walk from the hotel, so the lanes start on foot.", foodReason(preferences)),
            heritage,
            act("$tripId-d1-basilica", "14:30", "Basilica of Bom Jesus", "Old Goa", "75 min", ActivityKind.Place, "One ride from Panjim. See the basilica, then return before the evening crowd.", "Matches your interest in history and culture"),
            act("$tripId-d1-panjim", "16:30", "Panjim exploration", "Panaji", "90 min", ActivityKind.Experience, "Church Square and the river road, still inside the city.", "Shorter travel distance"),
            act("$tripId-d1-mandovi", "18:30", "Mandovi waterfront", "Mandovi riverfront", "60 min", ActivityKind.Experience, "Evening on the promenade before dinner.", "Fits your preferred travel pace"),
            act("$tripId-d1-dinner", "20:00", mealName(preferences, "Goan dinner in Panjim", "Vegetarian Goan dinner"), "Panaji", "90 min", ActivityKind.Meal, "Close the day where you are staying. North Goa is tomorrow.", foodReason(preferences)),
        )
    }

    private fun northGoa(tripId: String, preferences: TripPreferences): List<TripActivity> {
        val aguada = if (preferences.avoidStairs) {
            act(
                "$tripId-d2-aguada", "10:00", "Fort Aguada", "Candolim", "90 min", ActivityKind.Place,
                "The fort is the classic morning stop, but the climb is stairs.",
                reason = "",
                conflict = "May not match your preference to avoid stairs.",
                alternativeTitle = "Sinquerim lower promenade",
                alternativePlace = "Sinquerim",
                alternativeNote = "The same stretch of coast, from the lower road, without the climb.",
                alternativeReason = "Shorter walking distance, and no stairs",
            )
        } else {
            act("$tripId-d2-aguada", "10:00", "Fort Aguada", "Candolim", "90 min", ActivityKind.Place, "The fort, then the beach below it.", "Fits your preferred travel pace")
        }
        val midday = if (preferences.adventure) {
            act("$tripId-d2-candolim", "12:00", "Candolim kayak", "Candolim", "70 min", ActivityKind.Experience, "On the water in front of the fort, then lunch before the ride north.", "Matches your interest in adventure")
        } else {
            act("$tripId-d2-candolim", "12:00", "Candolim beach", "Candolim", "70 min", ActivityKind.Place, "A short walk from the fort. Leave in time for lunch.", "Fits your preferred travel pace")
        }
        return listOf(
            act("$tripId-d2-breakfast", "08:30", mealName(preferences, "Breakfast", "Vegetarian breakfast"), "Candolim", "45 min", ActivityKind.Meal, "Eat before the coast, so the morning is unhurried.", foodReason(preferences)),
            aguada,
            midday,
            act("$tripId-d2-lunch", "13:30", mealName(preferences, "Lunch in Candolim", "Vegetarian lunch in Candolim"), "Candolim", "70 min", ActivityKind.Meal, "Then one ride north to Anjuna.", "Within your selected budget"),
            act("$tripId-d2-anjuna", "15:00", "Anjuna Beach", "Anjuna", "90 min", ActivityKind.Place, "The outdoor hour. Rain here would move dinner and the sunset stop with it.", "Matches your interest in the coast"),
            act("$tripId-d2-vagator", "17:30", "Vagator sunset", "Vagator", "70 min", ActivityKind.Experience, "The cliff is a few minutes from Anjuna.", "Kept close so the evening does not need another long ride"),
            act("$tripId-d2-dinner", "20:00", mealName(preferences, "Dinner", "Vegetarian dinner"), "Vagator", "90 min", ActivityKind.Meal, "Stay in the north for the night.", foodReason(preferences)),
        )
    }

    private fun dudhsagar(tripId: String, preferences: TripPreferences): List<TripActivity> = listOf(
        act("$tripId-d3-breakfast", "06:45", mealName(preferences, "Early breakfast", "Light vegetarian breakfast"), "North Goa", "30 min", ActivityKind.Meal, "A light meal so the ride inland can leave on time.", foodReason(preferences)),
        act("$tripId-d3-travel", "07:30", "Travel to Dudhsagar", "Mollem", "3 hr", ActivityKind.Transit, "One ride inland. The return uses the same road, via Ponda.", "Connects the waterfall to the evening in the south"),
        act("$tripId-d3-falls", "11:00", "Dudhsagar waterfall", "Dudhsagar", "2 hr", ActivityKind.Place, "The reason for the early start. Keep the visit to one window.", if (preferences.adventure) "Matches your interest in nature" else "Fits your preferred travel pace"),
        act("$tripId-d3-lunch", "13:30", mealName(preferences, "Lunch", "Vegetarian lunch"), "Mollem", "60 min", ActivityKind.Meal, "Eat before the plantation, not after a second stop.", foodReason(preferences)),
        act("$tripId-d3-spice", "15:00", "Spice plantation", "Ponda", "90 min", ActivityKind.Experience, "On the way back toward the coast, not a separate day.", "Matches your interest in local experiences"),
        act("$tripId-d3-return", "17:30", "Return toward South Goa", "South Goa", "90 min", ActivityKind.Transit, "You sleep closer to Palolem than to Anjuna.", "Shorter travel tomorrow"),
        act("$tripId-d3-evening", "19:30", "Evening rest", "Palolem side", "90 min", ActivityKind.Experience, "No new sight. The beach morning needs an early night.", "Fits your preferred travel pace"),
    )

    private fun southLeisure(tripId: String, preferences: TripPreferences): List<TripActivity> = listOf(
        act("$tripId-south-breakfast", "09:00", "Breakfast", "Palolem", "45 min", ActivityKind.Meal, "On the beach you reached last night."),
        act("$tripId-south-palolem", "10:30", "Palolem", "Palolem", "2 hr", ActivityKind.Place, "Swim or a quiet walk. Colva is later, not instead."),
        act("$tripId-south-colva", "13:30", "Colva", "Colva", "90 min", ActivityKind.Place, "A slower beach on the way back toward Margao."),
        act("$tripId-south-lunch", "15:30", "Lunch", "Colva", "70 min", ActivityKind.Meal, "Then the rest of the day stays unbooked."),
    )

    private fun southAndDeparture(tripId: String, preferences: TripPreferences): List<TripActivity> = listOf(
        act("$tripId-d4-breakfast", "08:30", mealName(preferences, "Breakfast", "Vegetarian breakfast"), "Palolem", "45 min", ActivityKind.Meal, "Last slow morning before the ride north.", foodReason(preferences)),
        act("$tripId-d4-palolem", "09:30", if (preferences.adventure) "Palolem swim and shore walk" else "Palolem", "Palolem", "90 min", ActivityKind.Place, "Beach time with a clear stop so Colva still fits.", "Fits your preferred travel pace"),
        act("$tripId-d4-colva", "11:30", "Colva", "Colva", "70 min", ActivityKind.Place, "Closer to Madgaon than Palolem is.", "Shorter travel distance"),
        act("$tripId-d4-lunch", "13:15", mealName(preferences, "Lunch in Margao", "Vegetarian lunch in Margao"), "Margao", "60 min", ActivityKind.Meal, "Eat in Margao, not back on the beach.", foodReason(preferences)),
        act("$tripId-d4-return", "14:45", "Return to Madgaon", "Madgaon", "30 min", ActivityKind.Transit, "A short ride to the station.", "Keeps the departure unhurried"),
        act("$tripId-d4-depart", "16:20", "Madgaon → Mumbai", "Madgaon", "Departure · 16:20", ActivityKind.Transit, "The same line you arrived on, heading home.", "Fits the way you like to travel"),
    )

    private fun extraGoa(tripId: String, index: Int, preferences: TripPreferences): Triple<String, String, List<TripActivity>> {
        val options = listOf(
            Triple("Morjim and the quieter north", "A buffer day between the fort and the waterfall.", listOf(
                act("$tripId-x$index-a", "09:30", "Breakfast", "Morjim", "45 min", ActivityKind.Meal, "North, but calmer than Anjuna."),
                act("$tripId-x$index-b", "11:00", "Morjim beach", "Morjim", "2 hr", ActivityKind.Place, "Use this day if the main beaches felt crowded."),
                act("$tripId-x$index-c", "14:00", "Lunch", "Morjim", "70 min", ActivityKind.Meal, "Stay on this stretch of coast."),
                act("$tripId-x$index-d", "16:00", "Vagator only if you want the view", "Vagator", "70 min", ActivityKind.Experience, "Optional. The day still works if you skip it."),
            )),
            Triple("Old Goa at an easy pace", "You already passed the basilica on day one. This visit is the longer one.", listOf(
                act("$tripId-x$index-a", "09:30", "Breakfast in Panjim", "Panaji", "45 min", ActivityKind.Meal, "Back in the city for one day."),
                act("$tripId-x$index-b", "11:00", "Se Cathedral and the churches", "Old Goa", "2 hr", ActivityKind.Place, "The indoor option if the coast is wet."),
                act("$tripId-x$index-c", "14:00", "Lunch", "Panaji", "70 min", ActivityKind.Meal, "Return for the meal, not another monument."),
                act("$tripId-x$index-d", "16:30", "Altinho viewpoint", "Altinho", "60 min", ActivityKind.Experience, "A short climb above Panjim."),
            )),
            Triple("Hinterland afternoon", "Keep the morning free. One plantation is enough.", listOf(
                act("$tripId-x$index-a", "09:00", "Slow breakfast", "Your stay", "50 min", ActivityKind.Meal, "No early jeep today."),
                act("$tripId-x$index-b", "11:00", "Local market", "Margao", "90 min", ActivityKind.Place, "Madgaon market, then the plantation."),
                act("$tripId-x$index-c", "13:30", "Lunch", "Margao", "60 min", ActivityKind.Meal, "Eat before leaving town."),
                act("$tripId-x$index-d", "15:30", "Spice plantation", "Ponda", "90 min", ActivityKind.Experience, "A second, shorter visit if Dudhsagar already happened."),
            )),
        )
        return options[(index - 4).floorMod(options.size)]
    }

    private fun adaptGoaRain(original: List<TripActivity>, tripId: String): List<TripActivity> {
        return original.map { activity ->
            when {
                activity.title == "Anjuna Beach" -> activity.copy(
                    id = "$tripId-d2-indoor",
                    time = "15:00",
                    title = "Assagao indoor studio and cafe",
                    place = "Assagao",
                    duration = "90 min",
                    kind = ActivityKind.Experience,
                    note = "Moved indoors. A few minutes from Anjuna, out of the rain.",
                    reason = "Keeps the evening, with a shorter exposed stretch",
                )
                activity.title == "Vagator sunset" -> activity.copy(
                    time = "17:30",
                    title = "Vagator, only if the rain has passed",
                    place = "Vagator",
                    note = "Held, not cancelled. Go only if the cliff is dry.",
                )
                activity.title == "Dinner" || activity.title == "Vegetarian dinner" -> activity.copy(
                    place = "Anjuna",
                    note = "Dinner under cover in Anjuna instead of an open cliff table.",
                    reason = "The meal stays. Only the table moves under cover.",
                )
                else -> activity
            }
        }
    }

    private fun adaptGoaCafe(original: List<TripActivity>, tripId: String): List<TripActivity> {
        return adaptGoaRain(original, tripId).map { activity ->
            if (activity.title == "Assagao indoor studio and cafe") {
                activity.copy(
                    id = "$tripId-d2-cafe",
                    title = "Covered café in Anjuna",
                    place = "Anjuna",
                    duration = "70 min",
                    note = "A shorter move, still out of the rain, and dinner stays next door.",
                    reason = "Lower cost and less travel than leaving Anjuna",
                )
            } else {
                activity
            }
        }
    }

    private fun cityDay(
        tripId: String,
        destination: String,
        index: Int,
        count: Int,
        date: LocalDate,
        preferences: TripPreferences,
        origin: String,
    ): ItineraryDay {
        val spots = highlights[destination] ?: listOf("the old town", "a local market", "the riverfront", "a neighborhood walk")
        val spot = spots[index % spots.size]
        val last = index == count - 1
        val activities = if (index == 0) {
            listOf(
                act("$tripId-d${index}-in", "11:00", "$origin → $destination", destination, "Arrival", ActivityKind.Transit, "Travel from $origin, then settle in before the first walk.", "Fits the way you like to arrive"),
                act("$tripId-d${index}-meal", "13:00", mealName(preferences, "Lunch near the stay", "Vegetarian lunch near the stay"), destination, "75 min", ActivityKind.Meal, "First meal within walking distance.", foodReason(preferences)),
                act("$tripId-d${index}-walk", "16:00", "First look at $spot", spot, "90 min", ActivityKind.Place, "One area only. The rest of the trip can still move."),
                act("$tripId-d${index}-dinner", "19:30", "Dinner", destination, "90 min", ActivityKind.Meal, "Close the day where you are sleeping."),
            )
        } else if (last) {
            listOf(
                act("$tripId-d${index}-breakfast", "09:00", "Breakfast", destination, "45 min", ActivityKind.Meal, "A calm last morning."),
                act("$tripId-d${index}-spot", "10:30", spot, destination, "2 hr", ActivityKind.Place, "The last planned stop before the station or airport."),
                act("$tripId-d${index}-lunch", "13:30", "Lunch", destination, "60 min", ActivityKind.Meal, "Eat before the transfer, not after it."),
                act("$tripId-d${index}-out", "16:00", "Departure", destination, "Transfer", ActivityKind.Transit, "Leave after lunch, with time before the train or flight.", "Keeps the last day unhurried"),
            )
        } else {
            listOf(
                act("$tripId-d${index}-breakfast", "09:00", "Breakfast", destination, "45 min", ActivityKind.Meal, "Start close to the stay."),
                act("$tripId-d${index}-morning", "10:30", spot, destination, "2 hr", ActivityKind.Place, "The main stop for this day."),
                act("$tripId-d${index}-lunch", "13:15", "Lunch", destination, "70 min", ActivityKind.Meal, "A break before the afternoon."),
                act("$tripId-d${index}-outdoor", "15:00", spot, destination, "90 min", ActivityKind.Place, "The outdoor hour. Weather or a closure can move this stop without touching the meals."),
                act("$tripId-d${index}-evening", "18:30", "Evening in the neighborhood", destination, "80 min", ActivityKind.Experience, "No extra transfer."),
            )
        }
        return ItineraryDay(
            dayNumber = index + 1,
            dateLabel = date.format(dayFormat),
            title = if (last) "Departure day" else "Day in $destination",
            activities = activities,
            recommendation = if (!last && index == 1) "The 3:00 PM hour is the one Navora will move first." else null,
        )
    }

    private fun adaptGenericRain(original: List<TripActivity>, tripId: String, destination: String): List<TripActivity> {
        return original.map { activity ->
            if (activity.time == "15:00") {
                activity.copy(
                    id = "$tripId-indoor",
                    title = "Indoor local visit",
                    place = destination,
                    kind = ActivityKind.Experience,
                    note = "Moved inside for the rain. The morning stays as planned.",
                    reason = "Keeps the rest of the day",
                )
            } else {
                activity
            }
        }
    }

    private val highlights = mapOf(
        "Mumbai" to listOf("Gateway of India", "Colaba", "Marine Drive", "Bandra"),
        "Jaipur" to listOf("Amber Fort", "Hawa Mahal", "City Palace", "Johari Bazaar"),
        "Udaipur" to listOf("Lake Pichola", "City Palace", "Bagore Ki Haveli", "Fateh Sagar"),
        "Kerala" to listOf("Fort Kochi", "Alleppey backwaters", "Munnar", "Jew Town"),
        "Manali" to listOf("Old Manali", "Hadimba Temple", "Solang Valley", "Mall Road"),
        "Rishikesh" to listOf("Laxman Jhula", "Triveni Ghat", "Beatles Ashram"),
        "Delhi" to listOf("Humayun's Tomb", "Chandni Chowk", "Lodhi Garden", "India Gate"),
        "Hyderabad" to listOf("Charminar", "Golconda Fort", "Laad Bazaar", "Hussain Sagar"),
        "Kashmir" to listOf("Dal Lake", "Mughal gardens", "Old Srinagar"),
        "Dubai" to listOf("Al Fahidi", "Dubai Creek", "Jumeirah"),
        "Singapore" to listOf("Gardens by the Bay", "Chinatown", "Marina Bay"),
        "Bali" to listOf("Ubud", "Uluwatu", "Seminyak"),
        "Bangkok" to listOf("The Grand Palace", "Chinatown", "Chao Phraya"),
        "Tokyo" to listOf("Asakusa", "Yanaka", "Shibuya"),
        "Paris" to listOf("Le Marais", "Musée d'Orsay", "Seine"),
        "London" to listOf("South Bank", "Covent Garden", "Greenwich"),
        "Switzerland" to listOf("Lucerne", "Interlaken", "Old Town"),
    )

    private val goaTradeOffs = listOf(
        TradeOff("Assagao studio", "₹2,400", "20 min", "High comfort", "Leaves Anjuna, keeps Vagator if the rain clears, and dinner moves under cover."),
        TradeOff("Anjuna café", "₹1,200", "10 min", "Moderate comfort", "Stays in Anjuna. Less travel, a lower bill, and dinner remains next door."),
    )

    private val genericTradeOffs = listOf(
        TradeOff("Indoor visit", "Similar cost", "Short walk", "High comfort", "The afternoon moves inside. The morning stays."),
        TradeOff("Covered neighborhood stop", "Lower cost", "Shorter walk", "Moderate comfort", "A closer swap, with the evening meal unchanged."),
    )
}

private fun act(
    id: String,
    time: String,
    title: String,
    place: String,
    duration: String,
    kind: ActivityKind,
    note: String,
    reason: String = "",
    conflict: String? = null,
    alternativeTitle: String? = null,
    alternativePlace: String? = null,
    alternativeNote: String? = null,
    alternativeReason: String? = null,
): TripActivity = TripActivity(
    id, time, title, place, duration, kind, note, reason, conflict,
    alternativeTitle, alternativePlace, alternativeNote, alternativeReason,
)

private fun mealName(preferences: TripPreferences, regular: String, vegetarian: String): String {
    return if (preferences.vegetarian) vegetarian else regular
}

private fun foodReason(preferences: TripPreferences): String {
    return if (preferences.vegetarian) "Matches your food preference" else "Matches your interest in local food"
}

private fun Int.floorMod(size: Int): Int {
    val mod = this % size
    return if (mod < 0) mod + size else mod
}
