package ch.matheapp.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ch.matheapp.Einmaleins
import ch.matheapp.Highscore
import ch.matheapp.Spiel
import ch.matheapp.SpielLevel
import ch.matheapp.Speicher
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val LevelFarbenListe = listOf(Gruen, Tuerkis, Orange, Rot)

private fun farbeFuer(level: SpielLevel): Color = LevelFarbenListe[(level.nummer - 1).coerceIn(0, 3)]

@Composable
fun StartScreen(profil: String, onEinmaleins: () -> Unit, onPlusMinus: () -> Unit, onProfil: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("🧮", fontSize = 96.sp)
        Text("Mathe-App", style = MaterialTheme.typography.displayLarge.copy(fontSize = 52.sp), color = Lila)
        Text("Rechnen macht Spass!", style = MaterialTheme.typography.titleLarge, color = Orange)
        Spacer(Modifier.height(32.dp))
        OutlinedButton(onClick = onProfil, border = BorderStroke(2.dp, Lila)) {
            Text("👤 $profil  ·  wechseln", fontSize = 18.sp, color = Lila)
        }
        Spacer(Modifier.height(24.dp))
        GrosserKnopf("Einmaleins", "✖️", Lila, onEinmaleins)
        Spacer(Modifier.height(16.dp))
        GrosserKnopf("Plus & Minus", "➕", Tuerkis, onPlusMinus)
    }
}

/** Profil wählen, neues anlegen oder löschen. */
@Composable
fun ProfilScreen(
    profile: List<String>,
    aktuell: String?,
    onWaehlen: (String) -> Unit,
    onNeu: (String) -> Unit,
    onLoeschen: (String) -> Unit,
    onZurueck: (() -> Unit)?,
) {
    var name by remember { mutableStateOf("") }
    var frageLoeschen by remember { mutableStateOf<String?>(null) }
    val anlegen = { if (Speicher.bereinigen(name).isNotEmpty()) { onNeu(name); name = "" } }

    Column(Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(Modifier.fillMaxWidth()) {
            if (onZurueck != null) TextButton(onClick = onZurueck) { Text("← Zurück", fontSize = 16.sp) }
        }
        Text("👤 Wer spielt?", style = MaterialTheme.typography.headlineLarge, color = Lila)
        Spacer(Modifier.height(16.dp))
        LazyColumn(Modifier.weight(1f, fill = false), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(profile) { p ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (p == aktuell) Gelb else Color.White)
                        .clickable { onWaehlen(p) }
                        .padding(start = 20.dp, top = 6.dp, bottom = 6.dp, end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(p, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                    TextButton(onClick = { frageLoeschen = p }) { Text("🗑️", fontSize = 22.sp) }
                }
            }
        }
        Spacer(Modifier.height(20.dp))
        Text(
            if (profile.isEmpty()) "Wie heisst du?" else "Neues Profil",
            style = MaterialTheme.typography.titleLarge,
        )
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it.take(Speicher.MAX_NAME) },
                singleLine = true,
                placeholder = { Text("Name") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { anlegen() }),
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = anlegen,
                enabled = Speicher.bereinigen(name).isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = Gruen),
                modifier = Modifier.height(56.dp),
            ) { Text("＋", fontSize = 24.sp, fontWeight = FontWeight.Bold) }
        }
    }

    frageLoeschen?.let { p ->
        AlertDialog(
            onDismissRequest = { frageLoeschen = null },
            title = { Text("Profil «$p» löschen?") },
            text = { Text("Bestzeiten und Fehlerliste von $p werden gelöscht.") },
            confirmButton = { TextButton(onClick = { frageLoeschen = null; onLoeschen(p) }) { Text("Löschen", color = Rot) } },
            dismissButton = { TextButton(onClick = { frageLoeschen = null }) { Text("Abbrechen") } },
        )
    }
}

@Composable
fun EinmaleinsMenuScreen(
    profil: String,
    reihen: Set<Int>,
    onReihen: () -> Unit,
    onZeitspiel: () -> Unit,
    onUeben: () -> Unit,
    onHighscores: () -> Unit,
    onZurueck: () -> Unit,
) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onZurueck) { Text("← Zurück", fontSize = 16.sp) }
            Spacer(Modifier.weight(1f))
            Text("👤 $profil", style = MaterialTheme.typography.titleLarge, color = Lila)
        }
        Text("Einmaleins", style = MaterialTheme.typography.headlineLarge, color = Lila)
        Spacer(Modifier.height(20.dp))
        GrosserKnopf("Spiel auf Zeit", "⏱️", Orange, onZeitspiel)
        Spacer(Modifier.height(14.dp))
        GrosserKnopf("Üben", "🎯", Tuerkis, onUeben)
        OutlinedButton(onClick = onReihen, border = BorderStroke(2.dp, Tuerkis), modifier = Modifier.padding(top = 6.dp)) {
            Text("Reihen: ${Einmaleins.reihenText(reihen)}  ✏️", fontSize = 16.sp, color = Tuerkis)
        }
        Spacer(Modifier.height(14.dp))
        GrosserKnopf("Bestenliste", "🏆", Gruen, onHighscores)
        Spacer(Modifier.height(24.dp))
        Text(
            "Spiel auf Zeit: ${Spiel.FRAGEN_PRO_SPIEL} Aufgaben so schnell wie möglich.\n" +
                "Jeder Fehler gibt ${Spiel.STRAFE_MS_PRO_FEHLER / 1000} Sekunden Strafzeit.",
            textAlign = TextAlign.Center,
            fontSize = 15.sp,
        )
    }
}

@Composable
fun PlusMinusMenuScreen(
    profil: String,
    onZeitspiel: () -> Unit,
    onUeben: () -> Unit,
    onHighscores: () -> Unit,
    onZurueck: () -> Unit,
) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onZurueck) { Text("← Zurück", fontSize = 16.sp) }
            Spacer(Modifier.weight(1f))
            Text("👤 $profil", style = MaterialTheme.typography.titleLarge, color = Lila)
        }
        Text("Plus & Minus", style = MaterialTheme.typography.headlineLarge, color = Lila)
        Text("Zahlenraum bis 20", style = MaterialTheme.typography.titleLarge, color = Orange)
        Spacer(Modifier.height(20.dp))
        GrosserKnopf("Spiel auf Zeit", "⏱️", Orange, onZeitspiel)
        Spacer(Modifier.height(14.dp))
        GrosserKnopf("Üben", "🎯", Tuerkis, onUeben)
        Spacer(Modifier.height(14.dp))
        GrosserKnopf("Bestenliste", "🏆", Gruen, onHighscores)
        Spacer(Modifier.height(24.dp))
        Text(
            "Spiel auf Zeit: ${Spiel.FRAGEN_PRO_SPIEL} Aufgaben so schnell wie möglich.\n" +
                "Jeder Fehler gibt ${Spiel.STRAFE_MS_PRO_FEHLER / 1000} Sekunden Strafzeit.",
            textAlign = TextAlign.Center,
            fontSize = 15.sp,
        )
    }
}

/** Level fürs Spiel auf Zeit wählen; zeigt die eigene Bestzeit pro Level. */
@Composable
fun LevelScreen(
    levels: List<SpielLevel>,
    bestzeit: (SpielLevel) -> Highscore?,
    onLevel: (SpielLevel) -> Unit,
    onZurueck: () -> Unit,
) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(Modifier.fillMaxWidth()) { TextButton(onClick = onZurueck) { Text("← Zurück", fontSize = 16.sp) } }
        Text("Welches Level?", style = MaterialTheme.typography.headlineLarge, color = Lila)
        Spacer(Modifier.height(16.dp))
        levels.forEach { level ->
            val farbe = farbeFuer(level)
            Button(
                onClick = { onLevel(level) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = farbe, contentColor = Color.White),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 14.dp),
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Level ${level.nummer}", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
                    Text(level.beschreibung, fontSize = 16.sp)
                }
                Text(
                    bestzeit(level)?.let { "🏆 " + Spiel.formatZeit(it.endzeitMs) } ?: "noch keine\nBestzeit",
                    fontSize = 16.sp,
                    textAlign = TextAlign.End,
                )
            }
        }
    }
}

@Composable
fun HighscoreScreen(
    levels: List<SpielLevel>,
    start: SpielLevel,
    aktuellesProfil: String,
    bestenliste: (SpielLevel) -> List<Highscore>,
    onZurueck: () -> Unit,
) {
    var level by remember { mutableStateOf<SpielLevel>(start) }
    val liste = bestenliste(level)
    val datumFormat = remember { SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN) }

    Column(Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(Modifier.fillMaxWidth()) { TextButton(onClick = onZurueck) { Text("← Zurück", fontSize = 16.sp) } }
        Text("🏆 Bestenliste", style = MaterialTheme.typography.headlineLarge, color = Lila)
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            levels.forEach { l ->
                val gewaehlt = l == level
                val farbe = farbeFuer(l)
                Button(
                    onClick = { level = l },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (gewaehlt) farbe else Color.White,
                        contentColor = if (gewaehlt) Color.White else farbe,
                    ),
                    border = BorderStroke(2.dp, farbe),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 10.dp),
                ) { Text("L${l.nummer}", fontSize = 20.sp, fontWeight = FontWeight.Bold) }
            }
        }
        Text(
            "Level ${level.nummer}: ${level.beschreibung}",
            style = MaterialTheme.typography.titleLarge,
            color = farbeFuer(level),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 12.dp),
        )
        if (liste.isEmpty()) {
            Spacer(Modifier.height(32.dp))
            Text("Noch keine Resultate.\nSpiel eine Runde!", textAlign = TextAlign.Center, style = MaterialTheme.typography.titleLarge)
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            itemsIndexed(liste) { i, score ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (score.name == aktuellesProfil) Gelb else Color.White)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        when (i) {
                            0 -> "🥇"
                            1 -> "🥈"
                            2 -> "🥉"
                            else -> "${i + 1}."
                        },
                        fontSize = 26.sp,
                        modifier = Modifier.width(48.dp),
                    )
                    Column(Modifier.weight(1f)) {
                        Text(score.name, style = MaterialTheme.typography.titleLarge)
                        Text(datumFormat.format(Date(score.datum)) + " · ${score.richtig}/${Spiel.FRAGEN_PRO_SPIEL} richtig", fontSize = 13.sp)
                    }
                    Text(Spiel.formatZeit(score.endzeitMs), style = MaterialTheme.typography.titleLarge)
                }
            }
        }
    }
}
