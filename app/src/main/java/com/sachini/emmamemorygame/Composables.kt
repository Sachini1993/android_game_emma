package com.sachini.emmamemorygame

import android.app.Activity
import android.media.AudioManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import kotlinx.coroutines.delay

// ── Shield Card ───────────────────────────────────────────────────────────────

@Composable
fun ShieldCard(state: CardState, symbol: String, isLetter: Boolean, cardSize: Dp) {
    Box(
        modifier = Modifier.size(width = cardSize, height = cardSize * 1.1f),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(width = cardSize, height = cardSize * 1.1f)) {
            val w = size.width; val h = size.height
            val sx = w / 80f; val sy = h / 92f

            val shield = Path().apply {
                moveTo(40f*sx, 6f*sy)
                lineTo(72f*sx, 18f*sy)
                lineTo(72f*sx, 52f*sy)
                cubicTo(72f*sx,76f*sy, 60f*sx,84f*sy, 40f*sx,90f*sy)
                cubicTo(20f*sx,84f*sy, 8f*sx,76f*sy, 8f*sx,52f*sy)
                lineTo(8f*sx, 18f*sy); close()
            }
            val inner = Path().apply {
                moveTo(40f*sx, 14f*sy)
                lineTo(64f*sx, 24f*sy)
                lineTo(64f*sx, 50f*sy)
                cubicTo(64f*sx,70f*sy, 54f*sx,76f*sy, 40f*sx,82f*sy)
                cubicTo(26f*sx,76f*sy, 16f*sx,70f*sy, 16f*sx,50f*sy)
                lineTo(16f*sx, 24f*sy); close()
            }

            when (state) {
                CardState.HIDDEN -> {
                    drawPath(shield, AppColors.shieldHidden)
                    drawPath(shield, AppColors.shieldBorder, style = Stroke(1.5f*sx))
                    drawPath(inner, AppColors.shieldInner, style = Stroke(1f*sx), alpha = 0.5f)
                }
                CardState.REVEALED -> {
                    drawPath(shield, AppColors.shieldRevealed)
                    drawPath(shield, AppColors.shieldRevBorder, style = Stroke(2f*sx))
                    drawPath(inner, AppColors.shieldRevBorder, style = Stroke(1f*sx), alpha = 0.4f)
                }
                CardState.MATCHED -> {
                    drawPath(shield, AppColors.shieldMatched)
                    drawPath(shield, AppColors.shieldMatBorder, style = Stroke(2.5f*sx))
                    drawPath(inner, AppColors.shieldMatBorder, style = Stroke(1f*sx), alpha = 0.5f)
                }
            }
        }
        if (state == CardState.HIDDEN) {
            Text("?", fontSize = if (cardSize < 85.dp) 22.sp else 28.sp,
                color = AppColors.shieldText)
        } else {
            Text(symbol,
                fontSize = when {
                    isLetter -> if (cardSize < 85.dp) 28.sp else 38.sp
                    else     -> if (cardSize < 85.dp) 22.sp else 30.sp
                },
                color = if (state == CardState.MATCHED) AppColors.shieldMatColor
                else AppColors.shieldSymColor)
        }
    }
}

// ── Gem Composables ───────────────────────────────────────────────────────────

fun gemAchievementMessage(gemId: String): String = when (gemId) {
    "first_game"  -> "You took your very first step into memory training!"
    "level_3"     -> "Your pattern recognition skills are growing fast!"
    "streak_3"    -> "Three wins in a row — real focus and concentration!"
    "first_score" -> "You set your first benchmark — now try to beat it!"
    "level_5"     -> "Halfway to legend — your focus is truly masterful!"
    "streak_5"    -> "Five straight wins — you are completely unstoppable!"
    "speed_win"   -> "Your quick thinking and memory are exceptional!"
    "all_themes"  -> "A true explorer — you mastered every theme!"
    "games_10"    -> "Ten games deep — your brain is getting stronger every round!"
    "level_10"    -> "The ultimate achievement — you are a Memory Legend!"
    else          -> "An amazing achievement unlocked!"
}

@Composable
fun GemTooltip(gem: Gem, onDismiss: () -> Unit) {
    val offsetPx = with(LocalDensity.current) { 148.dp.roundToPx() }
    Popup(
        alignment = Alignment.TopCenter,
        offset = IntOffset(0, -offsetPx),
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true)
    ) {
        Box(
            modifier = Modifier
                .width(170.dp)
                .background(gem.bgColor, RoundedCornerShape(12.dp))
                .border(1.dp, gem.color.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                .clickable { onDismiss() }
                .padding(10.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(gem.emoji, fontSize = 28.sp)
                Text(gem.name, fontSize = 13.sp, color = gem.color, fontWeight = FontWeight.Bold)
                Text(
                    gemAchievementMessage(gem.id),
                    fontSize = 10.sp, color = gem.color,
                    textAlign = TextAlign.Center
                )
                Text("tap to close", fontSize = 9.sp, color = gem.color.copy(alpha = 0.5f))
            }
        }
    }
}

@Composable
fun GemPopup(gem: Gem, onDismiss: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth()
            .background(gem.bgColor, RoundedCornerShape(20.dp))
            .clickable { onDismiss() }.padding(16.dp),
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
    var tooltipGemId by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxWidth()
        .background(Color.White, RoundedCornerShape(14.dp)).padding(12.dp)) {
        Column {
            Row(horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()) {
                Text("💎 Gem Collection", fontSize = 13.sp, color = AppColors.purple)
                Text("${player.gemCount()}/${ALL_GEMS.size}",
                    fontSize = 12.sp, color = AppColors.xpSub)
            }
            Spacer(Modifier.height(6.dp))
            Box(modifier = Modifier.fillMaxWidth().height(6.dp)
                .background(Color(0xFFE0D7FF), RoundedCornerShape(3.dp))) {
                Box(modifier = Modifier
                    .fillMaxWidth(player.gemCount() / ALL_GEMS.size.toFloat()).height(6.dp)
                    .background(AppColors.purple, RoundedCornerShape(3.dp)))
            }
            Spacer(Modifier.height(10.dp))
            ALL_GEMS.chunked(5).forEach { rowGems ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)) {
                    rowGems.forEach { gem ->
                        val collected = player.gemsCollected.contains(gem.id)
                        val isSelected = tooltipGemId == gem.id
                        Column(horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier.size(48.dp)
                                    .background(
                                        if (collected) gem.bgColor else Color(0xFFF0F0F0),
                                        RoundedCornerShape(12.dp))
                                    .then(
                                        if (collected) Modifier.border(
                                            if (isSelected) 2.dp else 0.dp,
                                            gem.color.copy(alpha = 0.6f),
                                            RoundedCornerShape(12.dp)
                                        ) else Modifier
                                    )
                                    .clickable(enabled = collected) {
                                        tooltipGemId = if (isSelected) null else gem.id
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (collected) Text(gem.emoji, fontSize = 24.sp)
                                else Text("🔒", fontSize = 18.sp)

                                if (isSelected) {
                                    GemTooltip(gem) { tooltipGemId = null }
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
    Box(modifier = Modifier.background(Color.White, RoundedCornerShape(10.dp))
        .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontSize = 16.sp, color = valueColor)
            Text(label, fontSize = 10.sp, color = Color(0xFF888888))
        }
    }
}

@Composable
fun GlobalHighScoreBoard() {
    val scores = PlayerRegistry.allScores()
    Box(modifier = Modifier.fillMaxWidth()
        .background(AppColors.goldBg, RoundedCornerShape(14.dp)).padding(12.dp)) {
        Column {
            Text("🏆 All-Time Champions", fontSize = 14.sp, color = AppColors.goldText)
            Spacer(Modifier.height(6.dp))
            if (scores.isEmpty()) {
                Text("No scores yet — be the first champion!",
                    fontSize = 12.sp, color = Color(0xFF888888))
            } else {
                scores.forEachIndexed { i, (name, score) ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
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
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
        Difficulty.entries.forEach { d ->
            val isSelected = d == selected
            Box(modifier = Modifier.weight(1f)
                .background(if (isSelected) AppColors.purple else Color.White, RoundedCornerShape(10.dp))
                .clickable { onSelect(d) }.padding(vertical = 8.dp),
                contentAlignment = Alignment.Center) {
                Text(d.label, fontSize = 11.sp,
                    color = if (isSelected) Color.White else AppColors.purple)
            }
        }
    }
}

@Composable
fun ThemeSelector(selected: GameTheme, onSelect: (GameTheme) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.fillMaxWidth()) {
        ALL_THEMES.forEach { t ->
            val isSelected = t.name == selected.name
            Box(modifier = Modifier.weight(1f)
                .background(if (isSelected) AppColors.orange else Color.White, RoundedCornerShape(8.dp))
                .clickable { onSelect(t) }.padding(vertical = 6.dp),
                contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(t.emoji, fontSize = 14.sp)
                    Text(t.name, fontSize = 8.sp,
                        color = if (isSelected) Color.White else Color(0xFF555555))
                }
            }
        }
    }
}

// ── Break Screen ──────────────────────────────────────────────────────────────

@Composable
fun BreakScreen(activity: BreakActivity, onBreakEnd: () -> Unit, onQuit: () -> Unit = {}) {
    val breakDuration = 300  // 5 minutes
    var totalTimeLeft by remember { mutableStateOf(breakDuration) }
    var stepIndex by remember { mutableStateOf(0) }
    var stepTimeLeft by remember { mutableStateOf(activity.steps[0].durationSeconds) }

    val currentStep = activity.steps[stepIndex % activity.steps.size]
    val progressFraction = 1f - (totalTimeLeft / breakDuration.toFloat())
    val minutes = totalTimeLeft / 60
    val seconds = totalTimeLeft % 60

    LaunchedEffect(Unit) {
        var sIdx = 0
        var remaining = breakDuration
        while (remaining > 0) {
            val step = activity.steps[sIdx % activity.steps.size]
            pepperSpeak(if (appLanguage == Language.FINNISH) step.speakTextFi else step.speakText)
            var stepTimer = step.durationSeconds
            while (stepTimer > 0 && remaining > 0) {
                delay(1000)
                stepTimer--; remaining--
                stepTimeLeft = stepTimer
                totalTimeLeft = remaining
            }
            sIdx++
            stepIndex = sIdx
        }
        pepperSpeak(t("Wonderful! Great energy! You are amazing! Now let's get back to the memory game!",
            "Mahtavaa! Loistavaa energiaa! Olet ihana! Palataan nyt muistipeliin!"))
        onBreakEnd()
    }

    Box(
        modifier = Modifier.fillMaxSize()
            .background(activity.bgColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.padding(24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("🤖", fontSize = 28.sp)
                Column {
                    Text(t("Pepper's Fun Break!","Pepperin hauskatauko!"), fontSize = 18.sp,
                        color = activity.color, fontWeight = FontWeight.Bold)
                    Text(t("Take a break and move your body!","Pidä tauko ja liiku!"), fontSize = 12.sp,
                        color = activity.color.copy(alpha = 0.7f))
                }
            }

            Box(modifier = Modifier
                .background(activity.color, RoundedCornerShape(14.dp))
                .padding(horizontal = 20.dp, vertical = 8.dp)) {
                Text("${activity.emoji}  ${activity.title}",
                    fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }

            Box(
                modifier = Modifier.fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(20.dp))
                    .border(2.dp, activity.color.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(currentStep.emoji, fontSize = 52.sp)
                    Text(currentStep.instruction, fontSize = 15.sp,
                        color = activity.color, textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold)
                    val stepTotal = currentStep.durationSeconds
                    val stepFrac = (stepTimeLeft / stepTotal.toFloat()).coerceIn(0f, 1f)
                    Box(modifier = Modifier.fillMaxWidth().height(8.dp)
                        .background(activity.color.copy(alpha = 0.15f), RoundedCornerShape(4.dp))) {
                        Box(modifier = Modifier.fillMaxWidth(stepFrac).height(8.dp)
                            .background(activity.color, RoundedCornerShape(4.dp)))
                    }
                    Text("${stepTimeLeft}s", fontSize = 12.sp,
                        color = activity.color.copy(alpha = 0.7f))
                }
            }

            Box(modifier = Modifier.fillMaxWidth()
                .background(Color.White.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                .padding(10.dp)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()) {
                    Text(t("Break ends in","Tauko päättyy") + "  $minutes:${seconds.toString().padStart(2, '0')}",
                        fontSize = 13.sp, color = activity.color)
                    Spacer(Modifier.height(4.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(6.dp)
                        .background(activity.color.copy(alpha = 0.15f), RoundedCornerShape(3.dp))) {
                        Box(modifier = Modifier.fillMaxWidth(progressFraction).height(6.dp)
                            .background(activity.color, RoundedCornerShape(3.dp)))
                    }
                }
            }

            Text(t("Game resumes automatically after the break ✨","Peli jatkuu automaattisesti tauon jälkeen ✨"),
                fontSize = 11.sp, color = activity.color.copy(alpha = 0.6f))

            val breakContext = LocalContext.current
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Back to Game button
                Box(
                    modifier = Modifier.weight(1f)
                        .background(activity.color, RoundedCornerShape(14.dp))
                        .clickable { onBreakEnd() }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(t("▶  Back to Game","▶  Takaisin peliin"), fontSize = 15.sp, color = Color.White,
                        fontWeight = FontWeight.Bold)
                }

                // Quit button
                Box(
                    modifier = Modifier.weight(1f)
                        .background(Color(0xFFFFEBEE), RoundedCornerShape(14.dp))
                        .clickable {
                            SoundManager.stopBackgroundMusic()
                            onQuit()
                            (breakContext as? Activity)?.finish()
                        }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(t("❌  Quit Game","❌  Lopeta peli"), fontSize = 15.sp, color = Color(0xFFD32F2F),
                        fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ── Settings Panel ────────────────────────────────────────────────────────────

@Composable
fun SettingsPanel(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val audioManager = remember {
        context.getSystemService(android.content.Context.AUDIO_SERVICE) as AudioManager
    }
    val maxVol = remember { audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) }
    var musicVol by remember { mutableStateOf(SoundManager.musicVolume) }
    var voiceVol by remember {
        mutableStateOf(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / maxVol)
    }

    Box(
        modifier = Modifier.fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(340.dp)
                .background(Color.White, RoundedCornerShape(20.dp))
                .clickable { /* absorb clicks so panel stays open */ }
                .padding(24.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {

                // Title row
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Text(t("⚙️ Settings","⚙️ Asetukset"), fontSize = 20.sp, color = AppColors.purple,
                        fontWeight = FontWeight.Bold)
                    Text("✕", fontSize = 18.sp, color = Color(0xFF888888),
                        modifier = Modifier.clickable { onDismiss() })
                }

                // Music volume
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(t("🎵 Music Volume","🎵 Musiikin äänenvoimakkuus"), fontSize = 14.sp, color = Color(0xFF444444))
                        Text("${(musicVol * 100).toInt()}%", fontSize = 13.sp, color = AppColors.purple)
                    }
                    Slider(
                        value = musicVol,
                        onValueChange = {
                            musicVol = it
                            SoundManager.musicVolume = it
                        },
                        valueRange = 0f..1f,
                        colors = SliderDefaults.colors(
                            thumbColor = AppColors.purple,
                            activeTrackColor = AppColors.purple,
                            inactiveTrackColor = Color(0xFFD0CCEF)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Voice / master volume
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(t("🔊 Voice Volume","🔊 Puheen äänenvoimakkuus"), fontSize = 14.sp, color = Color(0xFF444444))
                        Text("${(voiceVol * 100).toInt()}%", fontSize = 13.sp, color = AppColors.orange)
                    }
                    Slider(
                        value = voiceVol,
                        onValueChange = {
                            voiceVol = it
                            val level = (it * maxVol).toInt().coerceIn(0, maxVol)
                            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, level, 0)
                        },
                        valueRange = 0f..1f,
                        colors = SliderDefaults.colors(
                            thumbColor = AppColors.orange,
                            activeTrackColor = AppColors.orange,
                            inactiveTrackColor = Color(0xFFF5C4B3)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Quit button
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .background(Color(0xFFFFEBEE), RoundedCornerShape(12.dp))
                        .clickable {
                            SoundManager.stopBackgroundMusic()
                            (context as? Activity)?.finish()
                        }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(t("❌  Quit Game","❌  Lopeta peli"), fontSize = 15.sp, color = Color(0xFFD32F2F),
                        fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
