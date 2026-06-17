package com.sachini.emmamemorygame

import androidx.compose.ui.graphics.Color

enum class Language { ENGLISH, FINNISH }

enum class CardState { HIDDEN, REVEALED, MATCHED }

data class Gem(
    val id: String, val emoji: String, val name: String,
    val description: String, val color: Color, val bgColor: Color
)

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
    var qTableFlat: List<Double> = listOf(0.6,0.2,0.2,0.2,0.6,0.2,0.2,0.2,0.6)
) {
    fun qTable() = Array(3) { r ->
        doubleArrayOf(qTableFlat[r*3], qTableFlat[r*3+1], qTableFlat[r*3+2])
    }
    fun updateQTable(t: Array<DoubleArray>) { qTableFlat = t.flatMap { it.toList() } }
    fun gemCount() = gemsCollected.size
}

data class CardItem(
    val id: Int, val symbol: String,
    val isFaceUp: Boolean = false,
    val isMatched: Boolean = false,
    val lastSeenAt: Long = 0L
)

data class GameTheme(val name: String, val emoji: String, val symbols: List<String>)

data class PlayerLevel(val level: Int, val title: String, val xpRequired: Int)

data class BreakStep(
    val instruction: String, val speakText: String, val speakTextFi: String,
    val emoji: String, val durationSeconds: Int
)

data class BreakActivity(
    val id: String, val title: String, val emoji: String,
    val color: Color, val bgColor: Color, val steps: List<BreakStep>
)

enum class Difficulty(val cols: Int, val rows: Int, val pairs: Int, val label: String) {
    EASY(4, 2, 4, "Easy 4×2"),
    MEDIUM(4, 3, 6, "Medium 4×3"),
    HARD(6, 3, 9, "Hard 6×3")
}
