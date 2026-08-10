package com.mhealth.aura.ui.screens.onboarding

import android.util.Patterns
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mhealth.aura.BuildConfig
import com.mhealth.aura.data.remote.BrevoOtpService
import com.mhealth.aura.ui.theme.BackgroundApp
import com.mhealth.aura.ui.theme.BlueLight
import com.mhealth.aura.ui.theme.BluePrimary
import com.mhealth.aura.ui.theme.BorderColor
import com.mhealth.aura.ui.theme.CardWhite
import com.mhealth.aura.ui.theme.TextDark

import kotlinx.coroutines.launch

private const val DEMO_OTP = "123456"

@Composable
fun OtpScreen(
    authService: BrevoOtpService? = null,
    onVerified: (String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var otpSent by remember { mutableStateOf(false) }
    var otp by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    val otpFocusRequester = remember { FocusRequester() }
    val scope = rememberCoroutineScope()
    val useBrevoOtp = authService?.isConfigured == true

    LaunchedEffect(otpSent) {
        if (otpSent) otpFocusRequester.requestFocus()
    }

    Column(modifier = Modifier.fillMaxSize().background(BackgroundApp)) {
        Column(
            modifier = Modifier.fillMaxWidth().background(CardWhite).padding(20.dp)
        ) {
            Box(
                modifier = Modifier.size(52.dp).background(BlueLight, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Email,
                    contentDescription = null,
                    tint = BluePrimary,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text("Verify Your Email", style = MaterialTheme.typography.headlineLarge)
            Text(
                "Aura will email a 6-digit code for secure in-app verification.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Column(
            modifier = Modifier.padding(20.dp).weight(1f),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it.trim()
                    error = null
                    if (otpSent) {
                        otpSent = false
                        otp = ""
                    }
                },
                label = { Text("Email address") },
                placeholder = { Text("name@example.com") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = error != null,
                supportingText = error?.let { message -> { Text(message) } },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BluePrimary)
            )

            Button(
                onClick = {
                    if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        error = "Enter a valid email address"
                        return@Button
                    }
                    scope.launch {
                        loading = true
                        error = null
                        val result = if (useBrevoOtp) {
                            authService.sendEmailOtp(email.lowercase())
                        } else {
                            Result.success(Unit)
                        }
                        loading = false
                        if (result.isSuccess) {
                            otpSent = true
                            otp = ""
                        } else {
                            if (BuildConfig.DEBUG) {
                                otpSent = true
                                otp = ""
                                error = result.exceptionOrNull()?.message
                                    ?: "Live Brevo OTP is not available yet. Use 123456 for this debug build."
                            } else {
                                error = result.exceptionOrNull()?.message
                                    ?: "Unable to send OTP. Please try again."
                            }
                        }
                    }
                },
                enabled = !loading,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BluePrimary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    when {
                        loading -> "Please wait..."
                        otpSent -> "Send code again"
                        else -> "Email me a 6-digit code"
                    },
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
            }

            if (otpSent) {
                Divider(color = BorderColor)
                Text("Enter OTP", style = MaterialTheme.typography.titleMedium)
                Text(
                    "6-digit code sent to $email",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    if (useBrevoOtp) {
                        "Check your email inbox for the Aura AMR verification code."
                    } else {
                        "Demo build code: $DEMO_OTP"
                    },
                    color = BluePrimary,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                )
                if (useBrevoOtp) {
                    if (BuildConfig.DEBUG) {
                        Text(
                            "Debug testing only: use 123456 only if the Brevo function is not deployed yet.",
                            color = BluePrimary,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { otpFocusRequester.requestFocus() }
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        repeat(6) { index ->
                            val char = otp.getOrNull(index)?.toString().orEmpty()
                            val isCurrent = otp.length == index
                            Box(
                                modifier = Modifier
                                    .size(44.dp, 52.dp)
                                    .background(
                                        if (char.isEmpty()) BackgroundApp else BlueLight,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .border(
                                        2.dp,
                                        if (isCurrent) BluePrimary else BorderColor,
                                        RoundedCornerShape(12.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    char,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDark
                                )
                            }
                        }
                    }
                    BasicTextField(
                        value = otp,
                        onValueChange = { value ->
                            otp = value.filter(Char::isDigit).take(6)
                            error = null
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier
                            .size(1.dp)
                            .focusRequester(otpFocusRequester)
                    )
                }

                Button(
                    onClick = {
                        scope.launch {
                            loading = true
                            error = null
                            if (useBrevoOtp) {
                                if (BuildConfig.DEBUG && otp == DEMO_OTP) {
                                    loading = false
                                    onVerified(email.lowercase())
                                } else {
                                    val result = authService.verifyEmailOtp(email.lowercase(), otp)
                                    loading = false
                                    if (result.isSuccess) {
                                        onVerified(email.lowercase())
                                    } else {
                                        error = result.exceptionOrNull()?.message
                                            ?: "Incorrect or expired OTP. Request a fresh code and enter the 6 digits from the Brevo email."
                                    }
                                }
                            } else {
                                loading = false
                                if (otp == DEMO_OTP) {
                                    onVerified(email.lowercase())
                                } else {
                                    error = "Incorrect OTP. Use $DEMO_OTP for this demo build."
                                }
                            }
                        }
                    },
                    enabled = otp.length == 6 && !loading,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BluePrimary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("Verify & Continue", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                }
            }

            error?.takeIf { otpSent }?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }

            Text(
                "By continuing you agree to Aura's Terms & Privacy Policy",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
