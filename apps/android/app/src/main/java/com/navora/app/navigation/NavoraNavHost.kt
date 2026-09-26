package com.navora.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.navora.app.ui.adaptive.AdaptiveChangeScreen
import com.navora.app.ui.explore.ExploreScreen
import com.navora.app.ui.home.HomeScreen
import com.navora.app.ui.itinerary.ItineraryScreen
import com.navora.app.ui.onboarding.OnboardingScreen
import com.navora.app.ui.plan.PlanTripScreen
import com.navora.app.ui.profile.PreferencesScreen
import com.navora.app.ui.profile.ProfileScreen
import com.navora.app.ui.session.SessionViewModel
import com.navora.app.ui.session.sessionViewModelFactory
import com.navora.app.ui.splash.SplashScreen
import com.navora.app.ui.trips.MyTripsScreen

@Composable
fun NavoraNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    sessionViewModel: SessionViewModel = viewModel(factory = sessionViewModelFactory()),
) {
    val snapshot by sessionViewModel.snapshot.collectAsStateWithLifecycle()
    val suggestions = snapshot.destinations.map { it.city }

    NavHost(
        navController = navController,
        startDestination = NavoraDestination.Splash.route,
        modifier = modifier,
    ) {
        composable(NavoraDestination.Splash.route) {
            SplashScreen(
                onFinished = {
                    navController.navigate(NavoraDestination.Onboarding.route) {
                        popUpTo(NavoraDestination.Splash.route) { inclusive = true }
                    }
                },
            )
        }
        composable(NavoraDestination.Onboarding.route) {
            OnboardingScreen(
                onFinished = {
                    navController.navigate(NavoraDestination.Home.route) {
                        popUpTo(NavoraDestination.Onboarding.route) { inclusive = true }
                    }
                },
            )
        }
        composable(NavoraDestination.Home.route) {
            HomeScreen(
                onPlanTrip = { navController.navigate(NavoraDestination.Plan.path()) },
                onOpenTrips = { navController.openTab(NavoraDestination.Trips) },
                onOpenExplore = { navController.openTab(NavoraDestination.Explore) },
                onOpenProfile = { navController.openTab(NavoraDestination.Profile) },
                onOpenPreferences = { navController.navigate(NavoraDestination.Preferences.route) },
                onOpenTrip = { tripId -> navController.navigate(NavoraDestination.Itinerary.path(tripId)) },
                onOpenAdaptation = { tripId -> navController.navigate(NavoraDestination.Adaptive.path(tripId)) },
            )
        }
        composable(NavoraDestination.Trips.route) {
            MyTripsScreen(
                onOpenTrip = { tripId -> navController.navigate(NavoraDestination.Itinerary.path(tripId)) },
            )
        }
        composable(NavoraDestination.Explore.route) {
            ExploreScreen(
                onPlanDestination = { city -> navController.navigate(NavoraDestination.Plan.path(city)) },
            )
        }
        composable(NavoraDestination.Profile.route) {
            ProfileScreen(
                onEditPreferences = { navController.navigate(NavoraDestination.Preferences.route) },
            )
        }
        composable(NavoraDestination.Preferences.route) {
            PreferencesScreen(onDone = { navController.popBackStack() })
        }
        composable(
            route = NavoraDestination.Plan.route,
            arguments = listOf(
                navArgument(DESTINATION_ARG) {
                    type = NavType.StringType
                    defaultValue = ""
                },
            ),
        ) { entry ->
            val destination = entry.arguments?.getString(DESTINATION_ARG).orEmpty()
            PlanTripScreen(
                prefilledDestination = destination,
                suggestions = suggestions,
                onBack = { navController.popBackStack() },
                onGenerated = { tripId ->
                    navController.navigate(NavoraDestination.Itinerary.path(tripId)) {
                        popUpTo(NavoraDestination.Plan.route) { inclusive = true }
                    }
                },
            )
        }
        composable(
            route = NavoraDestination.Itinerary.route,
            arguments = listOf(navArgument(TRIP_ID_ARG) { type = NavType.StringType }),
        ) { entry ->
            val tripId = entry.arguments?.getString(TRIP_ID_ARG).orEmpty()
            ItineraryScreen(
                tripId = tripId,
                onBack = { navController.popBackStack() },
                onOpenAdaptation = { navController.navigate(NavoraDestination.Adaptive.path(tripId)) },
            )
        }
        composable(
            route = NavoraDestination.Adaptive.route,
            arguments = listOf(navArgument(TRIP_ID_ARG) { type = NavType.StringType }),
        ) { entry ->
            val tripId = entry.arguments?.getString(TRIP_ID_ARG).orEmpty()
            AdaptiveChangeScreen(
                tripId = tripId,
                onBack = { navController.popBackStack() },
            )
        }
    }
}

private fun NavHostController.openTab(destination: NavoraDestination) {
    navigate(destination.route) {
        popUpTo(NavoraDestination.Home.route) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}
