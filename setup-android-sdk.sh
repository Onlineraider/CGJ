#!/bin/bash

# CGJ Android SDK Setup Script
# This script automatically downloads, installs, and configures Android SDK

set -e  # Exit on any error

echo "=== CGJ Android SDK Setup Script ==="
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if running as root
if [[ $EUID -eq 0 ]]; then
   print_error "This script should not be run as root"
   exit 1
fi

# Configuration
SDK_VERSION="8512546"
SDK_URL="https://dl.google.com/android/repository/commandlinetools-linux-${SDK_VERSION}_latest.zip"
SDK_DIR="/opt/android-sdk"
LOCAL_SDK_DIR="$(pwd)/android-sdk"

# Check if we can write to /opt
if [[ -w "/opt" ]]; then
    USE_SYSTEM_SDK=true
    FINAL_SDK_DIR="$SDK_DIR"
    print_status "Will install Android SDK to system directory: $SDK_DIR"
else
    USE_SYSTEM_SDK=false
    FINAL_SDK_DIR="$LOCAL_SDK_DIR"
    print_status "Will install Android SDK to local directory: $FINAL_SDK_DIR"
fi

# Check prerequisites
print_status "Checking prerequisites..."

# Check for wget
if ! command -v wget &> /dev/null; then
    print_error "wget is not installed. Please install it first."
    exit 1
fi

# Check for unzip
if ! command -v unzip &> /dev/null; then
    print_error "unzip is not installed. Please install it first."
    exit 1
fi

# Check for Java
if ! command -v java &> /dev/null; then
    print_warning "Java is not installed. Please install Java 8 or higher."
    print_status "You can install it with: sudo apt-get install openjdk-11-jdk"
fi

print_success "Prerequisites check completed"

# Create SDK directory
print_status "Creating SDK directory..."
if [[ "$USE_SYSTEM_SDK" == true ]]; then
    sudo mkdir -p "$SDK_DIR"
    sudo chown $USER:$USER "$SDK_DIR"
else
    mkdir -p "$FINAL_SDK_DIR"
fi

# Download Android SDK Command Line Tools
print_status "Downloading Android SDK Command Line Tools..."
if [[ -f "commandlinetools-linux-${SDK_VERSION}_latest.zip" ]]; then
    print_warning "SDK zip file already exists, skipping download"
else
    wget -q --show-progress "$SDK_URL" -O "commandlinetools-linux-${SDK_VERSION}_latest.zip"
fi

# Extract SDK
print_status "Extracting Android SDK..."
unzip -q "commandlinetools-linux-${SDK_VERSION}_latest.zip"

# Move to correct location
print_status "Installing Android SDK..."
mkdir -p "$FINAL_SDK_DIR/cmdline-tools"
mv cmdline-tools "$FINAL_SDK_DIR/cmdline-tools/latest"

# Set environment variables
export ANDROID_HOME="$FINAL_SDK_DIR"
export ANDROID_SDK_ROOT="$FINAL_SDK_DIR"
export PATH="$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools"

# Accept licenses
print_status "Accepting Android SDK licenses..."
echo "y" | "$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager" --licenses > /dev/null 2>&1 || {
    print_warning "License acceptance failed, trying interactive mode..."
    "$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager" --licenses
}

# Install required SDK components
print_status "Installing Android SDK components..."
"$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager" \
    "platform-tools" \
    "platforms;android-34" \
    "build-tools;34.0.0" \
    "extras;android;m2repository" \
    "extras;google;m2repository"

# Set permissions
if [[ "$USE_SYSTEM_SDK" == true ]]; then
    sudo chown -R $USER:$USER "$FINAL_SDK_DIR"
fi

# Create/update local.properties
print_status "Creating local.properties file..."
cat > local.properties << EOF
## This file must *NOT* be checked into Version Control Systems,
# as it contains information specific to your local configuration.
#
# Location of the SDK. This is only used by Gradle.
# For customization when using a Version Control System, please read the
# header note.
sdk.dir=$FINAL_SDK_DIR
EOF

# Make gradlew executable
print_status "Setting up Gradle wrapper..."
chmod +x gradlew

# Test the setup
print_status "Testing Android SDK installation..."
if "$ANDROID_HOME/platform-tools/adb" version > /dev/null 2>&1; then
    print_success "ADB is working correctly"
else
    print_error "ADB test failed"
    exit 1
fi

# Try a test build
print_status "Testing Gradle build..."
if ./gradlew tasks > /dev/null 2>&1; then
    print_success "Gradle is working correctly"
else
    print_warning "Gradle test failed, but SDK installation should be complete"
fi

# Create environment setup script
print_status "Creating environment setup script..."
cat > setup-env.sh << EOF
#!/bin/bash
# Environment setup script for CGJ Android development
export ANDROID_HOME="$FINAL_SDK_DIR"
export ANDROID_SDK_ROOT="$FINAL_SDK_DIR"
export PATH="\$PATH:\$ANDROID_HOME/cmdline-tools/latest/bin:\$ANDROID_HOME/platform-tools"
echo "Android SDK environment variables set:"
echo "  ANDROID_HOME=\$ANDROID_HOME"
echo "  ANDROID_SDK_ROOT=\$ANDROID_SDK_ROOT"
EOF

chmod +x setup-env.sh

# Clean up downloaded files
print_status "Cleaning up temporary files..."
rm -f "commandlinetools-linux-${SDK_VERSION}_latest.zip"

# Final instructions
echo ""
print_success "Android SDK setup completed successfully!"
echo ""
echo "=== Next Steps ==="
echo "1. Source the environment variables:"
echo "   source setup-env.sh"
echo ""
echo "2. Build the project:"
echo "   ./gradlew assembleDebug"
echo ""
echo "3. Or run the full build script:"
echo "   ./build-test.sh"
echo ""
echo "=== SDK Location ==="
echo "Android SDK installed at: $FINAL_SDK_DIR"
echo "local.properties configured with: sdk.dir=$FINAL_SDK_DIR"
echo ""
echo "=== Available Commands ==="
echo "  ./gradlew assembleDebug    - Build debug APK"
echo "  ./gradlew assembleRelease  - Build release APK"
echo "  ./gradlew test             - Run unit tests"
echo "  ./gradlew clean            - Clean build"
echo ""

print_success "Setup complete! You can now build your Android project."