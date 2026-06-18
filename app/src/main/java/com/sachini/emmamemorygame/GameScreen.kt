package com.sachini.emmamemorygame

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MemoryGameScreen(player: PlayerData, onChangePlayer: () -> Unit) {
    val ai = remember(player.name) { MemoryAI(player) }
    val predictor = remember { MemoryPredictor() }
    val tracker = remember { ReactionTracker() }
    val scrollState = rememberScrollState()

    var firstGame by remember { mutableStateOf(player.gamesPlayed == 0) }
    var theme by remember { mutableStateOf(ALL_THEMES.first()) }
    var difficulty by remember { mutableStateOf(Difficulty.MEDIUM) }
    var cards by remember { mutableStateOf(createCards(difficulty.pairs, theme)) }
    var firstPick by remember { mutableStateOf<Int?>(null) }
    var secondPick by remember { mutableStateOf<Int?>(null) }
    var isChecking by remember { mutableStateOf(false) }
    var matches by remember { mutableStateOf(0) }
    var turns by remember { mutableStateOf(0) }
    var memoryHint by remember { mutableStateOf("") }
    var retentionPct by remember { mutableStateOf(0) }
    var reactionHint by remember { mutableStateOf("") }
    var avgReactionMs by remember { mutableStateOf(0L) }
    var emmaMessage by remember { mutableStateOf("") }
    var lastXPEarned by remember { mutableStateOf(0) }
    var lastScore by remember { mutableStateOf(0) }
    var showLevelUp by remember { mutableStateOf(false) }
    var levelUpTitle by remember { mutableStateOf("") }
    var timeLeft by remember { mutableStateOf(120) }
    var timerRunning by remember { mutableStateOf(false) }
    var gameStarted by remember { mutableStateOf(false) }
    var playAgainMsg by remember { mutableStateOf(if (appLanguage == Language.FINNISH) PLAY_AGAIN_MESSAGES_FI.random() else PLAY_AGAIN_MESSAGES.random()) }
    var rightMsg by remember { mutableStateOf(if (appLanguage == Language.FINNISH) WIN_RIGHT_MESSAGES_FI.random() else WIN_RIGHT_MESSAGES.random()) }
    var newGemQueue by remember { mutableStateOf<List<Gem>>(emptyList()) }
    var displayXP by remember { mutableStateOf(player.totalXP) }
    var isMuted by remember { mutableStateOf(SoundManager.isMuted) }
    var incorrectCount by remember { mutableStateOf(0) }
    val boredomDetector = remember { BoredomDetector() }
    var showBreakScreen by remember { mutableStateOf(false) }
    var currentBreakActivity by remember { mutableStateOf(BREAK_ACTIVITIES.first()) }
    var showSettings by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(timerRunning) {
        while (timerRunning && timeLeft > 0) { delay(1000); timeLeft-- }
    }

    LaunchedEffect(gameStarted) {
        if (!gameStarted) return@LaunchedEffect
        while (true) {
            delay(10_000)
            if (gameStarted && !showBreakScreen) {
                val gameOver = matches == difficulty.pairs || timeLeft == 0
                if (!gameOver && boredomDetector.isBored()) {
                    currentBreakActivity = BREAK_ACTIVITIES.random()
                    showBreakScreen = true
                    timerRunning = false
                    SoundManager.stopBackgroundMusic()
                    pepperSpeak(t("Hey ${player.name}! Let's take a fun movement break! Time to wake up and move!",
                    "Hei ${player.name}! Otetaan hauska liikuntataruko! On aika herätä ja liikkua!"))
                }
            }
        }
    }

    val gameWon  = gameStarted && matches == difficulty.pairs
    val gameLost = gameStarted && timeLeft == 0 && !gameWon

    // Music control — stop only when game ends; startGame() and break return restart it
    LaunchedEffect(gameWon, gameLost) {
        if (gameWon || gameLost) SoundManager.stopBackgroundMusic()
    }
    val skill     = ai.playerSkill()
    val qVals     = ai.qValues(skill)
    val curLevel  = currentLevel(displayXP)
    val nxtLevel  = nextLevel(displayXP)
    val activity  = LocalContext.current

    if (gameWon || gameLost) timerRunning = false

    fun startGame(d: Difficulty, t: GameTheme) {
        difficulty = d; theme = t
        cards = createCards(d.pairs, t)
        firstPick = null; secondPick = null
        matches = 0; turns = 0
        timeLeft = 120; timerRunning = true
        gameStarted = true; firstGame = false
        memoryHint = ""; retentionPct = 0
        reactionHint = ""; avgReactionMs = 0L
        emmaMessage = ""; lastXPEarned = 0; lastScore = 0
        showLevelUp = false; newGemQueue = emptyList()
        incorrectCount = 0
        boredomDetector.reset()
        showBreakScreen = false
        tracker.reset()
        playAgainMsg = PLAY_AGAIN_MESSAGES.random()
        if (!SoundManager.isMuted) SoundManager.startBackgroundMusic()
        player.themesPlayed = player.themesPlayed + t.name
        pepperSpeak(t("Let's play! ${d.label} difficulty with ${t.name} theme. Good luck ${player.name}!",
            "Pelataan! ${if (d == Difficulty.EASY) "Helppo" else if (d == Difficulty.MEDIUM) "Keski" else "Vaikea"} vaikeustaso ${t.name}-teemalla. Onnea ${player.name}!"))
    }

    Box(modifier = Modifier.fillMaxSize().background(AppColors.background)) {
        Row(modifier = Modifier.fillMaxSize().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {

            // ── LEFT PANEL ────────────────────────────────────────────────────
            Column(
                modifier = Modifier.weight(0.42f).fillMaxHeight().verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth()
                    .background(AppColors.purple, RoundedCornerShape(12.dp)).padding(10.dp)) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("👤 ${player.name}", fontSize = 14.sp, color = Color.White)
                            Text("Lv.${curLevel.level} ${curLevel.title}",
                                fontSize = 11.sp, color = Color(0xFFCECBF6))
                            Text("💎 ${player.gemCount()}/${ALL_GEMS.size} ${t("gems","jalokiveä")}",
                                fontSize = 10.sp, color = Color(0xFFAFA9EC))
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Text("⚙️", fontSize = 18.sp,
                                modifier = Modifier.clickable { showSettings = true })
                            Text(if (isMuted) "🔇" else "🔊", fontSize = 18.sp,
                                modifier = Modifier.clickable {
                                    isMuted = !isMuted
                                    SoundManager.isMuted = isMuted
                                    if (isMuted) SoundManager.stopBackgroundMusic()
                                    else if (gameStarted && !gameWon && !gameLost) SoundManager.startBackgroundMusic()
                                })
                            Text(t("Change\nplayer","Vaihda\npelaaja"), fontSize = 10.sp, color = Color(0xFFAFA9EC),
                                modifier = Modifier.clickable { onChangePlayer() })
                        }
                    }
                }

                // Prominent quit button
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .background(Color(0xFFFFEBEE), RoundedCornerShape(12.dp))
                        .clickable {
                            SoundManager.stopBackgroundMusic()
                            (activity as? Activity)?.finish()
                        }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(t("❌  Quit Game","❌  Lopeta peli"), fontSize = 15.sp, color = Color(0xFFD32F2F),
                        fontWeight = FontWeight.Bold)
                }

                Box(modifier = Modifier.fillMaxWidth()
                    .background(AppColors.xpBg, RoundedCornerShape(12.dp)).padding(10.dp)) {
                    Column {
                        Row(horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()) {
                            Text("⭐ $displayXP XP", fontSize = 12.sp, color = AppColors.xpText)
                            if (lastXPEarned > 0)
                                Text("+$lastXPEarned XP!", fontSize = 11.sp, color = AppColors.green)
                        }
                        Spacer(Modifier.height(4.dp))
                        Box(modifier = Modifier.fillMaxWidth().height(8.dp)
                            .background(AppColors.xpTrack, RoundedCornerShape(4.dp))) {
                            Box(modifier = Modifier.fillMaxWidth(xpProgressPct(displayXP)).height(8.dp)
                                .background(AppColors.xpFill, RoundedCornerShape(4.dp)))
                        }
                        if (nxtLevel != null)
                            Text(t("${xpToNextLevel(displayXP)} XP to ${nxtLevel.title}","${xpToNextLevel(displayXP)} XP tasolle ${nxtLevel.title}"),
                                fontSize = 10.sp, color = AppColors.xpSub,
                                modifier = Modifier.padding(top = 2.dp))
                    }
                }

                if (showLevelUp) {
                    Box(modifier = Modifier.fillMaxWidth()
                        .background(AppColors.purple, RoundedCornerShape(12.dp)).padding(10.dp)
                        .clickable { showLevelUp = false }) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()) {
                            Text(t("🎊 LEVEL UP!","🎊 TASON NOUSU!"), fontSize = 16.sp, color = Color.White)
                            Text(levelUpTitle, fontSize = 12.sp, color = Color(0xFFCECBF6))
                            Text(t("tap to dismiss","napauta sulkeaksesi"), fontSize = 10.sp, color = Color(0xFFAFA9EC))
                        }
                    }
                }

                if (newGemQueue.isNotEmpty()) {
                    GemPopup(gem = newGemQueue.first()) { newGemQueue = newGemQueue.drop(1) }
                }

                if (player.currentStreak > 0) {
                    Box(modifier = Modifier.fillMaxWidth()
                        .background(AppColors.streakBg, RoundedCornerShape(10.dp)).padding(8.dp)) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()) {
                            val emoji = when {
                                player.currentStreak >= 10 -> "🔥🔥🔥"
                                player.currentStreak >= 5  -> "🔥🔥"
                                player.currentStreak >= 3  -> "🔥"
                                else -> "⭐"
                            }
                            val msg = when {
                                player.currentStreak >= 10 -> t("UNSTOPPABLE!","PYSÄYTTÄMÄTÖN!")
                                player.currentStreak >= 5  -> t("On a hot streak!","Huima putki!")
                                player.currentStreak >= 3  -> t("Keep it up!","Jatka samaan tapaan!")
                                else -> t("Win streak!","Voittoputki!")
                            }
                            Text("$emoji $msg", fontSize = 12.sp, color = AppColors.streakText)
                            Text("🔥 ${player.currentStreak}  ${t("Best","Paras")}: ${player.bestStreak}",
                                fontSize = 11.sp, color = Color(0xFF888888))
                        }
                    }
                }

                Text("Pepper Memory", fontSize = 20.sp, color = AppColors.orange)
                Text("${theme.emoji} ${theme.name} · ${difficulty.label}",
                    fontSize = 11.sp, color = AppColors.xpSub)

                if (firstGame) {
                    Text(t("Select Difficulty:","Valitse vaikeustaso:"), fontSize = 11.sp, color = Color(0xFF888888))
                    DifficultySelector(selected = difficulty) { d ->
                        difficulty = d; cards = createCards(d.pairs, theme)
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(t("Select Theme:","Valitse teema:"), fontSize = 11.sp, color = Color(0xFF888888))
                    ThemeSelector(selected = theme) { t ->
                        theme = t; cards = createCards(difficulty.pairs, t)
                    }
                }

                if (!gameStarted || gameWon || gameLost) {
                    Box(modifier = Modifier.fillMaxWidth()
                        .background(AppColors.purple, RoundedCornerShape(14.dp))
                        .clickable {
                            if (firstGame) startGame(difficulty, theme)
                            else {
                                val aiDiff = ai.chooseDifficulty()
                                val newTheme = ALL_THEMES.random()
                                rightMsg = if (gameWon)
                                    (if (appLanguage == Language.FINNISH) WIN_RIGHT_MESSAGES_FI else WIN_RIGHT_MESSAGES).random()
                                else
                                    (if (appLanguage == Language.FINNISH) LOSE_RIGHT_MESSAGES_FI else LOSE_RIGHT_MESSAGES).random()
                                startGame(aiDiff, newTheme)
                            }
                        }
                        .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center) {
                        Text(if (firstGame) t("▶  Start Game","▶  Aloita peli") else playAgainMsg,
                            fontSize = 14.sp, color = Color.White)
                    }
                }

                if (gameStarted) {
                    Row(horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()) {
                        StatChip("$matches/${difficulty.pairs}", t("Matches","Parit"), AppColors.green)
                        StatChip("$turns", t("Turns","Vuorot"), AppColors.purple)
                        StatChip(ai.skillLabel(), t("Skill","Taito"), AppColors.orange)
                    }

                    val timerColor = when {
                        timeLeft > 60 -> AppColors.timerFill
                        timeLeft > 20 -> Color(0xFFFF9800)
                        else -> Color(0xFFF44336)
                    }
                    Box(modifier = Modifier.fillMaxWidth()
                        .background(AppColors.timerBg, RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("⏱ ${timeLeft}s  ", fontSize = 13.sp, color = timerColor)
                            Box(modifier = Modifier.weight(1f).height(8.dp)
                                .background(AppColors.timerTrack, RoundedCornerShape(4.dp))) {
                                Box(modifier = Modifier.fillMaxWidth(timeLeft / 120f).height(8.dp)
                                    .background(timerColor, RoundedCornerShape(4.dp)))
                            }
                        }
                    }

                    if (emmaMessage.isNotEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth()
                            .background(AppColors.emmaMsgBg, RoundedCornerShape(10.dp)).padding(8.dp)) {
                            Text("🤖 Pepper: $emmaMessage",
                                fontSize = 12.sp, color = AppColors.emmaMsgText)
                        }
                    }

                    if (memoryHint.isNotEmpty() || reactionHint.isNotEmpty()) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()) {
                            if (memoryHint.isNotEmpty()) {
                                Box(modifier = Modifier.weight(1f)
                                    .background(AppColors.memoryBg, RoundedCornerShape(10.dp)).padding(8.dp)) {
                                    Column {
                                        Text(memoryHint, fontSize = 11.sp, color = AppColors.memoryText)
                                        Spacer(Modifier.height(3.dp))
                                        Box(modifier = Modifier.fillMaxWidth().height(5.dp)
                                            .background(Color(0xFFB7DFA0), RoundedCornerShape(3.dp))) {
                                            Box(modifier = Modifier.fillMaxWidth(retentionPct / 100f).height(5.dp)
                                                .background(when {
                                                    retentionPct > 80 -> AppColors.green
                                                    retentionPct > 50 -> Color(0xFFFF9800)
                                                    else -> Color(0xFFF44336)
                                                }, RoundedCornerShape(3.dp)))
                                        }
                                        Text("$retentionPct%", fontSize = 10.sp, color = AppColors.memoryText)
                                    }
                                }
                            }
                            if (reactionHint.isNotEmpty()) {
                                Box(modifier = Modifier.weight(1f)
                                    .background(AppColors.reactionBg, RoundedCornerShape(10.dp)).padding(8.dp)) {
                                    Column {
                                        Text(reactionHint, fontSize = 11.sp, color = AppColors.reactionText)
                                        if (avgReactionMs > 0)
                                            Text("${avgReactionMs}ms · ${tracker.confidence()}",
                                                fontSize = 10.sp, color = AppColors.reactionText)
                                    }
                                }
                            }
                        }
                    }

                    Box(modifier = Modifier.fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(12.dp)).padding(10.dp)) {
                        Column {
                            Text(t("📊 Skill Meter","📊 Taitomittari"), fontSize = 12.sp, color = Color(0xFF555555))
                            Spacer(Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly) {
                                listOf(t("Easy","Helppo"), t("Medium","Keski"), t("Hard","Vaikea")).forEachIndexed { i, label ->
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(label, fontSize = 10.sp, color = Color(0xFF888888))
                                        Spacer(Modifier.height(3.dp))
                                        Box(modifier = Modifier.width(28.dp).height(36.dp)
                                            .background(Color(0xFFE8F5E9), RoundedCornerShape(4.dp)),
                                            contentAlignment = Alignment.BottomCenter) {
                                            Box(modifier = Modifier.width(28.dp)
                                                .fillMaxHeight(qVals[i] / 100f)
                                                .background(
                                                    if (qVals[i] == qVals.max()) AppColors.skillBarHigh
                                                    else AppColors.skillBarLow, RoundedCornerShape(4.dp)))
                                        }
                                        Text("${qVals[i]}", fontSize = 11.sp,
                                            color = if (qVals[i] == qVals.max()) AppColors.green
                                            else Color(0xFF888888))
                                    }
                                }
                            }
                        }
                    }

                    if (gameWon) {
                        val xp = calcXP(matches, difficulty.pairs, turns,
                            timeLeft, difficulty, streakBonus(player.currentStreak))
                        val score = calcScore(matches, difficulty.pairs, turns, timeLeft, difficulty)
                        LaunchedEffect(Unit) {
                            lastXPEarned = xp; lastScore = score
                            val oldLevel = currentLevel(player.totalXP).level
                            player.totalXP += xp; displayXP = player.totalXP
                            if (currentLevel(player.totalXP).level > oldLevel) {
                                levelUpTitle = currentLevel(player.totalXP).title
                                showLevelUp = true
                                pepperSpeak(t("Level up! You are now ${currentLevel(player.totalXP).title}!",
                                    "Tasolle nousu! Olet nyt ${currentLevel(player.totalXP).title}!"))
                            }
                            player.currentStreak++
                            if (player.currentStreak > player.bestStreak)
                                player.bestStreak = player.currentStreak
                            if (score > player.bestScore) player.bestScore = score
                            ai.learn(difficulty, matches, difficulty.pairs, turns)
                            val newGems = checkNewGems(player, timeLeft)
                            if (newGems.isNotEmpty()) {
                                newGemQueue = newGems
                                pepperSpeak(t("Amazing! You unlocked a new gem! ${newGems.first().name}!",
                                    "Upea! Sait uuden jalokiven! ${newGems.first().name}!"))
                            }
                            emmaMessage = when {
                                newGems.isNotEmpty() -> "New gem unlocked! 💎"
                                score >= player.bestScore -> EmmaMessages.record()
                                showLevelUp -> EmmaMessages.levelUp()
                                player.currentStreak > 2 -> EmmaMessages.streak()
                                else -> EmmaMessages.win(timeLeft)
                            }
                            pepperWinCelebration(player.name, score)
                            PlayerRegistry.save()
                        }
                        Text(t("🎉 You Win, ${player.name}!","🎉 Voitit, ${player.name}!"), fontSize = 22.sp, color = AppColors.green)
                        Text("+$xp XP  🔥 ${t("Streak","Putki")}: ${player.currentStreak}",
                            fontSize = 13.sp, color = AppColors.purple)
                        Text(t("Score: $score pts","Pisteet: $score p"), fontSize = 16.sp, color = AppColors.goldText)
                        Spacer(Modifier.height(6.dp))
                        GlobalHighScoreBoard()

                    } else if (gameLost) {
                        val xp = calcXP(matches, difficulty.pairs, turns, 0, difficulty, 0)
                        LaunchedEffect(Unit) {
                            lastXPEarned = xp; player.totalXP += xp; displayXP = player.totalXP
                            player.currentStreak = 0
                            val newGems = checkNewGems(player, 0)
                            if (newGems.isNotEmpty()) newGemQueue = newGems
                            emmaMessage = "So close! Try again!"
                            pepperSpeak(t("Time is up ${player.name}! You found ${num(matches)} pairs. Don't give up, try again!",
                                "Aika loppui ${player.name}! Löysit ${num(matches)} paria. Älä anna periksi, yritä uudelleen!"))
                            PlayerRegistry.save()
                        }
                        Text(t("⏰ Time's Up, ${player.name}!","⏰ Aika loppui, ${player.name}!"), fontSize = 22.sp, color = AppColors.orange)
                        Text(t("Found $matches/${difficulty.pairs} pairs","Löysit $matches/${difficulty.pairs} paria"), fontSize = 13.sp)
                        Text(t("+$xp XP  🔥 Streak lost","+$xp XP  🔥 Putki poikki"), fontSize = 13.sp, color = AppColors.purple)
                        Spacer(Modifier.height(6.dp))
                        GlobalHighScoreBoard()
                    }
                }
            }

            // ── RIGHT PANEL ───────────────────────────────────────────────────
            Column(modifier = Modifier.weight(0.58f).fillMaxHeight(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally) {

                if (!gameStarted) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                            .verticalScroll(rememberScrollState()).padding(vertical = 12.dp)
                    ) {
                        Text("👋", fontSize = 56.sp)
                        Text(t("Hello, ${player.name}!","Hei, ${player.name}!"), fontSize = 24.sp, color = AppColors.orange)
                        if (player.gamesPlayed > 0) {
                            Text(t("Games: ${player.gamesPlayed}  •  Best: ${player.bestScore} pts",
                                "Pelejä: ${player.gamesPlayed}  •  Paras: ${player.bestScore} p"),
                                fontSize = 14.sp, color = Color(0xFF555555))
                            Text(t("Best streak: 🔥 ${player.bestStreak}","Paras putki: 🔥 ${player.bestStreak}"),
                                fontSize = 14.sp, color = AppColors.streakText)
                        } else {
                            Text(t("Select difficulty and theme\non the left, then press Start!",
                                "Valitse vaikeustaso ja teema\nvasemmalta, sitten paina Aloita!"),
                                fontSize = 14.sp, color = Color(0xFF888888))
                        }
                        Spacer(Modifier.height(4.dp))
                        GemShelf(player)
                    }

                } else if (gameWon || gameLost) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                            .verticalScroll(rememberScrollState()).padding(vertical = 12.dp)
                    ) {
                        Text(if (gameWon) "🎉" else "⏰", fontSize = 64.sp)
                        Text(if (gameWon) t("You Win!","Voitit!") else t("Time's Up!","Aika loppui!"),
                            fontSize = 30.sp,
                            color = if (gameWon) AppColors.green else AppColors.orange)
                        if (gameWon) {
                            Text(t("Score: $lastScore pts","Pisteet: $lastScore p"), fontSize = 20.sp, color = AppColors.goldText)
                            if (lastScore >= player.bestScore)
                                Text(t("🏆 New personal best!","🏆 Uusi henkilökohtainen ennätys!"), fontSize = 14.sp, color = AppColors.goldText)
                        } else {
                            Text(t("Found $matches/${difficulty.pairs} pairs","Löysit $matches/${difficulty.pairs} paria"),
                                fontSize = 16.sp, color = Color(0xFF555555))
                        }
                        Box(modifier = Modifier
                            .background(
                                if (gameWon) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                                RoundedCornerShape(16.dp))
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                            contentAlignment = Alignment.Center) {
                            Text(rightMsg, fontSize = 15.sp,
                                color = if (gameWon) AppColors.green else AppColors.orange)
                        }
                        Spacer(Modifier.height(4.dp))
                        GemShelf(player)
                    }

                } else {
                    val cardSize = when (difficulty) {
                        Difficulty.EASY   -> 110.dp
                        Difficulty.MEDIUM -> 95.dp
                        Difficulty.HARD   -> 75.dp
                    }
                    val spacing = if (difficulty == Difficulty.HARD) 5.dp else 8.dp

                    for (row in 0 until difficulty.rows) {
                        Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
                            for (col in 0 until difficulty.cols) {
                                val index = row * difficulty.cols + col
                                if (index >= cards.size) break
                                val card = cards[index]
                                val isLetter = card.symbol.length == 1 && card.symbol[0].isLetter()

                                Box(
                                    modifier = Modifier.size(cardSize).clickable {
                                        if (isChecking) return@clickable
                                        if (card.isMatched || card.isFaceUp) return@clickable

                                        boredomDetector.recordAction()
                                        cards = cards.toMutableList().also {
                                            it[index] = it[index].copy(
                                                isFaceUp = true,
                                                lastSeenAt = System.currentTimeMillis()
                                            )
                                        }

                                        if (firstPick == null) {
                                            tracker.cardRevealed()
                                            firstPick = index
                                            memoryHint = ""; emmaMessage = ""
                                        } else {
                                            val reaction = tracker.recordReaction()
                                            boredomDetector.recordReactionTime(reaction)
                                            reactionHint = tracker.hint(reaction)
                                            avgReactionMs = tracker.avgMs()
                                            retentionPct = predictor.retentionPct(cards[firstPick!!].lastSeenAt)
                                            memoryHint = predictor.hint(predictor.retention(cards[firstPick!!].lastSeenAt))
                                            secondPick = index; turns++; isChecking = true

                                            scope.launch {
                                                delay(800)
                                                val first = firstPick!!; val second = secondPick!!
                                                if (cards[first].symbol == cards[second].symbol) {
                                                    cards = cards.toMutableList().also {
                                                        it[first] = it[first].copy(isMatched = true, isFaceUp = false)
                                                        it[second] = it[second].copy(isMatched = true, isFaceUp = false)
                                                    }
                                                    matches++
                                                    val matchMsg = if (showLevelUp) EmmaMessages.levelUp()
                                                    else EmmaMessages.match()
                                                    emmaMessage = matchMsg
                                                } else {
                                                    cards = cards.toMutableList().also {
                                                        it[first] = it[first].copy(isFaceUp = false)
                                                        it[second] = it[second].copy(isFaceUp = false)
                                                    }
                                                    incorrectCount++
                                                    boredomDetector.recordWrongPick()
                                                    val wrongMsg = EmmaMessages.wrong()
                                                    emmaMessage = wrongMsg
                                                    if (incorrectCount % 5 == 0) {
                                                        pepperSpeak(if (appLanguage == Language.FINNISH)
                                                            PEPPER_MOTIVATION_SPEECHES_FI.random()
                                                        else
                                                            PEPPER_MOTIVATION_SPEECHES.random())
                                                    }
                                                }
                                                firstPick = null; secondPick = null; isChecking = false
                                            }
                                        }
                                    },
                                    contentAlignment = Alignment.Center
                                ) {
                                    ShieldCard(
                                        state = when {
                                            card.isMatched -> CardState.MATCHED
                                            card.isFaceUp  -> CardState.REVEALED
                                            else           -> CardState.HIDDEN
                                        },
                                        symbol = card.symbol,
                                        isLetter = isLetter,
                                        cardSize = cardSize
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(spacing))
                    }
                }
            }
        }

        if (showBreakScreen) {
            BreakScreen(
                activity = currentBreakActivity,
                onQuit = {
                    showBreakScreen = false
                    SoundManager.stopBackgroundMusic()
                    (activity as? Activity)?.finish()
                },
                onBreakEnd = {
                    showBreakScreen = false
                    boredomDetector.reset()
                    timerRunning = true
                    if (!SoundManager.isMuted) SoundManager.startBackgroundMusic()
                    pepperSpeak(t("Welcome back ${player.name}! Let's continue the game!",
                        "Tervetuloa takaisin ${player.name}! Jatketaan peliä!"))
                }
            )
        }

        if (showSettings) {
            SettingsPanel(onDismiss = { showSettings = false })
        }
    }
}
