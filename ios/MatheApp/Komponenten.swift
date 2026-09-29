import SwiftUI

extension Color {
    init(hex: UInt32) {
        self.init(red: Double((hex >> 16) & 0xFF) / 255, green: Double((hex >> 8) & 0xFF) / 255, blue: Double(hex & 0xFF) / 255)
    }
}

enum Theme {
    static let lila = Color(hex: 0x7B61FF)
    static let lilaHell = Color(hex: 0xE9E4FF)
    static let gelb = Color(hex: 0xFFC83D)
    static let gruen = Color(hex: 0x2FB463)
    static let gruenHell = Color(hex: 0xD9F5E3)
    static let rot = Color(hex: 0xFF5C5C)
    static let rotHell = Color(hex: 0xFFE0E0)
    static let orange = Color(hex: 0xFF9F43)
    static let tuerkis = Color(hex: 0x1FB5C9)
    static let hintergrund = Color(hex: 0xFFF8E7)
    static let text = Color(hex: 0x2D2A4A)

    static func levelFarbe(_ nummer: Int) -> Color {
        [gruen, tuerkis, orange, rot][max(0, min(3, nummer - 1))]
    }
}

/// Grosser, farbiger Knopf für Menüs (auch als Label für NavigationLink).
struct GrosserKnopf: View {
    let text: String
    let emoji: String
    let farbe: Color

    var body: some View {
        HStack(spacing: 16) {
            Text(emoji).font(.system(size: 32))
            Text(text).font(.system(size: 24, weight: .bold))
        }
        .foregroundColor(.white)
        .frame(maxWidth: .infinity, minHeight: 84)
        .background(farbe)
        .cornerRadius(24)
        .shadow(color: .black.opacity(0.2), radius: 4, y: 3)
    }
}

/// Zahlenfeld mit 0–9, Löschen und OK.
struct Zahlenfeld: View {
    let aktiv: Bool
    let onZiffer: (Int) -> Void
    let onLoeschen: () -> Void
    let onOk: () -> Void

    var body: some View {
        VStack(spacing: 10) {
            ForEach([[1, 2, 3], [4, 5, 6], [7, 8, 9]], id: \.self) { reihe in
                HStack(spacing: 10) {
                    ForEach(reihe, id: \.self) { z in
                        taste("\(z)", .white, Theme.text) { onZiffer(z) }
                    }
                }
            }
            HStack(spacing: 10) {
                taste("⌫", Theme.rotHell, Theme.rot, onLoeschen)
                taste("0", .white, Theme.text) { onZiffer(0) }
                taste("OK", Theme.gruen, .white, onOk)
            }
        }
    }

    private func taste(_ t: String, _ bg: Color, _ fg: Color, _ aktion: @escaping () -> Void) -> some View {
        Button(action: aktion) {
            Text(t)
                .font(.system(size: 28, weight: .bold))
                .frame(maxWidth: .infinity, minHeight: 64)
                .background(bg)
                .foregroundColor(fg)
                .cornerRadius(18)
                .shadow(color: .black.opacity(0.15), radius: 2, y: 2)
        }
        .disabled(!aktiv)
        .opacity(aktiv ? 1 : 0.5)
    }
}

/// Ziffer an die Eingabe hängen (max. 3 Stellen, keine führende 0).
func eingabeZiffer(_ eingabe: inout String, _ z: Int) {
    if eingabe.count < 3 && !(eingabe.isEmpty && z == 0) { eingabe += String(z) }
}

/// Die Aufgabe mit Eingabefeld, eingefärbt je nach Rückmeldung (nil = noch offen).
struct AufgabenAnzeige: View {
    let aufgabe: String
    let eingabe: String
    let korrekt: Bool?
    let loesung: Int

    private static let lob = ["Super! 🎉", "Richtig! ⭐", "Toll! 👍", "Genau! 🚀", "Klasse! 🌟"]

    var body: some View {
        let rand: Color = korrekt == true ? Theme.gruen : (korrekt == false ? Theme.rot : Theme.lilaHell)
        let hinter: Color = korrekt == true ? Theme.gruenHell : (korrekt == false ? Theme.rotHell : .white)
        VStack(spacing: 8) {
            HStack {
                Text("\(aufgabe) =").font(.system(size: 44, weight: .heavy))
                Text(eingabe.isEmpty ? "?" : eingabe)
                    .font(.system(size: 44, weight: .heavy))
                    .foregroundColor(eingabe.isEmpty ? Theme.lila.opacity(0.4) : Theme.text)
                    .frame(width: 110)
                    .background(Theme.lilaHell)
                    .cornerRadius(12)
            }
            Text(meldung)
                .font(.system(size: 22, weight: .bold))
                .foregroundColor(korrekt == false ? Theme.rot : Theme.gruen)
        }
        .foregroundColor(Theme.text)
        .frame(maxWidth: .infinity)
        .padding(.vertical, 20)
        .background(hinter)
        .cornerRadius(24)
        .overlay(RoundedRectangle(cornerRadius: 24).stroke(rand, lineWidth: 4))
    }

    private var meldung: String {
        switch korrekt {
        case .some(true): return Self.lob[(loesung + aufgabe.count) % Self.lob.count]
        case .some(false): return "Fast! Richtig ist \(loesung)"
        case .none: return " "
        }
    }
}

/// Zehn Punkte für den Fortschritt: grün = richtig, rot = falsch, lila = aktuell.
struct Fortschritt: View {
    let resultate: [Bool]
    let aktuell: Int
    let total: Int

    var body: some View {
        HStack(spacing: 6) {
            ForEach(0..<total, id: \.self) { i in
                Circle()
                    .fill(farbe(i))
                    .frame(width: i == aktuell ? 20 : 16, height: i == aktuell ? 20 : 16)
            }
        }
    }

    private func farbe(_ i: Int) -> Color {
        if i < resultate.count { return resultate[i] ? Theme.gruen : Theme.rot }
        return i == aktuell ? Theme.lila : Theme.lilaHell
    }
}
