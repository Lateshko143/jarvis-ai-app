package com.example

import android.app.Application
import com.example.core.action.ActionRegistry
import com.example.core.action.CalculateMathAction
import com.example.core.action.GetBatteryStatusAction
import com.example.core.action.GetDeviceInfoAction
import com.example.core.action.GetNetworkStatusAction
import com.example.core.action.GetStorageStatusAction
import com.example.core.action.LaunchAppAction
import com.example.core.action.OpenSettingsAction
import com.example.core.action.SetReminderAction
import com.example.core.skills.DynamicSkillEngine
import com.example.data.local.JarvisDatabase
import com.example.data.local.entity.MemoryEntity
import com.example.data.local.entity.SkillEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class JarvisApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    lateinit var database: JarvisDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        database = JarvisDatabase.getInstance(this)

        // 1. Register Core System Actions
        ActionRegistry.instance.apply {
            registerAction(GetBatteryStatusAction())
            registerAction(GetStorageStatusAction())
            registerAction(GetDeviceInfoAction())
            registerAction(GetNetworkStatusAction())
            registerAction(LaunchAppAction())
            registerAction(OpenSettingsAction())
            registerAction(CalculateMathAction())
            registerAction(SetReminderAction())
        }

        // 2. Attach Starter Skills
        val skillEngine = DynamicSkillEngine()
        val defaultSkills = skillEngine.getDefaultStarterSkills()
        for (skill in defaultSkills) {
            skillEngine.validateAndAttach(skill)
        }

        // 3. Seed starter data into Room if initial launch
        applicationScope.launch {
            try {
                val existingSkills = database.skillDao().getAllSkills().first()
                if (existingSkills.isEmpty()) {
                    for (skill in defaultSkills) {
                        database.skillDao().registerSkill(
                            SkillEntity(
                                skillId = skill.id,
                                name = skill.name,
                                version = skill.version,
                                description = skill.description,
                                manifestJson = skill.toJson(),
                                isEnabled = true
                            )
                        )
                    }

                    // Seed starter user memory
                    database.memoryDao().insertMemory(
                        MemoryEntity(
                            category = "PREFERENCE",
                            key = "Preferred Languages",
                            value = "Hindi, Hinglish, English"
                        )
                    )
                    database.memoryDao().insertMemory(
                        MemoryEntity(
                            category = "FACT",
                            key = "Assistant Protocol",
                            value = "JARVIS Personal Assistant v1.0, Offline-first with high-assurance phone automation."
                        )
                    )
                }
            } catch (e: Exception) {
                // Ignore initial seeding race condition
            }
        }
    }
}
