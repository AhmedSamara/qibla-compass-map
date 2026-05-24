Opening the Wear module in Android Studio

This folder is a minimal Gradle project intended to be opened directly in Android Studio.

Steps:

1. Open Android Studio.
2. Choose "Open" and select the `watch-app/wear` folder in this repo.
3. Let Android Studio sync and install any missing Gradle wrapper/AGP.
4. After Gradle sync, open the `app` module and run on a Wear emulator or connected device.

Notes:
- This scaffold is minimal: Android Studio may prompt to update Gradle plugin or the Kotlin version.
- If you don't have an AGP/Gradle wrapper in this environment, Android Studio will offer to download it.
- The module uses Compose and Play Services location; ensure you have network access during the first sync.
