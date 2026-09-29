package ch.matheapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import ch.matheapp.ui.EinmaleinsMenuScreen
import ch.matheapp.ui.HighscoreScreen
import ch.matheapp.ui.LevelScreen
import ch.matheapp.ui.MatheAppTheme
import ch.matheapp.ui.PlusMinusMenuScreen
import ch.matheapp.ui.ProfilScreen
import ch.matheapp.ui.ReihenAuswahlScreen
import ch.matheapp.ui.ResultatScreen
import ch.matheapp.ui.SpielResultat
import ch.matheapp.ui.StartScreen
import ch.matheapp.ui.UebenScreen
import ch.matheapp.ui.ZeitspielScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MatheAppTheme {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    MatheApp()
                }
            }
        }
    }
}

private enum class LevelZiel { ZEITSPIEL, UEBEN }

private sealed interface Bildschirm {
    data object Start : Bildschirm
    data object Profile : Bildschirm
    data object EinmaleinsMenu : Bildschirm
    data object ReihenAuswahl : Bildschirm
    data object PlusMinusMenu : Bildschirm
    data class LevelWahl(val fach: Fach, val ziel: LevelZiel) : Bildschirm
    data class Zeitspiel(val level: SpielLevel) : Bildschirm
    data class Resultat(val resultat: SpielResultat, val level: SpielLevel, val platzierung: Platzierung) : Bildschirm
    data class Bestenliste(val level: SpielLevel) : Bildschirm
    data object UebenEinmaleins : Bildschirm
    data class UebenLevel(val level: SpielLevel) : Bildschirm
}

private fun fachMenu(fach: Fach): Bildschirm =
    if (fach == Fach.EINMALEINS) Bildschirm.EinmaleinsMenu else Bildschirm.PlusMinusMenu

@Composable
private fun MatheApp() {
    val context = LocalContext.current
    val speicher = remember { Speicher(context.applicationContext) }
    var profile by remember { mutableStateOf(speicher.profile()) }
    var profil by remember { mutableStateOf(speicher.aktuellesProfil()) }
    var bildschirm by remember { mutableStateOf<Bildschirm>(if (profil == null) Bildschirm.Profile else Bildschirm.Start) }
    var reihen by remember { mutableStateOf(speicher.reihenLaden()) }
    // Neue Bestzeiten sollen sofort in den Listen erscheinen.
    var version by remember { mutableIntStateOf(0) }
    // Wird bei jedem Start eines Spiels erhöht, damit es mit frischem Zustand beginnt.
    var durchgang by remember { mutableIntStateOf(0) }

    val name = profil

    fun starte(ziel: Bildschirm) {
        durchgang++
        bildschirm = ziel
    }

    fun waehle(p: String) {
        speicher.setAktuellesProfil(p)
        profil = p
        bildschirm = Bildschirm.Start
    }

    BackHandler(enabled = bildschirm != Bildschirm.Start && !(bildschirm == Bildschirm.Profile && profil == null)) {
        bildschirm = when (val b = bildschirm) {
            Bildschirm.EinmaleinsMenu, Bildschirm.PlusMinusMenu, Bildschirm.Profile -> Bildschirm.Start
            is Bildschirm.LevelWahl -> fachMenu(b.fach)
            is Bildschirm.Zeitspiel -> fachMenu(b.level.fach)
            is Bildschirm.Resultat -> fachMenu(b.level.fach)
            is Bildschirm.Bestenliste -> fachMenu(b.level.fach)
            is Bildschirm.UebenLevel -> fachMenu(b.level.fach)
            else -> Bildschirm.EinmaleinsMenu
        }
    }

    Box(Modifier.safeDrawingPadding()) {
        key(durchgang) {
            when (val b = bildschirm) {
                Bildschirm.Start -> StartScreen(
                    profil = name ?: "",
                    onEinmaleins = { bildschirm = Bildschirm.EinmaleinsMenu },
                    onPlusMinus = { bildschirm = Bildschirm.PlusMinusMenu },
                    onProfil = { bildschirm = Bildschirm.Profile },
                )

                Bildschirm.Profile -> ProfilScreen(
                    profile = profile,
                    aktuell = name,
                    onWaehlen = ::waehle,
                    onNeu = { roh ->
                        speicher.profilHinzufuegen(roh)?.let {
                            profile = speicher.profile()
                            waehle(it)
                        }
                    },
                    onLoeschen = { p ->
                        speicher.profilLoeschen(p)
                        profile = speicher.profile()
                        if (p == profil) profil = speicher.aktuellesProfil()
                        version++
                    },
                    onZurueck = if (name != null) ({ bildschirm = Bildschirm.Start }) else null,
                )

                Bildschirm.EinmaleinsMenu -> EinmaleinsMenuScreen(
                    profil = name ?: "",
                    reihen = reihen,
                    onReihen = { starte(Bildschirm.ReihenAuswahl) },
                    onZeitspiel = { bildschirm = Bildschirm.LevelWahl(Fach.EINMALEINS, LevelZiel.ZEITSPIEL) },
                    onUeben = { starte(Bildschirm.UebenEinmaleins) },
                    onHighscores = { bildschirm = Bildschirm.Bestenliste(EinmalLevel.L1) },
                    onZurueck = { bildschirm = Bildschirm.Start },
                )

                Bildschirm.ReihenAuswahl -> ReihenAuswahlScreen(
                    start = reihen,
                    onFertig = {
                        reihen = it
                        speicher.reihenSpeichern(it)
                        bildschirm = Bildschirm.EinmaleinsMenu
                    },
                    onZurueck = { bildschirm = Bildschirm.EinmaleinsMenu },
                )

                Bildschirm.PlusMinusMenu -> PlusMinusMenuScreen(
                    profil = name ?: "",
                    onZeitspiel = { bildschirm = Bildschirm.LevelWahl(Fach.PLUSMINUS, LevelZiel.ZEITSPIEL) },
                    onUeben = { bildschirm = Bildschirm.LevelWahl(Fach.PLUSMINUS, LevelZiel.UEBEN) },
                    onHighscores = { bildschirm = Bildschirm.Bestenliste(PlusMinusLevel.L1) },
                    onZurueck = { bildschirm = Bildschirm.Start },
                )

                is Bildschirm.LevelWahl -> LevelScreen(
                    levels = levelsVon(b.fach),
                    bestzeit = { l -> name?.let { speicher.bestzeit(it, l) } },
                    onLevel = { l ->
                        starte(if (b.ziel == LevelZiel.ZEITSPIEL) Bildschirm.Zeitspiel(l) else Bildschirm.UebenLevel(l))
                    },
                    onZurueck = { bildschirm = fachMenu(b.fach) },
                )

                is Bildschirm.Zeitspiel -> ZeitspielScreen(
                    level = b.level,
                    onFertig = { resultat ->
                        val platzierung = speicher.eintragen(
                            b.level,
                            Highscore(
                                name ?: "?", b.level.fach, b.level.nummer,
                                resultat.endzeitMs, resultat.richtig, System.currentTimeMillis(),
                            ),
                        )
                        version++
                        bildschirm = Bildschirm.Resultat(resultat, b.level, platzierung)
                    },
                    onAbbrechen = { bildschirm = Bildschirm.LevelWahl(b.level.fach, LevelZiel.ZEITSPIEL) },
                )

                is Bildschirm.Resultat -> ResultatScreen(
                    resultat = b.resultat,
                    level = b.level,
                    platzierung = b.platzierung,
                    onNochmal = { starte(Bildschirm.Zeitspiel(b.level)) },
                    onHighscores = { bildschirm = Bildschirm.Bestenliste(b.level) },
                    onMenu = { bildschirm = fachMenu(b.level.fach) },
                )

                is Bildschirm.Bestenliste -> HighscoreScreen(
                    levels = levelsVon(b.level.fach),
                    start = b.level,
                    aktuellesProfil = name ?: "",
                    bestenliste = { l -> version.let { speicher.bestenliste(l) } },
                    onZurueck = { bildschirm = fachMenu(b.level.fach) },
                )

                Bildschirm.UebenEinmaleins -> UebenScreen(
                    pool = Einmaleins.aufgabenPool(reihen),
                    kopfText = "Reihen: ${Einmaleins.reihenText(reihen)}",
                    onZurueck = { bildschirm = Bildschirm.EinmaleinsMenu },
                )

                is Bildschirm.UebenLevel -> UebenScreen(
                    pool = b.level.aufgabenPool(),
                    kopfText = "Level ${b.level.nummer}: ${b.level.beschreibung}",
                    onZurueck = { bildschirm = Bildschirm.LevelWahl(b.level.fach, LevelZiel.UEBEN) },
                )
            }
        }
    }
}
