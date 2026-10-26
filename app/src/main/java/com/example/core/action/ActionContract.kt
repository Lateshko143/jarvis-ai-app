package com.example.core.action

import android.content.Context
import org.json.JSONObject

enum class ActionRiskLevel {
    SAFE,                  // Read-only queries (battery, storage, network info)
    CONFIRMATION_REQUIRED, // Launching apps, setting alarms, navigating settings
    HIGH_RISK              // Automated clicks, clearing data, modifying system state
}

data class ActionResult(
    val success: Boolean,
    val feedback: String,
    val payload: Map<String, Any> = emptyMap(),
    val error: Throwable? = null
)

interface ActionContract {
    val identifier: String
    val name: String
    val description: String
    val riskLevel: ActionRiskLevel
    val requiredPermissions: List<String>

    suspend fun execute(context: Context, parameters: JSONObject): ActionResult
    suspend fun verify(context: Context, parameters: JSONObject, result: ActionResult): Boolean
}
