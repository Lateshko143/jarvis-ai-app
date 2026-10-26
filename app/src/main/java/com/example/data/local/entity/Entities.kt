package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "memories")
data class MemoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String, // "FACT", "PREFERENCE", "NOTE", "ROUTINE"
    val key: String,
    val value: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "task_history")
data class TaskEntity(
    @PrimaryKey val taskId: String,
    val rawPrompt: String,
    val actionIdentifier: String,
    val status: String, // "SUCCESS", "FAILED", "CHALLENGE_PENDING", "CANCELLED"
    val feedback: String,
    val executionTimeMs: Long,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "installed_skills")
data class SkillEntity(
    @PrimaryKey val skillId: String,
    val name: String,
    val version: String,
    val description: String,
    val manifestJson: String,
    val isEnabled: Boolean = true,
    val installTimestamp: Long = System.currentTimeMillis()
)
