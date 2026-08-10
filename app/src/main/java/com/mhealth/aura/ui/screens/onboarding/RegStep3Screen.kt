package com.mhealth.aura.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mhealth.aura.ui.components.ProgressStepIndicator
import com.mhealth.aura.ui.components.SectionLabel
import com.mhealth.aura.ui.theme.BackgroundApp
import com.mhealth.aura.ui.theme.BluePrimary
import com.mhealth.aura.ui.theme.CardWhite
import com.mhealth.aura.ui.theme.GreenLight
import com.mhealth.aura.ui.theme.GreenSuccess
import com.mhealth.aura.ui.theme.TealPrimary

@Composable
fun RegStep3Screen(
    onComplete: (doctorName: String, hospital: String, hospitalLocation: String) -> Unit
) {
    var doctorName by remember { mutableStateOf("") }
    var hospital by remember { mutableStateOf("") }
    var hospitalLocation by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().background(BackgroundApp)) {
        Column(modifier = Modifier.fillMaxWidth().background(CardWhite).padding(16.dp)) {
            Text(
                "STEP 3 OF 3",
                style = MaterialTheme.typography.labelMedium.copy(color = TealPrimary),
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text("Doctor & Hospital", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(8.dp))
            ProgressStepIndicator(step = 3, total = 3)
        }

        Column(
            modifier = Modifier
                .padding(16.dp)
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            SectionLabel("Treating Doctor (Optional)")
            OutlinedTextField(
                value = doctorName,
                onValueChange = { doctorName = it },
                label = { Text("Doctor name") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BluePrimary)
            )
            OutlinedTextField(
                value = hospital,
                onValueChange = { hospital = it },
                label = { Text("Hospital / Clinic") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BluePrimary)
            )
            OutlinedTextField(
                value = hospitalLocation,
                onValueChange = { hospitalLocation = it },
                label = { Text("Hospital Location") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BluePrimary)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GreenLight, RoundedCornerShape(12.dp))
                    .border(1.dp, GreenSuccess.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Text(
                    "This care-contact information is stored in Aura for your own reference. " +
                        "Nothing is sent externally without a configured clinical service.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF065F46),
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
            Button(
                onClick = { onComplete(doctorName, hospital, hospitalLocation) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
            ) {
                Text(
                    "Complete Registration",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
            }
        }
    }
}
