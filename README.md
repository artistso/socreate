# SoCreate — Animate Your Imagination

Professional drawing & animation app for Samsung Galaxy Tab S10+, optimized for S Pen and 120Hz AMOLED display.

**Developer:** Steven Michael Allen Owens (@SoQuarky)  
**Company:** AdventuresInDrawing  
**Brands:** Soquarky · FunFYP · FPY.Lovely · Soquarky.Click  
**Website:** [soquarky.click](https://soquarky.click) · [artistso.com](https://artistso.com)  
**Support:** support@soquarky.click · **Crash Reports:** soquarky@artistso.com  

---

## Feature Matrix

### Drawing & Painting
| Feature | Status | Source |
|---------|--------|--------|
| 13 built-in brush presets | ✅ | — |
| Custom brush patterns | ✅ | ibisPaint |
| Pressure curves per brush | ✅ | ibisPaint |
| Velocity-based brush settings | ✅ | Clip Studio v5 |
| Brush smoothing / stabilizer | ✅ | FlipaClip |
| 28+ blend modes | ✅ | ibisPaint / HiPaint |
| Smart shape detection (8 types) | ✅ | Clip Studio v5 |
| Symmetry drawing (7 modes) | ✅ | HiPaint |
| Reference images | ✅ | HiPaint |
| Numeric keypad input | ✅ | ibisPaint |
| Floating panels (7 types) | ✅ | ibisPaint v14 |

### Layer System
| Feature | Status | Source |
|---------|--------|--------|
| Unlimited layers | ✅ | — |
| 16+ blend modes | ✅ | ibisPaint |
| Clipping masks | ✅ | HiPaint |
| Layer effects (shadow, glow, stroke) | ✅ | — |
| **Layer outlining** | ✅ NEW | Aseprite |
| Layer groups | ✅ | — |
| Adjustment layers | ✅ | — |

### Color System
| Feature | Status | Source |
|---------|--------|--------|
| Display P3 wide gamut | ✅ | Tab S10+ |
| HSB color picker | ✅ | — |
| Color harmony modes | ✅ | — |
| Quick eyedropper | ✅ | ibisPaint |
| **8 color themes** | ✅ NEW | HiPaint |
| Custom theme editor | ✅ NEW | HiPaint |
| Theme import/export | ✅ NEW | — |

### Animation
| Feature | Status | Source |
|---------|--------|--------|
| Multi-track timeline | ✅ | FlipaClip |
| Variable frame durations | ✅ | FlipaClip |
| Frame labels & keyframe flags | ✅ | FlipaClip |
| **Enhanced onion skin (8 modes)** | ✅ NEW | Aseprite |
| **Onion skin effect overlays (8 types)** | ✅ NEW | — |
| **Motion trails** | ✅ NEW | — |
| **Ghost frames** | ✅ NEW | — |
| **Multi-frame selection** | ✅ NEW | Aseprite/Resprite |
| **Batch editing (move, scale, rotate, flip, etc.)** | ✅ NEW | Aseprite |
| **Frame tags & color coding** | ✅ NEW | Aseprite |
| **Cel linking** | ✅ NEW | Aseprite |
| Audio tracks (6 tracks) | ✅ | FlipaClip |
| Voice recording | ✅ | FlipaClip |
| Rotoscoping | ✅ | FlipaClip |
| Draw outside canvas (2× oversize) | ✅ | FlipaClip |

### Puppet Mesh & Deformation
| Feature | Status | Source |
|---------|--------|--------|
| **Puppet warp** | ✅ | Clip Studio Paint |
| **Triangulated mesh generation** | ✅ NEW | — |
| **Mesh density control (6 levels)** | ✅ NEW | — |
| **Pin placement (6 types)** | ✅ NEW | — |
| **Bone hierarchy with IK** | ✅ NEW | — |
| **4 rig presets** (Humanoid, Quadruped, Face, Hand) | ✅ NEW | — |
| **Pose saving & interpolation** | ✅ NEW | — |
| **Vertex weight painting** | ✅ NEW | — |
| Liquify (6 modes) | ✅ | Clip Studio Paint |
| Smart shape detection | ✅ | Clip Studio v5 |

### On-Screen Controls (Aseprite/Resprite)
| Feature | Status | Source |
|---------|--------|--------|
| **Ctrl button** (toggle select) | ✅ NEW | Aseprite |
| **Shift button** (range select) | ✅ NEW | Aseprite |
| **Alt button** (subtract from selection) | ✅ NEW | Aseprite |
| Configurable position | ✅ NEW | — |
| Haptic feedback | ✅ NEW | — |
| Auto-hide option | ✅ NEW | — |

### Connectivity & Sharing
| Feature | Status | Notes |
|---------|--------|-------|
| **Google Sign-In** | ✅ NEW | User ↔ Google only; developer never sees credentials |
| **Share to YouTube** | ✅ NEW | Animations only; configurable title/description/privacy |
| **YouTube resolution up to 4K** | ✅ NEW | 480p to 4K export |
| **artistso.com integration** | ✅ NEW | Tutorials, demos, courses by @SoQuarky |

### Privacy & Data
| Feature | Status | Notes |
|---------|--------|-------|
| **Screen overlay permission** | ✅ NEW | Floating tools over other apps |
| **User-owned crash data** | ✅ NEW | Stored locally, opt-in sharing |
| **GitHub crash report posting** | ✅ NEW | User posts to their own repo |
| **Email crash reports** | ✅ NEW | to soquarky@artistso.com |
| Auto-save with crash recovery | ✅ | Clip Studio v5 |
| Timelapse recording (MP4/GIF) | ✅ | ibisPaint / HiPaint |

### Device Optimization (Samsung Galaxy Tab S10+)
| Feature | Status |
|---------|--------|
| 120Hz rendering with Choreographer | ✅ |
| S Pen 4096 pressure levels | ✅ |
| S Pen tilt & azimuth | ✅ |
| S Pen hover cursor | ✅ |
| Palm rejection (TOOL_TYPE filtering) | ✅ |
| Motion prediction | ✅ |
| Display P3 wide gamut | ✅ |
| Samsung DeX support | ✅ |
| Edge-to-edge immersive mode | ✅ |
| Keyboard shortcuts (B, E, L, Ctrl+Z, Ctrl+S) | ✅ |

---

## Architecture

- **Pattern:** MVI (Model-View-Intent) with pure state reducer
- **Layers:** Clean Architecture (Presentation → Domain → Data)
- **Language:** Kotlin with kotlinx.serialization, coroutines + Flow
- **DI:** Hilt (models defined, wiring pending)
- **DB:** Room (models defined, DAOs pending)
- **Rendering:** OpenGL ES 3.0 via NDK (stub)
- **Target:** arm64-v8a only (MediaTek Dimensity 9300+)
- **Min SDK:** 34 (Android 14) / **Target SDK:** 35

---

## Project Structure

```
SoCreate/
├── app/src/main/java/com/socreate/app/
│   ├── core/model/              # Domain models (MVI)
│   │   ├── AdvancedFeatures.kt  # 25+ feature models
│   │   ├── AdvancedIntents.kt   # 130+ feature intents
│   │   ├── AnimationAdvanced.kt # Onion skin, multi-frame, modifiers
│   │   ├── Branding.kt          # Developer identity
│   │   ├── BrushModels.kt       # 13 brush presets
│   │   ├── CanvasModels.kt      # Canvas, Project, Animation models
│   │   ├── DrawingIntents.kt    # Core drawing intents
│   │   ├── DrawingReducer.kt    # Pure state reducer
│   │   ├── DrawingState.kt      # 70+ state fields
│   │   ├── LayerModels.kt       # Layer system
│   │   ├── MviFramework.kt      # MVI base types
│   │   ├── PuppetMesh.kt        # Full puppet mesh system
│   │   ├── SoCreateColor.kt     # RGBA, HSB, Display P3
│   │   ├── StrokeModels.kt      # Stroke points with pressure/tilt
│   │   ├── TabS10Plus.kt        # Device profile & presets
│   │   ├── ThemeConfig.kt       # 8 color themes + custom
│   │   └── UserAccount.kt       # Auth, crash, YouTube, artistso
│   ├── engine/
│   │   ├── auth/
│   │   │   └── GoogleAuthHandler.kt  # Google Sign-In + YouTube
│   │   ├── brush/
│   │   │   ├── BrushEngine.kt
│   │   │   └── BrushPresets.kt
│   │   ├── canvas/
│   │   │   └── CanvasRenderer.kt
│   │   ├── crash/
│   │   │   └── CrashReportHandler.kt  # User-owned crash reports
│   │   ├── layer/
│   │   │   └── LayerCompositor.kt
│   │   ├── learning/
│   │   │   └── ArtistsoIntegration.kt # artistso.com content
│   │   ├── persistence/
│   │   │   └── AutoSaveEngine.kt
│   │   ├── puppet/
│   │   │   └── MeshEngine.kt          # Mesh generation + deformation
│   │   ├── renderer/
│   │   │   ├── LayerOutlineRenderer.kt # Aseprite-style outlines
│   │   │   ├── NativeRenderer.kt
│   │   │   ├── OnionSkinRenderer.kt    # Enhanced onion skin
│   │   │   ├── SmartShapeDetector.kt
│   │   │   ├── StrokeRenderer.kt
│   │   │   └── SymmetryRenderer.kt
│   │   ├── timelapse/
│   │   │   └── TimelapseRecorder.kt
│   │   └── undo/
│   │       └── UndoEngine.kt
│   ├── ui/
│   │   ├── drawing/
│   │   │   ├── DrawingActivity.kt
│   │   │   ├── DrawingCanvasView.kt
│   │   │   ├── DrawingViewModel.kt
│   │   │   └── ModifierButtonBar.kt    # On-screen Ctrl/Shift/Alt
│   │   ├── gallery/
│   │   │   └── GalleryActivity.kt
│   │   ├── settings/
│   │   │   └── SettingsActivity.kt     # (placeholder)
│   │   ├── crash/
│   │   │   └── CrashReportActivity.kt  # (placeholder)
│   │   ├── learning/
│   │   │   └── ArtistsoActivity.kt     # (placeholder)
│   │   └── theme/
│   │       └── ThemePickerActivity.kt  # (placeholder)
│   └── SoCreateApp.kt
├── app/src/main/
│   ├── AndroidManifest.xml       # All permissions & activities
│   ├── res/
│   │   ├── values/
│   │   │   ├── strings.xml       # 250+ strings
│   │   │   ├── colors.xml        # Theme colors
│   │   │   └── themes.xml        # Material3 dark
│   │   ├── xml/
│   │   │   ├── file_paths.xml    # FileProvider
│   │   │   └── backup_rules.xml
│   │   ├── drawable/             # 16 toolbar icons
│   │   ├── layout/               # Activity layouts
│   │   └── mipmap-anydpi-v26/    # Adaptive icon
│   └── cpp/
│       ├── socreate_renderer.cpp # OpenGL ES 3.0 stub
│       └── CMakeLists.txt
├── app/src/test/                 # 70+ unit tests
├── app/build.gradle.kts          # Build config + deps
├── build.gradle.kts
├── settings.gradle.kts
├── privacy-policy.html           # Full privacy policy
├── BUILD_GUIDE.md
└── README.md
```

---

## Build Options

See [BUILD_GUIDE.md](BUILD_GUIDE.md) for complete build instructions.

1. **Android Studio** — Open project, sync Gradle, Build APK
2. **GitHub Actions** — Push to main, download signed AAB from artifacts
3. **Termux on Tab S10+** — Install Android SDK in Termux, build on device

---

## Privacy Commitment

- **No data collection** — All artwork and project data stays on device
- **User-owned crash data** — Crash reports are stored locally and belong to the user
- **Google Sign-In is optional** — Only needed for YouTube sharing; connects user directly to Google
- **Developer never sees credentials** — Google tokens never pass through our servers
- **Opt-in sharing only** — Users choose whether to share crash reports via their own GitHub + email

See [privacy-policy.html](privacy-policy.html) for the complete privacy policy.

---

© 2026 Steven Michael Allen Owens. All rights reserved.  
SoCreate — Animate Your Imagination™  
An AdventuresInDrawing production
