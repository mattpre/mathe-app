import XCTest
@testable import MatheApp

final class ModelTests: XCTestCase {
    private func pool(_ minus: Bool, _ uebertrag: Bool) -> [Aufgabe] {
        PlusMinus.pool(mitMinus: minus, mitUebertrag: uebertrag)
    }

    private func hat(_ p: [Aufgabe], _ a: Int, _ b: Int, _ op: Operator) -> Bool {
        p.contains { $0.normalisiert == Aufgabe(a: a, b: b, op: op).normalisiert }
    }

    func testEinmaleinsPoolOhneDoppelte() {
        XCTAssertEqual(Einmaleins.pool(reihen: Einmaleins.alleReihen).count, 78)
        XCTAssertEqual(Einmaleins.pool(reihen: [7]).count, 12)
    }

    func testLevel1() {
        let p = pool(false, false)
        XCTAssertTrue(p.allSatisfy { $0.op == .plus && $0.ergebnis <= 20 && $0.a % 10 + $0.b % 10 < 10 })
        XCTAssertFalse(hat(p, 7, 8, .plus))
        XCTAssertTrue(hat(p, 12, 5, .plus))
    }

    func testLevel2() {
        let p = pool(true, false)
        XCTAssertFalse(hat(p, 13, 6, .minus))
        XCTAssertTrue(hat(p, 15, 3, .minus))
    }

    func testLevel3() {
        let p = pool(false, true)
        XCTAssertTrue(p.allSatisfy { $0.op == .plus && $0.a % 10 + $0.b % 10 >= 10 })
        XCTAssertTrue(hat(p, 7, 8, .plus))
    }

    func testLevel4() {
        let p = pool(true, true)
        XCTAssertTrue(hat(p, 13, 6, .minus))
        XCTAssertFalse(hat(p, 15, 3, .minus))
    }

    func testRundeHatZehnAufgaben() {
        for l in Level.plusminus + Level.einmaleins {
            XCTAssertEqual(Spiel.runde(pool: l.pool).count, 10)
        }
    }

    func testSubtraktionBehaeltReihenfolge() {
        let a = Aufgabe(a: 13, b: 6, op: .minus)
        XCTAssertEqual(a.vertauscht, a)
        XCTAssertEqual(a.text, "13 − 6")
        XCTAssertEqual(a.ergebnis, 7)
    }

    func testZeitFormatUndSterne() {
        XCTAssertEqual(Spiel.formatZeit(12_345), "12,3 s")
        XCTAssertEqual(Spiel.sterne(richtig: 10), 3)
        XCTAssertEqual(Spiel.sterne(richtig: 4), 0)
        XCTAssertEqual(Spiel.endzeitMs(zeitMs: 12_000, fehler: 2), 22_000)
    }
}
