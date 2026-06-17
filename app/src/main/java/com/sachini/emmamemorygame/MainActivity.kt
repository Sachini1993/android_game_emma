package com.sachini.emmamemorygame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.aldebaran.qi.Future
import com.aldebaran.qi.sdk.QiContext
import com.aldebaran.qi.sdk.QiSDK
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks

class MainActivity : ComponentActivity(), RobotLifecycleCallbacks {

    companion object {
        var robotContext: QiContext? = null
        var isSpeaking = false
        var currentLookAtFuture: Future<Void>? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        QiSDK.register(this, this)
        PlayerRegistry.init(this)
        setContent { AppRoot() }
    }

    override fun onDestroy() {
        super.onDestroy()
        SoundManager.stopBackgroundMusic()
        QiSDK.unregister(this, this)
    }

    override fun onPause() {
        super.onPause()
        SoundManager.stopBackgroundMusic()
    }

    override fun onResume() {
        super.onResume()
        // music restarts via GameScreen LaunchedEffect when game is active
    }

    override fun onRobotFocusGained(qiContext: QiContext) {
        robotContext = qiContext
        pepperSpeak(t("Hello! I am Pepper. Welcome to Pepper Memory Game! Touch the screen to start!",
            "Hei! Olen Pepper. Tervetuloa Pepper Memory -peliin! Kosketa näyttöä aloittaaksesi!"))
        pepperStartEyeContact(qiContext)
    }

    override fun onRobotFocusLost() {
        pepperStopEyeContact()
        robotContext = null
    }

    override fun onRobotFocusRefused(reason: String) {}
}

@Composable
fun AppRoot() {
    var currentPlayer by remember { mutableStateOf<PlayerData?>(null) }
    if (currentPlayer == null) {
        PlayerLoginScreen { player -> currentPlayer = player }
    } else {
        MemoryGameScreen(player = currentPlayer!!, onChangePlayer = { currentPlayer = null })
    }
}
