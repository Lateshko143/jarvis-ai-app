package com.example

import com.example.core.action.ActionRegistry
import com.example.core.action.CalculateMathAction
import com.example.core.action.GetBatteryStatusAction
import com.example.core.action.GetStorageStatusAction
import com.example.core.action.LaunchAppAction
import com.example.core.ai.ModelRouter
import com.example.core.skills.DynamicSkillEngine
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class JarvisAssistantUnitTest {

    private lateinit var registry: ActionRegistry
    private lateinit var router: ModelRouter
    private lateinit var skillEngine: DynamicSkillEngine

    @Before
    fun setup() {
        registry = ActionRegistry.instance
        registry.registerAction(GetBatteryStatusAction())
        registry.registerAction(GetStorageStatusAction())
        registry.registerAction(LaunchAppAction())
        registry.registerAction(CalculateMathAction())
        router = ModelRouter(registry)
        skillEngine = DynamicSkillEngine()
    }

    @Test
    fun testOfflineBatteryRuleEnglish() = runBlocking {
        val result = router.routeInput("Check my battery level please")
        assertEquals("device.get_battery", result.actionIdentifier)
        assertFalse(result.needsConfirmation)
    }

    @Test
    fun testOfflineBatteryRuleHinglish() = runBlocking {
        val result = router.routeInput("Jarvis phone ka battery check karo")
        assertEquals("device.get_battery", result.actionIdentifier)
    }

    @Test
    fun testOfflineStorageRuleHindi() = runBlocking {
        val result = router.routeInput("Storage check karo")
        assertEquals("device.get_storage", result.actionIdentifier)
    }

    @Test
    fun testAppLaunchExtraction() = runBlocking {
        val result = router.routeInput("WhatsApp kholo")
        assertEquals("apps.launch", result.actionIdentifier)
        assertEquals("whatsapp", result.parameters.optString("package_or_name"))
        assertTrue(result.needsConfirmation)
    }

    @Test
    fun testMathCalculationParsing() = runBlocking {
        val result = router.routeInput("calculate 45 * 10")
        assertEquals("utility.calculate", result.actionIdentifier)
        assertEquals("45 * 10", result.parameters.optString("expression"))
    }

    @Test
    fun testConversationalFallback() = runBlocking {
        val result = router.routeInput("Who are you?")
        assertNull(result.actionIdentifier)
        assertNotNull(result.directSpeechResponse)
        assertTrue(result.directSpeechResponse!!.contains("JARVIS"))
    }

    @Test
    fun testDynamicSkillEngineProposal() {
        val manifest = skillEngine.proposeSkill("Summarize daily expenditures")
        assertTrue(manifest.id.startsWith("custom."))
        assertNotNull(manifest.name)
        val validation = skillEngine.validateAndAttach(manifest)
        assertTrue(validation.isSuccess)
        assertNotNull(registry.getAction(manifest.id))
    }
}
