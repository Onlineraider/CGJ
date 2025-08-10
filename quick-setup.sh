#!/bin/bash

# Quick Android SDK Setup for CGJ Project
# Run this script to automatically set up everything needed for building

echo "🚀 Quick Android SDK Setup for CGJ Project"
echo ""

# Check if we're in the right directory
if [[ ! -f "app/build.gradle.kts" ]]; then
    echo "❌ Error: Please run this script from the CGJ project root directory"
    exit 1
fi

# Check if Android SDK is already set up
if [[ -f "local.properties" ]] && grep -q "sdk.dir" local.properties; then
    SDK_PATH=$(grep "sdk.dir" local.properties | cut -d'=' -f2)
    if [[ -d "$SDK_PATH" ]] && [[ -f "$SDK_PATH/platform-tools/adb" ]]; then
        echo "✅ Android SDK already installed at: $SDK_PATH"
        echo "   You can now run: ./gradlew assembleDebug"
        exit 0
    fi
fi

echo "📥 Installing Android SDK..."

# Create local SDK directory
mkdir -p android-sdk/cmdline-tools

# Download SDK if not exists
if [[ ! -f "commandlinetools-linux-8512546_latest.zip" ]]; then
    echo "📦 Downloading Android SDK Command Line Tools..."
    wget -q https://dl.google.com/android/repository/commandlinetools-linux-8512546_latest.zip
fi

# Extract and install
echo "🔧 Installing Android SDK..."
unzip -q commandlinetools-linux-8512546_latest.zip
mv cmdline-tools android-sdk/cmdline-tools/latest

# Set environment
export ANDROID_HOME="$(pwd)/android-sdk"
export PATH="$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools"

# Accept licenses and install components
echo "📋 Installing SDK components..."
echo "y" | $ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager --licenses > /dev/null 2>&1
$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0"

# Create local.properties
echo "⚙️  Configuring project..."
echo "sdk.dir=$ANDROID_HOME" > local.properties

# Make gradlew executable
chmod +x gradlew

# Clean up
rm -f commandlinetools-linux-8512546_latest.zip

echo ""
echo "✅ Setup complete!"
echo "🎯 You can now run: ./gradlew assembleDebug"
echo "📱 APK will be created at: app/build/outputs/apk/debug/app-debug.apk"