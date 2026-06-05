package com.sachini.emmamemorygame

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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

// ── Colors ────────────────────────────────────────────────────────────────────

object AppColors {
    val background     = Color(0xFFFFF8F0)
    val cardBack       = Color(0xFF534AB7)
    val cardBackText   = Color(0xFFCECBF6)
    val cardFace       = Color(0xFFFAEEDA)
    val cardMatched    = Color(0xFFEAF3DE)
    val xpBg           = Color(0xFFFAEEDA)
    val xpFill         = Color(0xFFD85A30)
    val xpTrack        = Color(0xFFF5C4B3)
    val xpText         = Color(0xFF633806)
    val xpSub          = Color(0xFF854F0B)
    val timerBg        = Color(0xFFE1F5EE)
    val timerFill      = Color(0xFF1D9E75)
    val timerTrack     = Color(0xFF9FE1CB)
    val timerText      = Color(0xFF085041)
    val emmaMsgBg      = Color(0xFFFBEAF0)
    val emmaMsgText    = Color(0xFF72243E)
    val memoryBg       = Color(0xFFE8F5E9)
    val memoryText     = Color(0xFF27500A)
    val reactionBg     = Color(0xFFE6F1FB)
    val reactionText   = Color(0xFF0C447C)
    val skillBarHigh   = Color(0xFF1D9E75)
    val skillBarLow    = Color(0xFF9FE1CB)
    val orange         = Color(0xFFD85A30)
    val green          = Color(0xFF1D9E75)
    val purple         = Color(0xFF534AB7)
    val streakBg       = Color(0xFFFFF3E0)
    val streakText     = Color(0xFFE65100)
    val goldBg         = Color(0xFFFFF8E1)
    val goldText       = Color(0xFFFF8F00)
}

// ── Data ──────────────────────────────────────────────────────────────────────

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
    EASY(3, 4, 6, "Easy 3×4"),
    MEDIUM(4, 4, 8, "Medium 4×4"),
    HARD(4, 6, 12, "Hard 4×6")
}

// ── Streak System ─────────────────────────────────────────────────────────────

class StreakSystem {
    var currentStreak by mutableStateOf(0)
    var bestStreak by mutableStateOf(0)

    fun onWin() {
        currentStreak++
        if (currentStreak > bestStreak) bestStreak = currentStreak
    }

    fun onLose() { currentStreak = 0 }

    fun streakEmoji() = when {
        currentStreak >= 10 -> "🔥🔥🔥"
        currentStreak >= 5  -> "🔥🔥"
        currentStreak >= 3  -> "🔥"
        currentStreak >= 1  -> "⭐"
        else                -> ""
    }

    fun streakMessage() = when {
        currentStreak >= 10 -> "UNSTOPPABLE!"
        currentStreak >= 5  -> "On a hot streak!"
        currentStreak >= 3  -> "Keep it up!"
        currentStreak >= 1  -> "Win streak!"
        else                -> ""
    }

    // Bonus XP for streaks
    fun streakBonus(): Int = when {
        currentStreak >= 10 -> 100
        currentStreak >= 5  -> 50
        currentStreak >= 3  -> 25
        currentStreak >= 1  -> 10
        else                -> 0
    }
}

// ── High Score System ─────────────────────────────────────────────────────────

class HighScoreSystem {
    private val scores = mutableStateListOf<HighScoreEntry>()
    var showNewRecord by mutableStateOf(false)

    fun calcScore(matches: Int, totalPairs: Int,
                  turns: Int, timeLeft: Int,
                  difficulty: Difficulty): Int {
        val base = when (difficulty) {
            Difficulty.EASY   -> 1000
            Difficulty.MEDIUM -> 2000
            Difficulty.HARD   -> 4000
        }
        val timeBonus   = timeLeft * 10
        val turnPenalty = (turns - totalPairs) * 50
        return maxOf(0, base + timeBonus - turnPenalty)
    }

    fun addScore(initials: String, score: Int, difficulty: Difficulty) {
        val entry = HighScoreEntry(initials, score, difficulty.label)
        scores.add(entry)
        scores.sortByDescending { it.score }
        if (scores.size > 5) scores.removeAt(scores.size - 1)
        showNewRecord = score >= (scores.firstOrNull()?.score ?: 0)
    }

    fun topScores() = scores.take(5)
    fun isTopScore(score: Int) = scores.isEmpty() ||
            score >= (scores.minOfOrNull { it.score } ?: 0)
}

// ── XP System ─────────────────────────────────────────────────────────────────

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

    fun xpToNextLevel(): Int {
        val next = nextLevel() ?: return 0
        return next.xpRequired - totalXP
    }

    fun xpProgressPct(): Float {
        val cur = currentLevel()
        val next = nextLevel() ?: return 1f
        val range = next.xpRequired - cur.xpRequired
        val progress = totalXP - cur.xpRequired
        return (progress.toFloat() / range).coerceIn(0f, 1f)
    }

    fun calcXP(matches: Int, totalPairs: Int, turns: Int,
               timeLeft: Int, difficulty: Difficulty,
               streakBonus: Int): Int {
        val base = when (difficulty) {
            Difficulty.EASY   -> 20
            Difficulty.MEDIUM -> 40
            Difficulty.HARD   -> 80
        }
        val completion  = if (matches == totalPairs) base else 0
        val timeBonus   = (timeLeft / 10) * 5
        val efficiency  = if (turns <= totalPairs + 2) 20 else 0
        return base + completion + timeBonus + efficiency + streakBonus
    }

    fun addXP(xp: Int) {
        val oldLevel = currentLevel().level
        totalXP += xp
        if (currentLevel().level > oldLevel) {
            levelUpTitle = currentLevel().title
            showLevelUp = true
        }
    }
}

// ── Themes ────────────────────────────────────────────────────────────────────

data class GameTheme(val name: String, val emoji: String, val symbols: List<String>)

val ALL_THEMES = listOf(
    GameTheme("Princesses", "👑", listOf(
        "🧝‍♀️","👸","🧜‍♀️","🧚‍♀️","🧙‍♀️","🦸‍♀️","🧛‍♀️","🧞‍♀️","🧟‍♀️","🧖‍♀️","🧝‍♂️","🧙‍♂️"
    )),
    GameTheme("Anime Heroes","⚔️", listOf(
        "🥷","🧝‍♂️","🦸‍♂️","🧙‍♂️","🧜‍♂️","🧚‍♂️","🤴","👲","🧑‍🎤","🥸","🤠","🧑‍🚀"
    )),
    GameTheme("Animals",    "🐾", listOf(
        "🐶","🐱","🐸","🦊","🐼","🦁","🐯","🐨","🐺","🦝","🐮","🐷"
    )),
    GameTheme("Space",      "🚀", listOf(
        "🚀","🌟","🪐","🌍","☄️","👽","🛸","🌙","⭐","🌠","🔭","💫"
    ))
)

// ── Emma Messages ─────────────────────────────────────────────────────────────

object EmmaMessages {
    private val onMatch   = listOf("Brilliant! 🌟","You remembered! 🎉","Perfect! 🔥","Amazing! 💡","Yes! ✨","Fantastic! 🎊","On fire! 🔥","Great job! 👏")
    private val onWrong   = listOf("Almost! 💪","Keep trying! 😊","You'll get it! 🙌","Don't give up! 🤗","Nearly! 👀","Stay focused! 🧠","Try again! 😄","Keep going! 🚀")
    private val onLevelUp = listOf("LEVEL UP! 🎊","New rank! 🏆","Getting stronger! ⚡","Incredible! 🌟")
    private val onStreak  = listOf("Streak bonus! 🔥","You're unstoppable! 🔥","Hot streak! 🔥")
    private val onWinFast = listOf("Lightning fast! ⚡","Speed champion! 🏆","Incredible! 🥇")
    private val onWinSlow = listOf("Patience wins! 🎯","Steady and sure! 🌟","Well done! 🎉")
    private val onRecord  = listOf("NEW RECORD! 🏆🎊","All time best! 🥇","Champion! 🏆")

    fun match()   = onMatch.random()
    fun wrong()   = onWrong.random()
    fun levelUp() = onLevelUp.random()
    fun streak()  = onStreak.random()
    fun record()  = onRecord.random()
    fun win(timeLeft: Int) =
        if (timeLeft > 30) onWinFast.random() else onWinSlow.random()
}

// ── AI: Q-Learning ────────────────────────────────────────────────────────────

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

    fun playerSkill(): Int = when {
        avgReward < 0.35 -> 0
        avgReward < 0.65 -> 1
        else -> 2
    }

    fun chooseDifficulty(): Difficulty {
        val epsilon = maxOf(0.1, 0.8 - gamesPlayed * 0.07)
        val skill = playerSkill()
        return if (Math.random() < epsilon) Difficulty.entries.random()
        else {
            val best = qTable[skill].indices.maxByOrNull { qTable[skill][it] } ?: 1
            Difficulty.entries[best]
        }
    }

    fun calcReward(matches: Int, totalPairs: Int, turns: Int): Double {
        if (turns == 0) return 0.0
        return ((matches.toDouble() / totalPairs) *
                (1.0 / ln(turns.toDouble() + 1.0))).coerceIn(0.0, 1.0)
    }

    fun learn(difficulty: Difficulty, matches: Int, totalPairs: Int, turns: Int) {
        val reward = calcReward(matches, totalPairs, turns)
        val skill = playerSkill()
        val action = difficulty.ordinal
        qTable[skill][action] += alpha * (reward - qTable[skill][action])
        avgReward = avgReward * 0.7 + reward * 0.3
        gamesPlayed++
    }

    fun skillLabel() = listOf("Beginner","Intermediate","Advanced")[playerSkill()]
    fun nextDiffLabel() = chooseDifficulty().label
    fun qValues(skill: Int) = qTable[skill].map { (it * 100).roundToInt() }
}

// ── AI: Ebbinghaus ────────────────────────────────────────────────────────────

class MemoryPredictor {
    fun retention(lastSeenAt: Long): Double {
        if (lastSeenAt == 0L) return 0.0
        val t = (System.currentTimeMillis() - lastSeenAt) / 1000.0
        return exp(-t / 30.0).coerceIn(0.0, 1.0)
    }
    fun hint(r: Double) = when {
        r == 0.0 -> ""
        r > 0.8  -> "🟢 Just saw it!"
        r > 0.5  -> "🟡 Think carefully..."
        else     -> "🔴 Seen a while ago"
    }
    fun retentionPct(lastSeenAt: Long) = (retention(lastSeenAt) * 100).roundToInt()
}

// ── AI: Reaction Time ─────────────────────────────────────────────────────────

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
        ms == 0L  -> ""
        ms < 800  -> "⚡ Quick!"
        ms < 2000 -> "🙂 Good pace"
        else      -> "🤔 Take your time"
    }
    fun confidence() = when {
        avgMs() == 0L  -> ""
        avgMs() < 800  -> "High confidence"
        avgMs() < 2000 -> "Medium confidence"
        else           -> "Low confidence"
    }
    fun reset() { times.clear(); revealedAt = 0L }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

fun createCards(pairs: Int, theme: GameTheme): List<CardItem> {
    val s = theme.symbols.take(pairs)
    return (s + s).mapIndexed { i, sym -> CardItem(i, sym) }.shuffled()
}

// ── Composables ───────────────────────────────────────────────────────────────

@Composable
fun StatChip(value: String, label: String, valueColor: Color) {
    Box(
        modifier = Modifier
            .background(Color.White, RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontSize = 15.sp, color = valueColor)
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
            Text("🏆 High Scores",
                fontSize = 13.sp, color = AppColors.goldText)
            Spacer(Modifier.height(6.dp))
            if (scores.isEmpty()) {
                Text("No scores yet — play to get on the board!",
                    fontSize = 12.sp, color = Color(0xFF888888))
            } else {
                scores.forEachIndexed { i, entry ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "${listOf("🥇","🥈","🥉","4️⃣","5️⃣")[i]} ${entry.initials}",
                            fontSize = 13.sp,
                            color = if (i == 0) AppColors.goldText
                            else Color(0xFF555555)
                        )
                        Text(entry.difficulty,
                            fontSize = 11.sp, color = Color(0xFF888888))
                        Text("${entry.score} pts",
                            fontSize = 13.sp,
                            color = if (i == 0) AppColors.goldText
                            else Color(0xFF555555))
                    }
                }
            }
        }
    }
}

// ── Main ──────────────────────────────────────────────────────────────────────

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

    var theme by remember { mutableStateOf(ALL_THEMES.random()) }
    var difficulty by remember { mutableStateOf(Difficulty.MEDIUM) }
    var cards by remember { mutableStateOf(createCards(difficulty.pairs, theme)) }
    var firstPick by remember { mutableStateOf<Int?>(null) }
    var secondPick by remember { mutableStateOf<Int?>(null) }
    var isChecking by remember { mutableStateOf(false) }
    var matches by remember { mutableStateOf(0) }
    var turns by remember { mutableStateOf(0) }
    var nextDifficulty by remember { mutableStateOf(Difficulty.MEDIUM) }
    var memoryHint by remember { mutableStateOf("") }
    var retentionPct by remember { mutableStateOf(0) }
    var reactionHint by remember { mutableStateOf("") }
    var avgReactionMs by remember { mutableStateOf(0L) }
    var emmaMessage by remember { mutableStateOf("") }
    var lastXPEarned by remember { mutableStateOf(0) }
    var lastScore by remember { mutableStateOf(0) }
    var timeLeft by remember { mutableStateOf(60) }
    var timerRunning by remember { mutableStateOf(true) }
    var showScoreBoard by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(timerRunning) {
        while (timerRunning && timeLeft > 0) {
            delay(1000); timeLeft--
        }
    }

    val gameWon  = matches == difficulty.pairs
    val gameLost = timeLeft == 0 && !gameWon
    val skill    = ai.playerSkill()
    val qVals    = ai.qValues(skill)
    val curLevel = xpSystem.currentLevel()
    val nextLevel= xpSystem.nextLevel()

    if (gameWon || gameLost) timerRunning = false

    fun resetGame(d: Difficulty) {
        val t = ALL_THEMES.random()
        theme = t; difficulty = d
        cards = createCards(d.pairs, t)
        firstPick = null; secondPick = null
        matches = 0; turns = 0
        timeLeft = 60; timerRunning = true
        memoryHint = ""; retentionPct = 0
        reactionHint = ""; avgReactionMs = 0L
        emmaMessage = ""; lastXPEarned = 0
        lastScore = 0; showScoreBoard = false
        xpSystem.showLevelUp = false
        highScoreSystem.showNewRecord = false
        tracker.reset()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ── XP Bar ────────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
                    .background(AppColors.xpBg, RoundedCornerShape(14.dp))
                    .padding(10.dp)
            ) {
                Column {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("⭐ Lv.${curLevel.level}  ${curLevel.title}",
                            fontSize = 12.sp, color = AppColors.xpText)
                        Text("${xpSystem.totalXP} XP",
                            fontSize = 11.sp, color = AppColors.xpSub)
                    }
                    Spacer(Modifier.height(5.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth().height(8.dp)
                            .background(AppColors.xpTrack, RoundedCornerShape(4.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(xpSystem.xpProgressPct())
                                .height(8.dp)
                                .background(AppColors.xpFill, RoundedCornerShape(4.dp))
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth().padding(top = 3.dp)
                    ) {
                        if (nextLevel != null)
                            Text("${xpSystem.xpToNextLevel()} XP to ${nextLevel.title}",
                                fontSize = 10.sp, color = AppColors.xpSub)
                        if (lastXPEarned > 0)
                            Text("+$lastXPEarned XP!",
                                fontSize = 11.sp, color = AppColors.green)
                    }
                }
            }

            // Level up banner
            if (xpSystem.showLevelUp) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth().padding(bottom = 6.dp)
                        .background(AppColors.purple, RoundedCornerShape(14.dp))
                        .padding(12.dp)
                        .clickable { xpSystem.showLevelUp = false }
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("🎊 LEVEL UP! 🎊", fontSize = 18.sp, color = Color.White)
                        Text("You are now: ${xpSystem.levelUpTitle}",
                            fontSize = 13.sp, color = Color(0xFFCECBF6))
                        Text("tap to dismiss", fontSize = 10.sp,
                            color = Color(0xFFAFA9EC))
                    }
                }
            }

            // ── Streak bar ────────────────────────────────────────────────────
            if (streakSystem.currentStreak > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth().padding(bottom = 6.dp)
                        .background(AppColors.streakBg, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "${streakSystem.streakEmoji()} ${streakSystem.streakMessage()}",
                            fontSize = 13.sp, color = AppColors.streakText
                        )
                        Text(
                            "🔥 ${streakSystem.currentStreak} wins  " +
                                    "Best: ${streakSystem.bestStreak}",
                            fontSize = 11.sp, color = Color(0xFF888888)
                        )
                    }
                }
            }

            // New record banner
            if (highScoreSystem.showNewRecord) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth().padding(bottom = 6.dp)
                        .background(AppColors.goldBg, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text("🏆 NEW HIGH SCORE: $lastScore pts!",
                        fontSize = 13.sp, color = AppColors.goldText)
                }
            }

            // Title
            Text("Emma Memory", fontSize = 22.sp, color = AppColors.orange,
                modifier = Modifier.padding(bottom = 2.dp))
            Text("${theme.emoji} ${theme.name} · ${difficulty.label}",
                fontSize = 12.sp, color = AppColors.xpSub,
                modifier = Modifier.padding(bottom = 6.dp))

            // Stats row
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
            ) {
                StatChip("$matches/${difficulty.pairs}", "Matches", AppColors.green)
                StatChip("$turns", "Turns", AppColors.purple)
                StatChip(ai.skillLabel(), "Skill", AppColors.orange)
            }

            // Timer
            val timerColor = when {
                timeLeft > 30 -> AppColors.timerFill
                timeLeft > 10 -> Color(0xFFFF9800)
                else          -> Color(0xFFF44336)
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth().padding(bottom = 6.dp)
                    .background(AppColors.timerBg, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 7.dp)
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
                                .fillMaxWidth(timeLeft / 60f).height(8.dp)
                                .background(timerColor, RoundedCornerShape(4.dp))
                        )
                    }
                }
            }

            // Emma message
            if (emmaMessage.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth().padding(bottom = 6.dp)
                        .background(AppColors.emmaMsgBg, RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                ) {
                    Text("🤖  Emma: $emmaMessage",
                        fontSize = 12.sp, color = AppColors.emmaMsgText)
                }
            }

            // Memory + Reaction side by side
            if (memoryHint.isNotEmpty() || reactionHint.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
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
                                Spacer(Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth().height(5.dp)
                                        .background(Color(0xFFB7DFA0), RoundedCornerShape(3.dp))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(retentionPct / 100f).height(5.dp)
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
                                    color = AppColors.memoryText,
                                    modifier = Modifier.padding(top = 2.dp))
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
                                if (avgReactionMs > 0) {
                                    Text("${avgReactionMs}ms · ${tracker.confidence()}",
                                        fontSize = 10.sp, color = AppColors.reactionText,
                                        modifier = Modifier.padding(top = 2.dp))
                                }
                            }
                        }
                    }
                }
            }

            // Skill Meter
            Box(
                modifier = Modifier
                    .fillMaxWidth().padding(bottom = 8.dp)
                    .background(Color.White, RoundedCornerShape(14.dp))
                    .padding(10.dp)
            ) {
                Column {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("📊 Skill Meter",
                            fontSize = 12.sp, color = Color(0xFF555555))
                        Text("Next: ${ai.nextDiffLabel()}",
                            fontSize = 10.sp, color = AppColors.orange)
                    }
                    Spacer(Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listOf("Easy","Medium","Hard").forEachIndexed { i, label ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.width(64.dp)
                            ) {
                                Text(label, fontSize = 11.sp, color = Color(0xFF888888))
                                Spacer(Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .width(32.dp).height(40.dp)
                                        .background(Color(0xFFE8F5E9),
                                            RoundedCornerShape(4.dp)),
                                    contentAlignment = Alignment.BottomCenter
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .width(32.dp)
                                            .fillMaxHeight(qVals[i] / 100f)
                                            .background(
                                                if (qVals[i] == qVals.max())
                                                    AppColors.skillBarHigh
                                                else AppColors.skillBarLow,
                                                RoundedCornerShape(4.dp)
                                            )
                                    )
                                }
                                Text("${qVals[i]}", fontSize = 12.sp,
                                    color = if (qVals[i] == qVals.max())
                                        AppColors.green else Color(0xFF888888),
                                    modifier = Modifier.padding(top = 3.dp))
                            }
                        }
                    }
                }
            }

            // ── Win Screen ────────────────────────────────────────────────────
            if (gameWon) {
                val xpEarned = xpSystem.calcXP(
                    matches, difficulty.pairs, turns,
                    timeLeft, difficulty,
                    streakSystem.streakBonus())
                val score = highScoreSystem.calcScore(
                    matches, difficulty.pairs, turns, timeLeft, difficulty)

                LaunchedEffect(Unit) {
                    lastXPEarned = xpEarned
                    lastScore = score
                    xpSystem.addXP(xpEarned)
                    streakSystem.onWin()
                    highScoreSystem.addScore("YOU", score, difficulty)
                    ai.learn(difficulty, matches, difficulty.pairs, turns)
                    nextDifficulty = ai.chooseDifficulty()
                    emmaMessage = when {
                        highScoreSystem.showNewRecord -> EmmaMessages.record()
                        xpSystem.showLevelUp          -> EmmaMessages.levelUp()
                        streakSystem.currentStreak > 2-> EmmaMessages.streak()
                        else                          -> EmmaMessages.win(timeLeft)
                    }
                }

                Text("🎉 You Win!", fontSize = 32.sp, color = AppColors.green,
                    modifier = Modifier.padding(bottom = 4.dp))
                Text("${EmmaMessages.win(timeLeft)} · $turns turns",
                    fontSize = 13.sp, modifier = Modifier.padding(bottom = 2.dp))
                Text("+$xpEarned XP  🔥 Streak: ${streakSystem.currentStreak}",
                    fontSize = 14.sp, color = AppColors.purple,
                    modifier = Modifier.padding(bottom = 4.dp))
                Text("Score: $score pts", fontSize = 18.sp, color = AppColors.goldText,
                    modifier = Modifier.padding(bottom = 8.dp))

                // High score board
                HighScoreBoard(highScoreSystem.topScores())
                Spacer(Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .background(AppColors.green, RoundedCornerShape(16.dp))
                        .clickable { resetGame(nextDifficulty) }
                        .padding(horizontal = 32.dp, vertical = 14.dp)
                ) {
                    Text("Play Again (${nextDifficulty.label})",
                        color = Color.White, fontSize = 16.sp)
                }

                // ── Lose Screen ───────────────────────────────────────────────────
            } else if (gameLost) {
                val xpEarned = xpSystem.calcXP(
                    matches, difficulty.pairs, turns, 0,
                    difficulty, 0)

                LaunchedEffect(Unit) {
                    lastXPEarned = xpEarned
                    xpSystem.addXP(xpEarned)
                    streakSystem.onLose()
                    emmaMessage = "So close! Try again! 💪"
                }

                Text("⏰ Time's Up!", fontSize = 32.sp, color = AppColors.orange,
                    modifier = Modifier.padding(bottom = 4.dp))
                Text("Found $matches/${difficulty.pairs} pairs",
                    fontSize = 14.sp, modifier = Modifier.padding(bottom = 4.dp))
                Text("+$xpEarned XP  🔥 Streak lost",
                    fontSize = 14.sp, color = AppColors.purple,
                    modifier = Modifier.padding(bottom = 8.dp))

                HighScoreBoard(highScoreSystem.topScores())
                Spacer(Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .background(AppColors.orange, RoundedCornerShape(16.dp))
                        .clickable { resetGame(difficulty) }
                        .padding(horizontal = 32.dp, vertical = 14.dp)
                ) {
                    Text("Try Again!", color = Color.White, fontSize = 16.sp)
                }

                // ── Card Grid ─────────────────────────────────────────────────────
            } else {
                for (row in 0 until difficulty.rows) {
                    Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        for (col in 0 until difficulty.cols) {
                            val index = row * difficulty.cols + col
                            if (index >= cards.size) break
                            val card = cards[index]
                            val isFlipped = card.isFaceUp || card.isMatched

                            Box(
                                modifier = Modifier
                                    .size(68.dp)
                                    .background(
                                        color = when {
                                            card.isMatched -> AppColors.cardMatched
                                            card.isFaceUp  -> AppColors.cardFace
                                            else           -> AppColors.cardBack
                                        },
                                        shape = RoundedCornerShape(12.dp)
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
                                    fontSize = if (isFlipped) 26.sp else 22.sp,
                                    color = if (!isFlipped) AppColors.cardBackText
                                    else Color.Unspecified
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(7.dp))
                }
            }
        }
    }
}