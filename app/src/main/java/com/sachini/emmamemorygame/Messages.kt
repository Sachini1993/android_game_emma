package com.sachini.emmamemorygame

import androidx.compose.ui.graphics.Color

object EmmaMessages {
    private val onMatch   = listOf("Brilliant!","You remembered!","Perfect!","Amazing!","Yes!","Fantastic!","On fire!","Great job!")
    private val onWrong   = listOf("Almost! Keep trying!","You will get it!","Don't give up!","Nearly there!","Stay focused!")
    private val onLevelUp = listOf("Level up! Getting stronger!","New rank achieved!","Incredible progress!")
    private val onStreak  = listOf("Streak bonus! You are unstoppable!","Hot streak! Keep going!","On fire!")
    private val onRecord  = listOf("New record! All time best!","Champion! Amazing score!","New high score!")
    private val onWinFast = listOf("Lightning fast! Speed champion!","Incredible speed!")
    private val onWinSlow = listOf("Patience wins! Well done!","Great game!")

    private val onMatchFi   = listOf("Loistava!","Muistit!","Täydellinen!","Upea!","Kyllä!","Mahtava!","Tulet kuumaksi!","Hyvää työtä!")
    private val onWrongFi   = listOf("Melkein! Jatka yrittämistä!","Onnistut kyllä!","Älä anna periksi!","Olet lähellä!","Pysy keskittyneenä!")
    private val onLevelUpFi = listOf("Tasolle nousu! Tulet vahvemmaksi!","Uusi arvo saavutettu!","Uskomaton edistyminen!")
    private val onStreakFi  = listOf("Putken bonus! Olet pysäyttämätön!","Kuuma putki! Jatka!","Olet tulessa!")
    private val onRecordFi  = listOf("Uusi ennätys! Kaikkien aikojen paras!","Mestari! Upea pisteet!","Uusi huipputulos!")
    private val onWinFastFi = listOf("Salamannopea! Nopeuden mestari!","Uskomaton nopeus!")
    private val onWinSlowFi = listOf("Kärsivällisyys voittaa! Hienosti!","Hieno peli!")

    fun match()   = if (appLanguage == Language.FINNISH) onMatchFi.random()   else onMatch.random()
    fun wrong()   = if (appLanguage == Language.FINNISH) onWrongFi.random()   else onWrong.random()
    fun levelUp() = if (appLanguage == Language.FINNISH) onLevelUpFi.random() else onLevelUp.random()
    fun streak()  = if (appLanguage == Language.FINNISH) onStreakFi.random()  else onStreak.random()
    fun record()  = if (appLanguage == Language.FINNISH) onRecordFi.random()  else onRecord.random()
    fun win(time: Int) = if (appLanguage == Language.FINNISH)
        (if (time > 60) onWinFastFi else onWinSlowFi).random()
    else
        (if (time > 60) onWinFast else onWinSlow).random()
}

// ── Win speeches ──────────────────────────────────────────────────────────────

val PEPPER_WIN_SPEECHES = listOf(
    "Hooray! You win! That was amazing!",
    "Yes! You did it! Congratulations! You are fantastic!",
    "Woohoo! You found all the pairs! Brilliant work!",
    "Incredible! You won! I knew you could do it!",
    "Yahoo! Perfect game! You are a true memory champion!",
    "Spectacular! You matched them all! I am so proud of you!",
    "Outstanding! You beat the game! You have an amazing memory!"
)

val PEPPER_WIN_SPEECHES_FI = listOf(
    "Hurraa! Voitit! Se oli upea suoritus!",
    "Kyllä! Onnistuit! Onnittelut! Olet loistava!",
    "Huhuu! Löysit kaikki parit! Mahtavaa työtä!",
    "Uskomaton! Voitit! Tiesin, että onnistut!",
    "Jee! Täydellinen peli! Olet todellinen muistimestari!",
    "Upea! Löysit kaikki! Olen niin ylpeä sinusta!",
    "Erinomainen! Voitit pelin! Sinulla on hämmästyttävä muisti!"
)

// ── Motivation speeches ───────────────────────────────────────────────────────

val PEPPER_MOTIVATION_SPEECHES = listOf(
    "Keep going, you can do it!",
    "Don't give up! You are getting closer!",
    "Stay focused! Believe in yourself!",
    "Every try makes you stronger! Keep going!",
    "You are doing great! Keep trying!"
)

val PEPPER_MOTIVATION_SPEECHES_FI = listOf(
    "Jatka, sinä pystyt siihen!",
    "Älä anna periksi! Pääset lähemmäksi!",
    "Pysy keskittyneenä! Usko itseesi!",
    "Jokainen yritys tekee sinut vahvemmaksi! Jatka!",
    "Teet hienosti! Jatka yrittämistä!"
)

// ── Break activities ──────────────────────────────────────────────────────────

val BREAK_ACTIVITIES = listOf(
    BreakActivity("simon_says", "Simon Says!", "🎭", Color(0xFF7B1FA2), Color(0xFFF3E5F5), listOf(
        BreakStep("Simon says... touch your nose!", "Simon says, touch your nose!", "Simon sanoo, kosketa nenääsi!", "👃", 6),
        BreakStep("Simon says... raise both arms up!", "Simon says, raise both arms up high!", "Simon sanoo, nosta molemmat kädet ylös!", "🙌", 6),
        BreakStep("Simon says... clap three times!", "Simon says, clap three times!", "Simon sanoo, taputa kolme kertaa!", "👏", 6),
        BreakStep("Simon says... pat your head!", "Simon says, pat your head!", "Simon sanoo, taputa päätäsi!", "🤚", 6),
        BreakStep("Touch your toes! — Simon didn't say! 😄", "Touch your toes! Oops, Simon did not say that!", "Kosketa varpaita! Hups, Simon ei sanonut niin! 😄", "😄", 7),
        BreakStep("Simon says... wiggle your fingers!", "Simon says, wiggle all your fingers!", "Simon sanoo, heiluta kaikkia sormiasi!", "🤙", 6),
        BreakStep("Simon says... jump once!", "Simon says, jump once!", "Simon sanoo, hyppää kerran!", "🦘", 6),
        BreakStep("Simon says... spin around!", "Simon says, spin all the way around!", "Simon sanoo, pyöri kokonaan ympäri!", "🌀", 7),
        BreakStep("Simon says... touch your shoulders!", "Simon says, touch both shoulders!", "Simon sanoo, kosketa molempia olkapäitä!", "💪", 6),
        BreakStep("Simon says... take a big bow!", "Simon says, take a big bow! Great job!", "Simon sanoo, kumarra syvään! Hyvää työtä!", "🎭", 6)
    )),
    BreakActivity("stretch", "Stretch & Refresh", "🧘", Color(0xFF1D9E75), Color(0xFFE1F5EE), listOf(
        BreakStep("Roll your shoulders slowly backwards", "Let's roll our shoulders slowly backwards. Ready? Go!", "Pyöritetään olkapäitä hitaasti taaksepäin. Valmiina? Aloitetaan!", "🔄", 8),
        BreakStep("Reach both arms as high as you can!", "Now reach both arms up as high as you can!", "Nyt nosta molemmat kädet ylös niin korkealle kuin pystyt!", "🙌", 8),
        BreakStep("Tilt your head gently to the left", "Gently tilt your head to the left and hold", "Kallista pää varovasti vasemmalle ja pidä", "↙️", 7),
        BreakStep("Now tilt your head to the right", "Now tilt to the right and hold", "Nyt oikealle ja pidä", "↗️", 7),
        BreakStep("Shake out your hands and wrists", "Shake out your hands and wrists, let it all go!", "Ravista käsiä ja ranteita, päästä irti kaikesta!", "🤲", 6),
        BreakStep("Breathe in... hold... breathe out", "Take a big deep breath in... hold it... now breathe all the way out", "Hengitä syvään sisään... pidätä... nyt hengitä kokonaan ulos", "😮‍💨", 9),
        BreakStep("Stretch your arms wide like a star!", "Stretch your arms out wide like a giant star!", "Levitä kädet leveälle kuin iso tähti!", "⭐", 8),
        BreakStep("Roll your shoulders one more time", "Last one! Roll those shoulders one more time", "Viimeinen! Pyöritä olkapäitä vielä kerran", "🔄", 8),
        BreakStep("Wiggle your fingers and toes", "Wiggle all your fingers and toes together", "Heiluta kaikkia sormiasi ja varpaita yhdessä", "🤙", 6),
        BreakStep("Relax and take one more deep breath", "Perfect! Relax and take one last deep breath", "Täydellinen! Rentoudu ja hengitä syvään vielä kerran", "💨", 7)
    )),
    BreakActivity("dance", "Dance Break!", "🕺", Color(0xFFD85A30), Color(0xFFFAEEDA), listOf(
        BreakStep("Clap your hands to the beat! 👏 👏 👏", "Clap your hands to the beat! Clap clap clap!", "Taputa käsiä rytmiin! Taputa taputa taputa!", "👏", 7),
        BreakStep("Wave your arms side to side!", "Wave your arms from side to side like the ocean!", "Heiluta käsiä puolelta toiselle kuin meri!", "🌊", 7),
        BreakStep("Stomp your feet on the ground!", "Stomp those feet! Left, right, left, right!", "Tömistä jaloilla! Vasen, oikea, vasen, oikea!", "👣", 7),
        BreakStep("Spin around! Spin spin spin!", "Everybody spin around! Spin spin spin!", "Kaikki pyörimään! Pyöri pyöri pyöri!", "🌀", 7),
        BreakStep("Wiggle your whole body!", "Now wiggle your whole body from head to toe!", "Nyt heiluta koko keho päästä varpaisiin!", "🐛", 7),
        BreakStep("Pump your fists in the air!", "Pump those fists up in the air! Go go go!", "Pumppaa nyrkit ilmaan! Mene mene mene!", "✊", 7),
        BreakStep("Shake your hips to the groove!", "Shake those hips and feel the music!", "Heiluta lantiota musiikin tahtiin!", "🎵", 7),
        BreakStep("Jump and land with a pose!", "Jump up and land in your best superhero pose!", "Hyppää ja laskeudu parhaaseen supersankaripoosiin!", "🦸", 7),
        BreakStep("Do the robot move! ⚙️🤖", "Now do the robot! Move like a machine!", "Nyt robottiliike! Liiku kuin kone!", "🤖", 7),
        BreakStep("Strike your best victory pose!", "And finally, strike your most amazing victory pose!", "Ja lopuksi, tee upein voittopoosi!", "🏆", 7)
    )),
    BreakActivity("movement", "Move & Groove", "🏃", Color(0xFF0C447C), Color(0xFFE6F1FB), listOf(
        BreakStep("March in place — left, right, left!", "Let's march in place! Left, right, left, right!", "Marssi paikalla! Vasen, oikea, vasen, oikea!", "🚶", 9),
        BreakStep("5 jumping jacks — ready? Go!", "Do five jumping jacks! Ready? One, two, three, four, five!", "Viisi hyppyhaarahyppelyä! Valmiina? Yksi, kaksi, kolme, neljä, viisi!", "⚡", 10),
        BreakStep("Balance on one foot for 5 seconds!", "Balance on one foot! Hold it... hold it...", "Tasapainota yhdellä jalalla! Pidä... pidä...", "🦩", 7),
        BreakStep("Now balance on the other foot!", "Switch feet! Balance on the other foot now!", "Vaihda jalkaa! Tasapainota toisella jalalla nyt!", "🦩", 7),
        BreakStep("Right hand touches left knee!", "Reach your right hand to touch your left knee!", "Oikea käsi koskettaa vasenta polvea!", "👋", 6),
        BreakStep("Left hand touches right knee!", "Now left hand to right knee! Keep it going!", "Nyt vasen käsi oikeaan polveen! Jatka!", "👋", 6),
        BreakStep("Pretend you are swimming!", "Pretend you are swimming! Make those big strokes!", "Teeskentele uivasi! Tee isoja vetoja!", "🏊", 8),
        BreakStep("3 big hops in place!", "Three big hops! Hop, hop, hop!", "Kolme isoa hyppyä! Hypp, hypp, hypp!", "🐸", 6),
        BreakStep("Reach down and touch the floor!", "Bend down and try to touch the floor!", "Kumarra alas ja yritä koskettaa lattiaa!", "⬇️", 7),
        BreakStep("Shake it all out and relax!", "Shake everything out and relax. Amazing work!", "Ravista kaikki ulos ja rentoudu. Mahtavaa työtä!", "😌", 8)
    ))
)

// ── Play again / result messages ──────────────────────────────────────────────

val PLAY_AGAIN_MESSAGES = listOf(
    "Can you beat your score? Play Again!",
    "One more round, you are on fire!",
    "Champion material. Prove it again!",
    "So close to perfection. Try again!",
    "Level up your brain. Play Again!",
    "Stronger every round. Go again!",
    "Your best game is still ahead!",
    "New challenge awaits. Dare to play?"
)

val PLAY_AGAIN_MESSAGES_FI = listOf(
    "Voitko päihittää oman tuloksesi? Pelaa uudelleen!",
    "Yksi kierros lisää, olet tulessa!",
    "Mestariainesta. Todista se uudelleen!",
    "Niin lähellä täydellisyyttä. Yritä uudelleen!",
    "Nosta aivosi tasolle. Pelaa uudelleen!",
    "Vahvempi joka kierroksella. Mene uudelleen!",
    "Paras pelisi on vielä edessä!",
    "Uusi haaste odottaa. Uskallat pelata?"
)

val WIN_RIGHT_MESSAGES = listOf(
    "Your next challenge is ready.\nDo you dare?",
    "Think you can do even better?\nProve it!",
    "A new mystery awaits you.\nAre you ready?",
    "Your brain is warming up.\nKeep going!"
)

val WIN_RIGHT_MESSAGES_FI = listOf(
    "Seuraava haaste odottaa.\nUskallat?",
    "Luuletko pystyväsi vielä paremmin?\nTodista se!",
    "Uusi mysteeri odottaa sinua.\nOletko valmis?",
    "Aivosi lämpenevät.\nJatka!"
)

val LOSE_RIGHT_MESSAGES = listOf(
    "So close! The cards are\nwaiting for you.",
    "Every champion fails first.\nTry again!",
    "The clock won't beat\nyou twice!"
)

val LOSE_RIGHT_MESSAGES_FI = listOf(
    "Niin lähellä! Kortit\nodottavat sinua.",
    "Jokainen mestari epäonnistuu ensin.\nYritä uudelleen!",
    "Kello ei voita\nsinua kahdesti!"
)
