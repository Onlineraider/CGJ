#!/bin/bash

echo "=== CGJ App Test Build ==="
echo ""

# Prüfe ob Gradle verfügbar ist
if ! command -v ./gradlew &> /dev/null; then
    echo "❌ Gradle Wrapper nicht gefunden"
    echo "Führe 'chmod +x gradlew' aus"
    exit 1
fi

# Prüfe Android SDK
if [ -z "$ANDROID_HOME" ]; then
    echo "⚠️  ANDROID_HOME nicht gesetzt"
    echo "Setze ANDROID_HOME auf dein Android SDK Verzeichnis"
    echo "z.B.: export ANDROID_HOME=/path/to/android/sdk"
fi

echo "🔍 Prüfe Projekt-Struktur..."
if [ ! -f "app/build.gradle.kts" ]; then
    echo "❌ app/build.gradle.kts nicht gefunden"
    exit 1
fi

if [ ! -f "app/src/main/java/com/cgj/app/MainActivity.kt" ]; then
    echo "❌ MainActivity.kt nicht gefunden"
    exit 1
fi

echo "✅ Projekt-Struktur OK"
echo ""

echo "🧹 Clean Build..."
./gradlew clean

echo ""
echo "🔨 Kompiliere Debug Version..."
./gradlew assembleDebug

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Build erfolgreich!"
    echo "📱 APK erstellt in: app/build/outputs/apk/debug/app-debug.apk"
    echo ""
    echo "🧪 Führe Tests aus..."
    ./gradlew test
    echo ""
    echo "🎯 Build und Tests abgeschlossen!"
else
    echo ""
    echo "❌ Build fehlgeschlagen!"
    echo "Prüfe die Fehlermeldungen oben"
    exit 1
fi