# CGJ Android App

A comprehensive Android application for school management with features including substitution plans, meal ordering, Moodle integration, and performance tracking.

## 🚀 Quick Start

Use the prebuilt apk found on the releases page

Or build your app:

### Automatic Setup (Recommended)

The easiest way to get started is using our automatic setup scripts:

#### Option 1: Quick Setup (Recommended)
```bash
./quick-setup.sh
```

#### Option 2: Full Setup with Advanced Options
```bash
./setup-android-sdk.sh
```

### Manual Setup

If you prefer manual setup, follow these steps:

1. **Install Prerequisites:**
   ```bash
   sudo apt-get update
   sudo apt-get install openjdk-11-jdk wget unzip
   ```

2. **Download Android SDK:**
   ```bash
   wget https://dl.google.com/android/repository/commandlinetools-linux-8512546_latest.zip
   unzip commandlinetools-linux-8512546_latest.zip
   mkdir -p android-sdk/cmdline-tools
   mv cmdline-tools android-sdk/cmdline-tools/latest
   ```

3. **Install SDK Components:**
   ```bash
   export ANDROID_HOME="$(pwd)/android-sdk"
   export PATH="$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools"
   
   echo "y" | $ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager --licenses
   $ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0"
   ```

4. **Configure Project:**
   ```bash
   echo "sdk.dir=$ANDROID_HOME" > local.properties
   chmod +x gradlew
   ```

## 🏗️ Building the Project

### Build Debug Version
```bash
./gradlew assembleDebug
```

### Build Release Version
```bash
./gradlew assembleRelease
```

### Run Tests
```bash
./gradlew test                    # Unit tests
./gradlew connectedAndroidTest    # Instrumented tests
```

### Clean Build
```bash
./gradlew clean
```

## 📱 Features

- **Vertretungsplan**: Dynamic PDF/image display with download functionality
- **Essen**: WebView for meal ordering system
- **Moodle**: App redirection with store fallback
- **Leistungen**: Home.InfoPoint + performance certificates PDF
- **Theme Switching**: Green/System colors
- **Download Functionality**: For PDFs and images
- **Reload Functionality**: For all screens

## 🛠️ Technical Stack

- **UI Framework**: Jetpack Compose
- **PDF Viewer**: Android PDF Viewer Library
- **Image Loading**: Coil
- **State Management**: DataStore Preferences
- **Networking**: HttpURLConnection
- **Min SDK**: 21 (Android 5.0)
- **Target SDK**: 34 (Android 14)

## 📁 Project Structure

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/cgj/app/
│   │   │   └── MainActivity.kt          # Main app logic
│   │   ├── res/
│   │   │   └── drawable/                # Icons and images
│   │   └── AndroidManifest.xml
│   ├── test/
│   │   └── java/com/cgj/app/
│   │       └── MainActivityTest.kt      # Unit tests
│   └── androidTest/
│       └── java/com/cgj/app/
│           └── MainActivityInstrumentedTest.kt  # Instrumented tests
├── build.gradle.kts                     # App-specific dependencies
└── proguard-rules.pro                   # Code obfuscation

build.gradle.kts                         # Project configuration
settings.gradle.kts                      # Project settings
```

## 🔧 Available Scripts

- `quick-setup.sh` - Quick Android SDK installation
- `setup-android-sdk.sh` - Comprehensive SDK setup with options
- `build-test.sh` - Full build and test execution
- `setup-env.sh` - Environment variables setup (created by setup scripts)

## 🐳 Docker Support

For containerized builds:

```bash
docker build -t cgj-android .
docker run -v $(pwd):/app cgj-android ./gradlew assembleDebug
```

## 📋 Troubleshooting

### Common Issues

1. **SDK location not found**
   - Run `./quick-setup.sh` to install Android SDK
   - Check that `local.properties` contains correct `sdk.dir` path

2. **Permission denied**
   - Run `chmod +x gradlew`
   - Run `chmod +x *.sh`

3. **Dependencies not found**
   - Run `./gradlew --refresh-dependencies`

4. **Build errors**
   - Run `./gradlew clean assembleDebug`
   - Check error messages in console

### Getting Help

- Check `BUILD_INSTRUCTIONS.md` for detailed build instructions
- Check `COMPILATION_SUMMARY.md` for compilation details
- Run `./gradlew tasks` to see all available tasks

## 📄 License

This project is not licensed 
