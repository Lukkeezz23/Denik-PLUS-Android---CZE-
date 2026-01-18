// FILE: ui/DetailIconMap.kt
package com.example.denikplus.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

fun detailIconFor(itemId: String): ImageVector {
    return when (itemId) {
        // sexual
        "sex" -> Icons.Default.Favorite
        "masturbation" -> Icons.Default.FavoriteBorder
        "menstruation" -> Icons.Default.WaterDrop
        "orgasm" -> Icons.Default.Bolt
        "contraception" -> Icons.Default.Shield

        // physical
        "walk" -> Icons.Default.DirectionsWalk
        "run" -> Icons.Default.DirectionsRun
        "gym" -> Icons.Default.FitnessCenter
        "bike" -> Icons.Default.DirectionsBike
        "hike" -> Icons.Default.Hiking
        "swim" -> Icons.Default.Pool
        "yoga" -> Icons.Default.SelfImprovement

        // sleep
        "good_sleep" -> Icons.Default.Bedtime
        "bad_sleep" -> Icons.Default.BedtimeOff
        "late" -> Icons.Default.NightsStay
        "early" -> Icons.Default.WbSunny
        "nightmares" -> Icons.Default.DarkMode

        // food
        "healthy" -> Icons.Default.Restaurant
        "fastfood" -> Icons.Default.LunchDining
        "coffee" -> Icons.Default.LocalCafe
        "sweets" -> Icons.Default.Cake
        "alcohol" -> Icons.Default.LocalBar

        // work
        "productive" -> Icons.Default.TaskAlt
        "stress" -> Icons.Default.Warning
        "deadline" -> Icons.Default.Schedule
        "meeting" -> Icons.Default.Groups

        // social
        "family" -> Icons.Default.FamilyRestroom
        "friends" -> Icons.Default.Group
        "party" -> Icons.Default.Celebration
        "alone" -> Icons.Default.Person

        // health
        "pain" -> Icons.Default.Healing
        "ill" -> Icons.Default.Sick
        "meds" -> Icons.Default.Medication
        "doctor" -> Icons.Default.MedicalServices

        // brainstorm
        "idea" -> Icons.Default.Lightbulb
        "project" -> Icons.Default.Assignment
        "goal" -> Icons.Default.Flag

        // other
        "travel" -> Icons.Default.Flight
        "shopping" -> Icons.Default.ShoppingCart
        "finance" -> Icons.Default.AttachMoney
        "cleaning" -> Icons.Default.CleaningServices

        else -> Icons.Default.Label
    }
}
