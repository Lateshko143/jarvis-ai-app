package com.example.core.action

import android.content.Context
import android.util.Log
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap

class ActionRegistry private constructor() {

    private val registeredActions = ConcurrentHashMap<String, ActionContract>()

    fun registerAction(action: ActionContract) {
        registeredActions[action.identifier] = action
        Log.i("ActionRegistry", "Action registered: ${action.identifier} [${action.riskLevel}]")
    }

    fun unregisterAction(identifier: String) {
        registeredActions.remove(identifier)
    }

    fun getAction(identifier: String): ActionContract? = registeredActions[identifier]

    fun getAllActions(): List<ActionContract> = registeredActions.values.toList()

    suspend fun dispatch(
        context: Context,
        identifier: String,
        params: JSONObject,
        userApproved: Boolean = false
    ): ActionResult {
        val action = registeredActions[identifier]
            ?: return ActionResult(
                success = false,
                feedback = "Command '$identifier' is not registered in JARVIS subsystems."
            )

        if (action.riskLevel != ActionRiskLevel.SAFE && !userApproved) {
            return ActionResult(
                success = false,
                feedback = "SECURITY_CHALLENGE: Action '${action.name}' requires explicit user confirmation.",
                payload = mapOf(
                    "challenge" to true,
                    "risk" to action.riskLevel.name,
                    "identifier" to identifier
                )
            )
        }

        return try {
            val result = action.execute(context, params)
            val verified = action.verify(context, params, result)
            if (verified) {
                result
            } else {
                ActionResult(
                    success = false,
                    feedback = "Action ${action.name} executed but state verification failed."
                )
            }
        } catch (e: Exception) {
            Log.e("ActionRegistry", "Execution error executing $identifier", e)
            ActionResult(
                success = false,
                feedback = "Subsystem error: ${e.localizedMessage ?: "Unknown failure"}",
                error = e
            )
        }
    }

    companion object {
        val instance: ActionRegistry by lazy { ActionRegistry() }
    }
}
