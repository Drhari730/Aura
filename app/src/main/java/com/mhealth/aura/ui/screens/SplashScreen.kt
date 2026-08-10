package com.mhealth.aura.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mhealth.aura.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    isOnboardingDone: Boolean,
    onSplashDone: (Boolean) -> Unit
) {
    LaunchedEffect(isOnboardingDone) {
        delay(1800)
        onSplashDone(isOnboardingDone)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.linearGradient(listOf(BluePrimary, TealPrimary))),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(28.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("🛡", fontSize = 44.sp)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Aura",
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = (-1).sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "FINISH YOUR COURSE. PROTECT THE FUTURE.",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    letterSpacing = 1.4.sp,
                    textAlign = TextAlign.Center
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf("ಕ", "తె", "हि", "En").forEach { lang ->
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(13.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(lang, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(false, true, false).forEach { active ->
                    Box(
                        modifier = Modifier
                            .width(if (active) 24.dp else 8.dp)
                            .height(8.dp)
                            .background(if (active) Color.White else Color.White.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                    )
                }
            }

            Box(
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(100.dp))
                    .padding(horizontal = 18.dp, vertical = 6.dp)
            ) {
                Text(
                    "Initiative by Ministry of Health · India",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.65f)
                )
            }
        }
    }
}
