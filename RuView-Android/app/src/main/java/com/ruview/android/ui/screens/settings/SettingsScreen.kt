package com.ruview.android.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ruview.android.data.websocket.WsState
import com.ruview.android.ui.components.ConnectionBanner
import com.ruview.android.ui.theme.Background
import com.ruview.android.ui.theme.CardBackground
import com.ruview.android.ui.theme.CardBorder
import com.ruview.android.ui.theme.Primary
import com.ruview.android.ui.theme.StatusGreen
import com.ruview.android.ui.theme.StatusRed
import com.ruview.android.ui.theme.StatusYellow
import com.ruview.android.ui.theme.TextPrimary
import com.ruview.android.ui.theme.TextSecondary
import com.ruview.android.viewmodel.RuViewViewModel

@Composable
fun SettingsScreen(viewModel: RuViewViewModel = hiltViewModel()) {
    val serverUrl by viewModel.serverUrl.collectAsState()
    val wsState by viewModel.wsState.collectAsState()
    val showSignalOverlay by viewModel.showSignalOverlay.collectAsState()
    val alertsEnabled by viewModel.alertsEnabled.collectAsState()
    val frame by viewModel.sensingFrame.collectAsState()

    var urlInput by remember(serverUrl) { mutableStateOf(serverUrl) }
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState())
    ) {
        ConnectionBanner(wsState = wsState, isSimulated = frame.simulated)

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "SETTINGS",
            color = Primary,
            fontSize = 12.sp,
            letterSpacing = 2.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Server configuration
        SettingsSection(title = "SERVER") {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = urlInput,
                    onValueChange = { urlInput = it },
                    label = { Text("Server URL", fontFamily = FontFamily.Monospace, fontSize = 12.sp) },
                    placeholder = { Text("http://192.168.1.100:3000", fontFamily = FontFamily.Monospace, fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Link, contentDescription = null, tint = Primary) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        focusManager.clearFocus()
                        viewModel.updateServerUrl(urlInput)
                    }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = CardBorder,
                        focusedLabelColor = Primary,
                        cursorColor = Primary,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.updateServerUrl(urlInput)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Wifi, contentDescription = null, tint = androidx.compose.ui.graphics.Color.Black)
                        Text(" Connect", color = androidx.compose.ui.graphics.Color.Black, fontFamily = FontFamily.Monospace)
                    }
                    Button(
                        onClick = { viewModel.disconnect() },
                        colors = ButtonDefaults.buttonColors(containerColor = CardBorder),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Disconnect", color = TextPrimary, fontFamily = FontFamily.Monospace)
                    }
                }

                // Connection status indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val (color, text) = when (wsState) {
                        WsState.CONNECTED    -> StatusGreen to "Connected"
                        WsState.CONNECTING   -> StatusYellow to "Connecting..."
                        WsState.RECONNECTING -> StatusYellow to "Reconnecting..."
                        WsState.SIMULATED    -> StatusRed to "Using simulated data"
                        WsState.DISCONNECTED -> TextSecondary to "Disconnected"
                    }
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = color, modifier = Modifier.padding(0.dp))
                    Text(text, color = color, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Display settings
        SettingsSection(title = "DISPLAY") {
            Column {
                SettingsToggle(
                    label = "Signal Strength Overlay",
                    description = "Show RSSI and signal metrics on live view",
                    checked = showSignalOverlay,
                    onCheckedChange = { viewModel.toggleSignalOverlay() }
                )
                HorizontalDivider(color = CardBorder, modifier = Modifier.padding(vertical = 8.dp))
                SettingsToggle(
                    label = "Alert Notifications",
                    description = "Receive alerts for MAT events",
                    checked = alertsEnabled,
                    onCheckedChange = { viewModel.toggleAlerts() }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // About
        SettingsSection(title = "ABOUT") {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                InfoRow("App Version", "1.0.0")
                InfoRow("Protocol", "REST + WebSocket")
                InfoRow("WS Endpoint", "/ws/sensing")
                InfoRow("Update Rate", "20 Hz")
                InfoRow("Keypoints", "17 COCO keypoints")
                InfoRow("Sensor Mode", if (frame.simulated) "Simulated" else "ESP32 Mesh")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = title,
            color = TextSecondary,
            fontSize = 10.sp,
            letterSpacing = 1.sp,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.height(8.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardBackground, RoundedCornerShape(12.dp))
                .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            content()
        }
    }
}

@Composable
private fun SettingsToggle(
    label: String,
    description: String,
    checked: Boolean,
    onCheckedChange: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = TextPrimary, fontSize = 14.sp, fontFamily = FontFamily.Monospace)
            Text(description, color = TextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
        }
        Switch(
            checked = checked,
            onCheckedChange = { onCheckedChange() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Primary,
                checkedTrackColor = Primary.copy(alpha = 0.3f),
                uncheckedThumbColor = TextSecondary,
                uncheckedTrackColor = CardBorder
            )
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = TextSecondary, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
        Text(value, color = TextPrimary, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
    }
}
