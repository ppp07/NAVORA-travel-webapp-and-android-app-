package com.navora.app.data

import com.navora.app.model.DemoSnapshot
import com.navora.app.model.TripEvent
import com.navora.app.model.TravelerProfile
import com.navora.app.model.Trip
import com.navora.app.model.TripDraft
import kotlinx.coroutines.flow.StateFlow

/**
 * Local trip catalog.
 *
 * A later implementation can serve the same snapshot from the shared API.
 * This client does not talk to a database and does not hold provider secrets.
 */
interface TripRepository {
    val snapshot: StateFlow<DemoSnapshot>

    fun generateTrip(draft: TripDraft): Trip

    fun adaptTo(tripId: String, eventName: String)

    fun applyAdaptation(tripId: String)

    fun selectTradeOff(tripId: String, option: Int)

    fun chooseAlternative(tripId: String, activityId: String)

    fun updateProfile(profile: TravelerProfile)
}
