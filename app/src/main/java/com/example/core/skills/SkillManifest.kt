package com.example.core.skills

import com.example.core.action.ActionRiskLevel
import org.json.JSONObject

data class SkillManifest(
    val id: String,
    val name: String,
    val version: String,
    val description: String,
    val sampleCommand: String,
    val executionType: String, // "LOCAL_LOGIC", "HTTP_GET", "DEVICE_AUTOMATION"
    val riskLevel: ActionRiskLevel
) {
    fun toJson(): String {
        return JSONObject().apply {
            put("id", id)
            put("name", name)
            put("version", version)
            put("description", description)
            put("sampleCommand", sampleCommand)
            put("executionType", executionType)
            put("riskLevel", riskLevel.name)
        }.toString()
    }

    companion object {
        fun fromJson(jsonStr: String): SkillManifest {
            val obj = JSONObject(jsonStr)
            return SkillManifest(
                id = obj.getString("id"),
                name = obj.getString("name"),
                version = obj.optString("version", "1.0.0"),
                description = obj.getString("description"),
                sampleCommand = obj.optString("sampleCommand", ""),
                executionType = obj.optString("executionType", "LOCAL_LOGIC"),
                riskLevel = ActionRiskLevel.valueOf(obj.optString("riskLevel", "SAFE"))
            )
        }
    }
}
