# Mathe-App – iOS (SwiftUI)

Gleicher Funktionsumfang wie die Android-App: Profile, Einmaleins und Plus & Minus, Spiel auf Zeit mit 4 Leveln,
Üben, Bestenlisten pro Fach und Level. Nur eigene Swift-Dateien, keine Abhängigkeiten. iOS 16 oder neuer.

**Nicht auf einem Mac gebaut oder getestet** – der Code wurde unter Windows geschrieben.

## Bauen (auf einem Mac mit Xcode 15+)

```bash
brew install xcodegen
cd ios
xcodegen            # erzeugt MatheApp.xcodeproj aus project.yml
open MatheApp.xcodeproj
```

Ohne XcodeGen: in Xcode ein neues «App»-Projekt (SwiftUI, Name `MatheApp`) anlegen, die Dateien aus `MatheApp/`
hineinziehen (die Vorlagen-`MatheAppApp.swift`/`ContentView.swift` ersetzen) und `MatheAppTests/` als Unit-Test-Target hinzufügen.

## Aufs iPhone / in den App Store

- Für ein eigenes iPhone reicht eine kostenlose Apple-ID: in Xcode unter *Signing & Capabilities* das eigene Team wählen.
  Die Bundle-ID `ch.matheapp` muss ggf. angepasst werden, falls sie schon vergeben ist.
- Für TestFlight oder den App Store braucht es das Apple Developer Program (99 USD/Jahr).
- Eine App-Icon-Grafik (1024×1024) fehlt noch: im Asset-Katalog ergänzen.
