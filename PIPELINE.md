# Build Pipeline
** SoCreate Android App with Web Testing Platform **

This pipeline enables:
1. Android APK builds (debug/release)
2. Web platform for rapid testing/iteration
3. Automated testing and CI/CD

## Quick Start

### Android Build
```bash
./gradlew assembleDebug      # Build debug APK
./gradlew assembleRelease    # Build release APK (needs keystore)
./gradlew connectedAndroidTest # Run tests on device/emulator
```

### Web Build
```bash
cd web
npm install
npm run dev                  # Development server
npm run build                # Production build
npm run preview              # Preview production build
```

### Testing Workflow
1. Make changes to shared code in `/shared/`
2. Test rapidly in web version: `cd web && npm run dev`
3. Build Android app when ready: `./gradlew assembleDebug`
4. Deploy web for team testing: `cd web && npm run build`

## Project Structure
```
SoCreate/
├── app/                    # Android application
│   └── src/main/
│       ├── java/com/socreate/app/
│       │   ├── core/      # MVI architecture, models
│       │   ├── data/      # Room database, DataStore
│       │   ├── di/        # Hilt dependency injection
│       │   ├── engine/    # Brush, canvas, timeline engines
│       │   └── ui/        # Compose UI, activities
│       └── res/           # Android resources
├── web/                    # Web testing platform
│   └── src/
│       ├── components/    # UI components
│       ├── engine/        # Canvas & drawing engine
│       ├── store/         # State management
│       └── utils/         # Shared utilities
├── shared/                 # Shared code (Kotlin/JS)
└── docs/                   # Documentation
```
