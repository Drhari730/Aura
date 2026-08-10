package com.mhealth.aura.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val AuraTypography = Typography(
    headlineLarge = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Unspecified, lineHeight = 30.sp),
    headlineMedium = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Unspecified),
    headlineSmall = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color.Unspecified),
    titleLarge = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.Unspecified),
    titleMedium = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.Unspecified),
    titleSmall = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.Unspecified),
    bodyLarge = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal, color = Color.Unspecified, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal, color = Color.Unspecified, lineHeight = 21.sp),
    bodySmall = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Normal, color = Color.Unspecified),
    labelLarge = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold),
    labelMedium = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.Unspecified, letterSpacing = 0.6.sp),
    labelSmall = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Normal, color = Color.Unspecified)
)
