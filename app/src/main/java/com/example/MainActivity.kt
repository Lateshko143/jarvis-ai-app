package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.components.ActionConfirmationDialog
import com.example.ui.screens.ActionsScreen
import com.example.ui.screens.DiagnosticsScreen
import com.example.ui.screens.HudScreen
import com.example.ui.screens.MemoryScreen
import com.example.ui.screens.SkillsScreen
import com.example.ui.theme.JarvisBackground
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisSurface
import com.example.ui.theme.JarvisSurfaceBorder
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        viewModel.initVoiceEngine()

        setContent {
            MyApplicationTheme {
                JarvisAppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun JarvisAppContent(viewModel: MainViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()
    val pendingChallenge by viewModel.pendingChallenge.collectAsState()

    // BackHandler to navigate back to HUD tab if currently on another tab
    if (currentTab != 0) {
        BackHandler {
            viewModel.selectTab(0)
        }
    }

    Scaffold(
        containerColor = JarvisBackground,
        bottomBar = {
            NavigationBar(
                containerColor = JarvisSurface,
                modifier = Modifier.navigationBarsPadding(),
                tonalElevation = 8.dp
            ) {
                val navItems = listOf(
                    Triple(0, "HUD", Icons.Default.GraphicEq),
                    Triple(1, "Actions", Icons.Default.ListAlt),
                    Triple(2, "Memory", Icons.Default.Memory),
                    Triple(3, "Skills", Icons.Default.AutoAwesome),
                    Triple(4, "System", Icons.Default.Security)
                )

                navItems.forEach { (index, label, icon) ->
                    val isSelected = currentTab == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { viewModel.selectTab(index) },
                        icon = {
                            Icon(
                                icon,
                                contentDescription = label,
                                tint = if (isSelected) JarvisCyan else Color.Gray
                            )
                        },
                        label = {
                            Text(
                                text = label,
                                color = if (isSelected) JarvisCyan else Color.Gray,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color(0xFF0C2442)
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (currentTab) {
                0 -> HudScreen(viewModel = viewModel)
                1 -> ActionsScreen(viewModel = viewModel)
                2 -> MemoryScreen(viewModel = viewModel)
                3 -> SkillsScreen(viewModel = viewModel)
                4 -> DiagnosticsScreen(viewModel = viewModel)
            }
        }
    }

    // Modal Confirmation Dialog for risky/phone-control actions
    pendingChallenge?.let { challenge ->
        ActionConfirmationDialog(
            actionName = challenge.actionName,
            identifier = challenge.actionIdentifier,
            riskLevel = challenge.riskLevel,
            prompt = challenge.rawPrompt,
            onConfirm = { viewModel.confirmChallenge() },
            onDismiss = { viewModel.dismissChallenge() }
        )
    }
}
