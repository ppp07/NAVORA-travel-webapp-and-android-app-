package com.navora.app.navigation

internal const val TRIP_ID_ARG = "tripId"
internal const val DESTINATION_ARG = "destination"

sealed class NavoraDestination(val route: String) {
    data object Splash : NavoraDestination("splash")

    data object Onboarding : NavoraDestination("onboarding")

    data object Home : NavoraDestination("home")

    data object Trips : NavoraDestination("trips")

    data object Explore : NavoraDestination("explore")

    data object Profile : NavoraDestination("profile")

    data object Preferences : NavoraDestination("preferences")

    data object Plan : NavoraDestination("plan?$DESTINATION_ARG={$DESTINATION_ARG}") {
        fun path(destination: String = ""): String {
            return "plan?$DESTINATION_ARG=${encodeQuery(destination)}"
        }
    }

    data object Itinerary : NavoraDestination("trip/{$TRIP_ID_ARG}") {
        fun path(tripId: String) = "trip/$tripId"
    }

    data object Adaptive : NavoraDestination("trip/{$TRIP_ID_ARG}/adapt") {
        fun path(tripId: String) = "trip/$tripId/adapt"
    }

    companion object {
        val bottomBarDestinations: List<NavoraDestination> = listOf(Home, Trips, Explore, Profile)
    }
}

private fun encodeQuery(value: String): String = buildString {
    value.forEach { char ->
        when {
            char.isLetterOrDigit() || char == '-' || char == '_' || char == '.' -> append(char)
            char == ' ' -> append("%20")
            else -> append("%" + char.code.toString(16).uppercase().padStart(2, '0'))
        }
    }
}
