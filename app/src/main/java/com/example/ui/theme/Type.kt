package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val MonospaceFontFamily = FontFamily.Monospace

fun createTerminalTypography(fontFamily: FontFamily = MonospaceFontFamily, baseSizeSp: Float = 14f): Typography {
    return Typography(
        displayLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (baseSizeSp * 2.2f).sp,
            lineHeight = (baseSizeSp * 2.6f).sp
        ),
        headlineMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = (baseSizeSp * 1.5f).sp,
            lineHeight = (baseSizeSp * 1.8f).sp
        ),
        titleMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = (baseSizeSp * 1.2f).sp,
            lineHeight = (baseSizeSp * 1.5f).sp
        ),
        bodyLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (baseSizeSp * 1.1f).sp,
            lineHeight = (baseSizeSp * 1.4f).sp,
            letterSpacing = 0.2.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = baseSizeSp.sp,
            lineHeight = (baseSizeSp * 1.35f).sp,
            letterSpacing = 0.2.sp
        ),
        bodySmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (baseSizeSp * 0.85f).sp,
            lineHeight = (baseSizeSp * 1.15f).sp
        ),
        labelLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = (baseSizeSp * 0.95f).sp,
            lineHeight = (baseSizeSp * 1.2f).sp
        ),
        labelSmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = (baseSizeSp * 0.75f).sp,
            lineHeight = (baseSizeSp * 1.0f).sp
        )
    )
}

val Typography = createTerminalTypography()
