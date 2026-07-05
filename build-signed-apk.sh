#!/bin/bash
# =====================================================
# SoCreate v2.0.0 — Build Signed APK + AAB Locally
# Run this on a machine with Android SDK + JDK 17
# =====================================================

set -e

echo "════════════════════════════════════════════════════════════"
echo "  SoCreate v2.0.0 Signed Release Builder"
echo "  Samsung Galaxy Tab S10+ (2026-07-05)"
echo "════════════════════════════════════════════════════════════"

# 1. Ensure keystore exists
if [ ! -f app/socreate-release.jks ]; then
    echo ""
    echo "🔑 Generating release keystore..."
    keytool -genkeypair -v \
      -keystore app/socreate-release.jks \
      -keyalg RSA -keysize 2048 -validity 10000 \
      -alias socreate \
      -storepass "${KEYSTORE_PASSWORD:-socreate123}" -keypass "${KEYSTORE_PASSWORD:-socreate123}" \
      -dname "CN=SoCreate, OU=AdventuresInDrawing, O=SoQuarky, L=Portland, S=OR, C=US"
    echo "✅ Keystore created"
fi

# 2. Create keystore.properties
echo ""
echo "📝 Creating keystore.properties..."
cat > keystore.properties << EOF
storeFile=app/socreate-release.jks
storePassword=${KEYSTORE_PASSWORD:-socreate123}
keyAlias=socreate
keyPassword=${KEYSTORE_PASSWORD:-socreate123}
EOF

# 3. Clean previous builds
echo ""
echo "🧹 Cleaning previous builds..."
./gradlew clean --no-daemon

# 4. Build Debug (for testing)
echo ""
echo "🔨 Building DEBUG APK..."
./gradlew assembleDebug --no-daemon

# 5. Build Signed Release
echo ""
echo "🔨 Building SIGNED RELEASE APK + AAB..."
./gradlew assembleRelease bundleRelease --no-daemon

echo ""
echo "════════════════════════════════════════════════════════════"
echo "  ✅ BUILD COMPLETE"
echo "════════════════════════════════════════════════════════════"

# Show results
echo ""
echo "📦 Generated files:"

DEBUG_APK=$(find app/build/outputs/apk/debug -name "*.apk" 2>/dev/null | head -1)
RELEASE_APK=$(find app/build/outputs/apk/release -name "*.apk" 2>/dev/null | head -1)
RELEASE_AAB=$(find app/build/outputs/bundle/release -name "*.aab" 2>/dev/null | head -1)

if [ -n "$DEBUG_APK" ]; then
    echo "   Debug APK:     $DEBUG_APK ($(du -h "$DEBUG_APK" | cut -f1))"
fi
if [ -n "$RELEASE_APK" ]; then
    echo "   Release APK:   $RELEASE_APK ($(du -h "$RELEASE_APK" | cut -f1))"
fi
if [ -n "$RELEASE_AAB" ]; then
    echo "   Release AAB:   $RELEASE_AAB ($(du -h "$RELEASE_AAB" | cut -f1))"
fi

echo ""
echo "🚀 Next steps:"
echo "   1. Install debug APK for testing:"
echo "      adb install $DEBUG_APK"
echo ""
echo "   2. Upload signed files to GitHub Release or Play Store"
echo ""
echo "SoCreate v2.0.0 ready for Tab S10+!"