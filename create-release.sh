#!/bin/bash
# SoCreate v2.0.0 Release Script
# Run this on a machine with full Android SDK + keytool

set -e

echo "════════════════════════════════════════════════════════════"
echo "  SoCreate v2.0.0 — Git Release + Signed APK Builder"
echo "  Samsung Galaxy Tab S10+ (2026-07-05)"
echo "════════════════════════════════════════════════════════════"

# 1. Git Release
echo ""
echo "📦 Creating Git Release..."
git config user.email "soquarky@artistso.com" || true
git config user.name "Steven Michael Allen Owens" || true

git add -A
git commit -m "v2.0.0 release prep: added missing activities, OverlayService, fixed navigation & minSdk" || true

git tag -d v2.0.0 2>/dev/null || true
git tag -a v2.0.0 -m "SoCreate v2.0.0 — Signed Release for Samsung Galaxy Tab S10+"

echo "✅ Git tag v2.0.0 created"

# 2. Generate Keystore (if missing)
if [ ! -f app/socreate-release.jks ]; then
    echo ""
    echo "🔑 Generating release keystore..."
    keytool -genkeypair -v \
      -keystore app/socreate-release.jks \
      -keyalg RSA -keysize 2048 -validity 10000 \
      -alias socreate \
      -storepass socreate123 -keypass socreate123 \
      -dname "CN=SoCreate, OU=AdventuresInDrawing, O=SoQuarky, L=Portland, S=OR, C=US"
    echo "✅ Keystore created: app/socreate-release.jks"
fi

# 3. Create keystore.properties
echo ""
echo "📝 Creating keystore.properties..."
cat > keystore.properties << EOF
storeFile=app/socreate-release.jks
storePassword=socreate123
keyAlias=socreate
keyPassword=socreate123
EOF
echo "✅ keystore.properties ready"

# 4. Build Signed Release APK + AAB
echo ""
echo "🔨 Building RELEASE APK + AAB..."
chmod +x gradlew

./gradlew clean assembleRelease bundleRelease --no-daemon

echo ""
echo "════════════════════════════════════════════════════════════"
echo "  ✅ RELEASE BUILD COMPLETE"
echo "════════════════════════════════════════════════════════════"

APK=$(find app/build/outputs/apk/release -name "*.apk" 2>/dev/null | head -1)
AAB=$(find app/build/outputs/bundle/release -name "*.aab" 2>/dev/null | head -1)

if [ -n "$APK" ]; then
    echo "📦 Signed APK: $APK"
    echo "   Size: $(du -h "$APK" | cut -f1)"
fi

if [ -n "$AAB" ]; then
    echo "📦 Signed AAB (Play Store): $AAB"
    echo "   Size: $(du -h "$AAB" | cut -f1)"
fi

echo ""
echo "🚀 Next steps:"
echo "   1. git push origin main --tags"
echo "   2. Create GitHub Release from tag v2.0.0"
echo "   3. Upload APK + AAB to the release"
echo ""
echo "SoCreate v2.0.0 ready for Tab S10+ & Play Store!"