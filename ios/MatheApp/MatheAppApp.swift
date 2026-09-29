import SwiftUI

@main
struct MatheAppApp: App {
    @StateObject private var speicher = Speicher()

    var body: some Scene {
        WindowGroup {
            RootView()
                .environmentObject(speicher)
        }
    }
}

enum Ziel: Hashable {
    case zeitspiel
    case ueben
}

enum Route: Hashable {
    case fach(Fach)
    case reihenAuswahl
    case levelWahl(Fach, Ziel)
    case zeitspiel(Level)
    case resultat(Level, SpielResultat, Platzierung)
    case bestenliste(Level)
    case uebenReihen
    case uebenLevel(Level)
}

struct RootView: View {
    @EnvironmentObject var speicher: Speicher
    @State private var path: [Route] = []
    @State private var zeigeProfile = false

    var body: some View {
        NavigationStack(path: $path) {
            StartView(zeigeProfile: $zeigeProfile)
                .navigationDestination(for: Route.self) { route in
                    ziel(route)
                }
        }
        .tint(Theme.lila)
        .fullScreenCover(isPresented: Binding(
            get: { speicher.aktuell == nil || zeigeProfile },
            set: { if !$0 { zeigeProfile = false } }
        )) {
            ProfilView(zeigeProfile: $zeigeProfile)
                .environmentObject(speicher)
        }
    }

    @ViewBuilder
    private func ziel(_ route: Route) -> some View {
        switch route {
        case .fach(let fach):
            FachMenuView(fach: fach)
        case .reihenAuswahl:
            ReihenAuswahlView()
        case .levelWahl(let fach, let z):
            LevelWahlView(fach: fach, ziel: z)
        case .zeitspiel(let level):
            ZeitspielView(
                level: level,
                onFertig: { resultat in
                    let p = speicher.eintragen(level, name: speicher.aktuell ?? "?",
                                               endzeitMs: resultat.endzeitMs, richtig: resultat.richtig)
                    path.removeLast()
                    path.append(.resultat(level, resultat, p))
                },
                onAbbrechen: { path.removeLast() }
            )
            .navigationBarBackButtonHidden(true)
        case .resultat(let level, let resultat, let platzierung):
            ResultatView(
                level: level, resultat: resultat, platzierung: platzierung,
                onNochmal: { path.removeLast(); path.append(.zeitspiel(level)) },
                onBestenliste: { path.append(.bestenliste(level)) },
                onMenue: { path = [.fach(level.fach)] }
            )
            .navigationBarBackButtonHidden(true)
        case .bestenliste(let level):
            BestenlisteView(start: level)
        case .uebenReihen:
            UebenView(pool: Einmaleins.pool(reihen: speicher.reihen.sorted()),
                      kopfText: "Reihen: \(Einmaleins.reihenText(speicher.reihen))")
        case .uebenLevel(let level):
            UebenView(pool: level.pool, kopfText: "Level \(level.nummer): \(level.beschreibung)")
        }
    }
}
