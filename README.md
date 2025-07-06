# Motiv8Me

Motiv8Me is an Android app designed to help users build positive habits and break unwanted ones through visual and motivational reinforcement. By automatically changing the home screen wallpaper to habit-related images and delivering motivational quotes via notifications, the app provides a constant, subtle reminder to stay on track.

## Features

*   **Guided Onboarding:** A streamlined first-launch experience to help users select their initial habit, set preferences for wallpaper changes and notifications, and grant necessary permissions smoothly. Includes conditional progression to ensure all steps are completed.
*   **Habit Selection:** Choose from a list of predefined habits to focus on (e.g., Stop Smoking, Exercise More).
*   **Automatic Wallpaper Change:** The app dynamically changes the device's home screen wallpaper based on the selected habit and user-defined frequency.
*   **Motivational Notifications:** Receive timely motivational quotes via notifications. Frequency and overall enablement of notifications are configurable.
*   **Comprehensive Settings Management:**
    *   **Personalization:** Easily adjust your selected Focus Habit, Wallpaper Frequency, and Notification Preferences (including enabling/disabling notifications and setting their frequency).
    *   **App Permissions:** Clearly view and manage permissions for Wallpaper and Notifications, with direct links to system settings.
    *   **Upgrade Information:** Details on features available in potential Pro versions.
*   **Modern UI:** Built with Jetpack Compose and Material Design 3, offering a clean interface that supports both light and dark themes.
*   **Performance & Reliability:** Designed to be efficient and reliable, with minimal impact on battery life using WorkManager for background tasks.

*Note: This version focuses on the free tier features. A future paid tier may offer advanced features like custom habits and wallpaper uploads.*

## Technical Stack

*   **Language:** Kotlin
*   **UI Toolkit:** Jetpack Compose with Material Design 3
*   **Architecture:** MVVM (Model-View-ViewModel)
*   **State Management:** Unidirectional Data Flow using ViewModel and StateFlow/SharedFlow
*   **Background Processing:** WorkManager for scheduling wallpaper changes and notifications
*   **Persistence:** Preferences DataStore for storing user settings
*   **Dependency Injection:** Hilt
*   **Navigation:** Compose Navigation
*   **Image Loading:** Coil (if still used for wallpapers)
*   **Permissions:** Runtime handling for `SET_WALLPAPER` (for direct wallpaper setting) and `POST_NOTIFICATIONS` (Android 13+).

## Getting Started

### Prerequisites

*   Android Studio (latest stable version)
*   JDK 17 or higher (Updated from JDK 11 as it's a common modern Android practice)

### Setup

1.  Clone the repository:
    ```bash
    git clone https://github.com/Manikandan-TK/motiv8me.git
    ```
2.  Open the project in Android Studio.
3.  Build the project to resolve dependencies (Sync Gradle).
4.  Run the app on an emulator or physical device.

## Project Structure

The project is organized into the following main directories:

*   `app/src/main/java/com/example/motiv8me/`
    *   `data/`: Manages data sources, including DataStore for settings and potentially habit/image repositories.
    *   `di/`: Hilt modules for dependency injection.
    *   `domain/`: Contains business logic, use cases, and data models.
    *   `service/`: WorkManager workers for background tasks (e.g., `WallpaperWorker`, `NotificationWorker`).
    *   `ui/`: User interface components, including:
        *   `features/`: Screen-specific Composables, ViewModels, and navigation (e.g., `onboarding`, `settings`, `notification_settings`).
        *   `theme/`: App theme, colors, typography.
        *   `components/`: Reusable UI components.
    *   `util/`: Utility classes, constants, and extension functions.

## Contributing

We welcome contributions to Motiv8Me! Please ensure your code adheres to the project's coding standards and best practices.
(Consider adding more specific contribution guidelines here if you plan to open it up, e.g., issue templates, PR process).

## License

This project is licensed under the MIT License - see the `LICENSE` file for details.
