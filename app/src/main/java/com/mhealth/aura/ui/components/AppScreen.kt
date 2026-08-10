package com.mhealth.aura.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mhealth.aura.ui.theme.BackgroundApp
import com.mhealth.aura.ui.theme.BluePrimary
import com.mhealth.aura.ui.theme.CardWhite

@Composable
fun AppScreen(
    title: String,
    subtitle: String = "",
    onBack: () -> Unit,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().background(BackgroundApp)) {
        Row(
            modifier = Modifier.fillMaxWidth().background(CardWhite).padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Back",
                color = BluePrimary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable(onClick = onBack).padding(end = 16.dp)
            )
            Column {
                Text(title, style = MaterialTheme.typography.headlineSmall)
                if (subtitle.isNotBlank()) {
                    Text(subtitle, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        content()
    }
}
