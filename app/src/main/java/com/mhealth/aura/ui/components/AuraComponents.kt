package com.mhealth.aura.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mhealth.aura.ui.theme.*

@Composable
fun AuraCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = CardWhite,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        content = { Column(modifier = Modifier.padding(16.dp), content = content) }
    )
}

@Composable
fun AuraChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    selectedColor: Color = BluePrimary,
    modifier: Modifier = Modifier
) {
    val bg = if (selected) selectedColor else BackgroundApp
    val textColor = if (selected) Color.White else TextMedium
    val borderColor = if (selected) selectedColor else BorderColor

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(100.dp))
            .background(bg)
            .border(1.dp, borderColor, RoundedCornerShape(100.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = textColor,
                fontSize = 12.sp
            )
        )
    }
}

@Composable
fun LabeledField(
    label: String,
    value: String = "",
    placeholder: String = "",
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(bottom = 14.dp)) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(bottom = 5.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(BackgroundApp, RoundedCornerShape(10.dp))
                .border(1.5.dp, BorderColor, RoundedCornerShape(10.dp))
                .padding(horizontal = 14.dp, vertical = 13.dp)
        ) {
            Text(
                text = value.ifEmpty { placeholder },
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = if (value.isEmpty()) TextLight else TextDark
                )
            )
        }
    }
}

@Composable
fun ProgressStepIndicator(step: Int, total: Int) {
    Column(modifier = Modifier.padding(bottom = 18.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Step $step of $total", style = MaterialTheme.typography.bodySmall)
            Text("${(step * 100 / total)}%", style = MaterialTheme.typography.bodySmall.copy(color = BluePrimary, fontWeight = FontWeight.SemiBold))
        }
        LinearProgressIndicator(
            progress = { step.toFloat() / total },
            modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
            color = BluePrimary,
            trackColor = BlueLight
        )
    }
}

@Composable
fun AdherenceRingChart(
    percent: Float,
    label: String,
    sublabel: String = "",
    size: Dp = 120.dp,
    ringColor: Color = BluePrimary,
    trackColor: Color = BlueLight,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.size(size)) {
            val stroke = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
            drawArc(color = trackColor, startAngle = -90f, sweepAngle = 360f, useCenter = false, style = stroke)
            drawArc(color = ringColor, startAngle = -90f, sweepAngle = 360f * percent, useCenter = false, style = stroke)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = (size.value / 5).sp, color = TextDark))
            if (sublabel.isNotEmpty()) {
                Text(sublabel, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp))
            }
        }
    }
}

@Composable
fun StreakBadge(count: Int) {
    Row(
        modifier = Modifier
            .background(AmberLight, RoundedCornerShape(12.dp))
            .border(1.dp, AmberWarning.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text("SAFE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AmberWarning)
        Column {
            Text("$count", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = AmberWarning, fontSize = 16.sp))
            Text("course days", style = MaterialTheme.typography.labelSmall.copy(color = TextLight))
        }
    }
}

@Composable
fun AuraShieldAvatar(size: Dp = 40.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .background(Brush.linearGradient(listOf(BluePrimary, TealPrimary)), RoundedCornerShape(size * 0.3f)),
        contentAlignment = Alignment.Center
    ) {
        Text("🛡", fontSize = (size.value * 0.4f).sp)
    }
}

@Composable
fun LanguagePillRow(selected: String = "kn") {
    val langs = listOf("ಕ" to "kn", "తె" to "te", "हि" to "hi")
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        langs.forEach { (label, code) ->
            val active = selected == code
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .background(if (active) BlueLight else BackgroundApp, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(label, fontSize = 12.sp, fontWeight = if (active) FontWeight.Bold else FontWeight.Normal, color = if (active) BluePrimary else TextLight)
            }
        }
    }
}

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium.copy(color = TextLight),
        modifier = modifier.padding(bottom = 8.dp)
    )
}

@Composable
fun ToggleSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        colors = SwitchDefaults.colors(
            checkedThumbColor = Color.White,
            checkedTrackColor = BluePrimary,
            uncheckedThumbColor = Color.White,
            uncheckedTrackColor = BorderColor
        )
    )
}
