import SwiftUI

/// Spiel auf Zeit: 10 Aufgaben, die Zeit läuft nur während der Eingabe. Jeder Fehler gibt 5 s Strafzeit.
struct ZeitspielView: View {
    let level: Level
    let onFertig: (SpielResultat) -> Void
    let onAbbrechen: () -> Void

    @State private var runde: [Aufgabe]
    @State private var index = 0
    @State private var eingabe = ""
    @State private var countdown = 3
    @State private var countdownAcc = 0.0
    @State private var feedback: Bool?
    @State private var resultate: [Bool] = []
    @State private var akkMs = 0.0
    @State private var frageStart = Date()
    @State private var anzeigeMs = 0.0
    @State private var aktiv = true

    private let ticker = Timer.publish(every: 0.05, on: .main, in: .common).autoconnect()

    init(level: Level, onFertig: @escaping (SpielResultat) -> Void, onAbbrechen: @escaping () -> Void) {
        self.level = level
        self.onFertig = onFertig
        self.onAbbrechen = onAbbrechen
        _runde = State(initialValue: Spiel.runde(pool: level.pool))
    }

    private var aufgabe: Aufgabe { runde[index] }
    private var fehler: Int { resultate.filter { !$0 }.count }

    var body: some View {
        VStack(spacing: 6) {
            HStack {
                Button("✕ Abbrechen") { aktiv = false; onAbbrechen() }
                Spacer()
                Text("⏱ " + Spiel.formatZeit(Int(anzeigeMs))).font(.system(size: 26, weight: .bold))
            }
            Text(fehler > 0 ? "+\(fehler * Spiel.strafeMsProFehler / 1000) s Strafzeit" : " ")
                .foregroundColor(Theme.rot)
            Fortschritt(resultate: resultate, aktuell: index, total: runde.count).padding(.top, 8)
            Text("Frage \(index + 1) von \(runde.count)")
            Spacer()
            if countdown > 0 {
                VStack {
                    Text("Bereit?").font(.system(size: 26, weight: .bold))
                    Text("\(countdown)").font(.system(size: 120, weight: .heavy)).foregroundColor(Theme.lila)
                }
            } else {
                AufgabenAnzeige(aufgabe: aufgabe.text, eingabe: eingabe, korrekt: feedback, loesung: aufgabe.ergebnis)
            }
            Spacer()
            Zahlenfeld(
                aktiv: countdown == 0 && feedback == nil,
                onZiffer: { eingabeZiffer(&eingabe, $0) },
                onLoeschen: { eingabe = String(eingabe.dropLast()) },
                onOk: pruefen
            )
        }
        .foregroundColor(Theme.text)
        .padding(16)
        .background(Theme.hintergrund.ignoresSafeArea())
        .onReceive(ticker) { _ in tick() }
        .onDisappear { aktiv = false }
    }

    private func tick() {
        if countdown > 0 {
            countdownAcc += 0.05
            if countdownAcc >= 0.8 {
                countdownAcc = 0
                countdown -= 1
                if countdown == 0 { frageStart = Date() }
            }
        } else if feedback == nil {
            anzeigeMs = akkMs + Date().timeIntervalSince(frageStart) * 1000
        }
    }

    private func pruefen() {
        guard !eingabe.isEmpty, feedback == nil, countdown == 0, let wert = Int(eingabe) else { return }
        akkMs += Date().timeIntervalSince(frageStart) * 1000
        anzeigeMs = akkMs
        let ok = wert == aufgabe.ergebnis
        resultate.append(ok)
        feedback = ok
        DispatchQueue.main.asyncAfter(deadline: .now() + (ok ? 0.6 : 1.6)) { weiter() }
    }

    private func weiter() {
        guard aktiv else { return }
        if index == runde.count - 1 {
            aktiv = false
            let falsche = runde.enumerated().filter { !resultate[$0.offset] }.map { $0.element }
            onFertig(SpielResultat(zeitMs: Int(akkMs), richtig: resultate.filter { $0 }.count, falsche: falsche))
        } else {
            index += 1
            eingabe = ""
            feedback = nil
            frageStart = Date()
        }
    }
}

struct ResultatView: View {
    let level: Level
    let resultat: SpielResultat
    let platzierung: Platzierung
    let onNochmal: () -> Void
    let onBestenliste: () -> Void
    let onMenue: () -> Void

    var body: some View {
        let sterne = Spiel.sterne(richtig: resultat.richtig)
        VStack(spacing: 10) {
            ScrollView {
                VStack(spacing: 10) {
                    Text(["Weiter üben! 💪", "Gut gemacht! 👍", "Sehr gut! 🎉", "Perfekt! 🏆"][sterne])
                        .font(.system(size: 34, weight: .heavy)).foregroundColor(Theme.lila)
                    Text(String(repeating: "⭐", count: sterne) + String(repeating: "☆", count: 3 - sterne))
                        .font(.system(size: 48))
                    Text("\(resultat.richtig) von \(Spiel.fragenProSpiel) richtig").font(.title2.bold())
                    Text("Zeit: \(Spiel.formatZeit(resultat.zeitMs))").font(.title3)
                    if resultat.fehler > 0 {
                        Text("Strafzeit: +\(resultat.fehler * Spiel.strafeMsProFehler / 1000) s")
                            .font(.title3).foregroundColor(Theme.rot)
                    }
                    Text("Endzeit: \(Spiel.formatZeit(resultat.endzeitMs))").font(.system(size: 26, weight: .bold))
                    Text("Level \(level.nummer)").font(.title2.bold()).foregroundColor(Theme.tuerkis)
                    Text(level.beschreibung).font(.system(size: 15)).multilineTextAlignment(.center)
                    if platzierung.persoenlicheBestzeit {
                        Text("🎖️ Neue persönliche Bestzeit!").font(.title2.bold()).foregroundColor(Theme.orange)
                    }
                    Text(platzierung.rang == 1 ? "🥇 Platz 1 in der Bestenliste!" : "Platz \(platzierung.rang) in der Bestenliste")
                        .font(.title2.bold()).foregroundColor(Theme.tuerkis)
                    if !resultat.falsche.isEmpty {
                        Text("Das üben wir noch:").font(.title2.bold()).padding(.top, 8)
                        ForEach(Array(resultat.falsche.enumerated()), id: \.offset) { _, a in
                            Text("\(a.text) = \(a.ergebnis)").font(.system(size: 22))
                        }
                    }
                }
            }
            Button(action: onNochmal) { GrosserKnopf(text: "Nochmal", emoji: "🔁", farbe: Theme.lila) }
            HStack(spacing: 24) {
                Button("🏆 Bestenliste", action: onBestenliste)
                Button("🏠 Menü", action: onMenue)
            }
            .font(.system(size: 18))
        }
        .foregroundColor(Theme.text)
        .padding(24)
        .background(Theme.hintergrund.ignoresSafeArea())
    }
}

/// Üben ohne Zeitdruck: endlos Aufgaben aus `pool`, mit Serien-Zähler.
struct UebenView: View {
    let pool: [Aufgabe]
    let kopfText: String

    @Environment(\.dismiss) private var dismiss
    @State private var folge: UebungsFolge
    @State private var aufgabe: Aufgabe
    @State private var eingabe = ""
    @State private var feedback: Bool?
    @State private var richtig = 0
    @State private var total = 0
    @State private var serie = 0
    @State private var besteSerie = 0
    @State private var aktiv = true

    init(pool: [Aufgabe], kopfText: String) {
        self.pool = pool
        self.kopfText = kopfText
        let f = UebungsFolge(pool: pool)
        _folge = State(initialValue: f)
        _aufgabe = State(initialValue: f.naechste())
    }

    var body: some View {
        VStack(spacing: 6) {
            HStack {
                Button("← Fertig") { aktiv = false; dismiss() }
                Spacer()
                Text("✅ \(richtig) / \(total)").font(.title2.bold())
            }
            Text(kopfText).multilineTextAlignment(.center)
            Text(serie > 0 ? "🔥 \(serie) in Folge" : " ")
                .font(.system(size: 26, weight: .bold)).foregroundColor(Theme.orange).padding(.top, 8)
            Text(besteSerie > 0 ? "Beste Serie: \(besteSerie)" : " ")
            Spacer()
            AufgabenAnzeige(aufgabe: aufgabe.text, eingabe: eingabe, korrekt: feedback, loesung: aufgabe.ergebnis)
            Spacer()
            Zahlenfeld(
                aktiv: feedback == nil,
                onZiffer: { eingabeZiffer(&eingabe, $0) },
                onLoeschen: { eingabe = String(eingabe.dropLast()) },
                onOk: pruefen
            )
        }
        .foregroundColor(Theme.text)
        .padding(16)
        .background(Theme.hintergrund.ignoresSafeArea())
        .navigationBarBackButtonHidden(true)
        .onDisappear { aktiv = false }
    }

    private func pruefen() {
        guard !eingabe.isEmpty, feedback == nil, let wert = Int(eingabe) else { return }
        let ok = wert == aufgabe.ergebnis
        total += 1
        if ok {
            richtig += 1
            serie += 1
            besteSerie = max(besteSerie, serie)
        } else {
            serie = 0
        }
        feedback = ok
        DispatchQueue.main.asyncAfter(deadline: .now() + (ok ? 0.7 : 2.0)) {
            guard aktiv else { return }
            aufgabe = folge.naechste()
            eingabe = ""
            feedback = nil
        }
    }
}
