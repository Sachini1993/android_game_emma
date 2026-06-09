package com.sachini.emmamemorygame

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
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
    val background   = Color(0xFFFFF8F0)
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
    // Shield card colors — teal ocean
    val shieldHidden   = Color(0xFF0F6E56)
    val shieldBorder   = Color(0xFF085041)
    val shieldInner    = Color(0xFF1D9E75)
    val shieldText     = Color(0xFF9FE1CB)
    val shieldRevealed = Color(0xFFFAEEDA)
    val shieldRevBorder= Color(0xFFEF9F27)
    val shieldMatched  = Color(0xFFE1F5EE)
    val shieldMatBorder= Color(0xFF1D9E75)
    val shieldSymColor = Color(0xFF633806)
    val shieldMatColor = Color(0xFF0F6E56)
}

// ── Shield Card ───────────────────────────────────────────────────────────────

enum class CardState { HIDDEN, REVEALED, MATCHED }

@Composable
fun ShieldCard(
    state: CardState,
    symbol: String,
    isLetter: Boolean,
    cardSize: Dp
) {
    Box(
        modifier = Modifier.size(width = cardSize, height = cardSize * 1.1f),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(width = cardSize, height = cardSize * 1.1f)) {
            val w = size.width
            val h = size.height
            val sx = w / 80f
            val sy = h / 92f

            val shield = Path().apply {
                moveTo(40f*sx, 6f*sy)
                lineTo(72f*sx, 18f*sy)
                lineTo(72f*sx, 52f*sy)
                cubicTo(72f*sx,76f*sy, 60f*sx,84f*sy, 40f*sx,90f*sy)
                cubicTo(20f*sx,84f*sy, 8f*sx,76f*sy, 8f*sx,52f*sy)
                lineTo(8f*sx, 18f*sy)
                close()
            }
            val inner = Path().apply {
                moveTo(40f*sx, 14f*sy)
                lineTo(64f*sx, 24f*sy)
                lineTo(64f*sx, 50f*sy)
                cubicTo(64f*sx,70f*sy, 54f*sx,76f*sy, 40f*sx,82f*sy)
                cubicTo(26f*sx,76f*sy, 16f*sx,70f*sy, 16f*sx,50f*sy)
                lineTo(16f*sx, 24f*sy)
                close()
            }

            when (state) {
                CardState.HIDDEN -> {
                    drawPath(shield, AppColors.shieldHidden)
                    drawPath(shield, AppColors.shieldBorder,
                        style = Stroke(1.5f * sx))
                    drawPath(inner, AppColors.shieldInner,
                        style = Stroke(1f * sx), alpha = 0.5f)
                }
                CardState.REVEALED -> {
                    drawPath(shield, AppColors.shieldRevealed)
                    drawPath(shield, AppColors.shieldRevBorder,
                        style = Stroke(2f * sx))
                    drawPath(inner, AppColors.shieldRevBorder,
                        style = Stroke(1f * sx), alpha = 0.4f)
                }
                CardState.MATCHED -> {
                    drawPath(shield, AppColors.shieldMatched)
                    drawPath(shield, AppColors.shieldMatBorder,
                        style = Stroke(2.5f * sx))
                    drawPath(inner, AppColors.shieldMatBorder,
                        style = Stroke(1f * sx), alpha = 0.5f)
                }
            }
        }

        if (state == CardState.HIDDEN) {
            Text("?",
                fontSize = if (cardSize < 85.dp) 22.sp else 28.sp,
                color = AppColors.shieldText)
        } else {
            Text(
                symbol,
                fontSize = when {
                    isLetter -> if (cardSize < 85.dp) 28.sp else 38.sp
                    else     -> if (cardSize < 85.dp) 22.sp else 30.sp
                },
                color = if (state == CardState.MATCHED)
                    AppColors.shieldMatColor else AppColors.shieldSymColor
            )
        }
    }
}

// ── Gem System ────────────────────────────────────────────────────────────────

data class Gem(
    val id: String,
    val emoji: String,
    val name: String,
    val description: String,
    val color: Color,
    val bgColor: Color
)

val ALL_GEMS = listOf(
    Gem("first_game",  "💎", "Ice Diamond",   "Play your first game",      Color(0xFF0C447C), Color(0xFFE6F1FB)),
    Gem("level_3",     "🔮", "Crystal Orb",   "Reach Lv.3",                Color(0xFF3C3489), Color(0xFFEEEDFE)),
    Gem("streak_3",    "🔥", "Fire Gem",       "Win 3 in a row",            Color(0xFF712B13), Color(0xFFFAECE7)),
    Gem("first_score", "🏆", "Gold Cup",       "Get your first high score", Color(0xFF633806), Color(0xFFFAEEDA)),
    Gem("level_5",     "💫", "Star Burst",     "Reach Lv.5",                Color(0xFF854F0B), Color(0xFFFAEEDA)),
    Gem("streak_5",    "⚡", "Lightning",      "Win 5 in a row",            Color(0xFF633806), Color(0xFFFAEEDA)),
    Gem("speed_win",   "🌟", "Golden Star",    "Win with 60s+ remaining",   Color(0xFF3B6D11), Color(0xFFEAF3DE)),
    Gem("all_themes",  "🌈", "Rainbow",        "Play all 5 themes",         Color(0xFF3C3489), Color(0xFFEEEDFE)),
    Gem("games_10",    "🧠", "Brain Trainer",  "Play 10 games",             Color(0xFF085041), Color(0xFFE1F5EE)),
    Gem("level_10",    "👑", "Royal Crown",    "Reach Lv.10 Legend",        Color(0xFF712B13), Color(0xFFFBEAF0))
)

fun checkNewGems(player: PlayerData, timeLeft: Int): List<Gem> {
    val newGems = mutableListOf<Gem>()
    val collected = player.gemsCollected.toMutableSet()

    fun check(id: String, condition: Boolean) {
        if (condition && !collected.contains(id)) {
            ALL_GEMS.find { it.id == id }?.let {
                newGems.add(it); collected.add(id)
            }
        }
    }

    check("first_game",  player.gamesPlayed >= 1)
    check("level_3",     currentLevel(player.totalXP).level >= 3)
    check("level_5",     currentLevel(player.totalXP).level >= 5)
    check("level_10",    currentLevel(player.totalXP).level >= 10)
    check("streak_3",    player.currentStreak >= 3)
    check("streak_5",    player.currentStreak >= 5)
    check("first_score", player.bestScore > 0)
    check("speed_win",   timeLeft >= 60)
    check("games_10",    player.gamesPlayed >= 10)
    check("all_themes",  player.themesPlayed.containsAll(ALL_THEMES.map { it.name }.toSet()))

    player.gemsCollected = collected.toList()
    return newGems
}

// ── Player Data ───────────────────────────────────────────────────────────────

data class PlayerData(
    val name: String,
    var totalXP: Int = 0,
    var gamesPlayed: Int = 0,
    var bestScore: Int = 0,
    var currentStreak: Int = 0,
    var bestStreak: Int = 0,
    var avgReward: Double = 0.5,
    var aiGamesPlayed: Int = 0,
    var gemsCollected: List<String> = emptyList(),
    var themesPlayed: Set<String> = emptySet(),
    var qTableFlat: List<Double> = listOf(
        0.6,0.2,0.2, 0.2,0.6,0.2, 0.2,0.2,0.6)
) {
    fun qTable() = Array(3) { r ->
        doubleArrayOf(qTableFlat[r*3], qTableFlat[r*3+1], qTableFlat[r*3+2])
    }
    fun updateQTable(t: Array<DoubleArray>) { qTableFlat = t.flatMap { it.toList() } }
    fun gemCount() = gemsCollected.size
}

// ── Player Registry ───────────────────────────────────────────────────────────

object PlayerRegistry {
    val players = mutableStateListOf<PlayerData>()
    var currentPlayer: PlayerData? = null
    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        prefs = context.getSharedPreferences("emma_players", Context.MODE_PRIVATE)
        load()
    }

    private fun load() {
        val count = prefs?.getInt("player_count", 0) ?: 0
        players.clear()
        for (i in 0 until count) {
            val name = prefs?.getString("player_${i}_name", null) ?: continue
            val p = PlayerData(name)
            p.totalXP       = prefs?.getInt("player_${i}_xp", 0) ?: 0
            p.gamesPlayed   = prefs?.getInt("player_${i}_games", 0) ?: 0
            p.bestScore     = prefs?.getInt("player_${i}_best", 0) ?: 0
            p.currentStreak = prefs?.getInt("player_${i}_streak", 0) ?: 0
            p.bestStreak    = prefs?.getInt("player_${i}_beststreak", 0) ?: 0
            p.avgReward     = (prefs?.getFloat("player_${i}_reward", 0.5f) ?: 0.5f).toDouble()
            p.aiGamesPlayed = prefs?.getInt("player_${i}_aigames", 0) ?: 0
            p.gemsCollected = (prefs?.getString("player_${i}_gems", "") ?: "")
                .split(",").filter { it.isNotEmpty() }
            p.themesPlayed  = (prefs?.getString("player_${i}_themes", "") ?: "")
                .split(",").filter { it.isNotEmpty() }.toSet()
            val def = listOf(0.6f,0.2f,0.2f,0.2f,0.6f,0.2f,0.2f,0.2f,0.6f)
            p.qTableFlat = (0 until 9).map {
                (prefs?.getFloat("player_${i}_q_$it", def[it]) ?: def[it]).toDouble()
            }
            players.add(p)
        }
    }

    fun save() {
        val e = prefs?.edit() ?: return
        e.putInt("player_count", players.size)
        players.forEachIndexed { i, p ->
            e.putString("player_${i}_name", p.name)
            e.putInt("player_${i}_xp", p.totalXP)
            e.putInt("player_${i}_games", p.gamesPlayed)
            e.putInt("player_${i}_best", p.bestScore)
            e.putInt("player_${i}_streak", p.currentStreak)
            e.putInt("player_${i}_beststreak", p.bestStreak)
            e.putFloat("player_${i}_reward", p.avgReward.toFloat())
            e.putInt("player_${i}_aigames", p.aiGamesPlayed)
            e.putString("player_${i}_gems", p.gemsCollected.joinToString(","))
            e.putString("player_${i}_themes", p.themesPlayed.joinToString(","))
            p.qTableFlat.forEachIndexed { j, v -> e.putFloat("player_${i}_q_$j", v.toFloat()) }
        }
        e.apply()
    }

    fun getOrCreate(name: String) =
        players.find { it.name.equals(name, ignoreCase = true) }
            ?: PlayerData(name).also { players.add(it) }

    fun allScores() = players.filter { it.bestScore > 0 }
        .map { Pair(it.name, it.bestScore) }
        .sortedByDescending { it.second }.take(5)
}

// ── Difficulty ────────────────────────────────────────────────────────────────

enum class Difficulty(val cols: Int, val rows: Int, val pairs: Int, val label: String) {
    EASY(4, 2, 4, "Easy 4×2"),
    MEDIUM(4, 3, 6, "Medium 4×3"),
    HARD(6, 3, 9, "Hard 6×3")
}

data class CardItem(
    val id: Int,
    val symbol: String,
    val isFaceUp: Boolean = false,
    val isMatched: Boolean = false,
    val lastSeenAt: Long = 0L
)

// ── AI ────────────────────────────────────────────────────────────────────────

class MemoryAI(private val player: PlayerData) {
    private val qTable = player.qTable()

    fun playerSkill() = when {
        player.avgReward < 0.35 -> 0
        player.avgReward < 0.65 -> 1
        else -> 2
    }

    fun chooseDifficulty(): Difficulty {
        val epsilon = maxOf(0.1, 0.8 - player.aiGamesPlayed * 0.07)
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
        qTable[skill][action] += 0.3 * (reward - qTable[skill][action])
        player.avgReward = player.avgReward * 0.7 + reward * 0.3
        player.aiGamesPlayed++; player.gamesPlayed++
        player.updateQTable(qTable)
    }

    fun skillLabel() = listOf("Beginner","Intermediate","Advanced")[playerSkill()]
    fun chooseDifficultyLabel() = chooseDifficulty().label
    fun qValues(skill: Int) = qTable[skill].map { (it * 100).roundToInt() }
}

// ── XP ────────────────────────────────────────────────────────────────────────

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

fun currentLevel(xp: Int) = LEVELS.lastOrNull { xp >= it.xpRequired } ?: LEVELS.first()
fun nextLevel(xp: Int) = LEVELS.getOrNull(currentLevel(xp).level)
fun xpToNextLevel(xp: Int) = nextLevel(xp)?.xpRequired?.minus(xp) ?: 0
fun xpProgressPct(xp: Int): Float {
    val cur = currentLevel(xp); val next = nextLevel(xp) ?: return 1f
    return ((xp - cur.xpRequired).toFloat() /
            (next.xpRequired - cur.xpRequired)).coerceIn(0f, 1f)
}
fun calcXP(matches: Int, totalPairs: Int, turns: Int,
           timeLeft: Int, difficulty: Difficulty, streakBonus: Int): Int {
    val base = when (difficulty) {
        Difficulty.EASY -> 20; Difficulty.MEDIUM -> 40; Difficulty.HARD -> 80
    }
    return base + (if (matches == totalPairs) base else 0) +
            (timeLeft / 10) * 5 + (if (turns <= totalPairs + 2) 20 else 0) + streakBonus
}
fun calcScore(matches: Int, totalPairs: Int, turns: Int,
              timeLeft: Int, difficulty: Difficulty): Int {
    val base = when (difficulty) {
        Difficulty.EASY -> 1000; Difficulty.MEDIUM -> 2000; Difficulty.HARD -> 4000
    }
    return maxOf(0, base + timeLeft * 10 - (turns - totalPairs) * 50)
}
fun streakBonus(streak: Int) = when {
    streak >= 10 -> 100; streak >= 5 -> 50
    streak >= 3  -> 25;  streak >= 1 -> 10; else -> 0
}

// ── Themes ────────────────────────────────────────────────────────────────────

data class GameTheme(val name: String, val emoji: String, val symbols: List<String>)

val ALL_THEMES = listOf(
    GameTheme("Princesses","👑", listOf("🧝‍♀️","👸","🧜‍♀️","🧚‍♀️","🧙‍♀️","🦸‍♀️","🧛‍♀️","🧞‍♀️","🧟‍♀️")),
    GameTheme("Anime",    "⚔️", listOf("🥷","🧝‍♂️","🦸‍♂️","🧙‍♂️","🧜‍♂️","🧚‍♂️","🤴","👲","🧑‍🎤")),
    GameTheme("Animals",  "🐾", listOf("🐶","🐱","🐸","🦊","🐼","🦁","🐯","🐨","🐺")),
    GameTheme("Space",    "🚀", listOf("🚀","🌟","🪐","🌍","☄️","👽","🛸","🌙","⭐")),
    GameTheme("Suomi ABC","🇫🇮", listOf("Ä","Ö","Å","Y","W","X","Z","Q","Ü"))
)

// ── Messages ──────────────────────────────────────────────────────────────────

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
    "Your brain is warming up.\nKeep going! 🔥"
)

val LOSE_RIGHT_MESSAGES = listOf(
    "So close! The cards are\nwaiting for you. 💪",
    "Every champion fails first.\nTry again! 🏆",
    "The clock won't beat\nyou twice! ⏱️🔥"
)

// ── Predictors ────────────────────────────────────────────────────────────────

class MemoryPredictor {
    fun retention(lastSeenAt: Long): Double {
        if (lastSeenAt == 0L) return 0.0
        return exp(-(System.currentTimeMillis() - lastSeenAt) / 1000.0 / 30.0).coerceIn(0.0, 1.0)
    }
    fun hint(r: Double) = when {
        r == 0.0 -> ""; r > 0.8 -> "🟢 Just saw it!"
        r > 0.5  -> "🟡 Think carefully..."; else -> "🔴 Seen a while ago"
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
        avgMs() == 0L  -> ""; avgMs() < 800  -> "High confidence"
        avgMs() < 2000 -> "Medium confidence"; else -> "Low confidence"
    }
    fun reset() { times.clear(); revealedAt = 0L }
}

fun createCards(pairs: Int, theme: GameTheme): List<CardItem> {
    val s = theme.symbols.take(pairs)
    return (s + s).mapIndexed { i, sym -> CardItem(i, sym) }.shuffled()
}

// ── Gem Composables ───────────────────────────────────────────────────────────

@Composable
fun GemPopup(gem: Gem, onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(gem.bgColor, RoundedCornerShape(20.dp))
            .clickable { onDismiss() }
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("✨ New Gem Unlocked! ✨", fontSize = 13.sp, color = gem.color)
            Text(gem.emoji, fontSize = 48.sp)
            Text(gem.name, fontSize = 18.sp, color = gem.color)
            Text(gem.description, fontSize = 12.sp, color = gem.color)
            Text("tap to collect", fontSize = 10.sp, color = gem.color.copy(alpha = 0.6f))
        }
    }
}

@Composable
fun GemShelf(player: PlayerData) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("💎 Gem Collection", fontSize = 13.sp, color = AppColors.purple)
                Text("${player.gemCount()}/${ALL_GEMS.size}",
                    fontSize = 12.sp, color = AppColors.xpSub)
            }
            Spacer(Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth().height(6.dp)
                    .background(Color(0xFFE0D7FF), RoundedCornerShape(3.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(player.gemCount() / ALL_GEMS.size.toFloat())
                        .height(6.dp)
                        .background(AppColors.purple, RoundedCornerShape(3.dp))
                )
            }
            Spacer(Modifier.height(10.dp))
            ALL_GEMS.chunked(5).forEach { rowGems ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                ) {
                    rowGems.forEach { gem ->
                        val collected = player.gemsCollected.contains(gem.id)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(
                                        if (collected) gem.bgColor else Color(0xFFF0F0F0),
                                        RoundedCornerShape(12.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (collected) {
                                    Text(gem.emoji, fontSize = 24.sp)
                                } else {
                                    Text("🔒", fontSize = 18.sp)
                                }
                            }
                            Text(gem.name, fontSize = 8.sp,
                                color = if (collected) gem.color else Color(0xFF999999),
                                modifier = Modifier.padding(top = 2.dp))
                        }
                    }
                    repeat(5 - rowGems.size) { Spacer(modifier = Modifier.weight(1f)) }
                }
            }
        }
    }
}

// ── Shared Composables ────────────────────────────────────────────────────────

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
fun GlobalHighScoreBoard() {
    val scores = PlayerRegistry.allScores()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppColors.goldBg, RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Column {
            Text("🏆 All-Time Champions", fontSize = 14.sp, color = AppColors.goldText)
            Spacer(Modifier.height(6.dp))
            if (scores.isEmpty()) {
                Text("No scores yet — be the first champion!",
                    fontSize = 12.sp, color = Color(0xFF888888))
            } else {
                scores.forEachIndexed { i, (name, score) ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${listOf("🥇","🥈","🥉","4️⃣","5️⃣")[i]} $name",
                            fontSize = 13.sp,
                            color = if (i == 0) AppColors.goldText else Color(0xFF555555))
                        Text("$score pts", fontSize = 13.sp,
                            color = if (i == 0) AppColors.goldText else Color(0xFF555555))
                    }
                }
            }
        }
    }
}

@Composable
fun DifficultySelector(selected: Difficulty, onSelect: (Difficulty) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxWidth()) {
        Difficulty.entries.forEach { d ->
            val isSelected = d == selected
            Box(
                modifier = Modifier.weight(1f)
                    .background(if (isSelected) AppColors.purple else Color.White,
                        RoundedCornerShape(10.dp))
                    .clickable { onSelect(d) }.padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) { Text(d.label, fontSize = 11.sp,
                color = if (isSelected) Color.White else AppColors.purple) }
        }
    }
}

@Composable
fun ThemeSelector(selected: GameTheme, onSelect: (GameTheme) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(5.dp),
        modifier = Modifier.fillMaxWidth()) {
        ALL_THEMES.forEach { t ->
            val isSelected = t.name == selected.name
            Box(
                modifier = Modifier.weight(1f)
                    .background(if (isSelected) AppColors.orange else Color.White,
                        RoundedCornerShape(8.dp))
                    .clickable { onSelect(t) }.padding(vertical = 6.dp),
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

// ── Login Screen ──────────────────────────────────────────────────────────────

enum class LoginStep { ASK_NEW_OR_OLD, ENTER_NEW_NAME, PICK_FROM_LIST }

@Composable
fun PlayerLoginScreen(onPlayerSelected: (PlayerData) -> Unit) {
    var step by remember { mutableStateOf(LoginStep.ASK_NEW_OR_OLD) }
    var nameInput by remember { mutableStateOf("") }
    val existingPlayers = PlayerRegistry.players

    Box(
        modifier = Modifier.fillMaxSize().background(AppColors.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState())
        ) {
            Text("🧠", fontSize = 56.sp)
            Text("Emma Memory Game", fontSize = 26.sp, color = AppColors.orange)
            Spacer(Modifier.height(4.dp))

            when (step) {
                LoginStep.ASK_NEW_OR_OLD -> {
                    Text("Welcome! Are you a new player\nor have you played before?",
                        fontSize = 18.sp, color = Color(0xFF555555))
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Box(modifier = Modifier
                            .background(AppColors.purple, RoundedCornerShape(14.dp))
                            .clickable { step = LoginStep.ENTER_NEW_NAME }
                            .padding(horizontal = 28.dp, vertical = 14.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("✨", fontSize = 24.sp)
                                Spacer(Modifier.height(4.dp))
                                Text("I am new!", fontSize = 16.sp, color = Color.White)
                            }
                        }
                        Box(modifier = Modifier
                            .background(
                                if (existingPlayers.isEmpty()) Color(0xFFCCCCCC)
                                else AppColors.green, RoundedCornerShape(14.dp))
                            .clickable { if (existingPlayers.isNotEmpty()) step = LoginStep.PICK_FROM_LIST }
                            .padding(horizontal = 28.dp, vertical = 14.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("👋", fontSize = 24.sp)
                                Spacer(Modifier.height(4.dp))
                                Text("I played before!", fontSize = 16.sp, color = Color.White)
                            }
                        }
                    }
                    if (existingPlayers.isEmpty())
                        Text("No players yet — be the first!", fontSize = 12.sp, color = Color(0xFF888888))
                    if (PlayerRegistry.allScores().isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        Box(modifier = Modifier.widthIn(max = 400.dp)) { GlobalHighScoreBoard() }
                    }
                }

                LoginStep.ENTER_NEW_NAME -> {
                    Text("What is your name?", fontSize = 20.sp, color = AppColors.purple)
                    Text("Tap the letters below:", fontSize = 13.sp, color = Color(0xFF888888))
                    Box(modifier = Modifier.widthIn(min = 280.dp)
                        .background(Color.White, RoundedCornerShape(12.dp)).padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            if (nameInput.isEmpty()) "Tap letters below..." else nameInput,
                            fontSize = 22.sp,
                            color = if (nameInput.isEmpty()) Color(0xFFAAAAAA) else AppColors.xpText)
                    }
                    val letters = listOf(
                        listOf("A","B","C","D","E","F","G"),
                        listOf("H","I","J","K","L","M","N"),
                        listOf("O","P","Q","R","S","T","U"),
                        listOf("V","W","X","Y","Z","Ä","Ö")
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        letters.forEach { row ->
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                row.forEach { letter ->
                                    Box(modifier = Modifier.size(40.dp)
                                        .background(Color.White, RoundedCornerShape(8.dp))
                                        .clickable { if (nameInput.length < 12) nameInput += letter },
                                        contentAlignment = Alignment.Center
                                    ) { Text(letter, fontSize = 15.sp, color = AppColors.purple) }
                                }
                            }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(modifier = Modifier
                            .background(Color(0xFFFFE0CC), RoundedCornerShape(10.dp))
                            .clickable { if (nameInput.isNotEmpty()) nameInput = nameInput.dropLast(1) }
                            .padding(horizontal = 18.dp, vertical = 10.dp)
                        ) { Text("⌫ Delete", fontSize = 14.sp, color = AppColors.orange) }
                        if (nameInput.isNotEmpty()) {
                            Box(modifier = Modifier
                                .background(Color(0xFFFFE0CC), RoundedCornerShape(10.dp))
                                .clickable { nameInput = "" }
                                .padding(horizontal = 18.dp, vertical = 10.dp)
                            ) { Text("✕ Clear", fontSize = 14.sp, color = AppColors.orange) }
                        }
                        if (nameInput.length >= 2) {
                            Box(modifier = Modifier
                                .background(AppColors.purple, RoundedCornerShape(10.dp))
                                .clickable {
                                    val player = PlayerRegistry.getOrCreate(nameInput)
                                    PlayerRegistry.currentPlayer = player
                                    PlayerRegistry.save()
                                    onPlayerSelected(player)
                                }
                                .padding(horizontal = 18.dp, vertical = 10.dp)
                            ) { Text("▶ Start as $nameInput", fontSize = 14.sp, color = Color.White) }
                        }
                    }
                    Text("← Back", fontSize = 13.sp, color = Color(0xFF888888),
                        modifier = Modifier.clickable { step = LoginStep.ASK_NEW_OR_OLD; nameInput = "" })
                }

                LoginStep.PICK_FROM_LIST -> {
                    Text("Welcome back! Who are you?", fontSize = 20.sp, color = AppColors.green)
                    Text("Tap your name to continue:", fontSize = 13.sp, color = Color(0xFF888888))
                    existingPlayers.forEach { player ->
                        val level = currentLevel(player.totalXP)
                        Box(modifier = Modifier.widthIn(min = 320.dp)
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .clickable { PlayerRegistry.currentPlayer = player; onPlayerSelected(player) }
                            .padding(16.dp)
                        ) {
                            Row(horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(player.name, fontSize = 18.sp, color = AppColors.purple)
                                    Text("Lv.${level.level} ${level.title}  •  ${player.gamesPlayed} games",
                                        fontSize = 11.sp, color = Color(0xFF888888))
                                    Text("💎 ${player.gemCount()}/${ALL_GEMS.size} gems",
                                        fontSize = 11.sp, color = AppColors.purple)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Best: ${player.bestScore} pts",
                                        fontSize = 13.sp, color = AppColors.goldText)
                                    Text("🔥 ${player.bestStreak} streak",
                                        fontSize = 11.sp, color = AppColors.streakText)
                                }
                            }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        Text("← Back", fontSize = 13.sp, color = Color(0xFF888888),
                            modifier = Modifier.clickable { step = LoginStep.ASK_NEW_OR_OLD })
                        Text("+ New player", fontSize = 13.sp, color = AppColors.purple,
                            modifier = Modifier.clickable { step = LoginStep.ENTER_NEW_NAME; nameInput = "" })
                    }
                }
            }
        }
    }
}

// ── Main Activity ─────────────────────────────────────────────────────────────

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PlayerRegistry.init(this)
        setContent { AppRoot() }
    }
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

// ── Game Screen ───────────────────────────────────────────────────────────────

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
    var playAgainMsg by remember { mutableStateOf(PLAY_AGAIN_MESSAGES.random()) }
    var rightMsg by remember { mutableStateOf(WIN_RIGHT_MESSAGES.random()) }
    var newGemQueue by remember { mutableStateOf<List<Gem>>(emptyList()) }
    var displayXP by remember { mutableStateOf(player.totalXP) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(timerRunning) {
        while (timerRunning && timeLeft > 0) { delay(1000); timeLeft-- }
    }

    val gameWon  = gameStarted && matches == difficulty.pairs
    val gameLost = gameStarted && timeLeft == 0 && !gameWon
    val skill    = ai.playerSkill()
    val qVals    = ai.qValues(skill)
    val curLevel = currentLevel(displayXP)
    val nxtLevel = nextLevel(displayXP)

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
        tracker.reset()
        playAgainMsg = PLAY_AGAIN_MESSAGES.random()
        player.themesPlayed = player.themesPlayed + t.name
    }

    Box(modifier = Modifier.fillMaxSize().background(AppColors.background)) {
        Row(modifier = Modifier.fillMaxSize().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {

            // ── LEFT PANEL ────────────────────────────────────────────────────
            Column(
                modifier = Modifier.weight(0.42f).fillMaxHeight()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Player header
                Box(modifier = Modifier.fillMaxWidth()
                    .background(AppColors.purple, RoundedCornerShape(12.dp)).padding(10.dp)) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("👤 ${player.name}", fontSize = 14.sp, color = Color.White)
                            Text("Lv.${curLevel.level} ${curLevel.title}",
                                fontSize = 11.sp, color = Color(0xFFCECBF6))
                            Text("💎 ${player.gemCount()}/${ALL_GEMS.size} gems",
                                fontSize = 10.sp, color = Color(0xFFAFA9EC))
                        }
                        Text("Change\nplayer", fontSize = 10.sp, color = Color(0xFFAFA9EC),
                            modifier = Modifier.clickable { onChangePlayer() })
                    }
                }

                // XP Bar
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
                            Text("${xpToNextLevel(displayXP)} XP to ${nxtLevel.title}",
                                fontSize = 10.sp, color = AppColors.xpSub,
                                modifier = Modifier.padding(top = 2.dp))
                    }
                }

                // Level up
                if (showLevelUp) {
                    Box(modifier = Modifier.fillMaxWidth()
                        .background(AppColors.purple, RoundedCornerShape(12.dp)).padding(10.dp)
                        .clickable { showLevelUp = false }) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()) {
                            Text("🎊 LEVEL UP!", fontSize = 16.sp, color = Color.White)
                            Text(levelUpTitle, fontSize = 12.sp, color = Color(0xFFCECBF6))
                            Text("tap to dismiss", fontSize = 10.sp, color = Color(0xFFAFA9EC))
                        }
                    }
                }

                // Gem popup
                if (newGemQueue.isNotEmpty()) {
                    GemPopup(gem = newGemQueue.first()) { newGemQueue = newGemQueue.drop(1) }
                }

                // Streak
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
                                player.currentStreak >= 10 -> "UNSTOPPABLE!"
                                player.currentStreak >= 5  -> "On a hot streak!"
                                player.currentStreak >= 3  -> "Keep it up!"
                                else -> "Win streak!"
                            }
                            Text("$emoji $msg", fontSize = 12.sp, color = AppColors.streakText)
                            Text("🔥 ${player.currentStreak}  Best: ${player.bestStreak}",
                                fontSize = 11.sp, color = Color(0xFF888888))
                        }
                    }
                }

                Text("Emma Memory", fontSize = 20.sp, color = AppColors.orange)
                Text("${theme.emoji} ${theme.name} · ${difficulty.label}",
                    fontSize = 11.sp, color = AppColors.xpSub)

                if (firstGame) {
                    Text("Select Difficulty:", fontSize = 11.sp, color = Color(0xFF888888))
                    DifficultySelector(selected = difficulty) { d ->
                        difficulty = d; cards = createCards(d.pairs, theme)
                    }
                    Spacer(Modifier.height(2.dp))
                    Text("Select Theme:", fontSize = 11.sp, color = Color(0xFF888888))
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
                                rightMsg = if (gameWon) WIN_RIGHT_MESSAGES.random()
                                else LOSE_RIGHT_MESSAGES.random()
                                startGame(aiDiff, newTheme)
                            }
                        }
                        .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(if (firstGame) "▶  Start Game" else playAgainMsg,
                            fontSize = 14.sp, color = Color.White)
                    }
                }

                if (gameStarted) {
                    Row(horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()) {
                        StatChip("$matches/${difficulty.pairs}", "Matches", AppColors.green)
                        StatChip("$turns", "Turns", AppColors.purple)
                        StatChip(ai.skillLabel(), "Skill", AppColors.orange)
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
                            Text("🤖 Emma: $emmaMessage", fontSize = 12.sp, color = AppColors.emmaMsgText)
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

                    // Skill Meter
                    Box(modifier = Modifier.fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(12.dp)).padding(10.dp)) {
                        Column {
                            Text("📊 Skill Meter", fontSize = 12.sp, color = Color(0xFF555555))
                            Spacer(Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly) {
                                listOf("Easy","Medium","Hard").forEachIndexed { i, label ->
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
                                                    else AppColors.skillBarLow,
                                                    RoundedCornerShape(4.dp)))
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
                                levelUpTitle = currentLevel(player.totalXP).title; showLevelUp = true
                            }
                            player.currentStreak++
                            if (player.currentStreak > player.bestStreak)
                                player.bestStreak = player.currentStreak
                            if (score > player.bestScore) player.bestScore = score
                            ai.learn(difficulty, matches, difficulty.pairs, turns)
                            val newGems = checkNewGems(player, timeLeft)
                            if (newGems.isNotEmpty()) newGemQueue = newGems
                            emmaMessage = when {
                                newGems.isNotEmpty() -> "New gem unlocked! 💎"
                                score >= player.bestScore -> EmmaMessages.record()
                                showLevelUp -> EmmaMessages.levelUp()
                                player.currentStreak > 2 -> EmmaMessages.streak()
                                else -> EmmaMessages.win(timeLeft)
                            }
                            PlayerRegistry.save()
                        }
                        Text("🎉 You Win, ${player.name}!", fontSize = 22.sp, color = AppColors.green)
                        Text("+$xp XP  🔥 Streak: ${player.currentStreak}",
                            fontSize = 13.sp, color = AppColors.purple)
                        Text("Score: $score pts", fontSize = 16.sp, color = AppColors.goldText)
                        Spacer(Modifier.height(6.dp))
                        GlobalHighScoreBoard()

                    } else if (gameLost) {
                        val xp = calcXP(matches, difficulty.pairs, turns, 0, difficulty, 0)
                        LaunchedEffect(Unit) {
                            lastXPEarned = xp; player.totalXP += xp; displayXP = player.totalXP
                            player.currentStreak = 0
                            val newGems = checkNewGems(player, 0)
                            if (newGems.isNotEmpty()) newGemQueue = newGems
                            emmaMessage = "So close! Try again! 💪"
                            PlayerRegistry.save()
                        }
                        Text("⏰ Time's Up, ${player.name}!", fontSize = 22.sp, color = AppColors.orange)
                        Text("Found $matches/${difficulty.pairs} pairs", fontSize = 13.sp)
                        Text("+$xp XP  🔥 Streak lost", fontSize = 13.sp, color = AppColors.purple)
                        Spacer(Modifier.height(6.dp))
                        GlobalHighScoreBoard()
                    }
                }
            }

            // ── RIGHT PANEL ───────────────────────────────────────────────────
            Column(
                modifier = Modifier.weight(0.58f).fillMaxHeight(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!gameStarted) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                            .verticalScroll(rememberScrollState()).padding(vertical = 12.dp)
                    ) {
                        Text("👋", fontSize = 56.sp)
                        Text("Hello, ${player.name}!", fontSize = 24.sp, color = AppColors.orange)
                        if (player.gamesPlayed > 0) {
                            Text("Games: ${player.gamesPlayed}  •  Best: ${player.bestScore} pts",
                                fontSize = 14.sp, color = Color(0xFF555555))
                            Text("Best streak: 🔥 ${player.bestStreak}",
                                fontSize = 14.sp, color = AppColors.streakText)
                        } else {
                            Text("Select difficulty and theme\non the left, then press Start!",
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
                        Text(if (gameWon) "You Win!" else "Time's Up!",
                            fontSize = 30.sp,
                            color = if (gameWon) AppColors.green else AppColors.orange)
                        if (gameWon) {
                            Text("Score: $lastScore pts", fontSize = 20.sp, color = AppColors.goldText)
                            if (lastScore >= player.bestScore)
                                Text("🏆 New personal best!", fontSize = 14.sp, color = AppColors.goldText)
                        } else {
                            Text("Found $matches/${difficulty.pairs} pairs",
                                fontSize = 16.sp, color = Color(0xFF555555))
                        }
                        Box(modifier = Modifier
                            .background(
                                if (gameWon) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                                RoundedCornerShape(16.dp))
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(rightMsg, fontSize = 15.sp,
                                color = if (gameWon) AppColors.green else AppColors.orange)
                        }
                        Spacer(Modifier.height(4.dp))
                        GemShelf(player)
                    }

                } else {
                    // ── Card Grid with Shield Shape ───────────────────────────
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
                                val isLetter = card.symbol.length == 1 &&
                                        card.symbol[0].isLetter()

                                Box(
                                    modifier = Modifier
                                        .size(cardSize)
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
                                                    predictor.retention(cards[firstPick!!].lastSeenAt))
                                                secondPick = index
                                                turns++
                                                isChecking = true

                                                scope.launch {
                                                    delay(800)
                                                    val first = firstPick!!
                                                    val second = secondPick!!
                                                    if (cards[first].symbol == cards[second].symbol) {
                                                        cards = cards.toMutableList().also {
                                                            it[first] = it[first].copy(isMatched = true, isFaceUp = false)
                                                            it[second] = it[second].copy(isMatched = true, isFaceUp = false)
                                                        }
                                                        matches++
                                                        emmaMessage = if (showLevelUp) EmmaMessages.levelUp()
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
    }
}