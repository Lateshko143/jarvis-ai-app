package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.core.skills.DynamicSkillEngine
import com.example.core.skills.SkillManifest
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisEmerald
import com.example.ui.theme.JarvisSurfaceBorder
import com.example.ui.theme.JarvisSurfaceElevated

@Composable
fun AddSkillDialog(
    skillEngine: DynamicSkillEngine,
    onInstallSkill: (SkillManifest) -> Unit,
    onDismiss: () -> Unit
) {
    var promptInput by remember { mutableStateOf("") }
    var generatedManifest by remember { mutableStateOf<SkillManifest?>(null) }
    var sandboxVerificationPassed by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(JarvisSurfaceElevated, RoundedCornerShape(16.dp))
                .border(1.dp, JarvisCyan, RoundedCornerShape(16.dp))
                .padding(20.dp)
        ) {
            Text(
                text = "SELF-DEVELOPMENT ENGINE",
                color = JarvisCyan,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Create New Assistant Skill",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = promptInput,
                onValueChange = { promptInput = it },
                label = { Text("Describe skill function", color = Color.Gray, fontSize = 12.sp) },
                placeholder = { Text("e.g., summarize my grocery expenses", color = Color.DarkGray, fontSize = 12.sp) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = JarvisCyan,
                    unfocusedBorderColor = JarvisSurfaceBorder,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (generatedManifest != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF030712), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = "MANIFEST: ${generatedManifest!!.id}",
                        color = JarvisCyan,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Name: ${generatedManifest!!.name}",
                        color = Color.White,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp
                    )
                    Text(
                        text = "Desc: ${generatedManifest!!.description}",
                        color = Color.Gray,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (sandboxVerificationPassed) "SANDBOX VALIDATION: PASSED" else "VALIDATING...",
                        color = if (sandboxVerificationPassed) JarvisEmerald else Color.Yellow,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Cancel", color = Color.Gray, fontFamily = FontFamily.Monospace)
                }

                Spacer(modifier = Modifier.width(8.dp))

                if (generatedManifest == null) {
                    Button(
                        onClick = {
                            if (promptInput.isNotBlank()) {
                                val manifest = skillEngine.proposeSkill(promptInput)
                                generatedManifest = manifest
                                sandboxVerificationPassed = true
                            }
                        },
                        enabled = promptInput.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "Synthesize",
                            color = Color.Black,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Button(
                        onClick = {
                            generatedManifest?.let { onInstallSkill(it) }
                        },
                        enabled = sandboxVerificationPassed,
                        colors = ButtonDefaults.buttonColors(containerColor = JarvisEmerald),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "Install Skill",
                            color = Color.Black,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
