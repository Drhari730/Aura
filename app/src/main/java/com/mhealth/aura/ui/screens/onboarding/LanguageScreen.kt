package com.mhealth.aura.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mhealth.aura.ui.theme.*

data class LangOption(val code: String, val nativeName: String, val englishName: String, val script: String)

@Composable
fun LanguageScreen(onLanguageSelected: (String) -> Unit) {
    val languages = listOf(
        LangOption("kn", "ಕನ್ನಡ", "Kannada", "ಕ"),
        LangOption("te", "తెలుగు", "Telugu", "తె"),
        LangOption("hi", "हिंदी", "Hindi", "हि"),
        LangOption("en", "English", "English", "En")
    )
    var selected by remember { mutableStateOf("kn") }

    Column(modifier = Modifier.fillMaxSize().background(BackgroundApp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardWhite)
                .padding(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(BlueLight, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("🌐", fontSize = 24.sp)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text("Choose Your Language", style = MaterialTheme.typography.headlineLarge)
            Text("आपकी भाषा चुनें / ನಿಮ್ಮ ಭಾಷೆ ಆರಿಸಿ", style = MaterialTheme.typography.bodyMedium.copy(color = TextMedium), modifier = Modifier.padding(top = 4.dp))
        }

        Column(
            modifier = Modifier.padding(16.dp).weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            languages.forEach { lang ->
                val isSelected = selected == lang.code
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) BlueLight else CardWhite)
                        .border(2.dp, if (isSelected) BluePrimary else BorderColor, RoundedCornerShape(16.dp))
                        .clickable { selected = lang.code }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(if (isSelected) BluePrimary else BackgroundApp, RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(lang.script, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Color.White else TextMedium)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(lang.nativeName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextDark)
                        Text(lang.englishName, style = MaterialTheme.typography.bodySmall)
                    }
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(BluePrimary, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("✓", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { onLanguageSelected(selected) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BluePrimary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Continue →", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }
        }
    }
}
