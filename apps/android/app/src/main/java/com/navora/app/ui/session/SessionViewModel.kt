package com.navora.app.ui.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.navora.app.data.AppGraph
import com.navora.app.data.TripRepository
import com.navora.app.model.DemoSnapshot
import com.navora.app.model.TravelerProfile
import kotlinx.coroutines.flow.StateFlow

class SessionViewModel(
    private val repository: TripRepository,
) : ViewModel() {
    val snapshot: StateFlow<DemoSnapshot> = repository.snapshot

    fun adaptTo(tripId: String, eventName: String) {
        repository.adaptTo(tripId, eventName)
    }

    fun applyAdaptation(tripId: String) {
        repository.applyAdaptation(tripId)
    }

    fun selectTradeOff(tripId: String, option: Int) {
        repository.selectTradeOff(tripId, option)
    }

    fun chooseAlternative(tripId: String, activityId: String) {
        repository.chooseAlternative(tripId, activityId)
    }

    fun updateProfile(profile: TravelerProfile) {
        repository.updateProfile(profile)
    }
}

fun sessionViewModelFactory(
    repository: TripRepository = AppGraph.tripRepository,
): ViewModelProvider.Factory = viewModelFactory {
    initializer { SessionViewModel(repository) }
}
