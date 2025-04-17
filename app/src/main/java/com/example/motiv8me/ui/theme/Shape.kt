package com.example.motiv8me.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// --- MODERN, PLAYFUL SHAPE SYSTEM ---
val Shapes = Shapes(
    extraSmall = RoundedCornerShape(12.dp), // Chips, small buttons
    small = RoundedCornerShape(20.dp),      // Cards, text fields
    medium = RoundedCornerShape(28.dp),     // Buttons, dialogs
    large = RoundedCornerShape(40.dp),      // Bottom sheets, nav drawers
    extraLarge = RoundedCornerShape(60.dp)  // Full-width search bars, banners
)