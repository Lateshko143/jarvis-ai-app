package com.example.data.repository

import com.example.data.local.JarvisDatabase
import com.example.data.local.entity.MemoryEntity
import com.example.data.local.entity.SkillEntity
import com.example.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

class JarvisRepository(private val database: JarvisDatabase) {

    val allMemories: Flow<List<MemoryEntity>> = database.memoryDao().getAllMemories()
    val recentTasks: Flow<List<TaskEntity>> = database.taskDao().getRecentTasks()
    val allSkills: Flow<List<SkillEntity>> = database.skillDao().getAllSkills()

    suspend fun saveMemory(category: String, key: String, value: String): Long {
        return database.memoryDao().insertMemory(
            MemoryEntity(category = category, key = key, value = value)
        )
    }

    suspend fun deleteMemory(id: Long) {
        database.memoryDao().deleteMemory(id)
    }

    suspend fun clearMemories() {
        database.memoryDao().clearAll()
    }

    suspend fun logTask(
        taskId: String,
        rawPrompt: String,
        actionIdentifier: String,
        status: String,
        feedback: String,
        executionTimeMs: Long
    ) {
        database.taskDao().logTask(
            TaskEntity(
                taskId = taskId,
                rawPrompt = rawPrompt,
                actionIdentifier = actionIdentifier,
                status = status,
                feedback = feedback,
                executionTimeMs = executionTimeMs
            )
        )
    }

    suspend fun clearTasks() {
        database.taskDao().clearTasks()
    }

    suspend fun registerSkill(skill: SkillEntity) {
        database.skillDao().registerSkill(skill)
    }

    suspend fun updateSkill(skill: SkillEntity) {
        database.skillDao().updateSkill(skill)
    }

    suspend fun removeSkill(skillId: String) {
        database.skillDao().removeSkill(skillId)
    }
}
