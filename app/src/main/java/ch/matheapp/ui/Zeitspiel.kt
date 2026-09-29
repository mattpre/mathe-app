package ch.matheapp.ui

import android.os.SystemClock
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ch.matheapp.Aufgabe
import ch.matheapp.Platzierung
import ch.matheapp.Spiel
import ch.matheapp.SpielLevel
import ch.matheapp.aufgabenPool
import kotlinx.coroutines.delay

data class SpielResultat(
    val zeitMs: Long,
    val richtig: Int,
    val falsche: List<Aufgabe>,
) {
    val fehler get() = falsche.size
    val endzeitMs get() = Spiel.endzeitMs(zeitMs, fehler)
}

/** Spiel auf Zeit: 10 Aufgaben, die Zeit läuft nur während der Eingabe. Jeder Fehler gibt 5 s Strafzeit. */
@Composable
fun ZeitspielScreen(
    level: SpielLevel,
    onFertig: (SpielResultat) -> Unit,
    onAbbrechen: () -> Unit,
) {
    val runde = remember { Spiel.erzeugeRunde(level.aufgabenPool()) }
    var index by remember { mutableIntStateOf(0) }
    var eingabe by remember { mutableStateOf("") }
    var countdown by remember { mutableIntStateOf(3) }
    var feedback by remember { mutableStateOf<Boolean?>(null) }
    val resultate = remember { mutableStateListOf<Boolean>() }
    var akkumuliertMs by remember { mutableLongStateOf(0L) }
    var frageStart by remember { mutableLongStateOf(0L) }
    var anzeigeMs by remember { mutableLongStateOf(0L) }

    val aufgabe = runde[index]
    val fehler = resultate.count { !it }

    LaunchedEffect(Unit) {
        while (countdown > 0) {
            delay(800)
            countdown--
        }
        frageStart = SystemClock.elapsedRealtime()
    }

    // Stoppuhr: läuft nur, solange eine Frage offen ist.
    LaunchedEffect(countdown, feedback) {
        if (countdown == 0 && feedback == null) {
            while (true) {
                anzeigeMs = akkumuliertMs + SystemClock.elapsedRealtime() - frageStart
                delay(50)
            }
        }
    }

    LaunchedEffect(feedback) {
        val ok = feedback ?: return@LaunchedEffect
        delay(if (ok) 600 else 1600)
        if (index == runde.lastIndex) {
            onFertig(
                SpielResultat(
                    zeitMs = akkumuliertMs,
                    richtig = resultate.count { it },
                    falsche = runde.filterIndexed { i, _ -> !resultate[i] },
                )
            )
        } else {
            index++
            eingabe = ""
            feedback = null
            frageStart = SystemClock.elapsedRealtime()
        }
    }

    fun pruefen() {
        if (eingabe.isEmpty() || feedback != null || countdown > 0) return
        akkumuliertMs += SystemClock.elapsedRealtime() - frageStart
        anzeigeMs = akkumuliertMs
        val ok = eingabe.toInt() == aufgabe.ergebnis
        resultate += ok
        feedback = ok
    }

    Column(
        Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onAbbrechen) { Text("✕ Abbrechen", fontSize = 16.sp) }
            Spacer(Modifier.weight(1f))
            Text("⏱ " + Spiel.formatZeit(anzeigeMs), style = MaterialTheme.typography.headlineMedium)
        }
        if (fehler > 0) {
            Text("+${fehler * Spiel.STRAFE_MS_PRO_FEHLER / 1000} s Strafzeit", color = Rot, fontSize = 16.sp)
        } else {
            Spacer(Modifier.height(22.dp))
        }
        Spacer(Modifier.height(8.dp))
        Fortschritt(resultate, index, runde.size)
        Text("Frage ${index + 1} von ${runde.size}", fontSize = 16.sp, modifier = Modifier.padding(top = 6.dp))
        Spacer(Modifier.height(16.dp))

        Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
            if (countdown > 0) {
                val skala by animateFloatAsState(if (countdown % 2 == 0) 1.2f else 1f, label = "countdown")
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Bereit?", style = MaterialTheme.typography.headlineMedium)
                    Text(
                        countdown.toString(),
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = 120.sp),
                        color = Lila,
                        modifier = Modifier.scale(skala),
                    )
                }
            } else {
                AufgabenAnzeige(aufgabe.toString(), eingabe, feedback, aufgabe.ergebnis)
            }
        }

        Zahlenfeld(
            aktiv = countdown == 0 && feedback == null,
            onZiffer = { if (eingabe.length < 3 && !(eingabe.isEmpty() && it == 0)) eingabe += it },
            onLoeschen = { eingabe = eingabe.dropLast(1) },
            onOk = ::pruefen,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun ResultatScreen(
    resultat: SpielResultat,
    level: SpielLevel,
    platzierung: Platzierung,
    onNochmal: () -> Unit,
    onHighscores: () -> Unit,
    onMenu: () -> Unit,
) {
    val sterne = Spiel.sterne(resultat.richtig)
    Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Column(
            Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                when (sterne) {
                    3 -> "Perfekt! 🏆"
                    2 -> "Sehr gut! 🎉"
                    1 -> "Gut gemacht! 👍"
                    else -> "Weiter üben! 💪"
                },
                style = MaterialTheme.typography.headlineLarge,
                color = Lila,
            )
            Text("⭐".repeat(sterne) + "☆".repeat(3 - sterne), fontSize = 48.sp)
            Text("${resultat.richtig} von ${Spiel.FRAGEN_PRO_SPIEL} richtig", style = MaterialTheme.typography.titleLarge)
            Text("Zeit: ${Spiel.formatZeit(resultat.zeitMs)}", style = MaterialTheme.typography.bodyLarge)
            if (resultat.fehler > 0) {
                Text(
                    "Strafzeit: +${resultat.fehler * Spiel.STRAFE_MS_PRO_FEHLER / 1000} s",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Rot,
                )
            }
            Text("Endzeit: ${Spiel.formatZeit(resultat.endzeitMs)}", style = MaterialTheme.typography.headlineMedium)
            Text("Level ${level.nummer}", style = MaterialTheme.typography.titleLarge, color = Tuerkis)
            Text(level.beschreibung, fontSize = 15.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            if (platzierung.persoenlicheBestzeit) {
                Text("🎖️ Neue persönliche Bestzeit!", style = MaterialTheme.typography.titleLarge, color = Orange)
            }
            Text(
                when (platzierung.rang) {
                    1 -> "🥇 Platz 1 in der Bestenliste!"
                    else -> "Platz ${platzierung.rang} in der Bestenliste"
                },
                style = MaterialTheme.typography.titleLarge,
                color = Tuerkis,
            )
            if (resultat.falsche.isNotEmpty()) {
                Text("Das üben wir noch:", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 8.dp))
                resultat.falsche.forEach { Text("$it = ${it.ergebnis}", fontSize = 22.sp) }
            }
        }
        Spacer(Modifier.height(12.dp))
        GrosserKnopf("Nochmal", "🔁", Lila, onNochmal)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TextButton(onClick = onHighscores) { Text("🏆 Bestenliste", fontSize = 18.sp) }
            TextButton(onClick = onMenu) { Text("🏠 Menü", fontSize = 18.sp) }
        }
    }
}
