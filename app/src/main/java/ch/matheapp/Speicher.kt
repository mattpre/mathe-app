package ch.matheapp

import android.content.Context

data class Highscore(
    val name: String,
    val fach: Fach,
    val levelNummer: Int,
    val endzeitMs: Long,
    val richtig: Int,
    val datum: Long,
)

/** Rang in der Bestenliste des Levels und ob die Zeit eine neue persönliche Bestzeit ist. */
data class Platzierung(val rang: Int, val persoenlicheBestzeit: Boolean)

/** Speichert Profile, Bestenlisten und Reihenauswahl lokal auf dem Gerät. */
class Speicher(context: Context) {
    private val prefs = context.getSharedPreferences("mathe-app", Context.MODE_PRIVATE)

    // --- Profile ---

    fun profile(): List<String> = prefs.getString(KEY_PROFILE, "").orEmpty().split('|').filter { it.isNotBlank() }

    fun aktuellesProfil(): String? = prefs.getString(KEY_AKTUELL, null)?.takeIf { it in profile() }

    fun setAktuellesProfil(name: String) {
        prefs.edit().putString(KEY_AKTUELL, name).apply()
    }

    /** Legt ein Profil an (oder wählt das gleichnamige) und gibt den bereinigten Namen zurück; null bei leerem Namen. */
    fun profilHinzufuegen(roh: String): String? {
        val name = bereinigen(roh)
        if (name.isEmpty()) return null
        val vorhanden = profile().firstOrNull { it.equals(name, ignoreCase = true) }
        if (vorhanden == null) prefs.edit().putString(KEY_PROFILE, (profile() + name).joinToString("|")).apply()
        return vorhanden ?: name
    }

    fun profilLoeschen(name: String) {
        prefs.edit().putString(KEY_PROFILE, (profile() - name).joinToString("|")).apply()
        scoresSpeichern(alleScores().filter { it.name != name })
        if (prefs.getString(KEY_AKTUELL, null) == name) prefs.edit().remove(KEY_AKTUELL).apply()
    }

    // --- Reihenauswahl fürs Einmaleins-Üben ---

    fun reihenLaden(): Set<Int> =
        Einmaleins.reihenAusText(prefs.getString(KEY_REIHEN, null) ?: Einmaleins.reihenAlsText(Einmaleins.ALLE_REIHEN))
            .ifEmpty { Einmaleins.ALLE_REIHEN }

    fun reihenSpeichern(reihen: Set<Int>) {
        prefs.edit().putString(KEY_REIHEN, Einmaleins.reihenAlsText(reihen)).apply()
    }

    // --- Bestenlisten: pro Fach, Level und Profil die Bestzeit ---

    private fun alleScores(): List<Highscore> =
        prefs.getString(KEY_SCORES, "").orEmpty()
            .split('|')
            .filter { it.isNotBlank() }
            .mapNotNull { eintrag ->
                val t = eintrag.split(';')
                if (t.size != 6) return@mapNotNull null
                Highscore(
                    name = t[0],
                    fach = Fach.entries.firstOrNull { it.name == t[1] } ?: return@mapNotNull null,
                    levelNummer = t[2].toIntOrNull() ?: return@mapNotNull null,
                    endzeitMs = t[3].toLongOrNull() ?: return@mapNotNull null,
                    richtig = t[4].toIntOrNull() ?: return@mapNotNull null,
                    datum = t[5].toLongOrNull() ?: return@mapNotNull null,
                )
            }

    private fun scoresSpeichern(liste: List<Highscore>) {
        prefs.edit()
            .putString(
                KEY_SCORES,
                liste.joinToString("|") { "${it.name};${it.fach.name};${it.levelNummer};${it.endzeitMs};${it.richtig};${it.datum}" },
            )
            .apply()
    }

    fun bestenliste(level: SpielLevel): List<Highscore> =
        alleScores().filter { it.fach == level.fach && it.levelNummer == level.nummer }.sortedWith(RANGFOLGE)

    fun bestzeit(name: String, level: SpielLevel): Highscore? = bestenliste(level).firstOrNull { it.name == name }

    /** Trägt das Resultat ein, falls es die bisherige Bestzeit des Profils in diesem Level schlägt. */
    fun eintragen(level: SpielLevel, score: Highscore): Platzierung {
        val alt = bestzeit(score.name, level)
        val besser = alt == null || score.endzeitMs < alt.endzeitMs
        if (besser) {
            scoresSpeichern(
                alleScores().filterNot { it.name == score.name && it.fach == score.fach && it.levelNummer == score.levelNummer } + score
            )
        }
        val rang = bestenliste(level).indexOfFirst { it.name == score.name } + 1
        return Platzierung(rang, besser)
    }

    companion object {
        private const val KEY_PROFILE = "profile"
        private const val KEY_AKTUELL = "aktuellesProfil"
        private const val KEY_REIHEN = "reihen"
        private const val KEY_SCORES = "bestzeiten2"
        const val MAX_NAME = 12
        val RANGFOLGE = compareBy<Highscore>({ it.endzeitMs }, { -it.richtig }, { it.datum })

        fun bereinigen(roh: String): String = roh.filterNot { it == ';' || it == '|' }.trim().take(MAX_NAME).trim()
    }
}
