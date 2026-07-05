# SoCreate GitHub Secrets Setup Guide

## Required Secrets for Signed Releases

Go to your repository → **Settings → Secrets and variables → Actions**

### 1. SOCREATE_KEYSTORE_BASE64 (Required for signed builds)

**How to generate:**

```bash
# On your local machine (with the keystore file)
base64 -w 0 app/socreate-release.jks > keystore.b64

# Copy the entire contents of keystore.b64 and paste it as the secret value
```

**Alternative (macOS):**
```bash
base64 app/socreate-release.jks | pbcopy
```

### 2. KEYSTORE_PASSWORD

Set this to the password you used when creating the keystore (e.g. `socreate123`).

---

## How to Use the Release Pipeline

### Option 1: Automatic (Recommended)

1. Create a version tag:
   ```bash
   git tag v2.0.1
   git push origin v2.0.1
   ```

2. The `release.yml` workflow will automatically:
   - Bump `versionCode` and `versionName`
   - Build signed APK + AAB
   - Create a GitHub Release with the files attached

### Option 2: Manual Trigger

1. Go to **Actions** tab
2. Select **SoCreate Signed Release**
3. Click **Run workflow**
4. Choose build type (`release` or `both`)

### Option 3: From the main CI

Pushing a tag to `main` will also trigger the signed release workflow.

---

## Files Overview

| File | Purpose |
|------|---------|
| `.github/workflows/release.yml` | Main signed release builder |
| `.github/workflows/build.yml` | Updated to trigger signed release |
| `.github/workflows/version-bump.sh` | Automatic version incrementer |
| `create-release.sh` | Local one-command release script |

---

## After First Successful Release

- Your Play Store `.aab` will be in the GitHub Release assets
- Debug and release APKs are also attached
- All builds are retained for 90 days

**Ready for Samsung Galaxy Tab S10+ and Google Play Store deployment!**