package com.navora.app.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.AltRoute
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.navora.app.ui.components.PrimaryButton
import com.navora.app.ui.theme.NavoraBlue
import com.navora.app.ui.theme.NavoraLavender
import com.navora.app.ui.theme.NavoraMint
import com.navora.app.ui.theme.NavoraMuted
import com.navora.app.ui.theme.NavoraNavy
import com.navora.app.ui.theme.NavoraPeach

private data class OnboardingPage(
    val title: String,
    val body: String,
    val icon: ImageVector,
    val wash: Color,
)

private val pages = listOf(
    OnboardingPage(
        title = "Plan your journey",
        body = "Set a destination, dates, travelers, and the way you like to spend a day. TripSaathi turns that into a plan you can actually follow.",
        icon = Icons.Outlined.Map,
        wash = NavoraBlue,
    ),
    OnboardingPage(
        title = "Adapt when plans change",
        body = "If rain, a closure, or a delay touches one stop, TripSaathi shows what later in the day has to move and rebuilds that part of the trip.",
        icon = Icons.AutoMirrored.Outlined.AltRoute,
        wash = NavoraPeach,
    ),
    OnboardingPage(
        title = "Discover smarter alternatives",
        body = "The new plan keeps what still works and replaces only what no longer fits. You can apply it, or stay with the original.",
        icon = Icons.Outlined.AutoAwesome,
        wash = NavoraMint,
    ),
)

@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    var pageIndex by rememberSaveable { mutableIntStateOf(0) }
    val page = pages[pageIndex]
    val last = pageIndex == pages.lastIndex

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = "${pageIndex + 1} of ${pages.size}",
                style = MaterialTheme.typography.labelLarge,
                color = NavoraMuted,
                modifier = Modifier.align(Alignment.CenterVertically),
            )
            TextButton(onClick = onFinished) { Text("Skip") }
        }
        LinearProgressIndicator(
            progress = { (pageIndex + 1f) / pages.size },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(999.dp)),
            color = NavoraNavy,
            trackColor = NavoraLavender,
        )
        Spacer(modifier = Modifier.height(28.dp))
        Illustration(icon = page.icon, wash = page.wash)
        Spacer(modifier = Modifier.height(28.dp))
        Text(text = page.title, style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = page.body, style = MaterialTheme.typography.bodyLarge, color = NavoraMuted)
        Spacer(modifier = Modifier.weight(1f))
        PrimaryButton(
            text = if (last) "Get started" else "Continue",
            onClick = {
                if (last) onFinished() else pageIndex += 1
            },
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun Illustration(icon: ImageVector, wash: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(168.dp)
                .offset(x = 28.dp, y = 12.dp)
                .clip(CircleShape)
                .background(NavoraLavender),
        )
        Box(
            modifier = Modifier
                .size(148.dp)
                .offset(x = (-24).dp, y = (-8).dp)
                .clip(RoundedCornerShape(40.dp))
                .background(wash),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = NavoraNavy,
                modifier = Modifier.size(56.dp),
            )
        }
    }
}
