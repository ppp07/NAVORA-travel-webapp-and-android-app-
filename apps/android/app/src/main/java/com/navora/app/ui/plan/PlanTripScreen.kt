package com.navora.app.ui.plan

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.navora.app.ui.theme.NavoraCard
import com.navora.app.ui.theme.NavoraCream
import com.navora.app.ui.theme.NavoraNavy
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.navora.app.ui.components.NavoraScreen
import com.navora.app.ui.components.PrimaryButton
import com.navora.app.ui.theme.NavoraMuted

private enum class DateTarget { Start, End }

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PlanTripScreen(
    prefilledDestination: String,
    suggestions: List<String>,
    onBack: () -> Unit,
    onGenerated: (String) -> Unit,
    viewModel: PlanTripViewModel = viewModel(factory = planTripViewModelFactory()),
) {
    val form by viewModel.form.collectAsStateWithLifecycle()
    var picking by rememberSaveable { mutableStateOf<DateTarget?>(null) }
    LaunchedEffect(prefilledDestination) {
        viewModel.prefill(prefilledDestination)
    }
    picking?.let { target ->
        NavoraDateDialog(
            target = target,
            initialEpochDay = if (target == DateTarget.Start) form.startEpochDay else form.endEpochDay,
            earliestEpochDay = if (target == DateTarget.End) form.startEpochDay else null,
            onDismiss = { picking = null },
            onConfirm = { epochDay ->
                val accepted = if (target == DateTarget.Start) {
                    viewModel.onStartSelected(epochDay)
                    true
                } else {
                    viewModel.onEndSelected(epochDay)
                }
                if (accepted) picking = null
            },
        )
    }

    NavoraScreen(title = "Plan a trip", onBack = onBack) {
        Text(
            text = "Dates and your preferences decide how the days are built.",
            style = MaterialTheme.typography.bodyMedium,
            color = NavoraMuted,
        )
        Text("Where are you starting from?", style = MaterialTheme.typography.titleMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Mumbai", "Pune", "Delhi", "Bengaluru", "Ahmedabad").forEach { city ->
                FilterChip(
                    selected = form.origin == city,
                    onClick = { viewModel.setOrigin(city) },
                    label = { Text(city) },
                )
            }
        }
        TripField(form.destination, viewModel::onDestination, "Destination", "City or region")
        if (suggestions.isNotEmpty()) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                suggestions.take(4).forEach { city ->
                    FilterChip(
                        selected = form.destination == city,
                        onClick = { viewModel.onDestination(city) },
                        label = { Text(city) },
                    )
                }
            }
        }
        DateSelectField(
            label = "Start date",
            value = form.startLabel,
            onClick = { picking = DateTarget.Start },
        )
        DateSelectField(
            label = "End date",
            value = form.endLabel,
            onClick = { picking = DateTarget.End },
        )
        Text("Shape this trip", style = MaterialTheme.typography.titleMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Yours" to "Match my profile", "Adventure" to "Adventure and nature").forEach { (id, label) ->
                FilterChip(
                    selected = form.lens == id,
                    onClick = { viewModel.setLens(id) },
                    label = { Text(label) },
                )
            }
        }
        Text(
            text = if (form.lens == "Adventure") {
                "Faster days, nature first, and no stair constraint on this trip."
            } else {
                "Uses your pace, food, budget, and access preferences."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = NavoraMuted,
        )
        if (form.durationLabel != null) {
            Text(
                text = form.durationLabel.orEmpty(),
                style = MaterialTheme.typography.titleMedium,
                color = NavoraNavy,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("Travelers", style = MaterialTheme.typography.titleMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                FilledTonalIconButton(onClick = viewModel::decreaseTravelers, enabled = form.travelers > 1) {
                    Icon(Icons.Filled.Remove, contentDescription = "Fewer travelers")
                }
                Text(
                    text = form.travelers.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 12.dp),
                )
                FilledTonalIconButton(onClick = viewModel::increaseTravelers, enabled = form.travelers < 8) {
                    Icon(Icons.Filled.Add, contentDescription = "More travelers")
                }
            }
        }
        Text("Travel style", style = MaterialTheme.typography.titleMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            travelStyles.forEach { style ->
                FilterChip(
                    selected = style in form.styles,
                    onClick = { viewModel.toggleStyle(style) },
                    label = { Text(style) },
                )
            }
        }
        Text("Budget", style = MaterialTheme.typography.titleMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            budgetOptions.forEach { budget ->
                FilterChip(
                    selected = form.budget == budget,
                    onClick = { viewModel.setBudget(budget) },
                    label = { Text(budget) },
                )
            }
        }
        Text("Interests", style = MaterialTheme.typography.titleMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            interestOptions.forEach { interest ->
                FilterChip(
                    selected = interest in form.interests,
                    onClick = { viewModel.toggleInterest(interest) },
                    label = { Text(interest) },
                )
            }
        }
        TripField(form.notes, viewModel::onNotes, "Notes", "Anything the plan should protect", singleLine = false)
        if (form.error != null) {
            Text(text = form.error.orEmpty(), color = MaterialTheme.colorScheme.error)
        }
        PrimaryButton(
            text = "Generate my trip",
            onClick = { viewModel.generate()?.let(onGenerated) },
        )
    }
}

@Composable
private fun DateSelectField(
    label: String,
    value: String,
    onClick: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            placeholder = { Text("Choose from the calendar") },
            trailingIcon = {
                Icon(Icons.Outlined.CalendarMonth, contentDescription = null)
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(onClick = onClick),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NavoraDateDialog(
    target: DateTarget,
    initialEpochDay: Long?,
    earliestEpochDay: Long?,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit,
) {
    val state = rememberDatePickerState(
        initialSelectedDateMillis = initialEpochDay?.let(::epochDayToUtcMillis),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val earliest = earliestEpochDay ?: return true
                return utcMillisToEpochDay(utcTimeMillis) >= earliest
            }
        },
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val millis = state.selectedDateMillis ?: return@TextButton
                    onConfirm(utcMillisToEpochDay(millis))
                },
            ) {
                Text("Select")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        colors = DatePickerDefaults.colors(containerColor = NavoraCream),
    ) {
        DatePicker(
            state = state,
            title = {
                Text(
                    text = if (target == DateTarget.Start) "Start date" else "End date",
                    modifier = Modifier.padding(start = 24.dp, top = 16.dp),
                    style = MaterialTheme.typography.titleLarge,
                )
            },
            colors = DatePickerDefaults.colors(
                containerColor = NavoraCard,
                selectedDayContainerColor = NavoraNavy,
                todayDateBorderColor = NavoraNavy,
            ),
        )
    }
}

private fun utcMillisToEpochDay(millis: Long): Long {
    return Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate().toEpochDay()
}

private fun epochDayToUtcMillis(epochDay: Long): Long {
    return LocalDate.ofEpochDay(epochDay).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
}

@Composable
private fun TripField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    hint: String,
    singleLine: Boolean = true,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        placeholder = { Text(hint) },
        singleLine = singleLine,
        minLines = if (singleLine) 1 else 2,
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
    )
}
