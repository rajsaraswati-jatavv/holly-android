#!/bin/bash

# Holly Android Build Script
# Builds the APK for Holly Voice Assistant

set -e

echo "========================================"
echo "  Holly Android Voice Assistant"
echo "  Build Script"
echo "========================================"
echo ""

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check Java version
echo -e "${YELLOW}Checking Java version...${NC}"
java -version

# Check Android SDK
echo -e "${YELLOW}Checking Android SDK...${NC}"
if [ -z "$ANDROID_HOME" ]; then
    echo -e "${RED}ERROR: ANDROID_HOME is not set!${NC}"
    echo "Please set ANDROID_HOME to your Android SDK path"
    exit 1
fi
echo "Android SDK: $ANDROID_HOME"

# Clean build
echo ""
echo -e "${YELLOW}Cleaning previous build...${NC}"
./gradlew clean

# Build debug APK
echo ""
echo -e "${YELLOW}Building Debug APK...${NC}"
./gradlew assembleDebug

# Check if build succeeded
if [ -f "app/build/outputs/apk/debug/app-debug.apk" ]; then
    echo ""
    echo -e "${GREEN}✓ Build successful!${NC}"
    echo ""
    echo "APK Location: app/build/outputs/apk/debug/app-debug.apk"
    ls -lh app/build/outputs/apk/debug/app-debug.apk
    echo ""
    
    # Optional: Install on connected device
    read -p "Install on connected device? (y/n): " -n 1 -r
    echo ""
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        echo -e "${YELLOW}Installing on device...${NC}"
        adb install -r app/build/outputs/apk/debug/app-debug.apk
        echo -e "${GREEN}✓ Installed!${NC}"
    fi
else
    echo -e "${RED}✗ Build failed!${NC}"
    exit 1
fi

echo ""
echo "========================================"
echo -e "${GREEN}Build Complete!${NC}"
echo "========================================"
