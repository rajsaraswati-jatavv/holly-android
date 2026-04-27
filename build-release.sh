#!/bin/bash

# Holly Android Release Build Script
# Builds signed release APK

set -e

echo "========================================"
echo "  Holly Android - Release Build"
echo "========================================"
echo ""

KEYSTORE_FILE="holly-release.jks"
KEY_ALIAS="holly"

# Check if keystore exists
if [ ! -f "$KEYSTORE_FILE" ]; then
    echo "Keystore not found. Generating new keystore..."
    keytool -genkeypair -v \
        -keystore $KEYSTORE_FILE \
        -keyalg RSA \
        -keysize 2048 \
        -validity 10000 \
        -alias $KEY_ALIAS
fi

# Build release APK
echo "Building Release APK..."
./gradlew assembleRelease

if [ -f "app/build/outputs/apk/release/app-release.apk" ]; then
    echo "✓ Release APK built successfully!"
    echo "Location: app/build/outputs/apk/release/app-release.apk"
    ls -lh app/build/outputs/apk/release/app-release.apk
else
    echo "✗ Release build failed!"
    exit 1
fi
