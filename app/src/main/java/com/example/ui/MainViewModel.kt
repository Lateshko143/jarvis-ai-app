package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.JarvisApplication
import com.example.core.action.ActionContract
import com.example.core.action.ActionRegistry
import com.example.core.action.ActionRiskLevel
import com.example.core.action.ActionResult
import com.example.core.ai.ModelRouter
import com.example.core.ai.ParsedIntent
import com.example.core.skills.DynamicSkillEngine
import com.example.core.skills.SkillManifest
import com.example.core.voice.VoiceEngine
import com.example.core.voice.VoiceEngineState
import com.example.data.local.entity.MemoryEntity
import com.example.data.local.entity.SkillEntity
import com.example.data.local.entity.TaskEntity
import com.example.data.repository.JarvisRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.UUID

data class PendingActionChallenge(
    val actionIdentifier: String,
    val actionName: String,
    val parameters: JSONObject,
    val rawPrompt: String,
    val riskLevel: ActionRiskLevel
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = JarvisRepository((application as JarvisApplication).database)
    val skillEngine = DynamicSkillEngine()
    val modelRouter = ModelRouter(ActionRegistry.instance)

    lateinit var voiceEngine: VoiceEngine
        private set

    val voiceState: StateFlow<VoiceEngineState> get() = voiceEngine.engineState
    val audioRms: StateFlow<Float> get() = voiceEngine.audioRms

    val tasks: StateFlow<List<TaskEntity>> = repository.recentTasks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val memories: StateFlow<List<MemoryEntity>> = repository.allMemories.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val skills: StateFlow<List<SkillEntity>> = repository.allSkills.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _currentTab = MutableStateFlow(0) // 0: HUD, 1: Actions, 2: Memory, 3: Skills, 4: Diagnostics
    val currentTab: StateFlow<Int> = _currentTab.asStateFlow()

    private val _pendingChallenge = MutableStateFlow<PendingActionChallenge?>(null)
    val pendingChallenge: StateFlow<PendingActionChallenge?> = _pendingChallenge.asStateFlow()

    private val _lastSpoken = MutableStateFlow<String?>("JARVIS Core Initialized. Standing by.")
    val lastSpoken: StateFlow<String?> = _lastSpoken.asStateFlow()

    fun initVoiceEngine() {
        if (!::voiceEngine.isInitialized) {
            voiceEngine = VoiceEngine(getApplication()) { spokenText ->
                processUserCommand(spokenText)
            }
        }
    }

    fun selectTab(index: Int) {
        _currentTab.value = index
    }

    fun dismissChallenge() {
        _pendingChallenge.value = null
    }

    fun confirmChallenge() {
        val challenge = _pendingChallenge.value ?: return
        _pendingChallenge.value = null

        viewModelScope.launch {
            executeRegisteredAction(
                actionIdentifier = challenge.actionIdentifier,
                parameters = challenge.parameters,
                rawPrompt = challenge.rawPrompt,
                userApproved = true
            )
        }
    }

    fun processUserCommand(input: String) {
        val trimmed = input.trim()
        if (trimmed.isBlank()) return

        viewModelScope.launch {
            val parsedIntent: ParsedIntent = modelRouter.routeInput(trimmed)

            // Direct speech answer (e.g. general conversation, greetings, help)
            if (parsedIntent.directSpeechResponse != null) {
                _lastSpoken.value = parsedIntent.directSpeechResponse
                voiceEngine.speak(parsedIntent.directSpeechResponse)
                repository.logTask(
                    taskId = UUID.randomUUID().toString(),
                    rawPrompt = trimmed,
                    actionIdentifier = "ai.conversational",
                    status = "SUCCESS",
                    feedback = parsedIntent.directSpeechResponse,
                    executionTimeMs = 85
                )
                return@launch
            }

            // Target action resolution
            if (parsedIntent.actionIdentifier != null) {
                val action = ActionRegistry.instance.getAction(parsedIntent.actionIdentifier)
                if (action == null) {
                    val msg = "Action '${parsedIntent.actionIdentifier}' is not available."
                    _lastSpoken.value = msg
                    voiceEngine.speak(msg)
                    return@launch
                }

                if (parsedIntent.needsConfirmation && action.riskLevel != ActionRiskLevel.SAFE) {
                    _pendingChallenge.value = PendingActionChallenge(
                        actionIdentifier = parsedIntent.actionIdentifier,
                        actionName = action.name,
                        parameters = parsedIntent.parameters,
                        rawPrompt = trimmed,
                        riskLevel = action.riskLevel
                    )
                    return@launch
                }

                executeRegisteredAction(
                    actionIdentifier = parsedIntent.actionIdentifier,
                    parameters = parsedIntent.parameters,
                    rawPrompt = trimmed,
                    userApproved = !parsedIntent.needsConfirmation
                )
            }
        }
    }

    suspend fun executeRegisteredAction(
        actionIdentifier: String,
        parameters: JSONObject,
        rawPrompt: String,
        userApproved: Boolean
    ) {
        val startTime = System.currentTimeMillis()
        val result: ActionResult = ActionRegistry.instance.dispatch(
            context = getApplication(),
            identifier = actionIdentifier,
            params = parameters,
            userApproved = userApproved
        )
        val latency = System.currentTimeMillis() - startTime

        _lastSpoken.value = result.feedback
        voiceEngine.speak(result.feedback)

        repository.logTask(
            taskId = UUID.randomUUID().toString(),
            rawPrompt = rawPrompt,
            actionIdentifier = actionIdentifier,
            status = if (result.success) "SUCCESS" else "FAILED",
            feedback = result.feedback,
            executionTimeMs = latency
        )
    }

    fun getAllActions(): List<ActionContract> = ActionRegistry.instance.getAllActions()

    fun saveMemory(category: String, key: String, value: String) {
        viewModelScope.launch {
            repository.saveMemory(category, key, value)
            val msg = "Recorded to memory vault: $key."
            _lastSpoken.value = msg
            voiceEngine.speak(msg)
        }
    }

    fun deleteMemory(id: Long) {
        viewModelScope.launch {
            repository.deleteMemory(id)
        }
    }

    fun clearMemories() {
        viewModelScope.launch {
            repository.clearMemories()
        }
    }

    fun installSkill(manifest: SkillManifest) {
        viewModelScope.launch {
            val validation = skillEngine.validateAndAttach(manifest)
            if (validation.isSuccess) {
                repository.registerSkill(
                    SkillEntity(
                        skillId = manifest.id,
                        name = manifest.name,
                        version = manifest.version,
                        description = manifest.description,
                        manifestJson = manifest.toJson(),
                        isEnabled = true
                    )
                )
                val msg = "Skill '${manifest.name}' validated and installed successfully."
                _lastSpoken.value = msg
                voiceEngine.speak(msg)
            } else {
                val errMsg = "Failed to install skill: ${validation.exceptionOrNull()?.message}"
                _lastSpoken.value = errMsg
                voiceEngine.speak(errMsg)
            }
        }
    }

    fun removeSkill(skillId: String) {
        viewModelScope.launch {
            ActionRegistry.instance.unregisterAction(skillId)
            repository.removeSkill(skillId)
        }
    }

    fun clearTelemetryLogs() {
        viewModelScope.launch {
            repository.clearTasks()
        }
    }

    override fun onCleared() {
        super.onCleared()
        if (::voiceEngine.isInitialized) {
            voiceEngine.destroy()
        }
    }
}
