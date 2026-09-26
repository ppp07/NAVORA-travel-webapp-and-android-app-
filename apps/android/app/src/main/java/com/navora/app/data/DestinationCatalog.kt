package com.navora.app.data

import com.navora.app.model.ActivityKind
import com.navora.app.model.TripEvent
import com.navora.app.model.TripPreferences

internal object DestinationCatalog {
    fun defaultEvent(city: String): TripEvent = when (city.lowercase()) {
        "goa", "mumbai", "bangkok", "bali", "singapore", "london", "paris", "kerala" -> TripEvent.HeavyRain
        "jaipur", "delhi", "dubai" -> TripEvent.ExtremeHeat
        "manali", "switzerland", "kashmir" -> TripEvent.Snowfall
        "tokyo" -> TripEvent.HeavyRain
        else -> TripEvent.HeavyRain
    }

    fun why(city: String, event: TripEvent): String = when (event) {
        TripEvent.HeavyRain, TripEvent.Storm ->
            "Wet weather in $city makes an exposed outdoor hour a poor use of the afternoon."
        TripEvent.ExtremeHeat ->
            "Midday heat in $city makes a long outdoor stop uncomfortable."
        TripEvent.Snowfall, TripEvent.RoadClosure ->
            "Mountain or road conditions around $city make the exposed plan unreliable."
        TripEvent.FlightDelay, TripEvent.TrainDelay ->
            "A later arrival in $city means the original first stop no longer fits."
        TripEvent.Traffic ->
            "Crossing $city in traffic would spend the hour on the road."
        TripEvent.AttractionClosed ->
            "That stop in $city is not open, so a nearby match is used instead."
        TripEvent.Crowded ->
            "The wait in $city would crowd out the rest of the afternoon."
        TripEvent.FeelingSick, TripEvent.LowEnergy, TripEvent.MoreRest, TripEvent.SlowerDay ->
            "The day in $city is lighter so walking and transfers drop."
        TripEvent.SpendLess ->
            "A lower-cost stop in $city keeps the shape of the day."
        TripEvent.SpendMore ->
            "A more comfortable stop in $city replaces the simpler one."
        TripEvent.MissedActivity ->
            "The missed stop in $city is replaced with something still reachable."
        TripEvent.AddActivities ->
            "One extra nearby stop is added in $city without moving dinner."
    }

    fun swaps(city: String, event: TripEvent, preferences: TripPreferences): List<Swap> {
        val indoor = indoorFor(city, preferences)
        val rest = listOf(
            Swap("Rest at your stay", "Your stay", "2 hr", ActivityKind.Experience, "This hour stays open. Meals and the other stops remain.", "No extra cost", "No travel", "High comfort"),
            Swap("Quiet meal nearby", city, "75 min", ActivityKind.Meal, "A short walk for food, then back. Nothing else is added.", preferences.budget, "10 min", "High comfort"),
            indoor.first(),
        )
        val delayed = listOf(
            Swap("Shorter first stop", city, "60 min", ActivityKind.Place, "Starts later, close to where you arrive, so the rest of the day can stay.", "Similar cost", "20 min", "High comfort"),
            indoor[0],
            indoor.getOrElse(1) { indoor[0] },
        )
        return when (event) {
            TripEvent.FeelingSick, TripEvent.LowEnergy, TripEvent.MoreRest, TripEvent.SlowerDay -> rest
            TripEvent.FlightDelay, TripEvent.TrainDelay -> delayed
            TripEvent.SpendLess -> listOf(indoor.last(), indoor.first(), rest[0])
            TripEvent.SpendMore -> listOf(indoor.first().copy(comfort = "High comfort", cost = "Higher"), indoor[0], indoor.getOrElse(1) { indoor[0] })
            TripEvent.Traffic, TripEvent.RoadClosure, TripEvent.Snowfall -> listOf(indoor[0], indoor.getOrElse(1) { indoor[0] }, rest[1])
            else -> indoor
        }
    }

    private fun indoorFor(city: String, preferences: TripPreferences): List<Swap> {
        val named = when (city.lowercase()) {
            "goa" -> listOf(
                triple("Assagao indoor studio and cafe", "Assagao", "A covered studio a few minutes from the beach."),
                triple("Fontainhas heritage cafe", "Fontainhas", "Culture and food indoors, still in Panjim."),
                triple("Panjim covered market", "Panaji", "A shorter, covered stop."),
            )
            "jaipur" -> listOf(
                triple("City Palace galleries", "City Palace", "Indoors, and cooler than the fort ramparts."),
                triple("Albert Hall Museum", "Ram Niwas Garden", "A museum hour instead of midday sun."),
                triple("Johari Bazaar lanes", "Old City", "Covered shopping close to the centre."),
            )
            "manali" -> listOf(
                triple("Old Manali cafe", "Old Manali", "Stay in town instead of the high road."),
                triple("Local wool market", "Mall Road", "A short indoor loop."),
                triple("Hotel lounge afternoon", "Your stay", "No mountain transfer."),
            )
            "mumbai" -> listOf(
                triple("Chhatrapati Shivaji museum", "Fort", "Indoors, near the seafront you would have walked."),
                triple("Colaba cafe hour", "Colaba", "Food, under cover."),
                triple("Kala Ghoda galleries", "Kala Ghoda", "A short cultural loop."),
            )
            "delhi" -> listOf(
                triple("National Museum", "Janpath", "Indoors, away from the heat and the open lawns."),
                triple("Khan Market lunch", "Khan Market", "A meal instead of a long outdoor stretch."),
                triple("Crafts Museum galleries", "Pragati Maidan", "Culture, mostly inside."),
            )
            "paris" -> listOf(
                triple("Musée d'Orsay", "Saint-Germain", "An indoor hour instead of the river walk."),
                triple("Covered passage and cafe", "Passage des Panoramas", "Food and architecture, out of the rain."),
                triple("Le Marais gallery", "Le Marais", "A short indoor cultural stop."),
            )
            "tokyo" -> listOf(
                triple("TeamLab or a city museum", "Central Tokyo", "Indoors, without a long outdoor transfer."),
                triple("Depachika food hall", "Tokyo Station", "A food hour under cover."),
                triple("Yanaka back streets, shortened", "Yanaka", "A smaller walk if you still want the neighborhood."),
            )
            "singapore" -> listOf(
                triple("National Gallery", "Civic District", "Indoors, close to the civic quarter."),
                triple("Maxwell food centre", "Chinatown", "A meal instead of an outdoor hour."),
                triple("Marina Bay indoor loop", "Marina Bay", "Mostly inside and connected."),
            )
            "dubai" -> listOf(
                triple("Al Fahidi indoor courtyards", "Al Fahidi", "Shade and short distances."),
                triple("Dubai Mall cultural hour", "Downtown", "Indoors through the heat."),
                triple("Creek abra, early or skipped", "Dubai Creek", "Only if you still want the water, kept short."),
            )
            "bangkok" -> listOf(
                triple("Museum Siam", "Phra Nakhon", "Indoors, near the river sights."),
                triple("Covered food lane", "Chinatown", "A meal instead of an open-air walk."),
                triple("Hotel pool or lounge", "Your stay", "Rest, without another taxi."),
            )
            "bali" -> listOf(
                triple("Ubud indoor spa hour", "Ubud", "Wellness instead of the beach."),
                triple("Local warung", "Ubud", "Food, close to the stay."),
                triple("Gallery walk, covered", "Ubud", "Culture without the open road."),
            )
            "london" -> listOf(
                triple("British Museum", "Bloomsbury", "Indoors instead of a park or river walk."),
                triple("Borough Market, covered", "London Bridge", "Food under cover."),
                triple("Southbank indoor hall", "South Bank", "A short cultural stop."),
            )
            "switzerland" -> listOf(
                triple("Lucerne old-town cafe", "Lucerne", "Stay low instead of a mountain transfer."),
                triple("Swiss Museum", "Lucerne", "Indoors if the weather turns."),
                triple("Lakefront, only a short stretch", "Lucerne", "A smaller outdoor window."),
            )
            else -> listOf(
                triple("Indoor local visit", city, "A covered stop near the rest of the day."),
                triple("Local meal", city, "Food instead of the exposed hour."),
                triple("Neighborhood cafe", city, "Short, and easy to leave."),
            )
        }
        val ordered = if (preferences.vegetarian || "Food" in preferences.interests) {
            named.sortedByDescending { if ("food" in it.third.lowercase() || "cafe" in it.first.lowercase() || "meal" in it.first.lowercase() || "warung" in it.first.lowercase() || "market" in it.first.lowercase()) 1 else 0 }
        } else {
            named
        }
        return ordered.map { (title, place, summary) ->
            Swap(title, place, "80 min", ActivityKind.Experience, summary, costFor(preferences), "15 min", comfortFor(preferences))
        }
    }

    private fun triple(title: String, place: String, summary: String) = Triple(title, place, summary)

    private fun costFor(preferences: TripPreferences): String = when (preferences.budget) {
        "Budget" -> "₹800"
        "Premium", "Luxury" -> "₹3,200"
        else -> "₹1,600"
    }

    private fun comfortFor(preferences: TripPreferences): String = when {
        preferences.walking == "Minimal" || preferences.relaxed -> "High comfort"
        preferences.adventure -> "Moderate comfort"
        else -> "Moderate comfort"
    }
}
