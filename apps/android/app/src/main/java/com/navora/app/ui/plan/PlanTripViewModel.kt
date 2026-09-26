package com.navora.app.ui.plan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.navora.app.data.AppGraph
import com.navora.app.data.TripRepository
import com.navora.app.model.TripDraft
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

data class PlanTripForm(
    val destination: String = "",
    val startEpochDay: Long? = null,
    val endEpochDay: Long? = null,
    val travelers: Int = 2,
    val styles: List<String> = listOf("Coast"),
    val budget: String = "Balanced",
    val interests: List<String> = emptyList(),
    val notes: String = "",
    val lens: String = "Yours",
    val origin: String = "Mumbai",
    val error: String? = null,
    val prefilled: Boolean = false,
) {
    val startLabel: String
        get() = startEpochDay?.let { LocalDate.ofEpochDay(it).format(dateFormat) }.orEmpty()

    val endLabel: String
        get() = endEpochDay?.let { LocalDate.ofEpochDay(it).format(dateFormat) }.orEmpty()

    val durationLabel: String?
        get() {
            val start = startEpochDay ?: return null
            val end = endEpochDay ?: return null
            val days = ChronoUnit.DAYS.between(LocalDate.ofEpochDay(start), LocalDate.ofEpochDay(end)).toInt() + 1
            return if (days == 1) "1 day" else "$days days"
        }
}

class PlanTripViewModel(
    private val repository: TripRepository,
) : ViewModel() {
    private val _form = MutableStateFlow(PlanTripForm())
    val form: StateFlow<PlanTripForm> = _form.asStateFlow()

    fun prefill(destination: String) {
        if (destination.isBlank() || _form.value.prefilled) return
        _form.value = _form.value.copy(destination = destination, prefilled = true, error = null)
    }

    fun onDestination(value: String) = update { copy(destination = value, error = null) }
    fun onNotes(value: String) = update { copy(notes = value, error = null) }

    fun onStartSelected(epochDay: Long) = update {
        val endCleared = endEpochDay != null && endEpochDay < epochDay
        copy(
            startEpochDay = epochDay,
            endEpochDay = if (endCleared) null else endEpochDay,
            error = if (endCleared) "Choose an end date on or after the new start date." else null,
        )
    }

    fun onEndSelected(epochDay: Long): Boolean {
        val start = _form.value.startEpochDay
        if (start == null) {
            _form.value = _form.value.copy(error = "Choose a start date first.")
            return false
        }
        if (epochDay < start) {
            _form.value = _form.value.copy(error = "End date cannot be before the start date.")
            return false
        }
        _form.value = _form.value.copy(endEpochDay = epochDay, error = null)
        return true
    }

    fun setBudget(value: String) = update { copy(budget = value) }
    fun setLens(value: String) = update { copy(lens = value, error = null) }
    fun setOrigin(value: String) = update { copy(origin = value, error = null) }

    fun toggleStyle(value: String) = update {
        copy(styles = styles.toggle(value), error = null)
    }

    fun toggleInterest(value: String) = update {
        copy(interests = interests.toggle(value))
    }

    fun decreaseTravelers() = update { copy(travelers = (travelers - 1).coerceAtLeast(1)) }
    fun increaseTravelers() = update { copy(travelers = (travelers + 1).coerceAtMost(8)) }

    fun generate(): String? {
        val current = _form.value
        val start = current.startEpochDay
        val end = current.endEpochDay
        if (current.destination.isBlank() || start == null || end == null) {
            _form.value = current.copy(error = "Add a destination and both dates to generate the trip.")
            return null
        }
        if (end < start) {
            _form.value = current.copy(error = "End date cannot be before the start date.")
            return null
        }
        val profile = repository.snapshot.value.profile
        val shaped = if (current.lens == "Adventure") {
            profile.copy(
                style = "Packed",
                interests = listOf("Adventure", "Nature"),
                food = "No strong preference",
                activityLevel = "High",
                mobility = "No special requirements",
                accessibility = emptyList(),
                pacingNeeds = emptyList(),
            )
        } else {
            profile
        }
        val trip = repository.generateTrip(
            TripDraft(
                destination = current.destination,
                startDate = current.startLabel,
                endDate = current.endLabel,
                startEpochDay = start,
                endEpochDay = end,
                travelers = current.travelers,
                styles = shaped.interests,
                budget = shaped.budget,
                interests = shaped.interests,
                notes = current.notes,
                origin = current.origin,
                pace = shaped.style,
                food = shaped.food,
                accommodation = shaped.accommodation,
                mobility = shaped.mobility,
                accessibility = shaped.accessibility,
                pacingNeeds = shaped.pacingNeeds,
                walking = shaped.walking,
                rest = shaped.rest,
            ),
        )
        return trip.id
    }

    private fun update(transform: PlanTripForm.() -> PlanTripForm) {
        _form.value = _form.value.transform()
    }
}

private fun List<String>.toggle(value: String): List<String> {
    return if (value in this) filterNot { it == value } else this + value
}

fun planTripViewModelFactory(
    repository: TripRepository = AppGraph.tripRepository,
): ViewModelProvider.Factory = viewModelFactory {
    initializer { PlanTripViewModel(repository) }
}

private val dateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM yyyy")

val travelStyles = listOf("Adventure", "Food", "Culture", "Nature", "Shopping", "Relaxation", "Coast")
val interestOptions = listOf("Heritage", "Food", "Coast", "Markets", "Temples", "Hills", "Backwaters")
val budgetOptions = listOf("Value", "Balanced", "Comfortable")
