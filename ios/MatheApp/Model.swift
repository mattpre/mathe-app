import Foundation

enum Operator: String, Hashable {
    case mal = "×"
    case plus = "+"
    case minus = "−"
}

struct Aufgabe: Hashable {
    let a: Int
    let b: Int
    let op: Operator

    var ergebnis: Int {
        switch op {
        case .mal: return a * b
        case .plus: return a + b
        case .minus: return a - b
        }
    }

    /// Gleiche Aufgabe unabhängig von der Reihenfolge (3 × 7 = 7 × 3, 3 + 7 = 7 + 3). Bei − zählt die Reihenfolge.
    var normalisiert: Aufgabe {
        op == .minus ? self : Aufgabe(a: min(a, b), b: max(a, b), op: op)
    }

    var vertauscht: Aufgabe {
        op == .minus ? self : Aufgabe(a: b, b: a, op: op)
    }

    var text: String { "\(a) \(op.rawValue) \(b)" }
}

enum Fach: String, Codable, Hashable {
    case einmaleins
    case plusminus
}

/// Eine Schwierigkeitsstufe fürs Spiel auf Zeit.
struct Level: Hashable, Identifiable {
    let fach: Fach
    let nummer: Int
    let beschreibung: String
    var reihen: [Int] = []          // Einmaleins
    var mitMinus = false            // Plus & Minus
    var mitUebertrag = false        // Plus & Minus

    var id: String { "\(fach.rawValue)-\(nummer)" }

    var pool: [Aufgabe] {
        switch fach {
        case .einmaleins: return Einmaleins.pool(reihen: reihen)
        case .plusminus: return PlusMinus.pool(mitMinus: mitMinus, mitUebertrag: mitUebertrag)
        }
    }

    static let einmaleins: [Level] = [
        Level(fach: .einmaleins, nummer: 1, beschreibung: "2er bis 5er", reihen: Array(2...5)),
        Level(fach: .einmaleins, nummer: 2, beschreibung: "2er bis 9er", reihen: Array(2...9)),
        Level(fach: .einmaleins, nummer: 3, beschreibung: "2er bis 12er", reihen: Array(2...12)),
        Level(fach: .einmaleins, nummer: 4, beschreibung: "3er, 6er, 7er, 8er, 9er, 12er", reihen: [3, 6, 7, 8, 9, 12]),
    ]

    static let plusminus: [Level] = [
        Level(fach: .plusminus, nummer: 1, beschreibung: "Addition bis 20 ohne Übertrag"),
        Level(fach: .plusminus, nummer: 2, beschreibung: "Addition und Subtraktion bis 20 ohne Übertrag", mitMinus: true),
        Level(fach: .plusminus, nummer: 3, beschreibung: "Addition bis 20 mit Übertrag", mitUebertrag: true),
        Level(fach: .plusminus, nummer: 4, beschreibung: "Addition und Subtraktion bis 20 mit Übertrag", mitMinus: true, mitUebertrag: true),
    ]

    static func alle(_ fach: Fach) -> [Level] {
        fach == .einmaleins ? einmaleins : plusminus
    }
}

enum Einmaleins {
    static let alleReihen = Array(1...12)

    /// Alle Aufgaben (ohne Doppelte wie 3 × 7 / 7 × 3), bei denen ein Faktor aus `reihen` stammt.
    static func pool(reihen: [Int]) -> [Aufgabe] {
        var s = Set<Aufgabe>()
        for r in reihen {
            for k in 1...12 { s.insert(Aufgabe(a: r, b: k, op: .mal).normalisiert) }
        }
        return Array(s)
    }

    static func reihenText(_ reihen: Set<Int>) -> String {
        reihen == Set(alleReihen) ? "alle" : reihen.sorted().map(String.init).joined(separator: ", ")
    }
}

/// Übertrag bei der Addition: die Einer ergeben zusammen 10 oder mehr (7 + 8).
/// Übertrag bei der Subtraktion: man muss borgen, weil die Einerziffer des ersten Werts
/// kleiner ist als die des zweiten (13 − 6).
enum PlusMinus {
    static func pool(mitMinus: Bool, mitUebertrag: Bool) -> [Aufgabe] {
        var s = Set<Aufgabe>()
        for a in 1...19 {
            for b in 1...19 where a + b <= 20 {
                let uebertrag = a % 10 + b % 10 >= 10
                if uebertrag == mitUebertrag { s.insert(Aufgabe(a: a, b: b, op: .plus).normalisiert) }
            }
        }
        if mitMinus {
            for a in 2...20 {
                for b in 1..<a {
                    let uebertrag = a % 10 < b % 10
                    if uebertrag == mitUebertrag { s.insert(Aufgabe(a: a, b: b, op: .minus)) }
                }
            }
        }
        return Array(s)
    }
}

/// Regeln, die für alle Spiele auf Zeit gelten.
enum Spiel {
    static let fragenProSpiel = 10
    static let strafeMsProFehler = 5_000

    /// `anzahl` zufällige Aufgaben aus `pool`, ohne Wiederholung solange der Pool reicht.
    static func runde(pool: [Aufgabe], anzahl: Int = fragenProSpiel) -> [Aufgabe] {
        precondition(!pool.isEmpty, "Aufgabenpool ist leer")
        var r: [Aufgabe] = []
        while r.count < anzahl {
            r.append(contentsOf: pool.shuffled().prefix(anzahl - r.count))
        }
        return r.map { Bool.random() ? $0.vertauscht : $0 }
    }

    static func endzeitMs(zeitMs: Int, fehler: Int) -> Int { zeitMs + fehler * strafeMsProFehler }

    static func sterne(richtig: Int, total: Int = fragenProSpiel) -> Int {
        if richtig >= total { return 3 }
        if richtig >= total * 8 / 10 { return 2 }
        if richtig >= total / 2 { return 1 }
        return 0
    }

    static func formatZeit(_ ms: Int) -> String {
        let zehntel = ms / 100
        return String(format: "%d,%d s", zehntel / 10, zehntel % 10)
    }
}

/// Endlose Aufgabenfolge fürs Üben ohne Zeitdruck (nie zweimal dieselbe Aufgabe hintereinander).
final class UebungsFolge {
    private let pool: [Aufgabe]
    private var letzte: Aufgabe?

    init(pool: [Aufgabe]) { self.pool = pool }

    func naechste() -> Aufgabe {
        var kandidat = pool.randomElement()!
        while pool.count > 1, kandidat == letzte?.normalisiert {
            kandidat = pool.randomElement()!
        }
        let neu = Bool.random() ? kandidat.vertauscht : kandidat
        letzte = neu
        return neu
    }
}

struct SpielResultat: Hashable {
    let zeitMs: Int
    let richtig: Int
    let falsche: [Aufgabe]

    var fehler: Int { falsche.count }
    var endzeitMs: Int { Spiel.endzeitMs(zeitMs: zeitMs, fehler: fehler) }
}

struct Platzierung: Hashable {
    let rang: Int
    let persoenlicheBestzeit: Bool
}
