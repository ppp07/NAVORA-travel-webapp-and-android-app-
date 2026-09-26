package com.navora.app.ui.trips

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CardTravel
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.navora.app.model.Trip
import com.navora.app.ui.components.NavoraScreen
import com.navora.app.ui.components.PrimaryButton
import com.navora.app.ui.components.SoftCard
import com.navora.app.ui.components.StatusPill
import com.navora.app.ui.components.TripProgress
import com.navora.app.ui.components.label
import com.navora.app.ui.components.wash
import com.navora.app.ui.session.SessionViewModel
import com.navora.app.ui.session.sessionViewModelFactory
import com.navora.app.ui.theme.NavoraMuted
import com.navora.app.ui.theme.NavoraNavy

@Composable
fun MyTripsScreen(
    onOpenTrip: (String) -> Unit,
    viewModel: SessionViewModel = viewModel(factory = sessionViewModelFactory()),
) {
    val snapshot by viewModel.snapshot.collectAsStateWithLifecycle()
    NavoraScreen(title = "My trips") {
        Text(
            text = "Journeys saved on this device.",
            style = MaterialTheme.typography.bodyMedium,
            color = NavoraMuted,
        )
        snapshot.trips.forEach { trip ->
            TripRow(trip = trip, onOpen = { onOpenTrip(trip.id) })
        }
    }
}

@Composable
private fun TripRow(trip: Trip, onOpen: () -> Unit) {
    SoftCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(trip.tone.wash()),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Outlined.CardTravel, contentDescription = null, tint = NavoraNavy)
            }
            androidx.compose.foundation.layout.Column(
                modifier = Modifier
                    .padding(start = 12.dp)
                    .weight(1f),
            ) {
                Text(text = trip.destination, style = MaterialTheme.typography.titleLarge)
                Text(
                    text = "${trip.startDate} – ${trip.endDate}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = NavoraMuted,
                )
            }
        }
        Row(modifier = Modifier.padding(top = 12.dp)) {
            StatusPill(trip.status.label(), trip.tone.wash())
        }
        Text(
            text = "${(trip.progress * 100).toInt()}% of the plan",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(top = 12.dp, bottom = 8.dp),
        )
        TripProgress(trip.progress)
        PrimaryButton(
            text = "Open trip",
            onClick = onOpen,
            modifier = Modifier.padding(top = 14.dp),
        )
    }
}
