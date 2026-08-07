# ToDoApp

Android todo app built with Jetpack Compose. Sign in with Google or continue as a guest, then create, edit, and complete tasks with optional images and due dates.

## Features

- **Auth** — Google Sign-In via Credential Manager, or guest (anonymous) access
- **Todos** — list, create, view/edit details, mark complete
- **Images** — attach from camera or gallery (Coil)
- **Due dates** — optional deadline on each task
- **Offline cache** — Room-backed local store; remote is source of truth when available
- **Pluggable remote** — `ApiService` currently bound to local JSON files; Firestore implementation is ready to swap in

## Tech stack

| Area | Choice |
|------|--------|
| UI | Jetpack Compose, Material 3 |
| Architecture | MVVM + single-activity navigation |
| DI | Hilt |
| Local DB | Room |
| Auth / backend | Firebase Auth, Firestore (optional), Credential Manager |
| Images | Coil |
| Language / JDK | Kotlin, Java 17 |
| Min / target SDK | 24 / 37 |

## Architecture

```
UI (Screens + ViewModels)
        ↓
   TodoManager
    ↙        ↘
ApiService   LocalRepService (Room)
```

- ViewModels talk to `TodoManager` for CRUD and sync
- Remote succeeds → update Room cache and emit state
- Remote fetch fails → keep serving Room data
- `SessionManager` holds `userId` so remotes work for Firebase users and local guests

Diagrams and package map: [docs/architecture.html](docs/architecture.html)  
Screen flow: [docs/screen-flow.html](docs/screen-flow.html)

## Project structure

```
app/src/main/java/com/nch/todoapp/
├── ui/           # login, list, create, details, theme, common
├── data/
│   ├── auth/     # AuthRepository, FirebaseAuth, SessionManager
│   ├── local/    # Room DB, DAO, LocalRepService
│   ├── remote/   # ApiService + LocalFile / Firebase / Fake
│   ├── manager/  # TodoManager
│   └── model/    # TodoItem
├── di/           # Hilt modules
├── AppNavigation.kt
└── MainActivity.kt
```

## Prerequisites

- Android Studio (recent stable) or JDK 17 + Android SDK
- A device or emulator (API 24+)
- Firebase project with Auth (Google + Anonymous) if you use Google Sign-In

### Firebase setup

1. Create a Firebase project and enable **Google** and **Anonymous** sign-in.
2. Add an Android app with package `com.nch.todoapp`.
3. Download `google-services.json` into `app/`.
4. Ensure `default_web_client_id` in `app/src/main/res/values/strings.xml` matches the **Web** OAuth client from Firebase (required by Credential Manager).

### Swap remote backend

In `di/AppModule.kt` (`RepositoryModule`), bind `FirebaseApiService` instead of `LocalFileApiService` when you want Firestore as the remote source of truth.

## Build & run

Open the project in Android Studio and run the `app` configuration, or from the CLI:

```bash
./gradlew :app:assembleDebug
./gradlew :app:installDebug
```

## Tests

```bash
./gradlew :app:testDebugUnitTest
```

Unit coverage includes `TodoManager` (`app/src/test/.../TodoManagerTest.kt`).

## License

Private / unpublished unless otherwise noted.
