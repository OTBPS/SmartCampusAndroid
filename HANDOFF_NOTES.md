# Smart Campus Android - Handoff Notes

## Quick Setup
1. Open this folder in Android Studio.
2. Let Gradle sync complete.
3. Build from Android Studio, or run:
   `./gradlew assembleDebug` (Linux/macOS) or `gradlew.bat assembleDebug` (Windows).

## Gradle Wrapper
- The Gradle wrapper is included in this project:
  - `gradlew`
  - `gradlew.bat`
  - `gradle/wrapper/gradle-wrapper.properties`
  - `gradle/wrapper/gradle-wrapper.jar`

## Handoff Hygiene
- Do not commit or hand off `local.properties` (machine-local SDK path).
- Build outputs are generated and not source:
  - root `build/`
  - module `app/build/`
  - `.gradle/`

## SDK Expectations
- `minSdk = 26`
- `compileSdk = 35`
- `targetSdk = 35`
