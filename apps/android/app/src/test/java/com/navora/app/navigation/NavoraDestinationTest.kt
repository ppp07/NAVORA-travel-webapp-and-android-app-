package com.navora.app.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class NavoraDestinationTest {
    @Test
    fun tripPaths_keepItineraryAndAdaptationDistinct() {
        assertEquals("trip/kyoto", NavoraDestination.Itinerary.path("kyoto"))
        assertEquals("trip/kyoto/adapt", NavoraDestination.Adaptive.path("kyoto"))
        assertEquals("plan?destination=Kyoto", NavoraDestination.Plan.path("Kyoto"))
    }

    @Test
    fun bottomBar_coversThePrimaryTabs() {
        val routes = NavoraDestination.bottomBarDestinations.map { it.route }
        assertEquals(listOf("home", "trips", "explore", "profile"), routes)
        assertFalse(routes.contains(NavoraDestination.Splash.route))
    }
}
