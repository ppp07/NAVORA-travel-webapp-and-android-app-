package com.navora.app.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.navora.app.ui.components.DnaBars
import com.navora.app.ui.components.NavoraScreen
import com.navora.app.ui.components.PrimaryButton
import com.navora.app.ui.components.SoftCard
import com.navora.app.ui.session.SessionViewModel
import com.navora.app.ui.session.sessionViewModelFactory
import com.navora.app.ui.theme.NavoraLavender
import com.navora.app.ui.theme.NavoraMuted
import com.navora.app.ui.theme.NavoraNavy
import com.navora.app.ui.theme.NavoraPeach

@Composable
fun ProfileScreen(
    onEditPreferences: () -> Unit,
    viewModel: SessionViewModel = viewModel(factory = sessionViewModelFactory()),
) {
    val snapshot by viewModel.snapshot.collectAsStateWithLifecycle()
    val profile = snapshot.profile
    val initial = profile.name.firstOrNull()?.uppercase() ?: "A"

    NavoraScreen(title = "Profile") {
        SoftCard(container = NavoraLavender) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(NavoraPeach),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = initial, style = MaterialTheme.typography.headlineSmall, color = NavoraNavy)
            }
            Text(
                text = profile.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 12.dp),
            )
            Text(text = profile.city, style = MaterialTheme.typography.bodyLarge)
            Fact("Email", profile.email)
            Fact("Phone", profile.phone)
            Fact("Date of birth", profile.birthDate)
            Fact("Language", profile.language)
        }
        SoftCard {
            Text(text = "${profile.name}'s Travel DNA", style = MaterialTheme.typography.titleLarge)
            Text(
                text = "${profile.style} pace  ·  ${profile.budget}  ·  ${profile.activityLevel} activity",
                style = MaterialTheme.typography.bodyMedium,
                color = NavoraMuted,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
            )
            DnaBars(profile.dna())
            Fact("Food", profile.food)
            Fact("Stay", profile.accommodation)
            Fact("Transport", profile.transport)
            Fact("Travel with", profile.socialStyle)
            Fact("Walking", profile.walking)
            Fact("Rest", profile.rest)
            Fact("Home city", profile.city)
            Fact("Interests", profile.interests.joinToString(", "))
            Fact("Priorities", profile.priorities.joinToString(", "))
            Fact("Access", profile.accessibility.ifEmpty { listOf("None selected") }.joinToString(", "))
            Fact("Pacing", profile.pacingNeeds.ifEmpty { listOf("None selected") }.joinToString(", "))
            Fact("Favorite places", profile.favoriteDestinations.joinToString(", "))
            Fact("Trips", snapshot.trips.size.toString())
            Fact(
                "Travel history",
                snapshot.trips.map { it.destination }.distinct().joinToString(", "),
            )
        }
        PrimaryButton(text = "Edit preferences", onClick = onEditPreferences)
        SoftCard(container = NavoraPeach) {
            Text(text = "Group travel", style = MaterialTheme.typography.titleMedium)
            Text(
                text = "Pace, budget, interests, food, stay, destinations, and access needs are saved together. That is what a group comparison would use.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

@Composable
private fun Fact(label: String, value: String) {
    if (value.isBlank()) return
    Text(
        text = label,
        style = MaterialTheme.typography.labelLarge,
        color = NavoraMuted,
        modifier = Modifier.padding(top = 12.dp),
    )
    Text(text = value, style = MaterialTheme.typography.bodyLarge)
}
