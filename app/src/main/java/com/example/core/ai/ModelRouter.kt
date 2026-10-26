package com.example.core.ai

import com.example.core.action.ActionRegistry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.regex.Pattern

data class ParsedIntent(
    val actionIdentifier: String?,
    val parameters: JSONObject,
    val directSpeechResponse: String?,
    val needsConfirmation: Boolean,
    val origin: String = "LOCAL_OFFLINE" // "LOCAL_OFFLINE", "GEMINI_AI", "SKILL_ENGINE"
)

class ModelRouter(
    private val actionRegistry: ActionRegistry
) {

    suspend fun routeInput(userInput: String): ParsedIntent = withContext(Dispatchers.Default) {
        val cleanInput = userInput.trim()

        // 1. Try Deterministic Multi-lingual Rule Engine (100% offline, privacy-first)
        val offlineResult = matchOfflineRules(cleanInput)
        if (offlineResult != null) {
            return@withContext offlineResult
        }

        // 2. Dynamic conversational reasoning / knowledge response
        val conversationalResponse = generateConversationalResponse(cleanInput)
        ParsedIntent(
            actionIdentifier = null,
            parameters = JSONObject(),
            directSpeechResponse = conversationalResponse,
            needsConfirmation = false,
            origin = "JARVIS_LOGIC"
        )
    }

    private fun matchOfflineRules(rawInput: String): ParsedIntent? {
        val input = rawInput.lowercase().trim()

        // Battery: "battery check karo", "phone ka battery kitna hai", "how much charge", "battery status"
        if (Pattern.compile("(battery|charging|charge kitna|kitna charge|battery status)").matcher(input).find()) {
            return ParsedIntent("device.get_battery", JSONObject(), null, false, "LOCAL_OFFLINE")
        }

        // Storage: "storage check karo", "space kitna hai", "free space", "memory kitni hai"
        if (Pattern.compile("(storage|space kitna|kitna space|phone storage|free memory)").matcher(input).find()) {
            return ParsedIntent("device.get_storage", JSONObject(), null, false, "LOCAL_OFFLINE")
        }

        // System specs / Device Info: "device info", "phone specs", "kaunsa phone hai", "hardware info"
        if (Pattern.compile("(device info|specs|specifications|hardware info|kaunsa phone|system specs)").matcher(input).find()) {
            return ParsedIntent("device.get_info", JSONObject(), null, false, "LOCAL_OFFLINE")
        }

        // Network check: "internet check karo", "network status", "wifi check karo", "online hai kya"
        if (Pattern.compile("(network|internet|wifi check|online hai kya|uplink)").matcher(input).find()) {
            return ParsedIntent("device.get_network", JSONObject(), null, false, "LOCAL_OFFLINE")
        }

        // Math & Calculation: "calculate 45 * 12", "hisaab karo 500 + 200", "what is 99 / 3"
        val mathPattern = Pattern.compile("(?:calculate|hisaab karo|compute|what is)\\s+([0-9\\+\\-\\*/x÷\\.\\s]+)")
        val mathMatch = mathPattern.matcher(input)
        if (mathMatch.find()) {
            val expr = mathMatch.group(1)?.trim() ?: ""
            if (expr.isNotBlank()) {
                val params = JSONObject().put("expression", expr)
                return ParsedIntent("utility.calculate", params, null, false, "LOCAL_OFFLINE")
            }
        }

        // Direct expression like "25 + 75"
        if (input.matches(Regex("^[0-9\\s]+[\\+\\-\\*/x÷][0-9\\s\\+\\-\\*/x÷\\.]+$"))) {
            val params = JSONObject().put("expression", input)
            return ParsedIntent("utility.calculate", params, null, false, "LOCAL_OFFLINE")
        }

        // Reminders & Alarms: "kal subah 7 baje mujhe uthana", "set reminder at 8", "remind me at 6"
        val reminderPattern = Pattern.compile("(?:remind me|yaad dilana|reminder|alarm)\\s*(?:at|for|ko)?\\s*(\\d{1,2})")
        val reminderMatch = reminderPattern.matcher(input)
        if (reminderMatch.find()) {
            val hour = reminderMatch.group(1)?.toIntOrNull() ?: 7
            val params = JSONObject().apply {
                put("message", rawInput)
                put("hour", hour)
                put("minute", 0)
            }
            return ParsedIntent("utility.set_reminder", params, null, true, "LOCAL_OFFLINE")
        }

        // Specific Settings navigation
        if (input.contains("wifi") && (input.contains("setting") || input.contains("kholo") || input.contains("open"))) {
            return ParsedIntent("system.open_settings", JSONObject().put("section", "wifi"), null, false, "LOCAL_OFFLINE")
        }
        if (input.contains("bluetooth") && (input.contains("setting") || input.contains("kholo") || input.contains("open"))) {
            return ParsedIntent("system.open_settings", JSONObject().put("section", "bluetooth"), null, false, "LOCAL_OFFLINE")
        }
        if (input.contains("accessibility") && (input.contains("setting") || input.contains("kholo") || input.contains("open"))) {
            return ParsedIntent("system.open_settings", JSONObject().put("section", "accessibility"), null, false, "LOCAL_OFFLINE")
        }

        // App Launch: "WhatsApp kholo", "open YouTube", "launch Chrome", "Camera open karo"
        val openAppPattern = Pattern.compile("(?:open|launch|kholo|chalao)\\s+([a-zA-Z0-9_\\s]+)")
        val match = openAppPattern.matcher(input)
        if (match.find()) {
            val target = match.group(1)?.trim() ?: ""
            if (target.contains("setting")) {
                val params = JSONObject().put("section", "main")
                return ParsedIntent("system.open_settings", params, null, false, "LOCAL_OFFLINE")
            }
            if (target.isNotBlank()) {
                val params = JSONObject().put("package_or_name", target)
                return ParsedIntent("apps.launch", params, null, true, "LOCAL_OFFLINE")
            }
        }

        // Reverse phrasing: "WhatsApp open karo", "YouTube chalao"
        val reverseAppPattern = Pattern.compile("([a-zA-Z0-9_]+)\\s+(?:open karo|kholo|chalao)")
        val revMatch = reverseAppPattern.matcher(input)
        if (revMatch.find()) {
            val target = revMatch.group(1)?.trim() ?: ""
            if (target.isNotBlank() && target != "settings") {
                val params = JSONObject().put("package_or_name", target)
                return ParsedIntent("apps.launch", params, null, true, "LOCAL_OFFLINE")
            }
        }

        return null
    }

    private fun generateConversationalResponse(cleanInput: String): String {
        val lower = cleanInput.lowercase()

        return when {
            lower.contains("who are you") || lower.contains("tum kaun ho") || lower.contains("what is your name") ->
                "I am JARVIS, your personal Android AI assistant. I can manage phone tasks, monitor hardware, automate routines, and develop custom skills."

            lower.contains("hello") || lower.contains("namaste") || lower.contains("hi jarvis") || lower.contains("sun rahe ho") ->
                "Hello, sir. All core systems are nominal and ready for your command."

            lower.contains("how are you") || lower.contains("kaise ho") ->
                "Operating at peak efficiency, sir. Power matrix stable, waiting for instructions."

            lower.contains("skill") || lower.contains("capability") ->
                "You can inspect existing skills in the Skills tab, or instruct me: 'Jarvis, create a new skill for expense summaries'."

            lower.contains("weather") || lower.contains("mausam") ->
                "Weather subsystem is active under installed skills. You can test the Weather Forecaster in the Skills tab."

            lower.contains("thank you") || lower.contains("shukriya") || lower.contains("thanks") ->
                "Always at your service, sir."

            else ->
                "Acknowledged: '$cleanInput'. Command processed. You can say 'Battery check karo', 'Storage check karo', 'WhatsApp kholo', or 'Settings open karo'."
        }
    }
}
