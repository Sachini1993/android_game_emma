package com.sachini.emmamemorygame

import com.aldebaran.qi.Future
import com.aldebaran.qi.sdk.QiContext
import com.aldebaran.qi.sdk.builder.AnimateBuilder
import com.aldebaran.qi.sdk.builder.AnimationBuilder
import com.aldebaran.qi.sdk.builder.LookAtBuilder
import com.aldebaran.qi.sdk.builder.SayBuilder
import com.aldebaran.qi.sdk.`object`.actuation.LookAtMovementPolicy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

var appLanguage: Language = Language.ENGLISH

/** Returns Finnish text when Finnish is selected, English otherwise. */
fun t(en: String, fi: String): String = if (appLanguage == Language.FINNISH) fi else en

fun pepperSpeak(text: String) {
    val context = MainActivity.robotContext ?: return
    if (MainActivity.isSpeaking) return
    GlobalScope.launch(Dispatchers.IO) {
        try {
            MainActivity.isSpeaking = true
            val say = SayBuilder.with(context).withText(text).build()
            say.run()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            MainActivity.isSpeaking = false
        }
    }
}

// Continuous eye contact tracking.
//
// QiSDK headFrame is a LIVE 3D frame — it updates in real-time as the person moves.
// So one LookAt started with headFrame automatically handles:
//   • Short child / seated student  → headFrame is at low height → Pepper looks down
//   • Tall adult                    → headFrame is at high height → Pepper looks up
//   • Person moves left/right/away  → headFrame shifts → Pepper's head & body follow
//
// The loop only needs to (re)start LookAt when:
//   1. No LookAt is running yet (startup or after win celebration)
//   2. The previous LookAt ended / was cancelled
//   3. A completely different person became the engaged human
fun pepperStartEyeContact(qiContext: QiContext) {
    GlobalScope.launch(Dispatchers.IO) {
        var trackedHumanId: Int? = null   // track by identity hash to detect person changes
        while (qiContext == MainActivity.robotContext) {
            try {
                val human = qiContext.humanAwareness.engagedHuman
                val humanId = if (human != null) System.identityHashCode(human) else null
                val futureFinished = MainActivity.currentLookAtFuture?.isDone ?: true

                if (futureFinished || humanId != trackedHumanId) {
                    // LookAt ended, was cancelled, or a different person is now engaged
                    MainActivity.currentLookAtFuture?.requestCancellation()
                    MainActivity.currentLookAtFuture = null
                    trackedHumanId = humanId

                    human?.let {
                        val lookAt = LookAtBuilder.with(qiContext)
                            .withFrame(it.headFrame)   // live frame — tracks height & movement
                            .build()
                        lookAt.policy = LookAtMovementPolicy.HEAD_AND_BASE
                        MainActivity.currentLookAtFuture = lookAt.async().run()
                    }
                }
            } catch (_: Exception) { /* recover on next tick */ }
            delay(800)   // check frequently so person changes feel instant
        }
    }
}

fun pepperStopEyeContact() {
    MainActivity.currentLookAtFuture?.requestCancellation()
    MainActivity.currentLookAtFuture = null
}

// Runs win celebration: stops eye contact, plays arm-raise animation + speech in parallel,
// then resumes eye contact pointing back at the player.
fun pepperWinCelebration(playerName: String, score: Int) {
    val context = MainActivity.robotContext ?: return
    if (MainActivity.isSpeaking) return
    GlobalScope.launch(Dispatchers.IO) {
        try {
            MainActivity.isSpeaking = true
            pepperStopEyeContact()
            SoundManager.stopBackgroundMusic()

            // Play short win fanfare first, then Pepper speaks
            SoundManager.playWinFanfare()

            val speechText = if (appLanguage == Language.FINNISH)
                "${PEPPER_WIN_SPEECHES_FI.random()} $playerName, sait $score pistettä!"
            else
                "${PEPPER_WIN_SPEECHES.random()} $playerName, you scored $score points!"
            val sayFuture: Future<Void> = SayBuilder.with(context)
                .withText(speechText).build().async().run()

            // Run arm animation in parallel with speech
            try {
                val anim = AnimationBuilder.with(context)
                    .withResources(R.raw.celebrate).build()
                AnimateBuilder.with(context).withAnimation(anim).build().async().run()
            } catch (_: Exception) { }

            sayFuture.get()
        } catch (_: Exception) {
        } finally {
            MainActivity.isSpeaking = false
            MainActivity.robotContext?.let { pepperStartEyeContact(it) }
        }
    }
}
