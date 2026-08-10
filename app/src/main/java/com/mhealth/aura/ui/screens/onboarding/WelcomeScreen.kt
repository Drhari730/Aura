package com.mhealth.aura.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mhealth.aura.ui.theme.BackgroundApp
import com.mhealth.aura.ui.theme.BlueLight
import com.mhealth.aura.ui.theme.BluePrimary
import com.mhealth.aura.ui.theme.CardWhite
import com.mhealth.aura.ui.theme.GreenSuccess
import com.mhealth.aura.ui.theme.TealPrimary
import com.mhealth.aura.ui.theme.TextDark
import com.mhealth.aura.ui.theme.TextMedium

@Composable
fun WelcomeScreen(
    onEmailLogin: () -> Unit,
    onNewRegistration: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundApp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(BluePrimary, TealPrimary)))
                .padding(horizontal = 22.dp, vertical = 34.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(Color.White.copy(alpha = 0.18f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("A", color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Aura AMR",
                    color = Color.White,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Login, register, and complete antibiotics safely",
                    color = Color.White.copy(alpha = 0.82f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }

        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                "Start here",
                style = MaterialTheme.typography.headlineSmall,
                color = TextDark
            )
            Text(
                "Use email OTP for secure access. New users continue to the registration forms after OTP verification.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMedium
            )

            Button(
                onClick = onEmailLogin,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BluePrimary,
                    contentColor = Color.White
                )
            ) {
                Icon(Icons.Filled.Email, contentDescription = null)
                Text(
                    "Login with Email OTP",
                    modifier = Modifier.padding(start = 10.dp),
                    fontWeight = FontWeight.Bold
                )
            }

            OutlinedButton(
                onClick = onNewRegistration,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = BluePrimary)
            ) {
                Icon(Icons.Filled.PersonAdd, contentDescription = null)
                Text(
                    "New Patient Registration",
                    modifier = Modifier.padding(start = 10.dp),
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardWhite, RoundedCornerShape(18.dp))
                    .border(1.dp, BluePrimary.copy(alpha = 0.12f), RoundedCornerShape(18.dp))
                    .padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(36.dp).background(BlueLight, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("OTP", color = GreenSuccess, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Column {
                    Text("No password needed", fontWeight = FontWeight.Bold, color = TextDark)
                    Text(
                        "Aura sends a one-time code to the patient email address.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMedium
                    )
                }
            }
        }
    }
}
