package com.sachini.emmamemorygame

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.mutableStateListOf
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.roundToInt

// ── Gem catalog ───────────────────────────────────────────────────────────────

val ALL_GEMS = listOf(
    Gem("first_game",  "💎", "Ice Diamond",  "Play your first game",      androidx.compose.ui.graphics.Color(0xFF0C447C), androidx.compose.ui.graphics.Color(0xFFE6F1FB)),
    Gem("level_3",     "🔮", "Crystal Orb",  "Reach Lv.3",                androidx.compose.ui.graphics.Color(0xFF3C3489), androidx.compose.ui.graphics.Color(0xFFEEEDFE)),
    Gem("streak_3",    "🔥", "Fire Gem",      "Win 3 in a row",            androidx.compose.ui.graphics.Color(0xFF712B13), androidx.compose.ui.graphics.Color(0xFFFAECE7)),
    Gem("first_score", "🏆", "Gold Cup",      "Get your first high score", androidx.compose.ui.graphics.Color(0xFF633806), androidx.compose.ui.graphics.Color(0xFFFAEEDA)),
    Gem("level_5",     "💫", "Star Burst",    "Reach Lv.5",                androidx.compose.ui.graphics.Color(0xFF854F0B), androidx.compose.ui.graphics.Color(0xFFFAEEDA)),
    Gem("streak_5",    "⚡", "Lightning",     "Win 5 in a row",            androidx.compose.ui.graphics.Color(0xFF633806), androidx.compose.ui.graphics.Color(0xFFFAEEDA)),
    Gem("speed_win",   "🌟", "Golden Star",   "Win with 60s+ remaining",   androidx.compose.ui.graphics.Color(0xFF3B6D11), androidx.compose.ui.graphics.Color(0xFFEAF3DE)),
    Gem("all_themes",  "🌈", "Rainbow",       "Play all 5 themes",         androidx.compose.ui.graphics.Color(0xFF3C3489), androidx.compose.ui.graphics.Color(0xFFEEEDFE)),
    Gem("games_10",    "🧠", "Brain Trainer", "Play 10 games",             androidx.compose.ui.graphics.Color(0xFF085041), androidx.compose.ui.graphics.Color(0xFFE1F5EE)),
    Gem("level_10",    "👑", "Royal Crown",   "Reach Lv.10 Legend",        androidx.compose.ui.graphics.Color(0xFF712B13), androidx.compose.ui.graphics.Color(0xFFFBEAF0))
)

// ── Themes ────────────────────────────────────────────────────────────────────

val ALL_THEMES = listOf(
    GameTheme("Princesses","👑", listOf("🧝‍♀️","👸","🧜‍♀️","🧚‍♀️","🧙‍♀️","🦸‍♀️","🧛‍♀️","🧞‍♀️","🧟‍♀️")),
    GameTheme("Anime",    "⚔️", listOf("🥷","🧝‍♂️","🦸‍♂️","🧙‍♂️","🧜‍♂️","🧚‍♂️","🤴","👲","🧑‍🎤")),
    GameTheme("Animals",  "🐾", listOf("🐶","🐱","🐸","🦊","🐼","🦁","🐯","🐨","🐺")),
    GameTheme("Space",    "🚀", listOf("🚀","🌟","🪐","🌍","☄️","👽","🛸","🌙","⭐")),
    GameTheme("Suomi ABC","🇫🇮", listOf("Ä","Ö","Å","Y","J","K","L","M","N"))
)

// ── XP / Levels ───────────────────────────────────────────────────────────────

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

// ── Gem logic ─────────────────────────────────────────────────────────────────

fun checkNewGems(player: PlayerData, timeLeft: Int): List<Gem> {
    val newGems = mutableListOf<Gem>()
    val collected = player.gemsCollected.toMutableSet()
    fun check(id: String, condition: Boolean) {
        if (condition && !collected.contains(id)) {
            ALL_GEMS.find { it.id == id }?.let { newGems.add(it); collected.add(id) }
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

    fun deletePlayer(player: PlayerData) {
        players.remove(player)
        save()
    }

    fun allScores() = players.filter { it.bestScore > 0 }
        .map { Pair(it.name, it.bestScore) }
        .sortedByDescending { it.second }.take(5)
}

// ── Memory AI ─────────────────────────────────────────────────────────────────

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

// ── Boredom Detection ─────────────────────────────────────────────────────────
// Detects disengagement from 3 behavioral signals:
//   1. Inactivity — no card tap for 30+ seconds
//   2. Slow reactions — avg reaction time consistently above 7 seconds
//   3. Rapid random tapping — 4+ wrong picks within 6 seconds (mindless guessing)

class BoredomDetector {
    private var lastActionAt: Long = System.currentTimeMillis()
    private val recentReactionTimes = mutableListOf<Long>()
    private val recentWrongTimes = mutableListOf<Long>()

    fun recordAction() { lastActionAt = System.currentTimeMillis() }

    fun recordReactionTime(ms: Long) {
        recentReactionTimes.add(ms)
        if (recentReactionTimes.size > 5) recentReactionTimes.removeAt(0)
    }

    fun recordWrongPick() {
        val now = System.currentTimeMillis()
        recentWrongTimes.add(now)
        if (recentWrongTimes.size > 5) recentWrongTimes.removeAt(0)
    }

    fun boredomScore(): Float {
        var score = 0f
        // Signal 1: inactivity — ramps from 0 at 30s idle up to 3.0 at 60s idle
        val idleMs = System.currentTimeMillis() - lastActionAt
        if (idleMs > 3_000) score += ((idleMs - 3_000) / 1_000f).coerceAtMost(3.0f)
        // Signal 2: slow reactions — alone is enough to trigger boredom
        if (recentReactionTimes.size >= 3 && recentReactionTimes.average() > 7000) score += 3.0f
        // Signal 3: rapid wrong tapping — alone is enough to trigger boredom
        if (recentWrongTimes.size >= 4) {
            val span = recentWrongTimes.last() - recentWrongTimes.first()
            if (span < 6_000) score += 3.0f
        }
        return score
    }

    fun isBored() = boredomScore() >= 3.0f

    fun reset() {
        lastActionAt = System.currentTimeMillis()
        recentReactionTimes.clear()
        recentWrongTimes.clear()
    }
}

// ── Memory Predictor & Reaction Tracker ──────────────────────────────────────

class MemoryPredictor {
    fun retention(lastSeenAt: Long): Double {
        if (lastSeenAt == 0L) return 0.0
        return exp(-(System.currentTimeMillis() - lastSeenAt) / 1000.0 / 30.0).coerceIn(0.0, 1.0)
    }
    fun hint(r: Double) = when {
        r == 0.0 -> ""; r > 0.8 -> "Just saw it!"
        r > 0.5  -> "Think carefully..."; else -> "Seen a while ago"
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
        ms == 0L -> ""; ms < 800 -> "Quick!"
        ms < 2000 -> "Good pace"; else -> "Take your time"
    }
    fun confidence() = when {
        avgMs() == 0L  -> ""; avgMs() < 800  -> "High confidence"
        avgMs() < 2000 -> "Medium confidence"; else -> "Low confidence"
    }
    fun reset() { times.clear(); revealedAt = 0L }
}

// ── Card factory ──────────────────────────────────────────────────────────────

fun createCards(pairs: Int, theme: GameTheme): List<CardItem> {
    val s = theme.symbols.take(pairs)
    return (s + s).mapIndexed { i, sym -> CardItem(i, sym) }.shuffled()
}
