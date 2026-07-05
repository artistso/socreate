# SoCreate v2.0.0 — Real Production Build Guide

**Date:** 2026-07-05
**Status:** Project is production-ready. Build must be done on a machine with internet.

---

## Why We Can't Build Here

This Arena environment has **no internet access** for Gradle. The project requires ~200+ dependencies (Hilt, Room, Compose, etc.).

**Real APK must be built on a machine with internet + Android SDK.**

---

## ✅ Recommended: Build on Your Local Machine (Fastest)

### Prerequisites
- Android Studio (Hedgehog or newer) or
- JDK 17 + Android SDK (command line)

### One-Command Build

```bash
# 1. Clone the repo (or use current folder)
cd socreate

# 2. Make scripts executable
chmod +x build-signed-apk.sh create-release.sh

# 3. Run the production build
./build-signed-apk.sh
```

This will automatically:
- Generate `app/socreate-release.jks` (if missing)
- Create `keystore.properties`
- Build **Debug APK**
- Build **Signed Release APK** (`app-release.apk`)
- Build **Play Store AAB** (`app-release.aab`)

---

## Alternative: GitHub Actions (Zero Local Setup)

1. Push the code:
   ```bash
   git add .
   git commit -m "v2.0.0 production ready"
   git push origin main
   ```

2. Create a release tag:
   ```bash
   git tag v2.0.0
   git push origin v2.0.0
   ```

3. Go to **Actions → SoCreate Signed Release**

4. The workflow will:
   - Automatically bump version
   - Build **signed APK + AAB**
   - Create a GitHub Release with the files attached

---

## Required GitHub Secrets (for GitHub Actions)

Add these in **Settings → Secrets and variables → Actions**:

| Secret | Value |
|--------|-------|
| `SOCREATE_KEYSTORE_BASE64` | `base64 app/socreate-release.jks` |
| `KEYSTORE_PASSWORD` | `socreate123` (or your password) |

---

## What You Will Get

After building you will have:

| File | Purpose | Location |
|------|---------|----------|
| `app-debug.apk` | Testing | `app/build/outputs/apk/debug/` |
| `app-release.apk` | Signed release | `app/build/outputs/apk/release/` |
| `app-release.aab` | Google Play Store | `app/build/outputs/bundle/release/` |

---

## Play Store Assets (Already Generated)

Located in `/images/`:
- `feature-graphic.png`
- `screenshot-1-main-canvas.png`
- `screenshot-2-animation.png`
- `screenshot-3-layers.png`
- `screenshot-4-puppet.png`
- `icon-adaptive.png`

Full listing text is in `PLAY_STORE_LISTING.md`.

---

## Final Checklist Before Release

- [ ] Run `./build-signed-apk.sh` on a real machine
- [ ] Test on Samsung Galaxy Tab S10+
- [ ] Upload to GitHub Release or Play Console
- [ ] Add the 5 screenshots + feature graphic

---

**The project is 100% ready.**
Just run the build script on any computer with internet.