# Motiv8Me

Motiv8Me is an Android application designed to help users break bad habits through automated motivational wallpapers. The app changes your device's wallpaper at customizable intervals to keep you motivated throughout your journey of breaking unwanted habits.

## Features

- **Multiple Habit Support**: Choose from various habits to break:
  - Quit Smoking
  - Stop Procrastinating
  - Less Screen Time
  - Quit Snacking

- **Customizable Intervals**: Choose how often the wallpaper changes:
  - 1 Hour
  - 3 Hours
  - 6 Hours
  - 12 Hours
  - 24 Hours

- **Background Service**: Continues to operate even when the app is closed
- **Active Mode Monitoring**: View when the next wallpaper change will occur
- **Material Design 3**: Modern Android UI with Material You theming

## Technical Requirements

- Android SDK 26 (Android 8.0) or higher
- Kotlin 1.9+
- Jetpack Compose for UI
- Uses Foreground Service for reliable wallpaper changes

## Project Structure

```
app/
├── src/
│   └── main/
│       ├── assets/
│       │   └── wallpapers/          # Motivational wallpapers organized by habit
│       ├── java/com/example/motiv8me/
│       │   ├── MainActivity.kt      # Habit selection screen
│       │   ├── SettingsActivity.kt  # Interval selection screen
│       │   ├── ActiveModeActivity.kt# Active monitoring screen
│       │   ├── WallpaperService.kt  # Background service
│       │   └── Habit.kt            # Data model
│       └── res/                     # Resources (strings, themes, etc.)
```

## Setup Instructions

1. Clone the repository
2. Open the project in Android Studio
3. Add your motivational wallpapers to the assets folder using this structure:
   ```
   assets/wallpapers/
   ├── quit_smoking/
   ├── stop_procrastinating/
   ├── less_screen_time/
   └── quit_snacking/
   ```
   Each folder should contain wallpapers named: `{habit_folder}_1.jpg` through `{habit_folder}_5.jpg`

4. Build and run the project

## Permissions Required

The app requires the following permissions in the Android Manifest:
- `android.permission.SET_WALLPAPER`: To change device wallpaper
- `android.permission.FOREGROUND_SERVICE`: For reliable background operation

## Contributing

Feel free to contribute to this project by:
1. Forking the repository
2. Creating a feature branch
3. Making your changes
4. Submitting a pull request

## License

This project is open source and available under the MIT License.
