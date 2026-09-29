package ch.matheapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Grosser, farbiger Knopf für Menüs. */
@Composable
fun GrosserKnopf(
    text: String,
    emoji: String,
    farbe: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(84.dp),
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(containerColor = farbe, contentColor = Color.White),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
    ) {
        Text(emoji, fontSize = 32.sp)
        Spacer(Modifier.width(16.dp))
        Text(text, fontSize = 24.sp, fontWeight = FontWeight.Bold)
    }
}

/** Zahlenfeld mit 0–9, Löschen und OK. */
@Composable
fun Zahlenfeld(
    aktiv: Boolean,
    onZiffer: (Int) -> Unit,
    onLoeschen: () -> Unit,
    onOk: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        listOf(listOf(1, 2, 3), listOf(4, 5, 6), listOf(7, 8, 9)).forEach { reihe ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                reihe.forEach { z -> Taste(z.toString(), Color.White, MaterialTheme.colorScheme.onSurface, aktiv) { onZiffer(z) } }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Taste("⌫", RotHell, Rot, aktiv, onLoeschen)
            Taste("0", Color.White, MaterialTheme.colorScheme.onSurface, aktiv) { onZiffer(0) }
            Taste("OK", Gruen, Color.White, aktiv, onOk)
        }
    }
}

@Composable
private fun RowScope.Taste(text: String, farbe: Color, textFarbe: Color, aktiv: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = aktiv,
        modifier = Modifier.weight(1f).height(68.dp),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = farbe,
            contentColor = textFarbe,
            disabledContainerColor = farbe.copy(alpha = 0.5f),
            disabledContentColor = textFarbe.copy(alpha = 0.5f),
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp),
    ) {
        Text(text, fontSize = 28.sp, fontWeight = FontWeight.Bold)
    }
}

/** Die Aufgabe mit Eingabefeld, eingefärbt je nach Rückmeldung (null = noch offen). */
@Composable
fun AufgabenAnzeige(aufgabe: String, eingabe: String, korrekt: Boolean?, loesung: Int, modifier: Modifier = Modifier) {
    val (hinter, rand) = when (korrekt) {
        true -> GruenHell to Gruen
        false -> RotHell to Rot
        null -> Color.White to LilaHell
    }
    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(rand)
            .padding(4.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(hinter)
            .padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("$aufgabe = ", style = MaterialTheme.typography.displayLarge.copy(fontSize = 48.sp))
            Box(
                Modifier
                    .width(110.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(LilaHell),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    eingabe.ifEmpty { "?" },
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 48.sp),
                    color = if (eingabe.isEmpty()) Lila.copy(alpha = 0.4f) else MaterialTheme.colorScheme.onSurface,
                )
            }
        }
        val meldung = remember(korrekt, aufgabe) {
            when (korrekt) {
                true -> listOf("Super! 🎉", "Richtig! ⭐", "Toll! 👍", "Genau! 🚀", "Klasse! 🌟").random()
                false -> "Fast! Richtig ist $loesung"
                null -> " "
            }
        }
        Text(
            meldung,
            style = MaterialTheme.typography.titleLarge,
            color = if (korrekt == false) Rot else Gruen,
            textAlign = TextAlign.Center,
        )
    }
}

/** Zehn Punkte für den Fortschritt: grün = richtig, rot = falsch, lila = aktuell. */
@Composable
fun Fortschritt(resultate: List<Boolean>, aktuell: Int, total: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(total) { i ->
            val farbe = when {
                i < resultate.size -> if (resultate[i]) Gruen else Rot
                i == aktuell -> Lila
                else -> LilaHell
            }
            Box(Modifier.size(if (i == aktuell) 20.dp else 16.dp).clip(CircleShape).background(farbe))
        }
    }
}
