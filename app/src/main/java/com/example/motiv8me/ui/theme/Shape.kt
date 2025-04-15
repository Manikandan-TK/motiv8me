package com.example.motiv8me.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Define the Material 3 shape scheme using RoundedCornerShape.
// These values correspond to the different size categories in M3.

val Shapes = Shapes(
    // Used by small components like Chips, Buttons (sometimes)
    extraSmall = RoundedCornerShape(4.dp),

    // Used by medium components like Cards, TextFields
    small = RoundedCornerShape(8.dp),

    // Default shape for many components like Buttons, Dialogs
    medium = RoundedCornerShape(12.dp),

    // Used by larger components like Navigation drawers, Bottom sheets
    large = RoundedCornerShape(16.dp),

    // Used by very large components like Search bars (full width)
    extraLarge = RoundedCornerShape(28.dp)
)