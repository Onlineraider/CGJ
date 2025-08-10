.PHONY: help build test clean install docker-build docker-test

help: ## Zeige diese Hilfe
	@echo "Verfügbare Kommandos:"
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-20s\033[0m %s\n", $$1, $$2}'

build: ## Kompiliere Debug APK
	@echo "🔨 Kompiliere Debug APK..."
	chmod +x gradlew
	./gradlew assembleDebug

test: ## Führe Tests aus
	@echo "🧪 Führe Tests aus..."
	chmod +x gradlew
	./gradlew test

clean: ## Clean Build
	@echo "🧹 Clean Build..."
	chmod +x gradlew
	./gradlew clean

install: ## Installiere APK auf verbundenem Device
	@echo "📱 Installiere APK..."
	adb install app/build/outputs/apk/debug/app-debug.apk

docker-build: ## Build mit Docker
	@echo "🐳 Build mit Docker..."
	docker-compose run --rm android-build

docker-test: ## Tests mit Docker
	@echo "🐳 Tests mit Docker..."
	docker-compose run --rm android-test

full-test: clean build test ## Vollständiger Test (Clean + Build + Test)
	@echo "✅ Vollständiger Test abgeschlossen!"

check: ## Prüfe Projekt-Struktur
	@echo "🔍 Prüfe Projekt-Struktur..."
	@test -f app/build.gradle.kts || (echo "❌ app/build.gradle.kts nicht gefunden" && exit 1)
	@test -f app/src/main/java/com/cgj/app/MainActivity.kt || (echo "❌ MainActivity.kt nicht gefunden" && exit 1)
	@test -f gradlew || (echo "❌ gradlew nicht gefunden" && exit 1)
	@echo "✅ Projekt-Struktur OK"

setup: check ## Setup Projekt
	@echo "⚙️  Setup Projekt..."
	chmod +x gradlew
	@echo "✅ Setup abgeschlossen!"