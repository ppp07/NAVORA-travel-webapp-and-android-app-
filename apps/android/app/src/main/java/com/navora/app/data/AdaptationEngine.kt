package com.navora.app.data

import com.navora.app.model.ActivityKind
import com.navora.app.model.AdaptiveScenario
import com.navora.app.model.RippleEffect
import com.navora.app.model.TradeOff
import com.navora.app.model.Trip
import com.navora.app.model.TripActivity
import com.navora.app.model.TripEvent
import com.navora.app.model.TripPreferences

internal object AdaptationEngine {
    fun plan(trip: Trip, event: TripEvent, preferences: TripPreferences): AdaptiveScenario {
        val located = locate(trip, event)
        val day = trip.days[located.dayIndex]
        val target = day.activities[located.activityIndex]
        val swaps = DestinationCatalog.swaps(trip.destination, event, preferences)
        val options = swaps.map { swap ->
            applySwap(day.activities, located.activityIndex, target, swap, event)
        }
        val primary = options.first()
        val unchanged = day.activities.map { it.title }.filter { title ->
            primary.any { it.title == title }
        }
        val changed = day.activities.zip(primary).count { it.first.title != it.second.title }
            .coerceAtLeast(1)
        val eveningMoved = day.activities.zip(primary).any { (before, after) ->
            before.title != after.title && before.time >= "17:00"
        }
        val mealMoved = day.activities.zip(primary).any { (before, after) ->
            before.kind == ActivityKind.Meal && before.title != after.title
        }
        return AdaptiveScenario(
            id = "${trip.id}-${event.name}",
            tripId = trip.id,
            dayNumber = day.dayNumber,
            headline = "Your plans changed.",
            changeLabel = "${event.label}.",
            changeDetail = "${target.title} at ${target.time} conflicts with this. ${DestinationCatalog.why(trip.destination, event)}",
            adaptationDetail = "${target.title} becomes ${swaps.first().title}. ${unchanged.size} stops on this day stay as planned.",
            before = day.activities,
            after = primary,
            alternateAfter = options.getOrElse(1) { primary },
            options = options,
            unchanged = unchanged,
            affectedTitle = target.title,
            eventLabel = event.label,
            ripple = RippleEffect(
                activities = changed,
                travelLinks = if (event == TripEvent.Traffic || event == TripEvent.RoadClosure || event == TripEvent.FlightDelay || event == TripEvent.TrainDelay) 1 else if (target.kind == ActivityKind.Transit) 1 else 1,
                meals = if (mealMoved) 1 else 0,
                eveningPlans = if (eveningMoved) 1 else 0,
            ),
            tradeOffs = swaps.map { swap ->
                TradeOff(swap.title, swap.cost, swap.travel, swap.comfort, swap.summary)
            },
            accessibilityNote = if (preferences.avoidStairs || preferences.walking == "Minimal") {
                "Stops with stairs or a long walk are treated as a poor fit, and a lower-effort option is offered first."
            } else {
                null
            },
        )
    }

    private fun locate(trip: Trip, event: TripEvent): Located {
        val dayIndex = when (event) {
            TripEvent.FlightDelay, TripEvent.TrainDelay -> 0
            else -> if (trip.days.size > 2) 1 else 0
        }
        val activities = trip.days[dayIndex].activities
        val index = when (event) {
            TripEvent.FlightDelay, TripEvent.TrainDelay ->
                activities.indexOfFirst { it.kind == ActivityKind.Place || it.kind == ActivityKind.Experience }
            TripEvent.FeelingSick, TripEvent.LowEnergy, TripEvent.MoreRest, TripEvent.SlowerDay ->
                activities.indices.filter { activities[it].kind != ActivityKind.Meal && activities[it].kind != ActivityKind.Transit }
                    .maxByOrNull { activities[it].duration } ?: activities.indexOfFirst { it.kind == ActivityKind.Place }
            TripEvent.Traffic, TripEvent.RoadClosure ->
                activities.indexOfFirst { it.isExposed() }.takeIf { it >= 0 }
                    ?: activities.indexOfFirst { it.kind == ActivityKind.Transit }
            else -> activities.indexOfFirst { it.time >= "14:00" && it.time < "18:00" && it.isExposed() }
                .takeIf { it >= 0 }
                ?: activities.indexOfFirst { it.time >= "12:00" && it.isExposed() }
                    .takeIf { it >= 0 }
                ?: activities.indexOfFirst { it.isExposed() }
        }.let { if (it < 0) activities.indexOfFirst { it.kind != ActivityKind.Meal }.coerceAtLeast(0) else it }
        return Located(dayIndex, index)
    }

    private fun applySwap(
        original: List<TripActivity>,
        index: Int,
        target: TripActivity,
        swap: Swap,
        event: TripEvent,
    ): List<TripActivity> {
        val replaced = target.copy(
            id = "${target.id}-adapted",
            title = swap.title,
            place = swap.place,
            duration = swap.minutes,
            kind = swap.kind,
            note = swap.summary,
            reason = "Replaces ${target.title} after ${event.label.lowercase()}.",
            conflict = null,
            alternativeTitle = null,
        )
        val updated = original.toMutableList()
        updated[index] = replaced
        if (event == TripEvent.AddActivities) {
            val insertAt = (index + 1).coerceAtMost(updated.size)
            updated.add(
                insertAt,
                target.copy(
                    id = "${target.id}-added",
                    time = "16:30",
                    title = "Extra local stop",
                    place = swap.place,
                    duration = "45 min",
                    kind = ActivityKind.Experience,
                    note = "Added because you asked for a fuller day. Nearby, so the evening still holds.",
                    reason = "Matches the request to add an activity",
                    conflict = null,
                    alternativeTitle = null,
                ),
            )
        }
        return updated
    }

    private fun TripActivity.isExposed(): Boolean {
        if (kind == ActivityKind.Meal || kind == ActivityKind.Transit) return false
        val text = "$title $place $note".lowercase()
        val words = listOf(
            "beach", "fort", "waterfall", "sunset", "kayak", "cliff", "outdoor",
            "viewpoint", "promenade", "garden", "lake", "market walk", "shore",
        )
        return words.any { it in text }
    }

    private data class Located(val dayIndex: Int, val activityIndex: Int)
}

internal data class Swap(
    val title: String,
    val place: String,
    val minutes: String,
    val kind: ActivityKind,
    val summary: String,
    val cost: String,
    val travel: String,
    val comfort: String,
)
