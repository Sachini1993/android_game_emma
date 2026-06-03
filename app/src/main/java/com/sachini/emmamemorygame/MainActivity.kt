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

data class CardItem(
    val id: Int,
    val symbol: String,
    val isFaceUp: Boolean = false,
    val isMatched: Boolean = false
)

fun createCards(): List<CardItem> {
    val symbols = listOf("🍎","🐶","🌟","🎵","🌈","🦋","🍕","🎯")
    return (symbols + symbols)
        .mapIndexed { index, symbol -> CardItem(index, symbol) }
        .shuffled()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MemoryGameScreen() }
    }
}

@Composable
fun MemoryGameScreen() {
    var cards by remember { mutableStateOf(createCards()) }
    var firstPick by remember { mutableStateOf<Int?>(null) }
    var secondPick by remember { mutableStateOf<Int?>(null) }
    var isChecking by remember { mutableStateOf(false) }
    var matches by remember { mutableStateOf(0) }
    var turns by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()

    // Check if game is won
    val gameWon = matches == 8

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Emma Memory Game", fontSize = 24.sp,
            modifier = Modifier.padding(bottom = 8.dp))

        // Score row
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            Text("Matches: $matches/8", fontSize = 14.sp, color = Color(0xFF4CAF50))
            Text("Turns: $turns", fontSize = 14.sp, color = Color(0xFF1565C0))
        }

        if (gameWon) {
            // Win screen
            Text("🎉 You Win!", fontSize = 36.sp,
                modifier = Modifier.padding(bottom = 16.dp))
            Text("Completed in $turns turns", fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 24.dp))
            Box(
                modifier = Modifier
                    .background(Color(0xFF1565C0), RoundedCornerShape(12.dp))
                    .clickable {
                        cards = createCards()
                        firstPick = null
                        secondPick = null
                        matches = 0
                        turns = 0
                    }
                    .padding(horizontal = 32.dp, vertical = 12.dp)
            ) {
                Text("Play Again", color = Color.White, fontSize = 18.sp)
            }
        } else {
            // Card grid
            for (row in 0..3) {
                Row {
                    for (col in 0..3) {
                        val index = row * 4 + col
                        val card = cards[index]
                        val isFlipped = card.isFaceUp || card.isMatched

                        Box(
                            modifier = Modifier
                                .padding(6.dp)
                                .size(70.dp)
                                .background(
                                    color = when {
                                        card.isMatched -> Color(0xFF4CAF50)
                                        card.isFaceUp -> Color(0xFFFF9800)
                                        else -> Color(0xFF1565C0)
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    // Ignore tap if: already checking, card already matched/faceup
                                    if (isChecking) return@clickable
                                    if (card.isMatched || card.isFaceUp) return@clickable

                                    // Flip this card face up
                                    cards = cards.toMutableList().also {
                                        it[index] = it[index].copy(isFaceUp = true)
                                    }

                                    if (firstPick == null) {
                                        // First card picked
                                        firstPick = index
                                    } else {
                                        // Second card picked
                                        secondPick = index
                                        turns++
                                        isChecking = true

                                        scope.launch {
                                            delay(800) // pause so player sees both cards

                                            val first = firstPick!!
                                            val second = secondPick!!

                                            if (cards[first].symbol == cards[second].symbol) {
                                                // Match! Mark both as matched
                                                cards = cards.toMutableList().also {
                                                    it[first] = it[first].copy(isMatched = true, isFaceUp = false)
                                                    it[second] = it[second].copy(isMatched = true, isFaceUp = false)
                                                }
                                                matches++
                                            } else {
                                                // No match — flip both back down
                                                cards = cards.toMutableList().also {
                                                    it[first] = it[first].copy(isFaceUp = false)
                                                    it[second] = it[second].copy(isFaceUp = false)
                                                }
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
                                fontSize = 28.sp
                            )
                        }
                    }
                }
            }
        }
    }
}