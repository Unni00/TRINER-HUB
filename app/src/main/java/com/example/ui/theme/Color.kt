package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Minimalist Monochrome Palette: Black, Light Charcoal & Crisp White Type
val PureWhite = Color(0xFFFFFFFF)           // Crisp White Primary Accent & Typography
val LightCharcoal = Color(0xFF22232A)        // Primary Card Surface (Light Charcoal)
val LightCharcoalVariant = Color(0xFF2C2E38) // Secondary Container / Pill Surface
val LightCharcoalHigh = Color(0xFF383A48)    // Elevated Surface / Active Controls
val CharcoalBorder = Color(0xFF3E4150)       // Crisp Charcoal Outlines
val SilverSlate = Color(0xFFE2E8F0)          // Secondary Platinum / Silver Accent
val MutedSilver = Color(0xFFCBD5E1)          // Soft Silver Slate Accent

// Aliases for component compatibility
val NeonGreen = PureWhite                    // Pure White Primary CTA & Selected Elements
val NeonGreenDark = SilverSlate
val NeonGreenContainer = LightCharcoalVariant
val OnNeonGreenContainer = PureWhite

val AthleticOrange = SilverSlate             // Platinum Silver Accent
val AthleticOrangeContainer = LightCharcoalHigh
val OnAthleticOrangeContainer = PureWhite

val AthleticCyan = MutedSilver               // Slate Silver Accent
val AthleticCyanContainer = LightCharcoalVariant

val AlertRed = Color(0xFFFF453A)

// Surfaces & Canvas: Black & Light Charcoal
val DarkBackground = Color(0xFF000000)       // Pure Deep Black Canvas
val DarkSurface = LightCharcoal              // Light Charcoal Cards (#22232A)
val DarkSurfaceVariant = LightCharcoalVariant// Light Charcoal Inputs & Pills (#2C2E38)
val DarkSurfaceHigh = LightCharcoalHigh      // Elevated Light Charcoal (#383A48)

// Typography: White Type
val TextPrimary = Color(0xFFFFFFFF)          // 100% Crisp White Typography
val TextSecondary = Color(0xFFCBD5E1)        // Light Silver / Off-White Subtitles
val TextTertiary = Color(0xFF94A3B8)         // Soft Light Charcoal Gray
val DividerDark = CharcoalBorder             // Minimalist Charcoal Outlines (#3E4150)


