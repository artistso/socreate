#!/bin/bash
# version-bump.sh
# Automatically bumps versionCode and versionName for SoCreate releases

set -e

BUILD_GRADLE="app/build.gradle.kts"

if [ ! -f "$BUILD_GRADLE" ]; then
    echo "❌ $BUILD_GRADLE not found"
    exit 1
fi

# Get current values
CURRENT_VERSION_CODE=$(grep "versionCode =" "$BUILD_GRADLE" | head -1 | sed 's/.*versionCode = \([0-9]*\).*/\1/')
CURRENT_VERSION_NAME=$(grep "versionName =" "$BUILD_GRADLE" | head -1 | sed 's/.*versionName = "\([^"]*\)".*/\1/')

echo "Current: versionCode=$CURRENT_VERSION_CODE, versionName=$CURRENT_VERSION_NAME"

# Bump logic
NEW_VERSION_CODE=$((CURRENT_VERSION_CODE + 1))

# Parse versionName (e.g. 2.0.0 → 2.0.1)
IFS='.' read -ra PARTS <<< "$CURRENT_VERSION_NAME"
MAJOR=${PARTS[0]}
MINOR=${PARTS[1]}
PATCH=${PARTS[2]}

NEW_PATCH=$((PATCH + 1))
NEW_VERSION_NAME="${MAJOR}.${MINOR}.${NEW_PATCH}"

echo "New:     versionCode=$NEW_VERSION_CODE, versionName=$NEW_VERSION_NAME"

# Apply changes
sed -i "s/versionCode = $CURRENT_VERSION_CODE/versionCode = $NEW_VERSION_CODE/" "$BUILD_GRADLE"
sed -i "s/versionName = \"$CURRENT_VERSION_NAME\"/versionName = \"$NEW_VERSION_NAME\"/" "$BUILD_GRADLE"

echo "✅ Version bumped successfully!"

# Output for GitHub Actions
echo "NEW_VERSION_CODE=$NEW_VERSION_CODE" >> $GITHUB_OUTPUT
echo "NEW_VERSION_NAME=$NEW_VERSION_NAME" >> $GITHUB_OUTPUT