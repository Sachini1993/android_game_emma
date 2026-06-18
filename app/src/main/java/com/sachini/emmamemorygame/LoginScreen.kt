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
            Text(t("Pepper Memory Game","Pepper Muistipeli"), fontSize = 26.sp, color = AppColors.orange)
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
                    Text(t("Welcome! Are you a new player\nor have you played before?",
                        "Tervetuloa! Oletko uusi pelaaja\nvai oletko pelannut ennen?"),
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
                                Text(t("I am new!","Olen uusi!"), fontSize = 16.sp, color = Color.White)
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
                                Text(t("I played before!","Olen pelannut!"), fontSize = 16.sp, color = Color.White)
                            }
                        }
                    }
                    if (existingPlayers.isEmpty())
                        Text(t("No players yet — be the first!","Ei pelaajia vielä — ole ensimmäinen!"),
                            fontSize = 12.sp, color = Color(0xFF888888))
                    if (PlayerRegistry.allScores().isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        Box(modifier = Modifier.widthIn(max = 400.dp)) { GlobalHighScoreBoard() }
                    }
                }

                // ── 2. Enter new name ────────────────────────────────────────
                LoginStep.ENTER_NEW_NAME -> {
                    Text(t("What is your name?","Mikä on nimesi?"), fontSize = 20.sp, color = AppColors.purple)
                    Text(t("Tap the letters below:","Napauta kirjaimia alla:"), fontSize = 13.sp, color = Color(0xFF888888))
                    Box(modifier = Modifier.widthIn(min = 280.dp)
                        .background(Color.White, RoundedCornerShape(12.dp)).padding(16.dp),
                        contentAlignment = Alignment.Center) {
                        Text(
                            if (nameInput.isEmpty()) t("Tap letters below...","Napauta kirjaimia...") else nameInput,
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
                            Text(t("⌫ Delete","⌫ Poista"), fontSize = 14.sp, color = AppColors.orange)
                        }
                        if (nameInput.isNotEmpty()) {
                            Box(modifier = Modifier
                                .background(Color(0xFFFFE0CC), RoundedCornerShape(10.dp))
                                .clickable { nameInput = "" }
                                .padding(horizontal = 18.dp, vertical = 10.dp)) {
                                Text(t("✕ Clear","✕ Tyhjennä"), fontSize = 14.sp, color = AppColors.orange)
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
                                Text(t("▶ Continue as $nameInput","▶ Jatka nimellä $nameInput"), fontSize = 14.sp, color = Color.White)
                            }
                        }
                    }
                    Text(t("← Back","← Takaisin"), fontSize = 13.sp, color = Color(0xFF888888),
                        modifier = Modifier.clickable {
                            step = LoginStep.ASK_NEW_OR_OLD; nameInput = ""
                        })
                }

                // ── 3. Returning player list ─────────────────────────────────
                LoginStep.PICK_FROM_LIST -> {
                    Text(t("Welcome back! Who are you?","Tervetuloa takaisin! Kuka olet?"), fontSize = 20.sp, color = AppColors.green)
                    Text(t("Tap your name to continue:","Napauta nimeäsi jatkaaksesi:"), fontSize = 13.sp, color = Color(0xFF888888))
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
                                    Text("Lv.${level.level} ${level.title}  •  ${player.gamesPlayed} ${t("games","peliä")}",
                                        fontSize = 11.sp, color = Color(0xFF888888))
                                    Text("💎 ${player.gemCount()}/${ALL_GEMS.size} ${t("gems","jalokiveä")}",
                                        fontSize = 11.sp, color = AppColors.purple)
                                }
                                Column(horizontalAlignment = Alignment.End,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(t("Best: ${player.bestScore} pts","Paras: ${player.bestScore} p"),
                                        fontSize = 13.sp, color = AppColors.goldText)
                                    Text("🔥 ${player.bestStreak} ${t("streak","putki")}",
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
                                                Text(t("Yes, delete","Kyllä, poista"), fontSize = 11.sp, color = Color(0xFFD32F2F),
                                                    fontWeight = FontWeight.Bold)
                                            }
                                            Box(modifier = Modifier
                                                .background(Color(0xFFEEEEEE), RoundedCornerShape(8.dp))
                                                .clickable { confirmDelete = false }
                                                .padding(horizontal = 10.dp, vertical = 5.dp)) {
                                                Text(t("Cancel","Peruuta"), fontSize = 11.sp, color = Color(0xFF555555))
                                            }
                                        }
                                    } else {
                                        Box(modifier = Modifier
                                            .background(Color(0xFFFFEBEE), RoundedCornerShape(8.dp))
                                            .clickable { confirmDelete = true }
                                            .padding(horizontal = 10.dp, vertical = 5.dp)) {
                                            Text(t("🗑 Remove","🗑 Poista"), fontSize = 11.sp, color = Color(0xFFD32F2F))
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        Text(t("← Back","← Takaisin"), fontSize = 13.sp, color = Color(0xFF888888),
                            modifier = Modifier.clickable { step = LoginStep.ASK_NEW_OR_OLD })
                        Text(t("+ New player","+ Uusi pelaaja"), fontSize = 13.sp, color = AppColors.purple,
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

    val pages = if (appLanguage == Language.FINNISH) listOf(
        OnboardPage("🧠", "Tervetuloa, $playerName!", AppColors.purple,
            "Pepper Muistipeli harjoittaa aivojasi hauskalla korttien yhdistämishaasteella!\n\n" +
            "Käännät kortteja löytääksesi pareja — mitä nopeammin löydät, sitä enemmän pisteitä saat.\n\n" +
            "Pepper-robotti kannustaa sinua ja pitää sinulle seuraa!"),
        OnboardPage("🃏", "Kuinka pelata", AppColors.orange,
            "① Napauta korttia kääntääksesi se ylöspäin.\n\n" +
            "② Napauta toista korttia — jos ne vastaavat, molemmat jäävät auki! ✅\n\n" +
            "③ Jos ne eivät vastaa, molemmat kääntyvät takaisin. Yritä muistaa!\n\n" +
            "④ Yhdistä KAIKKI parit ennen kuin 2 minuutin ajastin loppuu.\n\n" +
            "💡 Vinkki: Mene nopeasti — ansaitset enemmän XP:tä vähemmillä vuoroilla!"),
        OnboardPage("⚡", "Vaikeustasot", Color(0xFF1D9E75),
            "🟢 Helppo — 3 × 4 ruudukko (6 paria). Loistava aloittelijoille.\n\n" +
            "🟡 Keski — 4 × 4 ruudukko (8 paria). Hyvä haaste.\n\n" +
            "🔴 Vaikea — 4 × 5 ruudukko (10 paria). Maksimaalinen aivotreeni!\n\n" +
            "Tekoäly seuraa suoritustasi ja ehdottaa parasta vaikeustasoa sinulle.\n\n" +
            "🏆 Pelaa Vaikea-tilaa johdonmukaisesti avataksesi korkeimmat jalokivet ja XP-palkkiot!"),
        OnboardPage("⭐", "XP ja tasot", AppColors.purple,
            "Jokainen peli tuo sinulle XP:tä (kokemuspisteitä):\n\n" +
            "• Kaikkien parien yhdistäminen → iso XP-bonus\n" +
            "• Vähemmän vuoroja → XP-kerroin\n" +
            "• Jäljellä oleva aika → nopeusbonus\n" +
            "• Voittoputket → putkibonus 🔥\n\n" +
            "Nouse tasolta Muistialoittelija → Korttien kääntäjä → … → Muistilegenda (Taso 10)!\n\n" +
            "Korkeammat tasot avaavat vaikeampia haasteita."),
        OnboardPage("💎", "Jalokivet ja saavutukset", Color(0xFF0C447C),
            "Kerää erikoisjalokiviä saavuttamalla virstanpylväitä:\n\n" +
            "💎 Jäätimantti — Pelaa ensimmäinen pelisi\n" +
            "🔮 Kristallipallo — Saavuta taso 3\n" +
            "🔥 Tulijaspe — Voita 3 peliä putkeen\n" +
            "🏆 Kultapiala — Saa ensimmäinen ennätyksesi\n" +
            "💫 Tähtipurkaus — Saavuta taso 5\n" +
            "⚡ Salama — Voita 5 putkeen\n" +
            "🌟 Kultainen tähti — Voita yli 60 sekuntia jäljellä\n" +
            "🌈 Sateenkaari — Pelaa kaikki 5 teemaa\n" +
            "🧠 Aivoharjoittelija — Pelaa 10 peliä\n" +
            "👑 Kuninkaallinen kruunu — Saavuta taso 10 Legenda!\n\n" +
            "Napauta mitä tahansa jalokiveä hyllyllä nähdäksesi sen kuvauksen."),
        OnboardPage("🤸", "Liikuntatauot", Color(0xFF6B3D9A),
            "Jos Pepper huomaa, ettet ole napauttanut muutamaan sekuntiin, hän ehdottaa hauskaa liikuntataukoa!\n\n" +
            "Taukonäytöllä näkyy harjoituksia kuten venyttely, hyppelyharjoitukset tai tanssi.\n\n" +
            "Pepper ohjaa sinut jokaisen vaiheen läpi.\n\n" +
            "Tauon jälkeen peli jatkuu automaattisesti — TAI napauta ❌ Lopeta peli poistuaksesi.\n\n" +
            "Liikuntatauot auttavat sinua keskittymään ja elvyttämään aivojasi! 🧠⚡"),
        OnboardPage("⚙️", "Asetukset ja ääni", AppColors.orange,
            "Napauta ⚙️-kuvaketta pelisivun vasemmassa yläkulmassa avataksesi Asetukset:\n\n" +
            "🎵 Musiikin äänenvoimakkuus — liu'uta tehdäksesi taustamusiikin kovemmaksi tai hiljaisemmaksi.\n\n" +
            "🔊 Puheen äänenvoimakkuus — säätelee Pepperin puheäänen voimakkuutta.\n\n" +
            "❌ Lopeta peli — pysäyttää musiikin ja poistuu aloitusnäyttöön.\n\n" +
            "Voit myös napauttaa 🔊/🔇 mykistääksesi/poistaaksesi mykistyksen nopeasti pelin aikana."),
        OnboardPage("📋", "Käyttöehdot", Color(0xFF555555),
            "Lue ja hyväksy ennen pelaamista:\n\n" +
            "• Tämä sovellus on suunniteltu opetukselliseen ja virkistyskäyttöön.\n\n" +
            "• Pelaajanimet ja pisteet tallennetaan vain paikallisesti tälle laitteelle.\n\n" +
            "• Henkilötietoja ei lähetetä palvelimille tai kolmansille osapuolille.\n\n" +
            "• Taukojen aikana ehdotetut liikuntaharjoitukset ovat kevyitä ja vapaaehtoisia. Lopeta harjoittelu, jos tunnet epämukavuutta.\n\n" +
            "• Pepper-robotin vuorovaikutus vaatii yhdistetyn Pepper-robotin. Sovellus toimii ilman sitä.\n\n" +
            "• Napauttamalla \"Hyväksyn ja aloitan pelaamisen\" hyväksyt nämä ehdot.")
    ) else listOf(
        OnboardPage("🧠", "Welcome, $playerName!", AppColors.purple,
            "Pepper Memory Game trains your brain with a fun card-matching challenge!\n\n" +
            "You will flip cards to find matching pairs — the faster you match, the more points you earn.\n\n" +
            "Pepper the robot will cheer you on and keep you company!"),
        OnboardPage("🃏", "How to Play", AppColors.orange,
            "① Tap a card to flip it face-up.\n\n" +
            "② Tap a second card — if they match, both stay open! ✅\n\n" +
            "③ If they don't match, both flip back. Try to remember!\n\n" +
            "④ Match ALL pairs before the 2-minute timer runs out.\n\n" +
            "💡 Tip: Go fast — you earn more XP with fewer turns!"),
        OnboardPage("⚡", "Difficulty Levels", Color(0xFF1D9E75),
            "🟢 Easy — 3 × 4 grid (6 pairs). Great for beginners.\n\n" +
            "🟡 Medium — 4 × 4 grid (8 pairs). A good challenge.\n\n" +
            "🔴 Hard — 4 × 5 grid (10 pairs). Maximum brain workout!\n\n" +
            "The AI watches your performance and suggests the best difficulty for you.\n\n" +
            "🏆 Play Hard mode consistently to unlock the highest gems and XP rewards!"),
        OnboardPage("⭐", "XP & Levels", AppColors.purple,
            "Every game earns you XP (experience points):\n\n" +
            "• Match all pairs → big XP bonus\n" +
            "• Fewer turns → XP multiplier\n" +
            "• Time remaining → speed bonus\n" +
            "• Win streaks → streak bonus 🔥\n\n" +
            "Level up from Memory Rookie → Card Flipper → Pattern Seeker → … → Memory Legend (Lv.10)!\n\n" +
            "Higher levels unlock harder challenges."),
        OnboardPage("💎", "Gems & Achievements", Color(0xFF0C447C),
            "Collect special gems by reaching milestones:\n\n" +
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
            "Tap any gem on the shelf to see its description."),
        OnboardPage("🤸", "Activity Breaks", Color(0xFF6B3D9A),
            "If Pepper notices you haven't tapped for a few seconds, she will suggest a fun movement break!\n\n" +
            "The break screen shows exercises like stretching, jumping jacks, or dancing.\n\n" +
            "Pepper will guide you through each step.\n\n" +
            "After the break, the game resumes automatically — OR tap ❌ Quit Game to exit.\n\n" +
            "Movement breaks help you focus and re-energise your brain! 🧠⚡"),
        OnboardPage("⚙️", "Settings & Sound", AppColors.orange,
            "Tap the ⚙️ gear icon in the top-left of the game screen to open Settings:\n\n" +
            "🎵 Music Volume — slide to make the background music louder or softer.\n\n" +
            "🔊 Voice Volume — controls how loud Pepper speaks.\n\n" +
            "❌ Quit Game — stops the music and exits to the home screen.\n\n" +
            "You can also tap 🔊/🔇 to quickly mute/unmute the music during the game."),
        OnboardPage("📋", "Terms & Conditions", Color(0xFF555555),
            "Please read and accept before playing:\n\n" +
            "• This app is designed for educational and recreational use.\n\n" +
            "• Player names and scores are saved locally on this device only.\n\n" +
            "• No personal data is sent to any server or third party.\n\n" +
            "• Physical activity suggestions during breaks are gentle and optional. Stop any exercise if you feel discomfort.\n\n" +
            "• Pepper robot interactions require a connected Pepper robot. The app works without one.\n\n" +
            "• By tapping \"I Accept & Start Playing\" you agree to these terms.")
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
                            Text(t("I have read and accept the Terms & Conditions",
                                "Olen lukenut ja hyväksyn käyttöehdot"),
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
                    Text(t("← Back","← Takaisin"), fontSize = 15.sp, color = Color(0xFF555555))
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
                    Text(t("Next →","Seuraava →"), fontSize = 15.sp, color = Color.White, fontWeight = FontWeight.Bold)
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
                    Text(t("✅  I Accept & Start Playing","✅  Hyväksyn ja aloitan pelaamisen"),
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
