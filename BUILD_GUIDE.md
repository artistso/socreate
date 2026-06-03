# 🔨 How to Build SoCreate APK — Complete Guide

**Developer:** Steven Michael Allen Owens (@SoQuarky)
**Device:** Samsung Galaxy Tab S10+ (developing via Arena.ai)
**Target:** Google Play Store

---

## Option 1: 🏆 Recommended — Build on a Computer

Since you're developing on your Tab S10+ through Arena.ai, the most reliable path is:

### Step 1: Download the Project from Arena.ai
- In Arena.ai workspace, download the entire `SoCreate/` folder as a ZIP
- Transfer to your computer (USB-C from Tab S10+ or cloud upload)

### Step 2: Install Android Studio on Your Computer
- Download: https://developer.android.com/studio
- Install JDK 17 (Android Studio bundles this)
- On first launch, install Android SDK 34

### Step 3: Open & Build
```
1. Open Android Studio
2. File → Open → select the SoCreate folder
3. Wait for Gradle sync (first time takes 5-10 min)
4. Build → Build Bundle(s) / APK(s) → Build APK(s)
```

Output: `SoCreate/app/build/outputs/apk/debug/app-debug.apk`

### Step 4: Install on Your Tab S10+
**USB method:**
```
1. Connect Tab S10+ via USB-C
2. Enable Developer Options: Settings → About → tap Build Number 7×
3. Enable USB Debugging: Developer Options → USB Debugging
4. In Android Studio: Run → Select your Tab S10+
```

**Or transfer the APK:**
```
1. Copy app-debug.apk to your Tab S10+
2. Open Files app → tap the APK → Install
```

---

## Option 2: ☁️ Cloud Build with GitHub Actions (Free CI/CD)

Build APKs in the cloud — no computer needed. Just push code to GitHub.

### Step 1: Create a GitHub Repository
```bash
# In Arena.ai terminal or on GitHub.com:
git init
git add .
git commit -m "SoCreate v1.0.0 - Steven Michael Allen Owens"
git remote add origin https://github.com/SoQuarky/SoCreate.git
git push -u origin main
```

### Step 2: The workflow file is already created at:
`.github/workflows/build.yml` (see below)

### Step 3: Get Your APK
1. Go to your GitHub repo → Actions tab
2. Click the latest "Build SoCreate APK" run
3. Scroll down to Artifacts → Download `socreate-debug.apk`

---

## Option 3: 📱 Build Directly on Tab S10+ (Advanced)

Install Termux and build natively on your tablet:

```bash
# Install Termux from F-Droid (NOT Play Store version)
# Open Termux:

pkg update && pkg upgrade -y
pkg install openjdk-17 git -y

# Set up Java
export JAVA_HOME=/data/data/com.termux/files/usr/opt/openjdk
export PATH=$JAVA_HOME/bin:$PATH

# Clone your project
cd ~/storage/downloads  # Or wherever
git clone https://github.com/SoQuarky/SoCreate.git
cd SoCreate

# Create local.properties
echo "sdk.dir=$HOME/android-sdk" > local.properties

# Install Android SDK
mkdir -p ~/android-sdk/cmdline-tools
cd ~/android-sdk/cmdline-tools
# Download cmdline-tools from developer.android.com
# Extract and set up

# Build
chmod +x gradlew
./gradlew assembleDebug

# APK output:
# app/build/outputs/apk/debug/app-debug.apk
```

---

## 🏪 Google Play Store Submission Guide

### Prerequisites
✅ Google Play Developer Account ($25 one-time fee)
   → https://play.google.com/console/signup

### Step 1: Create a Release Keystore
```bash
keytool -genkeypair -v \
  -keystore socreate-release.jks \
  -keyalg RSA -keysize 2048 -validity 9125 \
  -alias socreate \
  -dname "CN=Steven Michael Allen Owens, O=AdventuresInDrawing, L=YourCity, ST=YourState, C=US"
```

**⚠️ BACK UP socreate-release.jks AND YOUR PASSWORDS!**
If you lose this, you can NEVER update your app on Play Store.
Store in Google Drive, USB drive, and a secure password manager.

### Step 2: Configure Signing
```bash
# Create keystore.properties in project root
cp keystore.properties.template keystore.properties
# Edit keystore.properties with your actual keystore path and passwords
```

### Step 3: Build Release AAB (Android App Bundle)
```bash
./gradlew bundleRelease
```
Output: `app/build/outputs/bundle/release/app-release.aab`

### Step 4: Upload to Play Store
1. Go to https://play.google.com/console
2. Create app → Fill in store listing
3. App signing → Use Google Play App Signing (recommended)
4. Upload `app-release.aab`
5. Fill in store listing details:

```
App name: SoCreate
Developer name: Soquarky / AdventuresInDrawing
Email: support@soquarky.click
Website: https://soquarky.click
Privacy policy: https://soquarky.click/privacy
Category: Art & Design
Tags: drawing, animation, 2D animation, digital art, flipbook
```

### Step 5: Store Listing Content

**Short description (80 chars):**
Professional 2D drawing & animation studio for Android tablets.

**Full description:**
SoCreate is a professional-grade 2D drawing and animation app designed
specifically for Android tablets. Optimized for Samsung Galaxy Tab S10+.

Whether you're a professional animator, illustrator, or just starting your
creative journey, SoCreate gives you everything you need to bring your
imagination to life.

✨ FEATURES:
• 13+ professional brushes with S Pen pressure sensitivity (4096 levels)
• 7 symmetry drawing modes (Mandala, Radial, Quadrant & more)
• Smart shape detection — draw freehand, get perfect shapes
• Full layer system with 28+ blend modes
• 120Hz smooth rendering on supported displays
• Liquify, Puppet Warp, and transform tools
• Time-lapse recording of your drawing process
• Audio tracks for animation dialogue and sound effects
• Frame-by-frame animation with onion skinning
• Auto-save with crash recovery
• Export to PNG, JPEG, PSD, MP4, GIF

🎨 DESIGNED FOR S PEN:
• Full pressure sensitivity with customizable curves
• Tilt and hover support with brush size preview
• Palm rejection for natural drawing
• S Pen button shortcuts

📱 OPTIMIZED FOR TAB S10+:
• Native 2800×1752 canvas (1:1 pixel mapping)
• Display P3 wide color gamut on AMOLED
• 120Hz rendering with motion prediction
• Samsung DeX support

Developed by Steven Michael Allen Owens (@SoQuarky)
An AdventuresInDrawing production
Brands: Soquarky · FunFYP · FPY.Lovely · Soquarky.Click

### Step 6: Required Assets
You'll need to create:
- **App icon**: 512×512 PNG
- **Feature graphic**: 1024×500 PNG
- **Screenshots**: Minimum 2, recommended 8 (on Tab S10+)
- **Privacy policy URL**: Required (host at soquarky.click/privacy)

### Step 7: Content Rating
Answer IARC questionnaire — SoCreate is a creative tool, so:
- No violence, no user-generated content sharing (initially)
- Rating: Everyone / PEGI 3

### Step 8: Pricing
- Free with in-app purchases, OR
- Paid app (you choose the price)
- Target all countries or selective rollout

---

## 🚀 Quick Start Checklist

- [ ] Download project from Arena.ai
- [ ] Install Android Studio on computer
- [ ] Build debug APK → test on Tab S10+
- [ ] Create release keystore → **BACK IT UP**
- [ ] Configure keystore.properties
- [ ] Build release AAB
- [ ] Create Google Play Developer account
- [ ] Create store listing with your branding
- [ ] Upload screenshots (capture on your Tab S10+)
- [ ] Write privacy policy at soquarky.click/privacy
- [ ] Upload AAB to Play Console
- [ ] Submit for review (usually 2-7 days for first app)

---

## 🔐 Security Notes

**Never commit to git:**
- `keystore.properties` — your signing passwords
- `*.jks` files — your signing key
- `google-play-service-account.json` — Play Console API access

**These are already in .gitignore.**

---

Questions? Reach out: support@soquarky.click
Built with ❤️ by Steven Michael Allen Owens (@SoQuarky)
AdventuresInDrawing — Soquarky · FunFYP · FPY.Lovely · Soquarky.Click
