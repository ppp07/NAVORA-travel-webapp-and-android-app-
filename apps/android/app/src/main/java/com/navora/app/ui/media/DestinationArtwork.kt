package com.navora.app.ui.media

import com.navora.app.R

fun destinationImage(city: String): Int = when (city) {
    "Goa" -> R.drawable.dest_goa
    "Jaipur" -> R.drawable.dest_jaipur
    "Manali" -> R.drawable.dest_manali
    "Mumbai" -> R.drawable.dest_mumbai
    "Delhi" -> R.drawable.dest_delhi
    "Paris" -> R.drawable.dest_paris
    "Tokyo" -> R.drawable.dest_tokyo
    "Singapore" -> R.drawable.dest_singapore
    "Dubai" -> R.drawable.dest_dubai
    "Bangkok" -> R.drawable.dest_bangkok
    "Bali" -> R.drawable.dest_bali
    "London" -> R.drawable.dest_london
    "Switzerland" -> R.drawable.dest_switzerland
    "Kerala" -> R.drawable.dest_kerala
    "Udaipur" -> R.drawable.dest_udaipur
    "Rishikesh" -> R.drawable.dest_rishikesh
    "Kashmir" -> R.drawable.dest_kashmir
    else -> R.drawable.dest_goa
}
