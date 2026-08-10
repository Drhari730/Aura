package com.mhealth.aura.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mhealth.aura.ui.components.*
import com.mhealth.aura.ui.theme.*
import com.mhealth.aura.data.location.IndiaLocationRepository

@Composable
fun RegStep1Screen(
    initialEmail: String,
    onNext: (
        name: String,
        age: String,
        gender: String,
        email: String,
        state: String,
        district: String,
        city: String,
        pincode: String
    ) -> Unit
) {
    val context = LocalContext.current
    val locationRepository = remember {
        IndiaLocationRepository(context.applicationContext)
    }
    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Male") }
    var email by remember(initialEmail) { mutableStateOf(initialEmail) }
    var state by remember { mutableStateOf("") }
    var district by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var pincode by remember { mutableStateOf("") }
    val districts = remember(state) { locationRepository.districts(state) }
    val cities = remember(state, district) { locationRepository.cities(state, district) }

    Column(modifier = Modifier.fillMaxSize().background(BackgroundApp)) {
        Column(modifier = Modifier.fillMaxWidth().background(CardWhite).padding(16.dp)) {
            Text("STEP 1 OF 3", style = MaterialTheme.typography.labelMedium.copy(color = TealPrimary), modifier = Modifier.padding(bottom = 4.dp))
            Text("Personal Details", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(8.dp))
            ProgressStepIndicator(step = 1, total = 3)
        }

        Column(
            modifier = Modifier.padding(16.dp).weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            OutlinedTextField(
                value = name, onValueChange = { name = it },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BluePrimary)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = age, onValueChange = { age = it },
                    label = { Text("Age") },
                    modifier = Modifier.weight(1f).padding(bottom = 14.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BluePrimary)
                )
                OutlinedTextField(
                    value = email, onValueChange = { email = it },
                    label = { Text("Email ID") },
                    readOnly = initialEmail.isNotBlank(),
                    modifier = Modifier.weight(2f).padding(bottom = 14.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BluePrimary)
                )
            }
            SectionLabel("Gender")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 14.dp)) {
                listOf("Male", "Female", "Other").forEach { g ->
                    AuraChip(label = g, selected = gender == g, onClick = { gender = g })
                }
            }
            SearchableDropdownField(
                label = "State / Union Territory",
                value = state,
                options = locationRepository.states(),
                onSelected = {
                    state = it
                    district = ""
                    city = ""
                },
                modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp),
            )
            SearchableDropdownField(
                label = "District",
                value = district,
                options = districts,
                onSelected = {
                    district = it
                    city = ""
                },
                enabled = state.isNotBlank(),
                modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp),
            )
            SearchableDropdownField(
                label = "City / Town",
                value = city,
                options = cities,
                onSelected = { city = it },
                enabled = district.isNotBlank(),
                modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp),
            )
            OutlinedTextField(
                value = pincode, onValueChange = { pincode = it },
                label = { Text("Pincode") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BluePrimary)
            )
            Button(
                onClick = {
                    onNext(name, age, gender, email, state, district, city, pincode)
                },
                enabled = name.isNotBlank() && state.isNotBlank() &&
                    district.isNotBlank() && city.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BluePrimary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Next: Medical Details →", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }
        }
    }
}
