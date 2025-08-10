# CGJ App - Build & Test Anleitung

## Voraussetzungen

### 1. Android Development Environment
- **Android Studio** (empfohlen) oder **Android SDK**
- **Java 8** oder höher
- **Gradle** (wird automatisch durch Gradle Wrapper bereitgestellt)

### 2. Android SDK Setup
```bash
# Setze ANDROID_HOME Umgebungsvariable
export ANDROID_HOME=/path/to/your/android/sdk

# Füge Android SDK Tools zum PATH hinzu
export PATH=$PATH:$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools
```

## Kompilierung

### Option 1: Automatisches Build-Script
```bash
# Führe das Build-Script aus
./build-test.sh
```

### Option 2: Manuelle Kompilierung
```bash
# Berechtigungen für Gradle Wrapper
chmod +x gradlew

# Clean Build
./gradlew clean

# Debug Version kompilieren
./gradlew assembleDebug

# Release Version kompilieren (optional)
./gradlew assembleRelease
```

## Tests

### Unit Tests
```bash
# Führe alle Unit Tests aus
./gradlew test

# Führe spezifische Tests aus
./gradlew test --tests "com.cgj.app.MainActivityTest"
```

### Instrumented Tests
```bash
# Führe Instrumented Tests aus (benötigt Android Device/Emulator)
./gradlew connectedAndroidTest
```

## APK Installation

### Debug APK
```bash
# APK wird erstellt in:
app/build/outputs/apk/debug/app-debug.apk

# Installation auf verbundenem Device
adb install app/build/outputs/apk/debug/app-debug.apk
```

## Bekannte Probleme & Lösungen

### 1. ANDROID_HOME nicht gesetzt
**Problem:** `SDK location not found`
**Lösung:** Setze ANDROID_HOME Umgebungsvariable

### 2. Gradle Wrapper Berechtigungen
**Problem:** `Permission denied`
**Lösung:** `chmod +x gradlew`

### 3. Abhängigkeiten nicht gefunden
**Problem:** `Could not resolve dependencies`
**Lösung:** 
```bash
./gradlew --refresh-dependencies
```

### 4. Compile Errors
**Häufige Probleme:**
- Fehlende Imports
- Syntax-Fehler
- Inkompatible API-Level

**Lösung:** Prüfe die Fehlermeldungen in der Konsole

## Projekt-Struktur

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/cgj/app/
│   │   │   └── MainActivity.kt          # Haupt-App-Logik
│   │   ├── res/
│   │   │   └── drawable/                # Icons und Bilder
│   │   └── AndroidManifest.xml
│   ├── test/
│   │   └── java/com/cgj/app/
│   │       └── MainActivityTest.kt      # Unit Tests
│   └── androidTest/
│       └── java/com/cgj/app/
│           └── MainActivityInstrumentedTest.kt  # Instrumented Tests
├── build.gradle.kts                     # App-spezifische Dependencies
└── proguard-rules.pro                   # Code-Obfuskation

build.gradle.kts                         # Projekt-Konfiguration
settings.gradle.kts                      # Projekt-Einstellungen
```

## Features der App

### ✅ Implementiert
- **Vertretungsplan**: Dynamische PDF/Bild-Anzeige mit Download
- **Essen**: WebView für Bestellsystem
- **Moodle**: App-Weiterleitung mit Store-Fallback
- **Leistungen**: Home.InfoPoint + Leistungsnachweise PDF
- **Theme-Switching**: Grün/System-Farben
- **Download-Funktionalität**: Für PDFs und Bilder
- **Reload-Funktionalität**: Für alle Screens

### 🔧 Technische Details
- **UI Framework**: Jetpack Compose
- **PDF Viewer**: Android PDF Viewer Library
- **Image Loading**: Coil
- **State Management**: DataStore Preferences
- **Networking**: HttpURLConnection
- **Min SDK**: 21 (Android 5.0)
- **Target SDK**: 34 (Android 14)

## Debugging

### Logs anzeigen
```bash
# Alle Logs
adb logcat

# Nur App-spezifische Logs
adb logcat | grep "com.cgj.app"
```

### App neu starten
```bash
# App beenden
adb shell am force-stop com.cgj.app

# App starten
adb shell am start -n com.cgj.app/.MainActivity
```

## Support

Bei Problemen:
1. Prüfe die Fehlermeldungen in der Konsole
2. Stelle sicher, dass alle Voraussetzungen erfüllt sind
3. Versuche einen Clean Build: `./gradlew clean assembleDebug`
4. Prüfe die Android SDK Installation