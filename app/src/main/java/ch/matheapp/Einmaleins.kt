package ch.matheapp

import kotlin.random.Random

enum class Operator(val zeichen: String) { MAL("×"), PLUS("+"), MINUS("−") }

data class Aufgabe(val a: Int, val b: Int, val operator: Operator = Operator.MAL) {
    val ergebnis: Int
        get() = when (operator) {
            Operator.MAL -> a * b
            Operator.PLUS -> a + b
            Operator.MINUS -> a - b
        }

    /** Gleiche Aufgabe unabhängig von der Reihenfolge (3 × 7 = 7 × 3, 3 + 7 = 7 + 3). Bei − zählt die Reihenfolge. */
    fun normalisiert(): Aufgabe =
        if (operator == Operator.MINUS) this else Aufgabe(minOf(a, b), maxOf(a, b), operator)

    fun vertauscht(): Aufgabe = if (operator == Operator.MINUS) this else Aufgabe(b, a, operator)

    override fun toString() = "$a ${operator.zeichen} $b"
}

enum class Fach { EINMALEINS, PLUSMINUS }

/** Eine Schwierigkeitsstufe fürs Spiel auf Zeit. */
interface SpielLevel {
    val fach: Fach
    val nummer: Int
    val beschreibung: String
}

enum class EinmalLevel(override val nummer: Int, val reihen: Set<Int>, override val beschreibung: String) : SpielLevel {
    L1(1, (2..5).toSet(), "2er bis 5er"),
    L2(2, (2..9).toSet(), "2er bis 9er"),
    L3(3, (2..12).toSet(), "2er bis 12er"),
    L4(4, setOf(3, 6, 7, 8, 9, 12), "3er, 6er, 7er, 8er, 9er, 12er");

    override val fach: Fach get() = Fach.EINMALEINS
}

fun SpielLevel.aufgabenPool(): List<Aufgabe> = when (this) {
    is EinmalLevel -> Einmaleins.aufgabenPool(reihen)
    is PlusMinusLevel -> PlusMinus.aufgabenPool(this)
    else -> error("Unbekanntes Level")
}

fun levelsVon(fach: Fach): List<SpielLevel> = when (fach) {
    Fach.EINMALEINS -> EinmalLevel.entries
    Fach.PLUSMINUS -> PlusMinusLevel.entries
}

/** Regeln und Hilfen, die für alle Spiele auf Zeit gelten. */
object Spiel {
    const val FRAGEN_PRO_SPIEL = 10
    const val STRAFE_MS_PRO_FEHLER = 5_000L

    /** [anzahl] zufällige Aufgaben aus [pool], ohne Wiederholung solange der Pool reicht. */
    fun erzeugeRunde(pool: List<Aufgabe>, anzahl: Int = FRAGEN_PRO_SPIEL, random: Random = Random.Default): List<Aufgabe> {
        require(pool.isNotEmpty()) { "Aufgabenpool ist leer" }
        val runde = mutableListOf<Aufgabe>()
        while (runde.size < anzahl) {
            runde += pool.shuffled(random).take(anzahl - runde.size)
        }
        return runde.map { if (random.nextBoolean()) it.vertauscht() else it }
    }

    /** Endzeit fürs Zeitspiel: gemessene Zeit plus Strafzeit pro Fehler. Kleiner ist besser. */
    fun endzeitMs(zeitMs: Long, fehler: Int): Long = zeitMs + fehler * STRAFE_MS_PRO_FEHLER

    /** 0 bis 3 Sterne je nach Anzahl richtiger Antworten. */
    fun sterne(richtig: Int, total: Int = FRAGEN_PRO_SPIEL): Int = when {
        richtig >= total -> 3
        richtig >= total * 8 / 10 -> 2
        richtig >= total / 2 -> 1
        else -> 0
    }

    fun formatZeit(ms: Long): String {
        val zehntel = ms / 100
        return "%d,%d s".format(zehntel / 10, zehntel % 10)
    }
}

object Einmaleins {
    val ALLE_REIHEN: Set<Int> = (1..12).toSet()

    /** Alle Aufgaben (ohne Doppelte wie 3 × 7 / 7 × 3), bei denen ein Faktor aus [reihen] stammt und der andere zwischen 1 und 12 liegt. */
    fun aufgabenPool(reihen: Set<Int>): List<Aufgabe> {
        require(reihen.isNotEmpty() && reihen.all { it in 1..12 }) { "Reihen müssen zwischen 1 und 12 liegen" }
        return reihen.flatMap { r -> (1..12).map { Aufgabe(r, it).normalisiert() } }.distinct()
    }

    fun erzeugeRunde(
        anzahl: Int = Spiel.FRAGEN_PRO_SPIEL,
        reihen: Set<Int> = ALLE_REIHEN,
        random: Random = Random.Default,
    ): List<Aufgabe> = Spiel.erzeugeRunde(aufgabenPool(reihen), anzahl, random)

    /** Kurzer Text für eine Reihenauswahl, z. B. "alle" oder "2, 5, 10". */
    fun reihenText(reihen: Set<Int>): String =
        if (reihen == ALLE_REIHEN) "alle" else reihen.sorted().joinToString(", ")

    fun reihenAlsText(reihen: Set<Int>): String = reihen.sorted().joinToString(",")

    fun reihenAusText(text: String): Set<Int> =
        text.split(',').mapNotNull { it.trim().toIntOrNull() }.filter { it in 1..12 }.toSet()
}

/** Endlose Aufgabenfolge fürs Üben ohne Zeitdruck. */
class UebungsFolge(
    private val pool: List<Aufgabe>,
    private val random: Random = Random.Default,
) {
    private var letzte: Aufgabe? = null

    fun naechste(): Aufgabe {
        var kandidat: Aufgabe
        do {
            kandidat = pool.random(random)
        } while (pool.size > 1 && kandidat == letzte?.normalisiert())
        val neu = if (random.nextBoolean()) kandidat.vertauscht() else kandidat
        letzte = neu
        return neu
    }
}
