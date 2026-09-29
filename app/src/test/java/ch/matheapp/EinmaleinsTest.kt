package ch.matheapp

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class EinmaleinsTest {

    @Test
    fun poolOhneDoppelte() {
        assertEquals(78, Einmaleins.aufgabenPool(Einmaleins.ALLE_REIHEN).size)
        assertEquals(12, Einmaleins.aufgabenPool(setOf(7)).size)
        assertEquals(23, Einmaleins.aufgabenPool(setOf(2, 3)).size)
    }

    @Test
    fun rundeHatZehnVerschiedeneAufgabenImBereich() {
        repeat(100) { seed ->
            val runde = Einmaleins.erzeugeRunde(random = Random(seed))
            assertEquals(10, runde.size)
            assertTrue(runde.all { it.a in 1..12 && it.b in 1..12 })
            assertEquals(10, runde.map { it.normalisiert() }.toSet().size)
        }
    }

    @Test
    fun rundeRespektiertReihen() {
        val runde = Einmaleins.erzeugeRunde(anzahl = 30, reihen = setOf(2), random = Random(1))
        assertEquals(30, runde.size)
        assertTrue(runde.all { it.a == 2 || it.b == 2 })
    }

    @Test
    fun uebungsFolgeKeineDirekteWiederholung() {
        val folge = UebungsFolge(Einmaleins.aufgabenPool(setOf(5)), Random(7))
        var vorher = folge.naechste()
        repeat(200) {
            val neu = folge.naechste()
            assertNotEquals(vorher.normalisiert(), neu.normalisiert())
            vorher = neu
        }
    }

    @Test
    fun endzeitEnthaeltStrafe() {
        assertEquals(22_000L, Spiel.endzeitMs(12_000L, 2))
    }

    @Test
    fun sterne() {
        assertEquals(3, Spiel.sterne(10))
        assertEquals(2, Spiel.sterne(8))
        assertEquals(1, Spiel.sterne(5))
        assertEquals(0, Spiel.sterne(4))
    }

    @Test
    fun texte() {
        assertEquals("12,3 s", Spiel.formatZeit(12_345))
        assertEquals("alle", Einmaleins.reihenText(Einmaleins.ALLE_REIHEN))
        assertEquals("2, 5, 10", Einmaleins.reihenText(setOf(10, 2, 5)))
        assertEquals(setOf(2, 5, 10), Einmaleins.reihenAusText(Einmaleins.reihenAlsText(setOf(10, 2, 5))))
    }

    @Test
    fun einmalLevels() {
        assertEquals(setOf(2, 3, 4, 5), EinmalLevel.L1.reihen)
        assertEquals(8, EinmalLevel.L2.reihen.size)
        assertEquals(11, EinmalLevel.L3.reihen.size)
        assertEquals(setOf(3, 6, 7, 8, 9, 12), EinmalLevel.L4.reihen)
        repeat(50) { seed ->
            val runde = Einmaleins.erzeugeRunde(reihen = EinmalLevel.L4.reihen, random = Random(seed))
            assertTrue(runde.all { it.a in EinmalLevel.L4.reihen || it.b in EinmalLevel.L4.reihen })
        }
    }
}

class PlusMinusTest {
    private fun pool(l: PlusMinusLevel) = PlusMinus.aufgabenPool(l)
    private fun hat(l: PlusMinusLevel, a: Int, b: Int, op: Operator) =
        pool(l).any { it.normalisiert() == Aufgabe(a, b, op).normalisiert() }

    @Test
    fun level1NurAdditionOhneUebertrag() {
        val p = pool(PlusMinusLevel.L1)
        assertTrue(p.all { it.operator == Operator.PLUS && it.ergebnis <= 20 && it.a % 10 + it.b % 10 < 10 })
        assertFalse(hat(PlusMinusLevel.L1, 7, 8, Operator.PLUS))
        assertTrue(hat(PlusMinusLevel.L1, 12, 5, Operator.PLUS))
    }

    @Test
    fun level2AdditionUndSubtraktionOhneUebertrag() {
        assertFalse(hat(PlusMinusLevel.L2, 13, 6, Operator.MINUS))
        assertTrue(hat(PlusMinusLevel.L2, 15, 3, Operator.MINUS))
        assertTrue(hat(PlusMinusLevel.L2, 4, 5, Operator.PLUS))
        assertTrue(pool(PlusMinusLevel.L2).any { it.operator == Operator.MINUS })
    }

    @Test
    fun level3AdditionMitUebertrag() {
        val p = pool(PlusMinusLevel.L3)
        assertTrue(p.all { it.operator == Operator.PLUS && it.ergebnis in 10..20 && it.a % 10 + it.b % 10 >= 10 })
        assertTrue(hat(PlusMinusLevel.L3, 7, 8, Operator.PLUS))
    }

    @Test
    fun level4MitUebertrag() {
        assertTrue(hat(PlusMinusLevel.L4, 13, 6, Operator.MINUS))
        assertFalse(hat(PlusMinusLevel.L4, 15, 3, Operator.MINUS))
        assertTrue(hat(PlusMinusLevel.L4, 9, 6, Operator.PLUS))
    }

    @Test
    fun ergebnisseNieNegativUndImBereich() {
        PlusMinusLevel.entries.forEach { l ->
            assertTrue(pool(l).isNotEmpty())
            assertTrue(pool(l).all { it.ergebnis in 1..20 })
        }
    }

    @Test
    fun rundeHatZehnAufgaben() {
        PlusMinusLevel.entries.forEach { l ->
            assertEquals(10, PlusMinus.erzeugeRunde(l, random = Random(1)).size)
        }
    }

    @Test
    fun subtraktionBehaeltReihenfolge() {
        assertEquals(Aufgabe(13, 6, Operator.MINUS), Aufgabe(13, 6, Operator.MINUS).vertauscht())
        assertEquals("13 − 6", Aufgabe(13, 6, Operator.MINUS).toString())
        assertEquals(7, Aufgabe(13, 6, Operator.MINUS).ergebnis)
    }
}
