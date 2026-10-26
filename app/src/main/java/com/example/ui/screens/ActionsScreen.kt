package com.example.ui.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.action.ActionRiskLevel
import com.example.ui.MainViewModel
import com.example.ui.theme.JarvisAmber
import com.example.ui.theme.JarvisBackground
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisEmerald
import com.example.ui.theme.JarvisRedAlert
import com.example.ui.theme.JarvisSurfaceBorder
import com.example.ui.theme.JarvisSurfaceElevated

@Composable
fun ActionsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val actions = viewModel.getAllActions()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(JarvisBackground)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "ACTION REGISTRY",
                    color = JarvisCyan,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Declared & sandboxed phone integrations",
                    color = Color.Gray,
                    fontSize = 11.sp
                )
            }
            Box(
                modifier = Modifier
                    .background(Color(0xFF0C1B33), RoundedCornerShape(8.dp))
                    .border(1.dp, JarvisSurfaceBorder, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${actions.size} SUBSYSTEMS",
                    color = JarvisCyan,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(actions) { action ->
                val riskColor = when (action.riskLevel) {
                    ActionRiskLevel.SAFE -> JarvisEmerald
                    ActionRiskLevel.CONFIRMATION_REQUIRED -> JarvisAmber
                    ActionRiskLevel.HIGH_RISK -> JarvisRedAlert
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(JarvisSurfaceElevated, RoundedCornerShape(12.dp))
                        .border(1.dp, JarvisSurfaceBorder, RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = action.name,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Box(
                                modifier = Modifier
                                    .background(riskColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                    .border(1.dp, riskColor.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = action.riskLevel.name,
                                    color = riskColor,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "ID: ${action.identifier}",
                            color = JarvisCyan,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = action.description,
                            color = Color(0xFFCBD5E1),
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = {
                                    // Test trigger the action
                                    when (action.identifier) {
                                        "device.get_battery" -> viewModel.processUserCommand("Battery check karo")
                                        "device.get_storage" -> viewModel.processUserCommand("Storage check karo")
                                        "device.get_info" -> viewModel.processUserCommand("Device info")
                                        "device.get_network" -> viewModel.processUserCommand("Network status")
                                        "apps.launch" -> viewModel.processUserCommand("Settings kholo")
                                        "system.open_settings" -> viewModel.processUserCommand("Open settings")
                                        "utility.calculate" -> viewModel.processUserCommand("Calculate 25 * 14")
                                        "utility.set_reminder" -> viewModel.processUserCommand("Remind me at 8")
                                        else -> viewModel.processUserCommand(action.name)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F2648)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.PlayArrow,
                                    contentDescription = "Test Execute",
                                    tint = JarvisCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Execute Test",
                                    color = JarvisCyan,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
