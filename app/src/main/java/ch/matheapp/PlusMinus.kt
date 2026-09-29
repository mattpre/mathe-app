package ch.matheapp

import kotlin.random.Random

/** Stufen für Plus & Minus im Zahlenraum bis 20. */
enum class PlusMinusLevel(
    override val nummer: Int,
    override val beschreibung: String,
    val operatoren: Set<Operator>,
    val mitUebertrag: Boolean,
) : SpielLevel {
    L1(1, "Addition bis 20 ohne Übertrag", setOf(Operator.PLUS), false),
    L2(2, "Addition und Subtraktion bis 20 ohne Übertrag", setOf(Operator.PLUS, Operator.MINUS), false),
    L3(3, "Addition bis 20 mit Übertrag", setOf(Operator.PLUS), true),
    L4(4, "Addition und Subtraktion bis 20 mit Übertrag", setOf(Operator.PLUS, Operator.MINUS), true);

    override val fach: Fach get() = Fach.PLUSMINUS
}

/**
 * Übertrag bei der Addition: die Einer ergeben zusammen 10 oder mehr (7 + 8).
 * Übertrag bei der Subtraktion: man muss von den Zehnern borgen, weil die Einerziffer des
 * ersten Werts kleiner ist als die des zweiten (13 − 6).
 */
object PlusMinus {
    fun aufgabenPool(level: PlusMinusLevel): List<Aufgabe> {
        val aufgaben = mutableListOf<Aufgabe>()
        if (Operator.PLUS in level.operatoren) {
            for (a in 1..19) for (b in 1..19) {
                if (a + b > 20) continue
                val uebertrag = a % 10 + b % 10 >= 10
                if (uebertrag == level.mitUebertrag) aufgaben += Aufgabe(a, b, Operator.PLUS)
            }
        }
        if (Operator.MINUS in level.operatoren) {
            for (a in 2..20) for (b in 1 until a) {
                val uebertrag = a % 10 < b % 10
                if (uebertrag == level.mitUebertrag) aufgaben += Aufgabe(a, b, Operator.MINUS)
            }
        }
        return aufgaben.map { it.normalisiert() }.distinct()
    }

    fun erzeugeRunde(level: PlusMinusLevel, anzahl: Int = Spiel.FRAGEN_PRO_SPIEL, random: Random = Random.Default): List<Aufgabe> =
        Spiel.erzeugeRunde(aufgabenPool(level), anzahl, random)
}
