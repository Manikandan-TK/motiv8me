Motiv8Me
Motiv8Me is an Android app designed to help users break bad habits through visual and motivational reinforcement. By automatically changing the home screen wallpaper to habit-related images and delivering motivational quotes via notifications, the app provides a constant, subtle reminder to stay on track.
Features

Habit Selection: Choose from a list of predefined habits to focus on.
Frequency Selection: Set how often the wallpaper changes (e.g., every 30 minutes, hourly, daily).
Automatic Wallpaper Change: The app changes the home screen wallpaper based on the selected habit and frequency.
Motivational Notifications: Receive motivational quotes via notifications at a user-defined frequency.
Settings Management: Easily adjust your habit, wallpaper frequency, and notification preferences.
Modern UI: Built with Jetpack Compose and Material Design 3, supporting both light and dark themes.
Onboarding: A streamlined first-launch experience to guide you through setup.
Performance & Reliability: Designed to be efficient and reliable, with minimal impact on battery life.

Note: This version focuses on the free tier features. A future paid tier will offer advanced features like custom habits and wallpaper uploads.
Technical Stack

Language: Kotlin
UI Toolkit: Jetpack Compose with Material Design 3
Architecture: MVVM (Model-View-ViewModel)
State Management: Unidirectional Data Flow using ViewModel and StateFlow
Background Processing: WorkManager for scheduling wallpaper changes and notifications
Persistence: Preferences DataStore for storing user settings
Dependency Injection: Hilt
Navigation: Compose Navigation
Image Loading: Coil
Permissions: Required for setting wallpaper, posting notifications, and handling boot events

Getting Started
Prerequisites

Android Studio (latest stable version)
JDK 11 or higher

Setup

Clone the repository:git clone https://github.com/Manikandan-TK/motiv8me.git


Open the project in Android Studio.
Build the project to resolve dependencies.
Run the app on an emulator or physical device.

Project Structure
The project is organized into the following main directories:

data/: Manages data sources, including DataStore for settings.
domain/: Contains business logic, data models, and use cases.
di/: Hilt modules for dependency injection.
service/: WorkManager workers for background tasks.
ui/: User interface components, including navigation, themes, and feature-specific screens.
util/: Utility classes and constants.

Contributing
We welcome contributions to Motiv8Me! Please ensure your code adheres to the project's coding standards and best practices.
License
This project is licensed under the MIT License - see the LICENSE file for details.
