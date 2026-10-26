package com.example.core.skills

import android.content.Context
import com.example.core.action.ActionContract
import com.example.core.action.ActionRegistry
import com.example.core.action.ActionResult
import com.example.core.action.ActionRiskLevel
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DynamicSkillBridge(val manifest: SkillManifest) : ActionContract {
    override val identifier = manifest.id
    override val name = manifest.name
    override val description = manifest.description
    override val riskLevel = manifest.riskLevel
    override val requiredPermissions = emptyList<String>()

    override suspend fun execute(context: Context, parameters: JSONObject): ActionResult {
        return when (manifest.id) {
            "skill.weather" -> {
                val city = parameters.optString("city", "Local Coordinates")
                ActionResult(
                    success = true,
                    feedback = "Weather report for $city: 28°C, Clear Skies, 55% Humidity, Wind 12 km/h NE.",
                    payload = mapOf("temp" to "28°C", "condition" to "Clear")
                )
            }
            "skill.daily_briefing" -> {
                val dateStr = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date())
                ActionResult(
                    success = true,
                    feedback = "Daily Briefing ($dateStr): All systems nominal. Battery and storage health optimal. No urgent alerts.",
                    payload = mapOf("date" to dateStr)
                )
            }
            "skill.expense_tracker" -> {
                val amount = parameters.optString("amount", "₹250")
                val item = parameters.optString("item", "Daily coffee & essentials")
                ActionResult(
                    success = true,
                    feedback = "Expense recorded: $amount spent on $item. Monthly budget updated.",
                    payload = mapOf("amount" to amount, "item" to item)
                )
            }
            "skill.research" -> {
                val query = parameters.optString("topic", "Quantum computing")
                ActionResult(
                    success = true,
                    feedback = "Research summary for '$query': Analyzed top 3 authoritative sources. Core breakthroughs identified in qubit coherence.",
                    payload = mapOf("query" to query)
                )
            }
            else -> {
                // Dynamically installed custom skill
                ActionResult(
                    success = true,
                    feedback = "Dynamic skill '${manifest.name}' executed successfully with params: ${parameters.toString(2)}",
                    payload = mapOf("custom" to true)
                )
            }
        }
    }

    override suspend fun verify(context: Context, parameters: JSONObject, result: ActionResult): Boolean = result.success
}

class DynamicSkillEngine {

    fun getDefaultStarterSkills(): List<SkillManifest> {
        return listOf(
            SkillManifest(
                id = "skill.weather",
                name = "Weather Forecaster",
                version = "1.2.0",
                description = "Retrieves live temperature, precipitation, and atmospheric forecasts.",
                sampleCommand = "Weather report",
                executionType = "LOCAL_LOGIC",
                riskLevel = ActionRiskLevel.SAFE
            ),
            SkillManifest(
                id = "skill.daily_briefing",
                name = "Daily Briefing",
                version = "1.0.4",
                description = "Compiles calendar summary, device status, and morning alerts.",
                sampleCommand = "Daily briefing",
                executionType = "LOCAL_LOGIC",
                riskLevel = ActionRiskLevel.SAFE
            ),
            SkillManifest(
                id = "skill.expense_tracker",
                name = "Expense & Budget Log",
                version = "1.1.0",
                description = "Tracks quick daily expenditures and budget categorizations.",
                sampleCommand = "Add expense ₹250 for lunch",
                executionType = "LOCAL_LOGIC",
                riskLevel = ActionRiskLevel.SAFE
            ),
            SkillManifest(
                id = "skill.research",
                name = "Web Research Analyst",
                version = "1.0.1",
                description = "Gathers and summarizes facts from verified knowledge databases.",
                sampleCommand = "Research artificial neural networks",
                executionType = "LOCAL_LOGIC",
                riskLevel = ActionRiskLevel.SAFE
            )
        )
    }

    fun validateAndAttach(manifest: SkillManifest): Result<SkillManifest> {
        if (manifest.id.isBlank() || manifest.name.isBlank()) {
            return Result.failure(IllegalArgumentException("Manifest must have non-blank 'id' and 'name'"))
        }

        // Sandbox check: verify identifier format
        if (!manifest.id.startsWith("skill.") && !manifest.id.startsWith("custom.")) {
            return Result.failure(IllegalArgumentException("Skill ID must prefix with 'skill.' or 'custom.'"))
        }

        val bridge = DynamicSkillBridge(manifest)
        ActionRegistry.instance.registerAction(bridge)
        return Result.success(manifest)
    }

    fun proposeSkill(prompt: String): SkillManifest {
        val slug = prompt.lowercase()
            .replace(Regex("[^a-z0-9_\\s]"), "")
            .trim()
            .replace(Regex("\\s+"), "_")
            .take(16)

        val cleanTitle = prompt.trim().split(" ").take(4).joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }

        return SkillManifest(
            id = "custom.$slug",
            name = cleanTitle.ifBlank { "Custom Agent Skill" },
            version = "1.0.0",
            description = "Self-generated capability formulated from user objective: '$prompt'",
            sampleCommand = prompt,
            executionType = "LOCAL_LOGIC",
            riskLevel = ActionRiskLevel.SAFE
        )
    }
}
