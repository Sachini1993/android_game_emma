package com.sachini.emmamemorygame

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import android.os.Bundle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import kotlin.math.ln
import kotlin.math.roundToInt
import kotlin.math.exp

object AppColors {
    val background   = Color(0xFFFFF8F0)
    val cardBack     = Color(0xFF534AB7)
    val cardBackText = Color(0xFFCECBF6)
    val cardFace     = Color(0xFFFAEEDA)
    val cardMatched  = Color(0xFFEAF3DE)
    val xpBg         = Color(0xFFFAEEDA)
    val xpFill       = Color(0xFFD85A30)
    val xpTrack      = Color(0xFFF5C4B3)
    val xpText       = Color(0xFF633806)
    val xpSub        = Color(0xFF854F0B)
    val timerBg      = Color(0xFFE1F5EE)
    val timerFill    = Color(0xFF1D9E75)
    val timerTrack   = Color(0xFF9FE1CB)
    val emmaMsgBg    = Color(0xFFFBEAF0)
    val emmaMsgText  = Color(0xFF72243E)
    val memoryBg     = Color(0xFFE8F5E9)
    val memoryText   = Color(0xFF27500A)
    val reactionBg   = Color(0xFFE6F1FB)
    val reactionText = Color(0xFF0C447C)
    val skillBarHigh = Color(0xFF1D9E75)
    val skillBarLow  = Color(0xFF9FE1CB)
    val orange       = Color(0xFFD85A30)
    val green        = Color(0xFF1D9E75)
    val purple       = Color(0xFF534AB7)
    val streakBg     = Color(0xFFFFF3E0)
    val streakText   = Color(0xFFE65100)
    val goldBg       = Color(0xFFFFF8E1)
    val goldText     = Color(0xFFFF8F00)
}

data class CardItem(
    val id: Int,
    val symbol: String,
    val isFaceUp: Boolean = false,
    val isMatched: Boolean = false,
    val lastSeenAt: Long = 0L
)

data class HighScoreEntry(
    val initials: String,
    val score: Int,
    val difficulty: String
)

enum class Difficulty(val cols: Int, val rows: Int, val pairs: Int, val label: String) {
    EASY(4, 2, 4, "Easy 4×2"),
    MEDIUM(4, 3, 6, "Medium 4×3"),
    HARD(6, 3, 9, "Hard 6×3")
}

class StreakSystem {
    var currentStreak by mutableStateOf(0)
    var bestStreak by mutableStateOf(0)
    fun onWin() { currentStreak++; if (currentStreak > bestStreak) bestStreak = currentStreak }
    fun onLose() { currentStreak = 0 }
    fun streakEmoji() = when {
        currentStreak >= 10 -> "🔥🔥🔥"
        currentStreak >= 5  -> "🔥🔥"
        currentStreak >= 3  -> "🔥"
        currentStreak >= 1  -> "⭐"
        else -> ""
    }
    fun streakMessage() = when {
        currentStreak >= 10 -> "UNSTOPPABLE!"
        currentStreak >= 5  -> "On a hot streak!"
        currentStreak >= 3  -> "Keep it up!"
        currentStreak >= 1  -> "Win streak!"
        else -> ""
    }
    fun streakBonus() = when {
        currentStreak >= 10 -> 100
        currentStreak >= 5  -> 50
        currentStreak >= 3  -> 25
        currentStreak >= 1  -> 10
        else -> 0
    }
}

class HighScoreSystem {
    private val scores = mutableStateListOf<HighScoreEntry>()
    var showNewRecord by mutableStateOf(false)

    fun calcScore(matches: Int, totalPairs: Int, turns: Int,
                  timeLeft: Int, difficulty: Difficulty): Int {
        val base = when (difficulty) {
            Difficulty.EASY   -> 1000
            Difficulty.MEDIUM -> 2000
            Difficulty.HARD   -> 4000
        }
        return maxOf(0, base + timeLeft * 10 - (turns - totalPairs) * 50)
    }

    fun addScore(initials: String, score: Int, difficulty: Difficulty) {
        scores.add(HighScoreEntry(initials, score, difficulty.label))
        scores.sortByDescending { it.score }
        if (scores.size > 5) scores.removeAt(scores.size - 1)
        showNewRecord = score >= (scores.firstOrNull()?.score ?: 0)
    }

    fun topScores() = scores.take(5)
}

data class PlayerLevel(val level: Int, val title: String, val xpRequired: Int)

val LEVELS = listOf(
    PlayerLevel(1,  "Memory Rookie",   0),
    PlayerLevel(2,  "Card Flipper",    100),
    PlayerLevel(3,  "Pattern Seeker",  250),
    PlayerLevel(4,  "Sharp Eyes",      500),
    PlayerLevel(5,  "Focus Master",    800),
    PlayerLevel(6,  "Brain Trainer",   1200),
    PlayerLevel(7,  "Symbol Wizard",   1700),
    PlayerLevel(8,  "Mind Reader",     2300),
    PlayerLevel(9,  "Memory Champion", 3000),
    PlayerLevel(10, "Memory Legend",   4000)
)

class XPSystem {
    var totalXP by mutableStateOf(0)
    var showLevelUp by mutableStateOf(false)
    var levelUpTitle by mutableStateOf("")

    fun currentLevel() = LEVELS.lastOrNull { totalXP >= it.xpRequired } ?: LEVELS.first()
    fun nextLevel() = LEVELS.getOrNull(currentLevel().level)
    fun xpToNextLevel() = nextLevel()?.xpRequired?.minus(totalXP) ?: 0
    fun xpProgressPct(): Float {
        val cur = currentLevel(); val next = nextLevel() ?: return 1f
        return ((totalXP - cur.xpRequired).toFloat() /
                (next.xpRequired - cur.xpRequired)).coerceIn(0f, 1f)
    }
    fun calcXP(matches: Int, totalPairs: Int, turns: Int,
               timeLeft: Int, difficulty: Difficulty, streakBonus: Int): Int {
        val base = when (difficulty) {
            Difficulty.EASY -> 20; Difficulty.MEDIUM -> 40; Difficulty.HARD -> 80
        }
        val completion = if (matches == totalPairs) base else 0
        val timeBonus = (timeLeft / 10) * 5
        val efficiency = if (turns <= totalPairs + 2) 20 else 0
        return base + completion + timeBonus + efficiency + streakBonus
    }
    fun addXP(xp: Int) {
        val old = currentLevel().level; totalXP += xp
        if (currentLevel().level > old) {
            levelUpTitle = currentLevel().title; showLevelUp = true
        }
    }
}

data class GameTheme(val name: String, val emoji: String, val symbols: List<String>)

val ALL_THEMES = listOf(
    GameTheme("Princesses", "👑", listOf("🧝‍♀️","👸","🧜‍♀️","🧚‍♀️","🧙‍♀️","🦸‍♀️","🧛‍♀️","🧞‍♀️","🧟‍♀️")),
    GameTheme("Anime",     "⚔️", listOf("🥷","🧝‍♂️","🦸‍♂️","🧙‍♂️","🧜‍♂️","🧚‍♂️","🤴","👲","🧑‍🎤")),
    GameTheme("Animals",   "🐾", listOf("🐶","🐱","🐸","🦊","🐼","🦁","🐯","🐨","🐺")),
    GameTheme("Space",     "🚀", listOf("🚀","🌟","🪐","🌍","☄️","👽","🛸","🌙","⭐")),
    GameTheme("Suomi ABC", "🇫🇮", listOf("Ä","Ö","Å","Y","W","X","Z","Q","Ü"))
)

// Rotating motivational messages for Play Again button
val PLAY_AGAIN_MESSAGES = listOf(
    "🔥 Can you beat your score? Play Again!",
    "⚡ One more round — you're on fire!",
    "🏆 Champion material. Prove it again!",
    "🎯 So close to perfection. Try again!",
    "🚀 Level up your brain — Play Again!",
    "💪 Stronger every round. Go again!",
    "🌟 Your best game is still ahead!",
    "🎮 New challenge awaits. Dare to play?"
)

val WIN_RIGHT_MESSAGES = listOf(
    "Your next challenge is ready.\nDo you dare? 🎯",
    "Think you can do even better?\nProve it! 🏆",
    "A new mystery awaits you.\nAre you ready? 🌟",
    "Your brain is warming up.\nKeep going! 🔥",
    "The cards are shuffled.\nCan you master them? 🃏"
)

val LOSE_RIGHT_MESSAGES = listOf(
    "So close! The cards\nare waiting for you. 💪",
    "Every champion fails first.\nTry again! 🏆",
    "Your best round is\njust one game away! 🌟",
    "The clock won't beat\nyou twice! ⏱️🔥"
)

object EmmaMessages {
    private val onMatch   = listOf("Brilliant! 🌟","You remembered! 🎉","Perfect! 🔥","Amazing! 💡","Yes! ✨","Fantastic! 🎊","On fire! 🔥","Great job! 👏")
    private val onWrong   = listOf("Almost! 💪","Keep trying! 😊","You'll get it! 🙌","Don't give up! 🤗","Nearly! 👀","Stay focused! 🧠")
    private val onLevelUp = listOf("LEVEL UP! 🎊","New rank! 🏆","Getting stronger! ⚡","Incredible! 🌟")
    private val onStreak  = listOf("Streak bonus! 🔥","Unstoppable! 🔥","Hot streak! 🔥")
    private val onRecord  = listOf("NEW RECORD! 🏆🎊","All time best! 🥇","Champion! 🏆")
    private val onWinFast = listOf("Lightning fast! ⚡","Speed champion! 🏆")
    private val onWinSlow = listOf("Patience wins! 🎯","Well done! 🎉")

    fun match()   = onMatch.random()
    fun wrong()   = onWrong.random()
    fun levelUp() = onLevelUp.random()
    fun streak()  = onStreak.random()
    fun record()  = onRecord.random()
    fun win(t: Int) = if (t > 60) onWinFast.random() else onWinSlow.random()
}

class MemoryAI {
    private val qTable = Array(3) { row ->
        doubleArrayOf(
            if (row == 0) 0.6 else 0.2,
            if (row == 1) 0.6 else 0.2,
            if (row == 2) 0.6 else 0.2
        )
    }
    private val alpha = 0.3
    private var gamesPlayed = 0
    private var avgReward = 0.5

    fun playerSkill() = when {
        avgReward < 0.35 -> 0
        avgReward < 0.65 -> 1
        else -> 2
    }
    fun chooseDifficulty(): Difficulty {
        val epsilon = maxOf(0.1, 0.8 - gamesPlayed * 0.07)
        val skill = playerSkill()
        return if (Math.random() < epsilon) Difficulty.entries.random()
        else Difficulty.entries[qTable[skill].indices.maxByOrNull { qTable[skill][it] } ?: 1]
    }
    fun calcReward(matches: Int, totalPairs: Int, turns: Int): Double {
        if (turns == 0) return 0.0
        return ((matches.toDouble() / totalPairs) *
                (1.0 / ln(turns.toDouble() + 1.0))).coerceIn(0.0, 1.0)
    }
    fun learn(difficulty: Difficulty, matches: Int, totalPairs: Int, turns: Int) {
        val reward = calcReward(matches, totalPairs, turns)
        val skill = playerSkill(); val action = difficulty.ordinal
        qTable[skill][action] += alpha * (reward - qTable[skill][action])
        avgReward = avgReward * 0.7 + reward * 0.3; gamesPlayed++
    }
    fun skillLabel() = listOf("Beginner","Intermediate","Advanced")[playerSkill()]
    fun chooseDifficultyLabel() = chooseDifficulty().label
    fun qValues(skill: Int) = qTable[skill].map { (it * 100).roundToInt() }
}

class MemoryPredictor {
    fun retention(lastSeenAt: Long): Double {
        if (lastSeenAt == 0L) return 0.0
        return exp(-(System.currentTimeMillis() - lastSeenAt) / 1000.0 / 30.0).coerceIn(0.0, 1.0)
    }
    fun hint(r: Double) = when {
        r == 0.0 -> ""; r > 0.8 -> "🟢 Just saw it!"
        r > 0.5 -> "🟡 Think carefully..."; else -> "🔴 Seen a while ago"
    }
    fun retentionPct(lastSeenAt: Long) = (retention(lastSeenAt) * 100).roundToInt()
}

class ReactionTracker {
    private var revealedAt: Long = 0L
    private val times = mutableListOf<Long>()
    fun cardRevealed() { revealedAt = System.currentTimeMillis() }
    fun recordReaction(): Long {
        if (revealedAt == 0L) return 0L
        val r = System.currentTimeMillis() - revealedAt
        times.add(r); revealedAt = 0L; return r
    }
    fun avgMs() = if (times.isEmpty()) 0L else times.average().toLong()
    fun hint(ms: Long) = when {
        ms == 0L -> ""; ms < 800 -> "⚡ Quick!"
        ms < 2000 -> "🙂 Good pace"; else -> "🤔 Take your time"
    }
    fun confidence() = when {
        avgMs() == 0L -> ""; avgMs() < 800 -> "High confidence"
        avgMs() < 2000 -> "Medium confidence"; else -> "Low confidence"
    }
    fun reset() { times.clear(); revealedAt = 0L }
}

fun createCards(pairs: Int, theme: GameTheme): List<CardItem> {
    val s = theme.symbols.take(pairs)
    return (s + s).mapIndexed { i, sym -> CardItem(i, sym) }.shuffled()
}

@Composable
fun StatChip(value: String, label: String, valueColor: Color) {
    Box(
        modifier = Modifier
            .background(Color.White, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontSize = 16.sp, color = valueColor)
            Text(label, fontSize = 10.sp, color = Color(0xFF888888))
        }
    }
}

@Composable
fun HighScoreBoard(scores: List<HighScoreEntry>) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppColors.goldBg, RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Column {
            Text("🏆 High Scores", fontSize = 14.sp, color = AppColors.goldText)
            Spacer(Modifier.height(6.dp))
            if (scores.isEmpty()) {
                Text("No scores yet!", fontSize = 12.sp, color = Color(0xFF888888))
            } else {
                scores.forEachIndexed { i, entry ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "${listOf("🥇","🥈","🥉","4️⃣","5️⃣")[i]} ${entry.initials}",
                            fontSize = 13.sp,
                            color = if (i == 0) AppColors.goldText else Color(0xFF555555)
                        )
                        Text(entry.difficulty, fontSize = 11.sp, color = Color(0xFF888888))
                        Text("${entry.score} pts", fontSize = 13.sp,
                            color = if (i == 0) AppColors.goldText else Color(0xFF555555))
                    }
                }
            }
        }
    }
}

@Composable
fun DifficultySelector(selected: Difficulty, onSelect: (Difficulty) -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Difficulty.entries.forEach { d ->
            val isSelected = d == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(
                        if (isSelected) AppColors.purple else Color.White,
                        RoundedCornerShape(10.dp)
                    )
                    .clickable { onSelect(d) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(d.label, fontSize = 11.sp,
                    color = if (isSelected) Color.White else AppColors.purple)
            }
        }
    }
}

@Composable
fun ThemeSelector(selected: GameTheme, onSelect: (GameTheme) -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        ALL_THEMES.forEach { t ->
            val isSelected = t.name == selected.name
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(
                        if (isSelected) AppColors.orange else Color.White,
                        RoundedCornerShape(8.dp)
                    )
                    .clickable { onSelect(t) }
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(t.emoji, fontSize = 14.sp)
                    Text(t.name, fontSize = 8.sp,
                        color = if (isSelected) Color.White else Color(0xFF555555))
                }
            }
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MemoryGameScreen() }
    }
}

@Composable
fun MemoryGameScreen() {
    val ai = remember { MemoryAI() }
    val predictor = remember { MemoryPredictor() }
    val tracker = remember { ReactionTracker() }
    val xpSystem = remember { XPSystem() }
    val streakSystem = remember { StreakSystem() }
    val highScoreSystem = remember { HighScoreSystem() }
    val scrollState = rememberScrollState()

    var firstGame by remember { mutableStateOf(true) }
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
    var timeLeft by remember { mutableStateOf(120) }
    var timerRunning by remember { mutableStateOf(false) }
    var gameStarted by remember { mutableStateOf(false) }
    var playAgainMessage by remember { mutableStateOf(PLAY_AGAIN_MESSAGES.random()) }
    var rightPanelMessage by remember { mutableStateOf(WIN_RIGHT_MESSAGES.random()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(timerRunning) {
        while (timerRunning && timeLeft > 0) {
            delay(1000); timeLeft--
        }
    }

    val gameWon  = gameStarted && matches == difficulty.pairs
    val gameLost = gameStarted && timeLeft == 0 && !gameWon
    val skill    = ai.playerSkill()
    val qVals    = ai.qValues(skill)
    val curLevel = xpSystem.currentLevel()
    val nextLevel = xpSystem.nextLevel()

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
        xpSystem.showLevelUp = false
        highScoreSystem.showNewRecord = false
        tracker.reset()
        // Pick fresh motivational messages
        playAgainMessage = PLAY_AGAIN_MESSAGES.random()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── LEFT PANEL ────────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .weight(0.42f)
                    .fillMaxHeight()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // XP Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AppColors.xpBg, RoundedCornerShape(12.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("⭐ Lv.${curLevel.level} ${curLevel.title}",
                                fontSize = 12.sp, color = AppColors.xpText)
                            Text("${xpSystem.totalXP} XP",
                                fontSize = 11.sp, color = AppColors.xpSub)
                        }
                        Spacer(Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth().height(8.dp)
                                .background(AppColors.xpTrack, RoundedCornerShape(4.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(xpSystem.xpProgressPct()).height(8.dp)
                                    .background(AppColors.xpFill, RoundedCornerShape(4.dp))
                            )
                        }
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth().padding(top = 2.dp)
                        ) {
                            if (nextLevel != null)
                                Text("${xpSystem.xpToNextLevel()} XP to ${nextLevel.title}",
                                    fontSize = 10.sp, color = AppColors.xpSub)
                            if (lastXPEarned > 0)
                                Text("+$lastXPEarned XP!", fontSize = 11.sp,
                                    color = AppColors.green)
                        }
                    }
                }

                // Level up banner
                if (xpSystem.showLevelUp) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(AppColors.purple, RoundedCornerShape(12.dp))
                            .padding(10.dp)
                            .clickable { xpSystem.showLevelUp = false }
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("🎊 LEVEL UP!", fontSize = 16.sp, color = Color.White)
                            Text(xpSystem.levelUpTitle, fontSize = 12.sp,
                                color = Color(0xFFCECBF6))
                            Text("tap to dismiss", fontSize = 10.sp,
                                color = Color(0xFFAFA9EC))
                        }
                    }
                }

                // Streak
                if (streakSystem.currentStreak > 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(AppColors.streakBg, RoundedCornerShape(10.dp))
                            .padding(8.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("${streakSystem.streakEmoji()} ${streakSystem.streakMessage()}",
                                fontSize = 12.sp, color = AppColors.streakText)
                            Text("🔥 ${streakSystem.currentStreak}  Best: ${streakSystem.bestStreak}",
                                fontSize = 11.sp, color = Color(0xFF888888))
                        }
                    }
                }

                // Title
                Text("Emma Memory", fontSize = 20.sp, color = AppColors.orange)
                Text("${theme.emoji} ${theme.name} · ${difficulty.label}",
                    fontSize = 11.sp, color = AppColors.xpSub)

                // First game: manual selectors only
                if (firstGame) {
                    Text("Select Difficulty:", fontSize = 11.sp, color = Color(0xFF888888))
                    DifficultySelector(selected = difficulty) { d ->
                        difficulty = d
                        cards = createCards(d.pairs, theme)
                    }
                    Spacer(Modifier.height(2.dp))
                    Text("Select Theme:", fontSize = 11.sp, color = Color(0xFF888888))
                    ThemeSelector(selected = theme) { t ->
                        theme = t
                        cards = createCards(difficulty.pairs, t)
                    }
                }

                // Start / Play Again button
                if (!gameStarted || gameWon || gameLost) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(AppColors.purple, RoundedCornerShape(14.dp))
                            .clickable {
                                if (firstGame) {
                                    startGame(difficulty, theme)
                                } else {
                                    val aiDiff = ai.chooseDifficulty()
                                    val newTheme = ALL_THEMES.random()
                                    rightPanelMessage = if (gameWon)
                                        WIN_RIGHT_MESSAGES.random()
                                    else LOSE_RIGHT_MESSAGES.random()
                                    startGame(aiDiff, newTheme)
                                }
                            }
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            if (firstGame) "▶  Start Game"
                            else playAgainMessage,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                }

                if (gameStarted) {
                    // Stats
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        StatChip("$matches/${difficulty.pairs}", "Matches", AppColors.green)
                        StatChip("$turns", "Turns", AppColors.purple)
                        StatChip(ai.skillLabel(), "Skill", AppColors.orange)
                    }

                    // Timer
                    val timerColor = when {
                        timeLeft > 60 -> AppColors.timerFill
                        timeLeft > 20 -> Color(0xFFFF9800)
                        else -> Color(0xFFF44336)
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(AppColors.timerBg, RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("⏱ ${timeLeft}s  ", fontSize = 13.sp, color = timerColor)
                            Box(
                                modifier = Modifier
                                    .weight(1f).height(8.dp)
                                    .background(AppColors.timerTrack, RoundedCornerShape(4.dp))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(timeLeft / 120f).height(8.dp)
                                        .background(timerColor, RoundedCornerShape(4.dp))
                                )
                            }
                        }
                    }

                    // Emma message
                    if (emmaMessage.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(AppColors.emmaMsgBg, RoundedCornerShape(10.dp))
                                .padding(8.dp)
                        ) {
                            Text("🤖 Emma: $emmaMessage",
                                fontSize = 12.sp, color = AppColors.emmaMsgText)
                        }
                    }

                    // Memory + Reaction
                    if (memoryHint.isNotEmpty() || reactionHint.isNotEmpty()) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (memoryHint.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(AppColors.memoryBg, RoundedCornerShape(10.dp))
                                        .padding(8.dp)
                                ) {
                                    Column {
                                        Text(memoryHint, fontSize = 11.sp,
                                            color = AppColors.memoryText)
                                        Spacer(Modifier.height(3.dp))
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth().height(5.dp)
                                                .background(Color(0xFFB7DFA0),
                                                    RoundedCornerShape(3.dp))
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth(retentionPct / 100f)
                                                    .height(5.dp)
                                                    .background(
                                                        when {
                                                            retentionPct > 80 -> AppColors.green
                                                            retentionPct > 50 -> Color(0xFFFF9800)
                                                            else -> Color(0xFFF44336)
                                                        },
                                                        RoundedCornerShape(3.dp)
                                                    )
                                            )
                                        }
                                        Text("$retentionPct%", fontSize = 10.sp,
                                            color = AppColors.memoryText)
                                    }
                                }
                            }
                            if (reactionHint.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(AppColors.reactionBg, RoundedCornerShape(10.dp))
                                        .padding(8.dp)
                                ) {
                                    Column {
                                        Text(reactionHint, fontSize = 11.sp,
                                            color = AppColors.reactionText)
                                        if (avgReactionMs > 0)
                                            Text("${avgReactionMs}ms · ${tracker.confidence()}",
                                                fontSize = 10.sp, color = AppColors.reactionText)
                                    }
                                }
                            }
                        }
                    }

                    // Skill Meter
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Text("📊 Skill Meter", fontSize = 12.sp, color = Color(0xFF555555))
                            Spacer(Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                listOf("Easy","Medium","Hard").forEachIndexed { i, label ->
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(label, fontSize = 10.sp, color = Color(0xFF888888))
                                        Spacer(Modifier.height(3.dp))
                                        Box(
                                            modifier = Modifier
                                                .width(28.dp).height(36.dp)
                                                .background(Color(0xFFE8F5E9),
                                                    RoundedCornerShape(4.dp)),
                                            contentAlignment = Alignment.BottomCenter
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .width(28.dp)
                                                    .fillMaxHeight(qVals[i] / 100f)
                                                    .background(
                                                        if (qVals[i] == qVals.max())
                                                            AppColors.skillBarHigh
                                                        else AppColors.skillBarLow,
                                                        RoundedCornerShape(4.dp)
                                                    )
                                            )
                                        }
                                        Text("${qVals[i]}", fontSize = 11.sp,
                                            color = if (qVals[i] == qVals.max())
                                                AppColors.green else Color(0xFF888888))
                                    }
                                }
                            }
                        }
                    }

                    // Win result
                    if (gameWon) {
                        val xpEarned = xpSystem.calcXP(matches, difficulty.pairs,
                            turns, timeLeft, difficulty, streakSystem.streakBonus())
                        val score = highScoreSystem.calcScore(matches, difficulty.pairs,
                            turns, timeLeft, difficulty)
                        LaunchedEffect(Unit) {
                            lastXPEarned = xpEarned; lastScore = score
                            xpSystem.addXP(xpEarned); streakSystem.onWin()
                            highScoreSystem.addScore("YOU", score, difficulty)
                            ai.learn(difficulty, matches, difficulty.pairs, turns)
                            emmaMessage = when {
                                highScoreSystem.showNewRecord -> EmmaMessages.record()
                                xpSystem.showLevelUp -> EmmaMessages.levelUp()
                                streakSystem.currentStreak > 2 -> EmmaMessages.streak()
                                else -> EmmaMessages.win(timeLeft)
                            }
                        }
                        Text("🎉 You Win!", fontSize = 26.sp, color = AppColors.green)
                        Text("+$xpEarned XP  🔥 Streak: ${streakSystem.currentStreak}",
                            fontSize = 13.sp, color = AppColors.purple)
                        Text("Score: $score pts", fontSize = 16.sp, color = AppColors.goldText)
                        Spacer(Modifier.height(6.dp))
                        HighScoreBoard(highScoreSystem.topScores())

                    } else if (gameLost) {
                        val xpEarned = xpSystem.calcXP(matches, difficulty.pairs,
                            turns, 0, difficulty, 0)
                        LaunchedEffect(Unit) {
                            lastXPEarned = xpEarned; xpSystem.addXP(xpEarned)
                            streakSystem.onLose()
                            emmaMessage = "So close! Try again! 💪"
                        }
                        Text("⏰ Time's Up!", fontSize = 26.sp, color = AppColors.orange)
                        Text("Found $matches/${difficulty.pairs} pairs", fontSize = 13.sp)
                        Text("+$xpEarned XP  🔥 Streak lost", fontSize = 13.sp,
                            color = AppColors.purple)
                        Spacer(Modifier.height(6.dp))
                        HighScoreBoard(highScoreSystem.topScores())
                    }
                }
            }

            // ── RIGHT PANEL ───────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .weight(0.58f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!gameStarted) {
                    // Welcome screen
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text("🧠", fontSize = 72.sp)
                        Spacer(Modifier.height(16.dp))
                        Text("Welcome to Emma Memory!",
                            fontSize = 22.sp, color = AppColors.orange)
                        Spacer(Modifier.height(8.dp))
                        Text("Select your difficulty and theme\non the left, then press Start!",
                            fontSize = 14.sp, color = Color(0xFF888888))
                    }

                } else if (gameWon || gameLost) {
                    // Result on right side
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(if (gameWon) "🎉" else "⏰", fontSize = 72.sp)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            if (gameWon) "You Win!" else "Time's Up!",
                            fontSize = 32.sp,
                            color = if (gameWon) AppColors.green else AppColors.orange
                        )
                        Spacer(Modifier.height(8.dp))
                        if (gameWon) {
                            Text("Score: $lastScore pts", fontSize = 20.sp,
                                color = AppColors.goldText)
                        } else {
                            Text("Found $matches/${difficulty.pairs} pairs",
                                fontSize = 16.sp, color = Color(0xFF555555))
                        }
                        Spacer(Modifier.height(20.dp))
                        // Attractive motivational message
                        Box(
                            modifier = Modifier
                                .background(
                                    if (gameWon) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                                    RoundedCornerShape(16.dp)
                                )
                                .padding(horizontal = 20.dp, vertical = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                rightPanelMessage,
                                fontSize = 16.sp,
                                color = if (gameWon) AppColors.green else AppColors.orange
                            )
                        }
                    }

                } else {
                    // Card grid
                    for (row in 0 until difficulty.rows) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            for (col in 0 until difficulty.cols) {
                                val index = row * difficulty.cols + col
                                if (index >= cards.size) break
                                val card = cards[index]
                                val isFlipped = card.isFaceUp || card.isMatched
                                val isLetter = card.symbol.length == 1 &&
                                        card.symbol[0].isLetter()

                                Box(
                                    modifier = Modifier
                                        .size(90.dp)
                                        .background(
                                            color = when {
                                                card.isMatched -> AppColors.cardMatched
                                                card.isFaceUp  -> AppColors.cardFace
                                                else           -> AppColors.cardBack
                                            },
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                        .clickable {
                                            if (isChecking) return@clickable
                                            if (card.isMatched || card.isFaceUp) return@clickable

                                            cards = cards.toMutableList().also {
                                                it[index] = it[index].copy(
                                                    isFaceUp = true,
                                                    lastSeenAt = System.currentTimeMillis()
                                                )
                                            }

                                            if (firstPick == null) {
                                                tracker.cardRevealed()
                                                firstPick = index
                                                memoryHint = ""
                                                emmaMessage = ""
                                            } else {
                                                val reaction = tracker.recordReaction()
                                                reactionHint = tracker.hint(reaction)
                                                avgReactionMs = tracker.avgMs()
                                                retentionPct = predictor.retentionPct(
                                                    cards[firstPick!!].lastSeenAt)
                                                memoryHint = predictor.hint(
                                                    predictor.retention(
                                                        cards[firstPick!!].lastSeenAt))
                                                secondPick = index
                                                turns++
                                                isChecking = true

                                                scope.launch {
                                                    delay(800)
                                                    val first = firstPick!!
                                                    val second = secondPick!!
                                                    if (cards[first].symbol == cards[second].symbol) {
                                                        cards = cards.toMutableList().also {
                                                            it[first] = it[first].copy(
                                                                isMatched = true, isFaceUp = false)
                                                            it[second] = it[second].copy(
                                                                isMatched = true, isFaceUp = false)
                                                        }
                                                        matches++
                                                        emmaMessage = if (xpSystem.showLevelUp)
                                                            EmmaMessages.levelUp()
                                                        else EmmaMessages.match()
                                                    } else {
                                                        cards = cards.toMutableList().also {
                                                            it[first] = it[first].copy(isFaceUp = false)
                                                            it[second] = it[second].copy(isFaceUp = false)
                                                        }
                                                        emmaMessage = EmmaMessages.wrong()
                                                    }
                                                    firstPick = null
                                                    secondPick = null
                                                    isChecking = false
                                                }
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (isFlipped) card.symbol else "?",
                                        fontSize = when {
                                            !isFlipped -> 28.sp
                                            isLetter   -> 42.sp
                                            else       -> 34.sp
                                        },
                                        color = when {
                                            !isFlipped     -> AppColors.cardBackText
                                            card.isMatched -> Color(0xFF2E7D32)
                                            else           -> AppColors.xpText
                                        }
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}