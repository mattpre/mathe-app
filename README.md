# Mathe-App – Android

Mathe-Trainer für die Primarschule (Kotlin + Jetpack Compose). Anforderungen: `mathe-app.md`.

- **Profile**: mehrere Namen, jedes Profil hat eigene Bestzeiten.
- **Einmaleins**
  - Spiel auf Zeit: 10 Aufgaben, Stoppuhr läuft nur während der Eingabe, +5 s pro Fehler. Nur die 4 Level wählbar:
    L1 = 2er–5er, L2 = 2er–9er, L3 = 2er–12er, L4 = 3er, 6er, 7er, 8er, 9er, 12er.
  - Üben: endlos ohne Zeitdruck, Reihen 1–12 einzeln wählbar.
- **Plus & Minus** (Zahlenraum bis 20)
  - Spiel auf Zeit und Üben, jeweils mit 4 Leveln: L1 = Addition ohne Übertrag, L2 = Addition und Subtraktion ohne Übertrag,
    L3 = Addition mit Übertrag, L4 = Addition und Subtraktion mit Übertrag.
  - Übertrag bei der Addition: Einer ergeben zusammen 10 oder mehr (7 + 8). Bei der Subtraktion: man muss borgen (13 − 6).
- **Bestenliste**: pro Fach und Level die Bestzeit jedes Profils (Name + Zeit).

Code: Logik in `app/src/main/java/ch/matheapp/` (`Einmaleins.kt`, `PlusMinus.kt`, mit Unit-Tests), Speicherung in `Speicher.kt`, Bildschirme in `ui/`.

Werkzeuge liegen in `%USERPROFILE%\android-tools` (JDK 17, Android SDK, Gradle).

```bash
export JAVA_HOME="$USERPROFILE/android-tools/jdk-17.0.20.1+1"
./gradlew testDebugUnitTest assembleRelease
```

Signierte APK: `app/build/outputs/apk/release/Mathe-app.apk` (Kopie im Projektordner: `Mathe-App.apk`).
Signiert wird mit `mathe-app.jks` und `keystore.properties` im Projektordner (nicht ins Git, gut sichern –
ohne diesen Schlüssel lassen sich Updates nicht über die bestehende Installation einspielen).

Auf ein per USB verbundenes Handy (USB-Debugging an) installieren:

```bash
"$USERPROFILE/android-tools/sdk/platform-tools/adb.exe" install -r Mathe-App.apk
```
