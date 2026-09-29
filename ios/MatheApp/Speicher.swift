import Foundation

struct Highscore: Codable, Hashable {
    var name: String
    var fach: Fach
    var levelNummer: Int
    var endzeitMs: Int
    var richtig: Int
    var datum: Date
}

/// Speichert Profile, Bestenlisten und Reihenauswahl lokal auf dem Gerät.
final class Speicher: ObservableObject {
    static let maxName = 12
    private let d = UserDefaults.standard

    @Published var profile: [String] {
        didSet { d.set(profile, forKey: "profile") }
    }
    @Published var aktuell: String? {
        didSet { d.set(aktuell, forKey: "aktuell") }
    }
    @Published var reihen: Set<Int> {
        didSet { d.set(reihen.sorted(), forKey: "reihen") }
    }
    @Published private(set) var scores: [Highscore] {
        didSet {
            if let data = try? JSONEncoder().encode(scores) { d.set(data, forKey: "bestzeiten") }
        }
    }

    init() {
        let p = d.stringArray(forKey: "profile") ?? []
        profile = p
        let a = d.string(forKey: "aktuell")
        aktuell = a.flatMap { p.contains($0) ? $0 : nil }
        let r = (d.array(forKey: "reihen") as? [Int]) ?? []
        reihen = r.isEmpty ? Set(Einmaleins.alleReihen) : Set(r)
        if let data = d.data(forKey: "bestzeiten"), let s = try? JSONDecoder().decode([Highscore].self, from: data) {
            scores = s
        } else {
            scores = []
        }
    }

    static func bereinigen(_ roh: String) -> String {
        String(roh.trimmingCharacters(in: .whitespacesAndNewlines).prefix(maxName))
            .trimmingCharacters(in: .whitespaces)
    }

    // MARK: Profile

    func profilHinzufuegen(_ roh: String) {
        let name = Speicher.bereinigen(roh)
        guard !name.isEmpty else { return }
        if let vorhanden = profile.first(where: { $0.caseInsensitiveCompare(name) == .orderedSame }) {
            aktuell = vorhanden
        } else {
            profile.append(name)
            aktuell = name
        }
    }

    func profilLoeschen(_ name: String) {
        profile.removeAll { $0 == name }
        scores.removeAll { $0.name == name }
        if aktuell == name { aktuell = nil }
    }

    // MARK: Bestenlisten (pro Fach, Level und Profil die Bestzeit)

    func bestenliste(_ level: Level) -> [Highscore] {
        scores
            .filter { $0.fach == level.fach && $0.levelNummer == level.nummer }
            .sorted { ($0.endzeitMs, -$0.richtig, $0.datum) < ($1.endzeitMs, -$1.richtig, $1.datum) }
    }

    func bestzeit(_ name: String, _ level: Level) -> Highscore? {
        bestenliste(level).first { $0.name == name }
    }

    /// Trägt das Resultat ein, falls es die bisherige Bestzeit des Profils in diesem Level schlägt.
    func eintragen(_ level: Level, name: String, endzeitMs: Int, richtig: Int) -> Platzierung {
        let alt = bestzeit(name, level)
        let besser = alt == nil || endzeitMs < alt!.endzeitMs
        if besser {
            scores.removeAll { $0.name == name && $0.fach == level.fach && $0.levelNummer == level.nummer }
            scores.append(Highscore(name: name, fach: level.fach, levelNummer: level.nummer,
                                    endzeitMs: endzeitMs, richtig: richtig, datum: Date()))
        }
        let rang = (bestenliste(level).firstIndex { $0.name == name } ?? 0) + 1
        return Platzierung(rang: rang, persoenlicheBestzeit: besser)
    }
}
