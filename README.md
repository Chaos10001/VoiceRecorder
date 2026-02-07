# Voice Recording App

A modern Android application for recording and managing voice notes, built with Kotlin and Jetpack Compose.

## Features

- **Audio Recording**: High-quality voice recording with real-time status.
- **Recordings Management**: View a list of all your saved voice recordings.
- **Playback**: Integrated audio player to listen to your recordings.
- **Modern UI**: Built with Jetpack Compose and Material 3 for a smooth, responsive user experience.
- **Local Storage**: Persistent storage of recording metadata using Room database.
- **Permissions Handling**: Seamless permission requests using Accompanist.

## Tech Stack

- **Language**: [Kotlin](https://kotlinlang.org/)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose)
- **Dependency Injection**: [Hilt](https://developer.android.com/training/dependency-injection/hilt-android)
- **Database**: [Room](https://developer.android.com/training/data-storage/room)
- **Asynchronous Work**: [Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) & [Flow](https://kotlinlang.org/docs/flow.html)
- **Navigation**: [Compose Navigation](https://developer.android.com/jetpack/compose/navigation)
- **Audio API**: Android Media APIs
- **Architecture**: MVVM (Model-View-ViewModel) with a clean layered structure (`data`, `domain`, `presentation`).

## Project Structure

- `data/`: Contains repository implementations, data sources (Room DB), and mappers.
- `domain/`: Contains business logic, models, and repository interfaces.
- `presentation/`: Contains UI components, screens, ViewModels, and theme definitions.
- `di/`: Hilt modules for dependency injection.

## Getting Started

### Prerequisites

- Android Studio Ladybug or newer.
- Android SDK 24 or higher.

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/Chaos10001/VoiceRecorder.git
   ```
2. Open the project in Android Studio.
3. Sync the project with Gradle files.
4. Run the app on an emulator or a physical device.```
