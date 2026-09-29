import SwiftUI

struct StartView: View {
    @EnvironmentObject var speicher: Speicher
    @Binding var zeigeProfile: Bool

    var body: some View {
        VStack(spacing: 12) {
            Spacer()
            Text("🧮").font(.system(size: 96))
            Text("Mathe-App").font(.system(size: 52, weight: .heavy)).foregroundColor(Theme.lila)
            Text("Rechnen macht Spass!").font(.title2.bold()).foregroundColor(Theme.orange)
            Button {
                zeigeProfile = true
            } label: {
                Text("👤 \(speicher.aktuell ?? "")  ·  wechseln")
                    .font(.system(size: 18))
                    .padding(.horizontal, 20).padding(.vertical, 10)
                    .overlay(RoundedRectangle(cornerRadius: 20).stroke(Theme.lila, lineWidth: 2))
            }
            .padding(.top, 20)
            NavigationLink(value: Route.fach(.einmaleins)) {
                GrosserKnopf(text: "Einmaleins", emoji: "✖️", farbe: Theme.lila)
            }
            .padding(.top, 12)
            NavigationLink(value: Route.fach(.plusminus)) {
                GrosserKnopf(text: "Plus & Minus", emoji: "➕", farbe: Theme.tuerkis)
            }
            Spacer()
        }
        .padding(24)
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(Theme.hintergrund.ignoresSafeArea())
        .navigationBarHidden(true)
    }
}

/// Profil wählen, neues anlegen oder löschen.
struct ProfilView: View {
    @EnvironmentObject var speicher: Speicher
    @Binding var zeigeProfile: Bool
    @State private var name = ""
    @State private var loeschen: String?

    var body: some View {
        VStack(spacing: 12) {
            HStack {
                if speicher.aktuell != nil {
                    Button("← Zurück") { zeigeProfile = false }
                }
                Spacer()
            }
            Text("👤 Wer spielt?").font(.system(size: 34, weight: .heavy)).foregroundColor(Theme.lila)
            ScrollView {
                VStack(spacing: 8) {
                    ForEach(speicher.profile, id: \.self) { p in
                        HStack {
                            Text(p).font(.title2.bold()).frame(maxWidth: .infinity, alignment: .leading)
                            Button("🗑️") { loeschen = p }.font(.title2)
                        }
                        .padding(.horizontal, 20).padding(.vertical, 10)
                        .background(p == speicher.aktuell ? Theme.gelb : Color.white)
                        .cornerRadius(16)
                        .contentShape(Rectangle())
                        .onTapGesture {
                            speicher.aktuell = p
                            zeigeProfile = false
                        }
                    }
                }
            }
            Text(speicher.profile.isEmpty ? "Wie heisst du?" : "Neues Profil").font(.title2.bold())
            HStack {
                TextField("Name", text: $name)
                    .textFieldStyle(.roundedBorder)
                    .submitLabel(.done)
                    .onSubmit(anlegen)
                    .onChange(of: name) { name = String($0.prefix(Speicher.maxName)) }
                Button(action: anlegen) {
                    Text("＋").font(.system(size: 24, weight: .bold))
                        .frame(width: 56, height: 40)
                        .background(Theme.gruen).foregroundColor(.white).cornerRadius(10)
                }
                .disabled(Speicher.bereinigen(name).isEmpty)
            }
        }
        .foregroundColor(Theme.text)
        .padding(16)
        .background(Theme.hintergrund.ignoresSafeArea())
        .alert("Profil «\(loeschen ?? "")» löschen?", isPresented: Binding(get: { loeschen != nil }, set: { if !$0 { loeschen = nil } })) {
            Button("Löschen", role: .destructive) { if let p = loeschen { speicher.profilLoeschen(p) }; loeschen = nil }
            Button("Abbrechen", role: .cancel) { loeschen = nil }
        } message: {
            Text("Die Bestzeiten von \(loeschen ?? "") werden gelöscht.")
        }
    }

    private func anlegen() {
        guard !Speicher.bereinigen(name).isEmpty else { return }
        speicher.profilHinzufuegen(name)
        name = ""
        zeigeProfile = false
    }
}

struct FachMenuView: View {
    @EnvironmentObject var speicher: Speicher
    let fach: Fach

    var body: some View {
        ScrollView {
            VStack(spacing: 14) {
                Text(fach == .einmaleins ? "Einmaleins" : "Plus & Minus")
                    .font(.system(size: 34, weight: .heavy)).foregroundColor(Theme.lila)
                if fach == .plusminus {
                    Text("Zahlenraum bis 20").font(.title2.bold()).foregroundColor(Theme.orange)
                }
                Text("👤 \(speicher.aktuell ?? "")").font(.title3.bold()).foregroundColor(Theme.lila)
                Spacer().frame(height: 8)
                NavigationLink(value: Route.levelWahl(fach, .zeitspiel)) {
                    GrosserKnopf(text: "Spiel auf Zeit", emoji: "⏱️", farbe: Theme.orange)
                }
                if fach == .einmaleins {
                    NavigationLink(value: Route.uebenReihen) {
                        GrosserKnopf(text: "Üben", emoji: "🎯", farbe: Theme.tuerkis)
                    }
                    NavigationLink(value: Route.reihenAuswahl) {
                        Text("Reihen: \(Einmaleins.reihenText(speicher.reihen))  ✏️")
                            .font(.system(size: 16))
                            .padding(.horizontal, 16).padding(.vertical, 8)
                            .overlay(RoundedRectangle(cornerRadius: 20).stroke(Theme.tuerkis, lineWidth: 2))
                            .foregroundColor(Theme.tuerkis)
                    }
                } else {
                    NavigationLink(value: Route.levelWahl(fach, .ueben)) {
                        GrosserKnopf(text: "Üben", emoji: "🎯", farbe: Theme.tuerkis)
                    }
                }
                NavigationLink(value: Route.bestenliste(Level.alle(fach)[0])) {
                    GrosserKnopf(text: "Bestenliste", emoji: "🏆", farbe: Theme.gruen)
                }
                Text("Spiel auf Zeit: \(Spiel.fragenProSpiel) Aufgaben so schnell wie möglich.\nJeder Fehler gibt \(Spiel.strafeMsProFehler / 1000) Sekunden Strafzeit.")
                    .font(.system(size: 15)).multilineTextAlignment(.center).padding(.top, 10)
            }
            .foregroundColor(Theme.text)
            .padding(24)
        }
        .background(Theme.hintergrund.ignoresSafeArea())
    }
}

/// Reihen an- und abwählen (fürs Einmaleins-Üben).
struct ReihenAuswahlView: View {
    @EnvironmentObject var speicher: Speicher
    @Environment(\.dismiss) private var dismiss
    @State private var auswahl: Set<Int> = []

    var body: some View {
        VStack(spacing: 12) {
            Text("Welche Reihen?").font(.system(size: 34, weight: .heavy)).foregroundColor(Theme.lila)
            Text("Tippe auf die Reihen, die du üben willst.").font(.title3)
            LazyVGrid(columns: Array(repeating: GridItem(.flexible(), spacing: 10), count: 3), spacing: 10) {
                ForEach(1...12, id: \.self) { r in
                    let an = auswahl.contains(r)
                    Button {
                        if an { auswahl.remove(r) } else { auswahl.insert(r) }
                    } label: {
                        Text("\(r)×").font(.system(size: 24, weight: .bold))
                            .frame(maxWidth: .infinity, minHeight: 64)
                            .background(an ? Theme.lila : Color.white)
                            .foregroundColor(an ? .white : Theme.lila)
                            .cornerRadius(16)
                            .overlay(RoundedRectangle(cornerRadius: 16).stroke(Theme.lila, lineWidth: 2))
                    }
                }
            }
            .padding(.top, 12)
            HStack(spacing: 12) {
                Button("Alle") { auswahl = Set(Einmaleins.alleReihen) }.buttonStyle(.bordered)
                Button("Keine") { auswahl = [] }.buttonStyle(.bordered)
            }
            Spacer()
            if auswahl.isEmpty { Text("Wähle mindestens eine Reihe.").foregroundColor(Theme.rot) }
            Button {
                guard !auswahl.isEmpty else { return }
                speicher.reihen = auswahl
                dismiss()
            } label: {
                GrosserKnopf(text: "Fertig", emoji: "✅", farbe: auswahl.isEmpty ? Theme.lila.opacity(0.4) : Theme.gruen)
            }
        }
        .foregroundColor(Theme.text)
        .padding(16)
        .background(Theme.hintergrund.ignoresSafeArea())
        .onAppear { auswahl = speicher.reihen }
    }
}

/// Level wählen (Spiel auf Zeit oder Üben); zeigt die eigene Bestzeit pro Level.
struct LevelWahlView: View {
    @EnvironmentObject var speicher: Speicher
    let fach: Fach
    let ziel: Ziel

    var body: some View {
        ScrollView {
            VStack(spacing: 12) {
                Text("Welches Level?").font(.system(size: 34, weight: .heavy)).foregroundColor(Theme.lila)
                ForEach(Level.alle(fach)) { level in
                    NavigationLink(value: ziel == .zeitspiel ? Route.zeitspiel(level) : Route.uebenLevel(level)) {
                        HStack {
                            VStack(alignment: .leading) {
                                Text("Level \(level.nummer)").font(.system(size: 26, weight: .heavy))
                                Text(level.beschreibung).font(.system(size: 15)).multilineTextAlignment(.leading)
                            }
                            Spacer()
                            if ziel == .zeitspiel {
                                Text(speicher.aktuell.flatMap { speicher.bestzeit($0, level) }
                                    .map { "🏆 " + Spiel.formatZeit($0.endzeitMs) } ?? "noch keine\nBestzeit")
                                    .font(.system(size: 16)).multilineTextAlignment(.trailing)
                            }
                        }
                        .foregroundColor(.white)
                        .padding(.horizontal, 20).padding(.vertical, 14)
                        .frame(maxWidth: .infinity)
                        .background(Theme.levelFarbe(level.nummer))
                        .cornerRadius(24)
                    }
                }
            }
            .foregroundColor(Theme.text)
            .padding(16)
        }
        .background(Theme.hintergrund.ignoresSafeArea())
    }
}

struct BestenlisteView: View {
    @EnvironmentObject var speicher: Speicher
    let start: Level
    @State private var level: Level

    init(start: Level) {
        self.start = start
        _level = State(initialValue: start)
    }

    var body: some View {
        let liste = speicher.bestenliste(level)
        VStack(spacing: 12) {
            Text("🏆 Bestenliste").font(.system(size: 34, weight: .heavy)).foregroundColor(Theme.lila)
            Picker("Level", selection: $level) {
                ForEach(Level.alle(start.fach)) { l in Text("L\(l.nummer)").tag(l) }
            }
            .pickerStyle(.segmented)
            Text("Level \(level.nummer): \(level.beschreibung)")
                .font(.title3.bold()).foregroundColor(Theme.levelFarbe(level.nummer)).multilineTextAlignment(.center)
            if liste.isEmpty {
                Text("Noch keine Resultate.\nSpiel eine Runde!").font(.title3.bold()).multilineTextAlignment(.center).padding(.top, 32)
            }
            ScrollView {
                VStack(spacing: 8) {
                    ForEach(Array(liste.enumerated()), id: \.offset) { i, s in
                        HStack {
                            Text(["🥇", "🥈", "🥉"].indices.contains(i) ? ["🥇", "🥈", "🥉"][i] : "\(i + 1).")
                                .font(.system(size: 26)).frame(width: 48)
                            VStack(alignment: .leading) {
                                Text(s.name).font(.title3.bold())
                                Text("\(s.datum.formatted(date: .numeric, time: .omitted)) · \(s.richtig)/\(Spiel.fragenProSpiel) richtig")
                                    .font(.system(size: 13))
                            }
                            Spacer()
                            Text(Spiel.formatZeit(s.endzeitMs)).font(.title3.bold())
                        }
                        .padding(.horizontal, 16).padding(.vertical, 12)
                        .background(s.name == speicher.aktuell ? Theme.gelb : Color.white)
                        .cornerRadius(16)
                    }
                }
            }
        }
        .foregroundColor(Theme.text)
        .padding(16)
        .background(Theme.hintergrund.ignoresSafeArea())
    }
}
