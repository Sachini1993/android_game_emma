package com.sachini.emmamemorygame

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class LoginStep { ASK_NEW_OR_OLD, ENTER_NEW_NAME, PICK_FROM_LIST, ONBOARDING }

@Composable
fun PlayerLoginScreen(onPlayerSelected: (PlayerData) -> Unit) {
    var step by remember { mutableStateOf(LoginStep.ASK_NEW_OR_OLD) }
    var nameInput by remember { mutableStateOf("") }
    var pendingNewPlayer by remember { mutableStateOf<PlayerData?>(null) }
    val existingPlayers = PlayerRegistry.players

    Box(modifier = Modifier.fillMaxSize().background(AppColors.background),
        contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState())
        ) {
            Text("🧠", fontSize = 56.sp)
            Text("Pepper Memory Game", fontSize = 26.sp, color = AppColors.orange)
            Spacer(Modifier.height(4.dp))

            // Language selector
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf(Language.ENGLISH to "🇬🇧  English", Language.FINNISH to "🇫🇮  Suomi").forEach { (lang, label) ->
                    val selected = appLanguage == lang
                    Box(
                        modifier = Modifier
                            .background(if (selected) AppColors.purple else Color.White, RoundedCornerShape(10.dp))
                            .clickable { appLanguage = lang }
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(label, fontSize = 14.sp, color = if (selected) Color.White else AppColors.purple)
                    }
                }
            }
            Spacer(Modifier.height(4.dp))

            when (step) {

                // ── 1. New or returning ──────────────────────────────────────
                LoginStep.ASK_NEW_OR_OLD -> {
                    Text("Welcome! Are you a new player\nor have you played before?",
                        fontSize = 18.sp, color = Color(0xFF555555))
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Box(modifier = Modifier
                            .background(AppColors.purple, RoundedCornerShape(14.dp))
                            .clickable { step = LoginStep.ENTER_NEW_NAME }
                            .padding(horizontal = 28.dp, vertical = 14.dp)) {
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
                            .padding(horizontal = 28.dp, vertical = 14.dp)) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("👋", fontSize = 24.sp)
                                Spacer(Modifier.height(4.dp))
                                Text("I played before!", fontSize = 16.sp, color = Color.White)
                            }
                        }
                    }
                    if (existingPlayers.isEmpty())
                        Text("No players yet — be the first!",
                            fontSize = 12.sp, color = Color(0xFF888888))
                    if (PlayerRegistry.allScores().isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        Box(modifier = Modifier.widthIn(max = 400.dp)) { GlobalHighScoreBoard() }
                    }
                }

                // ── 2. Enter new name ────────────────────────────────────────
                LoginStep.ENTER_NEW_NAME -> {
                    Text("What is your name?", fontSize = 20.sp, color = AppColors.purple)
                    Text("Tap the letters below:", fontSize = 13.sp, color = Color(0xFF888888))
                    Box(modifier = Modifier.widthIn(min = 280.dp)
                        .background(Color.White, RoundedCornerShape(12.dp)).padding(16.dp),
                        contentAlignment = Alignment.Center) {
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
                                        contentAlignment = Alignment.Center) {
                                        Text(letter, fontSize = 15.sp, color = AppColors.purple)
                                    }
                                }
                            }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(modifier = Modifier
                            .background(Color(0xFFFFE0CC), RoundedCornerShape(10.dp))
                            .clickable { if (nameInput.isNotEmpty()) nameInput = nameInput.dropLast(1) }
                            .padding(horizontal = 18.dp, vertical = 10.dp)) {
                            Text("⌫ Delete", fontSize = 14.sp, color = AppColors.orange)
                        }
                        if (nameInput.isNotEmpty()) {
                            Box(modifier = Modifier
                                .background(Color(0xFFFFE0CC), RoundedCornerShape(10.dp))
                                .clickable { nameInput = "" }
                                .padding(horizontal = 18.dp, vertical = 10.dp)) {
                                Text("✕ Clear", fontSize = 14.sp, color = AppColors.orange)
                            }
                        }
                        if (nameInput.length >= 2) {
                            Box(modifier = Modifier
                                .background(AppColors.purple, RoundedCornerShape(10.dp))
                                .clickable {
                                    val player = PlayerRegistry.getOrCreate(nameInput)
                                    PlayerRegistry.currentPlayer = player
                                    PlayerRegistry.save()
                                    pendingNewPlayer = player
                                    // New user → show onboarding before game
                                    step = LoginStep.ONBOARDING
                                }
                                .padding(horizontal = 18.dp, vertical = 10.dp)) {
                                Text("▶ Continue as $nameInput", fontSize = 14.sp, color = Color.White)
                            }
                        }
                    }
                    Text("← Back", fontSize = 13.sp, color = Color(0xFF888888),
                        modifier = Modifier.clickable {
                            step = LoginStep.ASK_NEW_OR_OLD; nameInput = ""
                        })
                }

                // ── 3. Returning player list ─────────────────────────────────
                LoginStep.PICK_FROM_LIST -> {
                    Text("Welcome back! Who are you?", fontSize = 20.sp, color = AppColors.green)
                    Text("Tap your name to continue:", fontSize = 13.sp, color = Color(0xFF888888))
                    existingPlayers.forEach { player ->
                        val level = currentLevel(player.totalXP)
                        var confirmDelete by remember(player.name) { mutableStateOf(false) }
                        Box(modifier = Modifier.widthIn(min = 320.dp)
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .padding(16.dp)) {
                            Row(horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically) {
                                // Player info — tap to log in
                                Column(modifier = Modifier.weight(1f).clickable {
                                    PlayerRegistry.currentPlayer = player
                                    pepperSpeak(t("Welcome back ${player.name}! Good to see you again!",
                                        "Tervetuloa takaisin ${player.name}! Kiva nähdä sinut taas!"))
                                    onPlayerSelected(player)
                                }) {
                                    Text(player.name, fontSize = 18.sp, color = AppColors.purple)
                                    Text("Lv.${level.level} ${level.title}  •  ${player.gamesPlayed} games",
                                        fontSize = 11.sp, color = Color(0xFF888888))
                                    Text("💎 ${player.gemCount()}/${ALL_GEMS.size} gems",
                                        fontSize = 11.sp, color = AppColors.purple)
                                }
                                Column(horizontalAlignment = Alignment.End,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("Best: ${player.bestScore} pts",
                                        fontSize = 13.sp, color = AppColors.goldText)
                                    Text("🔥 ${player.bestStreak} streak",
                                        fontSize = 11.sp, color = AppColors.streakText)
                                    Spacer(Modifier.height(4.dp))
                                    if (confirmDelete) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Box(modifier = Modifier
                                                .background(Color(0xFFFFEBEE), RoundedCornerShape(8.dp))
                                                .clickable {
                                                    PlayerRegistry.deletePlayer(player)
                                                    confirmDelete = false
                                                }
                                                .padding(horizontal = 10.dp, vertical = 5.dp)) {
                                                Text("Yes, delete", fontSize = 11.sp, color = Color(0xFFD32F2F),
                                                    fontWeight = FontWeight.Bold)
                                            }
                                            Box(modifier = Modifier
                                                .background(Color(0xFFEEEEEE), RoundedCornerShape(8.dp))
                                                .clickable { confirmDelete = false }
                                                .padding(horizontal = 10.dp, vertical = 5.dp)) {
                                                Text("Cancel", fontSize = 11.sp, color = Color(0xFF555555))
                                            }
                                        }
                                    } else {
                                        Box(modifier = Modifier
                                            .background(Color(0xFFFFEBEE), RoundedCornerShape(8.dp))
                                            .clickable { confirmDelete = true }
                                            .padding(horizontal = 10.dp, vertical = 5.dp)) {
                                            Text("🗑 Remove", fontSize = 11.sp, color = Color(0xFFD32F2F))
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        Text("← Back", fontSize = 13.sp, color = Color(0xFF888888),
                            modifier = Modifier.clickable { step = LoginStep.ASK_NEW_OR_OLD })
                        Text("+ New player", fontSize = 13.sp, color = AppColors.purple,
                            modifier = Modifier.clickable {
                                step = LoginStep.ENTER_NEW_NAME; nameInput = ""
                            })
                    }
                }

                // ── 4. New-user onboarding + T&C ────────────────────────────
                LoginStep.ONBOARDING -> {
                    OnboardingScreen(
                        playerName = pendingNewPlayer?.name ?: nameInput,
                        onAccept = {
                            val player = pendingNewPlayer ?: return@OnboardingScreen
                            pepperSpeak(t("Hello ${player.name}! Welcome to Pepper Memory Game!",
                                "Hei ${player.name}! Tervetuloa Pepper Memory -peliin!"))
                            onPlayerSelected(player)
                        }
                    )
                }
            }
        }
    }
}

// ── Onboarding / Tutorial / Terms & Conditions ────────────────────────────────

@Composable
fun OnboardingScreen(playerName: String, onAccept: () -> Unit) {
    var page by remember { mutableStateOf(0) }
    var accepted by remember { mutableStateOf(false) }

    val pages = listOf(
        OnboardPage(
            emoji = "🧠",
            title = "Welcome, $playerName!",
            color = AppColors.purple,
            content = "Pepper Memory Game trains your brain with a fun card-matching challenge!\n\n" +
                "You will flip cards to find matching pairs — the faster you match, the more points you earn.\n\n" +
                "Pepper the robot will cheer you on and keep you company!"
        ),
        OnboardPage(
            emoji = "🃏",
            title = "How to Play",
            color = AppColors.orange,
            content = "① Tap a card to flip it face-up.\n\n" +
                "② Tap a second card — if they match, both stay open! ✅\n\n" +
                "③ If they don't match, both flip back. Try to remember!\n\n" +
                "④ Match ALL pairs before the 2-minute timer runs out.\n\n" +
                "💡 Tip: Go fast — you earn more XP with fewer turns!"
        ),
        OnboardPage(
            emoji = "⚡",
            title = "Difficulty Levels",
            color = Color(0xFF1D9E75),
            content = "🟢 Easy — 3 × 4 grid (6 pairs). Great for beginners.\n\n" +
                "🟡 Medium — 4 × 4 grid (8 pairs). A good challenge.\n\n" +
                "🔴 Hard — 4 × 5 grid (10 pairs). Maximum brain workout!\n\n" +
                "The AI watches your performance and suggests the best difficulty for you.\n\n" +
                "🏆 Play Hard mode consistently to unlock the highest gems and XP rewards!"
        ),
        OnboardPage(
            emoji = "⭐",
            title = "XP & Levels",
            color = AppColors.purple,
            content = "Every game earns you XP (experience points):\n\n" +
                "• Match all pairs → big XP bonus\n" +
                "• Fewer turns → XP multiplier\n" +
                "• Time remaining → speed bonus\n" +
                "• Win streaks → streak bonus 🔥\n\n" +
                "Level up from Memory Rookie → Card Flipper → Pattern Seeker → … → Memory Legend (Lv.10)!\n\n" +
                "Higher levels unlock harder challenges."
        ),
        OnboardPage(
            emoji = "💎",
            title = "Gems & Achievements",
            color = Color(0xFF0C447C),
            content = "Collect special gems by reaching milestones:\n\n" +
                "💎 Ice Diamond — Play your first game\n" +
                "🔮 Crystal Orb — Reach Level 3\n" +
                "🔥 Fire Gem — Win 3 games in a row\n" +
                "🏆 Gold Cup — Get your first high score\n" +
                "💫 Star Burst — Reach Level 5\n" +
                "⚡ Lightning — Win 5 in a row\n" +
                "🌟 Golden Star — Win with 60+ seconds left\n" +
                "🌈 Rainbow — Play all 5 themes\n" +
                "🧠 Brain Trainer — Play 10 games\n" +
                "👑 Royal Crown — Reach Level 10 Legend!\n\n" +
                "Tap any gem on the shelf to see its description."
        ),
        OnboardPage(
            emoji = "🤸",
            title = "Activity Breaks",
            color = Color(0xFF6B3D9A),
            content = "If Pepper notices you haven't tapped for a few seconds, she will suggest a fun movement break!\n\n" +
                "The break screen shows exercises like stretching, jumping jacks, or dancing.\n\n" +
                "Pepper will guide you through each step.\n\n" +
                "After the break, the game resumes automatically — OR tap ❌ Quit Game to exit.\n\n" +
                "Movement breaks help you focus and re-energise your brain! 🧠⚡"
        ),
        OnboardPage(
            emoji = "⚙️",
            title = "Settings & Sound",
            color = AppColors.orange,
            content = "Tap the ⚙️ gear icon in the top-left of the game screen to open Settings:\n\n" +
                "🎵 Music Volume — slide to make the background music louder or softer.\n\n" +
                "🔊 Voice Volume — controls how loud Pepper speaks.\n\n" +
                "❌ Quit Game — stops the music and exits to the home screen.\n\n" +
                "You can also tap 🔊/🔇 to quickly mute/unmute the music during the game."
        ),
        OnboardPage(
            emoji = "📋",
            title = "Terms & Conditions",
            color = Color(0xFF555555),
            content = "Please read and accept before playing:\n\n" +
                "• This app is designed for educational and recreational use.\n\n" +
                "• Player names and scores are saved locally on this device only.\n\n" +
                "• No personal data is sent to any server or third party.\n\n" +
                "• Physical activity suggestions during breaks are gentle and optional. Stop any exercise if you feel discomfort.\n\n" +
                "• Pepper robot interactions require a connected Pepper robot. The app works without one.\n\n" +
                "• By tapping \"I Accept & Start Playing\" you agree to these terms."
        )
    )

    val currentPage = pages[page]
    val isLast = page == pages.lastIndex

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.widthIn(max = 460.dp)
    ) {
        // Page indicator dots
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            pages.indices.forEach { i ->
                Box(
                    modifier = Modifier
                        .size(if (i == page) 10.dp else 7.dp)
                        .background(
                            if (i == page) currentPage.color else Color(0xFFCCCCCC),
                            RoundedCornerShape(50)
                        )
                )
            }
        }

        // Content card
        Box(
            modifier = Modifier.fillMaxWidth()
                .background(Color.White, RoundedCornerShape(20.dp))
                .border(2.dp, currentPage.color.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(currentPage.emoji, fontSize = 48.sp)
                Text(currentPage.title, fontSize = 20.sp, color = currentPage.color,
                    fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Text(currentPage.content, fontSize = 13.sp, color = Color(0xFF444444),
                    lineHeight = 20.sp)

                // T&C accept checkbox on last page
                if (isLast) {
                    Spacer(Modifier.height(4.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth()
                            .background(
                                if (accepted) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { accepted = !accepted }
                            .padding(14.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Text(if (accepted) "✅" else "☐", fontSize = 22.sp)
                            Text("I have read and accept the Terms & Conditions",
                                fontSize = 13.sp, color = Color(0xFF444444),
                                fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Navigation buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (page > 0) {
                Box(
                    modifier = Modifier.weight(1f)
                        .background(Color(0xFFEEEEEE), RoundedCornerShape(12.dp))
                        .clickable { page-- }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("← Back", fontSize = 15.sp, color = Color(0xFF555555))
                }
            }

            if (!isLast) {
                Box(
                    modifier = Modifier.weight(1f)
                        .background(currentPage.color, RoundedCornerShape(12.dp))
                        .clickable { page++ }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Next →", fontSize = 15.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            } else {
                Box(
                    modifier = Modifier.weight(1f)
                        .background(
                            if (accepted) AppColors.green else Color(0xFFCCCCCC),
                            RoundedCornerShape(12.dp)
                        )
                        .clickable(enabled = accepted) { onAccept() }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("✅  I Accept & Start Playing",
                        fontSize = 15.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        Text("${page + 1} / ${pages.size}", fontSize = 11.sp, color = Color(0xFF999999))
    }
}

private data class OnboardPage(
    val emoji: String,
    val title: String,
    val color: androidx.compose.ui.graphics.Color,
    val content: String
)
