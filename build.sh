#!/bin/bash
# SoCreate Build Script — run inside Codespace terminal
# Usage: ./build.sh [debug|release|aab]

set -e

echo "═══════════════════════════════════════════════════"
echo "  SoCreate v1.1.0 Build Script"
echo "  Samsung Galaxy Tab S10+ Optimized"
echo "═══════════════════════════════════════════════════"

# Check Java
echo ""
echo "☕ Java version:"
java -version 2>&1 | head -1

# Check Android SDK
echo ""
echo "📱 Android SDK: $ANDROID_HOME"
if [ -d "$ANDROID_HOME/platforms/android-35" ]; then
    echo "   ✅ android-35 platform installed"
else
    echo "   ⚠️  android-35 platform missing — installing..."
    yes | sdkmanager "platforms;android-35" "build-tools;35.0.0"
fi

# Accept licenses
echo ""
echo "📋 Accepting SDK licenses..."
yes | sdkmanager --licenses 2>/dev/null || true

chmod +x gradlew

MODE=${1:-debug}

case "$MODE" in
  debug)
    echo ""
    echo "🔨 Building DEBUG APK..."
    ./gradlew assembleDebug --stacktrace 2>&1 | tail -50
    APK=$(find app/build/outputs/apk/debug -name "*.apk" 2>/dev/null | head -1)
    if [ -n "$APK" ]; then
      SIZE=$(du -h "$APK" | cut -f1)
      echo ""
      echo "═══════════════════════════════════════════════════"
      echo "  ✅ DEBUG APK BUILT SUCCESSFULLY!"
      echo "  📦 $APK ($SIZE)"
      echo "═══════════════════════════════════════════════════"
    fi
    ;;
  release)
    echo ""
    echo "🔨 Building RELEASE APK..."
    if [ ! -f app/socreate-release.jks ]; then
      echo "⚠️  No keystore found. Run './build.sh keystore' first."
      exit 1
    fi
    ./gradlew assembleRelease --stacktrace 2>&1 | tail -50
    APK=$(find app/build/outputs/apk/release -name "*.apk" 2>/dev/null | head -1)
    if [ -n "$APK" ]; then
      SIZE=$(du -h "$APK" | cut -f1)
      echo ""
      echo "═══════════════════════════════════════════════════"
      echo "  ✅ RELEASE APK BUILT SUCCESSFULLY!"
      echo "  📦 $APK ($SIZE)"
      echo "═══════════════════════════════════════════════════"
    fi
    ;;
  aab)
    echo ""
    echo "🔨 Building RELEASE AAB (for Play Store)..."
    if [ ! -f app/socreate-release.jks ]; then
      echo "⚠️  No keystore found. Run './build.sh keystore' first."
      exit 1
    fi
    ./gradlew bundleRelease --stacktrace 2>&1 | tail -50
    AAB=$(find app/build/outputs/bundle/release -name "*.aab" 2>/dev/null | head -1)
    if [ -n "$AAB" ]; then
      SIZE=$(du -h "$AAB" | cut -f1)
      echo ""
      echo "═══════════════════════════════════════════════════"
      echo "  ✅ RELEASE AAB BUILT SUCCESSFULLY!"
      echo "  📦 $AAB ($SIZE)"
      echo "═══════════════════════════════════════════════════"
    fi
    ;;
  keystore)
    echo ""
    echo "🔑 Generating release keystore..."
    keytool -genkeypair -v \
      -keystore app/socreate-release.jks \
      -keyalg RSA \
      -keysize 2048 \
      -validity 10000 \
      -alias socreate \
      -storetype JKS
    echo ""
    echo "✅ Keystore created at app/socreate-release.jks"
    echo "⚠️  BACK THIS UP! Keep the password safe!"
    echo ""
    echo "Now create keystore.properties:"
    echo "  echo 'storeFile=socreate-release.jks' > keystore.properties"
    echo "  echo 'storePassword=YOUR_PASSWORD' >> keystore.properties"
    echo "  echo 'keyAlias=socreate' >> keystore.properties"
    echo "  echo 'keyPassword=YOUR_PASSWORD' >> keystore.properties"
    ;;
  *)
    echo "Usage: ./build.sh [debug|release|aab|keystore]"
    ;;
esac
