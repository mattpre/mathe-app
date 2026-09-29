package ch.matheapp.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ch.matheapp.Aufgabe
import ch.matheapp.Einmaleins
import ch.matheapp.UebungsFolge
import kotlinx.coroutines.delay

/** Reihen an- und abwählen. Gilt für alle Spielarten. */
@Composable
fun ReihenAuswahlScreen(start: Set<Int>, onFertig: (Set<Int>) -> Unit, onZurueck: () -> Unit) {
    var reihen by remember { mutableStateOf(start) }
    Column(Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(Modifier.fillMaxWidth()) { TextButton(onClick = onZurueck) { Text("← Zurück", fontSize = 16.sp) } }
        Text("Welche Reihen?", style = MaterialTheme.typography.headlineLarge, color = Lila)
        Text(
            "Tippe auf die Reihen, die du üben willst.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(20.dp))
        (1..12).chunked(3).forEach { zeile ->
            Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                zeile.forEach { r ->
                    val gewaehlt = r in reihen
                    Button(
                        onClick = { reihen = if (gewaehlt) reihen - r else reihen + r },
                        modifier = Modifier.weight(1f).height(64.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (gewaehlt) Lila else Color.White,
                            contentColor = if (gewaehlt) Color.White else Lila,
                        ),
                        border = BorderStroke(2.dp, Lila),
                    ) { Text("$r×", fontSize = 24.sp, fontWeight = FontWeight.Bold) }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = { reihen = Einmaleins.ALLE_REIHEN }) { Text("Alle", fontSize = 18.sp) }
            OutlinedButton(onClick = { reihen = emptySet() }) { Text("Keine", fontSize = 18.sp) }
        }
        Spacer(Modifier.weight(1f))
        if (reihen.isEmpty()) {
            Text("Wähle mindestens eine Reihe.", color = Rot, fontSize = 16.sp)
            Spacer(Modifier.height(8.dp))
        }
        GrosserKnopf(
            "Fertig", "✅", if (reihen.isEmpty()) Lila.copy(alpha = 0.4f) else Gruen,
            onClick = { if (reihen.isNotEmpty()) onFertig(reihen) },
        )
    }
}

/** Üben ohne Zeitdruck: endlos Aufgaben aus [pool], mit Serien-Zähler. */
@Composable
fun UebenScreen(pool: List<Aufgabe>, kopfText: String, onZurueck: () -> Unit) {
    val folge = remember { UebungsFolge(pool) }
    var aufgabe by remember { mutableStateOf(folge.naechste()) }
    var eingabe by remember { mutableStateOf("") }
    var feedback by remember { mutableStateOf<Boolean?>(null) }
    var richtig by remember { mutableIntStateOf(0) }
    var total by remember { mutableIntStateOf(0) }
    var serie by remember { mutableIntStateOf(0) }
    var besteSerie by remember { mutableIntStateOf(0) }

    LaunchedEffect(feedback) {
        val ok = feedback ?: return@LaunchedEffect
        delay(if (ok) 700 else 2000)
        aufgabe = folge.naechste()
        eingabe = ""
        feedback = null
    }

    Column(Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onZurueck) { Text("← Fertig", fontSize = 16.sp) }
            Spacer(Modifier.weight(1f))
            Text("✅ $richtig / $total", style = MaterialTheme.typography.titleLarge)
        }
        Text(kopfText, fontSize = 16.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(
            if (serie > 0) "🔥 $serie in Folge" else " ",
            style = MaterialTheme.typography.headlineMedium,
            color = Orange,
        )
        Text(if (besteSerie > 0) "Beste Serie: $besteSerie" else " ", fontSize = 16.sp)
        Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
            AufgabenAnzeige(aufgabe.toString(), eingabe, feedback, aufgabe.ergebnis)
        }
        Zahlenfeld(
            aktiv = feedback == null,
            onZiffer = { if (eingabe.length < 3 && !(eingabe.isEmpty() && it == 0)) eingabe += it },
            onLoeschen = { eingabe = eingabe.dropLast(1) },
            onOk = {
                if (eingabe.isNotEmpty() && feedback == null) {
                    val ok = eingabe.toInt() == aufgabe.ergebnis
                    total++
                    if (ok) {
                        richtig++
                        serie++
                        besteSerie = maxOf(besteSerie, serie)
                    } else {
                        serie = 0
                    }
                    feedback = ok
                }
            },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
