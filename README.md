# DMRC Helper – Delhi Metro Companion App

A Jetpack Compose Android app that helps Delhi Metro commuters find routes, view the metro map, locate nearby parking, and access ticket booking information.

---

## Prerequisites

| Tool | Minimum Version | Notes |
|------|----------------|-------|
| [Android Studio](https://developer.android.com/studio) | Ladybug (2024.2.1) or newer | Required to build the project |
| JDK | 11 | Bundled with Android Studio |
| Android SDK | API 36 (Android 16) | Install via SDK Manager |
| Android device / emulator | API 24 (Android 7.0) or higher | Physical device or AVD |

> **Note:** The project uses Android Gradle Plugin **8.13.0** and Kotlin **2.0.21**, both of which require Android Studio Ladybug or a newer release.

---

## Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/samarthsg/DMRC-Helper.git
cd DMRC-Helper
```

### 2. Open in Android Studio

1. Launch **Android Studio**.
2. On the Welcome screen, click **Open** (or go to **File → Open…**).
3. Navigate to the cloned `DMRC-Helper` folder and click **OK**.
4. Wait for Gradle to sync. Android Studio will download all required dependencies automatically. This may take a few minutes on the first run.

### 3. Install the Required SDK

If Android Studio shows a **"Missing SDK"** or **"Install missing platform"** error:

1. Open **File → Project Structure → SDK Location** and confirm the Android SDK path is set.
2. Open **Tools → SDK Manager**.
3. Under **SDK Platforms**, tick **Android 16 (API 36)** and click **Apply**.
4. Under **SDK Tools**, ensure **Android SDK Build-Tools** and **Android Emulator** are installed.

---

## Running the App

### Option A – Android Emulator (AVD)

1. Open **Tools → Device Manager** (or click the device icon in the toolbar).
2. Click **Create Device**.
3. Choose a phone hardware profile (e.g., **Pixel 8**) and click **Next**.
4. Select a system image with **API 24 or higher** (API 36 recommended). Click **Download** next to the image if it is not already installed, then click **Next**.
5. Accept the default AVD name and click **Finish**.
6. Click the green **▶ Run** button (or press **Shift + F10**) to build and launch the app on the emulator.

> **Location on the emulator:** The app requests location permission to show nearby parking. In the emulator you can simulate a location via **Extended Controls (⋮) → Location → Set Location**.

### Option B – Physical Android Device

1. On your Android device, enable **Developer Options**:
   - Go to **Settings → About Phone** and tap **Build Number** seven times.
2. In **Developer Options**, enable **USB Debugging**.
3. Connect the device to your computer with a USB cable.
4. Android Studio will detect the device. Select it in the device drop-down next to the Run button.
5. Click the green **▶ Run** button (or press **Shift + F10**).

> The app requires **Android 7.0 (API 24)** or higher.

---

## Permissions

The app requests the following permissions at runtime:

| Permission | Purpose |
|------------|---------|
| `ACCESS_FINE_LOCATION` | Show your location on the metro map and find nearby parking |
| `ACCESS_COARSE_LOCATION` | Fallback location for parking search |
| `INTERNET` | Load OpenStreetMap tiles and routing data |

Grant these permissions when prompted on first launch to enable all features.

---

## Project Structure

```
DMRC-Helper/
├── app/
│   └── src/
│       └── main/
│           ├── assets/
│           │   ├── dmrc_stations.json          # Metro station data
│           │   └── delhi_metro_parking_clean.json  # Parking lot data
│           ├── java/com/example/metrohelper/
│           │   ├── MainActivity.kt             # Single-activity entry point (Compose NavHost)
│           │   └── ui/theme/                   # Material 3 theme, colours, typography
│           └── res/                            # Icons, drawables, strings
├── build.gradle.kts                            # Root build script
├── app/build.gradle.kts                        # App module build script
├── gradle/libs.versions.toml                   # Version catalog
└── settings.gradle.kts
```

---

## Key Dependencies

| Library | Purpose |
|---------|---------|
| Jetpack Compose + Material 3 | UI framework |
| Navigation Compose | In-app screen navigation |
| OSMDroid | OpenStreetMap-based metro map |
| Google Play Services Location | Device location |
| OkHttp | Routing API requests |
| Kotlin Coroutines | Asynchronous operations |

---

## Troubleshooting

| Problem | Solution |
|---------|----------|
| Gradle sync fails with "Could not resolve…" | Check your internet connection and retry via **File → Sync Project with Gradle Files** |
| Build error: "Installed Build Tools revision … is corrupted" | Open SDK Manager and reinstall Build Tools |
| App crashes immediately on launch | Make sure location permissions are granted, and that your emulator/device has an internet connection |
| Map tiles don't load | Ensure the `INTERNET` permission is granted and the device/emulator has internet access |
| "INSTALL_FAILED_INSUFFICIENT_STORAGE" | Free up space on the emulator by wiping its data (**Device Manager → Wipe Data**) |

---

## Contributing

Pull requests are welcome. Please open an issue first to discuss what you would like to change.

## License

This project is open-source. See the repository for license details.
