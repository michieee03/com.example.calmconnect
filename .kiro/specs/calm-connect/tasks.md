# Implementation Plan: Calm Connect

## Overview

Incremental implementation of the Calm Connect Android app in Kotlin using MVC architecture. Tasks build from project setup through data layer, controllers, UI, and integration — each step wired into the previous before moving on.

## Tasks

- [x] 1. Project setup and core infrastructure
  - Convert the existing Java stub to Kotlin; configure `build.gradle.kts` with all required dependencies (Room, LiveData, WorkManager, Coroutines, Navigation, MPAndroidChart, Glide, Kotest, play-services-maps, play-services-location, places)
  - Add `google_maps_api_key` to `local.properties` and expose via `BuildConfig`
  - Create the base package structure: `model/`, `controller/`, `view/`, `db/`, `util/`
  - Define all controller interfaces (`MoodController`, `SoundController`, `QuoteController`, `StressReliefController`, `StudyTimerController`, `GameController`, `LocationController`, `RoutineController`, `ProfileController`, `NotificationController`, `LoginController`)
  - Define all enums: `SoundType`, `BreathingPattern`, `GameType`, `TimerPhase`
  - _Requirements: 14.1_

- [x] 2. Room database and data models
  - [x] 2.1 Define Room entities and DAOs
    - Implement `MoodEntry`, `Quote`, `JournalEntry`, `RoutineStep`, `UserProfile` entity data classes with `@Entity` annotations
    - Implement DAOs: `MoodDao`, `QuoteDao`, `JournalDao`, `RoutineDao`, `ProfileDao` with insert/update/delete/query methods returning `LiveData` where appropriate
    - Create `AppDatabase` with `@Database` annotation wiring all entities and DAOs
    - Implement `CalmPlace` in-memory data class
    - _Requirements: 1.1, 2.1, 4.3, 5.4, 9.1, 10.1_

  - [ ]* 2.2 Write property test for MoodEntry round-trip (Property 1)
    - **Property 1: Mood save round-trip**
    - **Validates: Requirements 1.1, 2.1**

  - [x] 2.3 Implement Repository classes
    - Implement `MoodRepository`, `QuoteRepository`, `JournalRepository`, `RoutineRepository`, `ProfileRepository` wrapping their respective DAOs
    - All DB operations run on `Dispatchers.IO` via coroutines
    - _Requirements: 1.1, 2.1, 4.3, 5.4, 9.1, 10.1_

- [x] 3. Checkpoint — Ensure all tests pass, ask the user if questions arise.

- [x] 4. Mood Check-In and History controllers
  - [x] 4.1 Implement `MoodControllerImpl`
    - Validate emotion against `VALID_EMOTIONS_SET`; reject empty/invalid emotion with `Result.Error`
    - Reject notes exceeding 500 characters
    - Persist `MoodEntry` via `MoodRepository.insert()`
    - Expose `getMoodHistory()` as `LiveData<List<MoodEntry>>` ordered by timestamp ascending
    - Implement `deleteMood(id)` via repository
    - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 2.1, 2.2, 2.3_

  - [ ]* 4.2 Write property test for mood validation — invalid emotion (Property 2)
    - **Property 2: Mood validation rejects invalid inputs**
    - **Validates: Requirements 1.2, 1.3**

  - [ ]* 4.3 Write property test for mood note length boundary (Property 3)
    - **Property 3: Mood note length boundary**
    - **Validates: Requirements 1.4, 1.5**

  - [ ]* 4.4 Write property test for mood history ordering (Property 4)
    - **Property 4: Mood history ordering**
    - **Validates: Requirements 2.2**

- [x] 5. Quote of the Day controller
  - [x] 5.1 Implement `QuoteControllerImpl`
    - Seed the bundled quotes list in `QuoteRepository` on first run
    - Implement `getDailyQuote()` using `hashDate(today) MOD quotes.size` for deterministic selection
    - Implement `saveToFavorites`, `removeFavorite`, `getFavoriteQuotes`, `searchQuotes` (case-insensitive text/author match)
    - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 4.6_

  - [ ]* 5.2 Write property test for daily quote determinism (Property 6)
    - **Property 6: Daily quote determinism**
    - **Validates: Requirements 4.1**

  - [ ]* 5.3 Write property test for daily quote index bounds (Property 7)
    - **Property 7: Daily quote index bounds**
    - **Validates: Requirements 4.2**

  - [ ]* 5.4 Write property test for favorite quote round-trip (Property 8)
    - **Property 8: Favorite quote round-trip**
    - **Validates: Requirements 4.3, 4.4**

  - [ ]* 5.5 Write property test for favorite removal exclusion (Property 9)
    - **Property 9: Favorite removal exclusion**
    - **Validates: Requirements 4.5**

  - [ ]* 5.6 Write property test for quote search relevance (Property 10)
    - **Property 10: Quote search relevance**
    - **Validates: Requirements 4.6**

- [x] 6. Stress Relief controller (breathing, meditation, journaling)
  - [x] 6.1 Implement `StressReliefControllerImpl`
    - Implement `startBreathingExercise(pattern)`: resolve phase sequence from `BreathingPattern`, iterate phases in declared order, expose current phase via `LiveData`
    - Implement `startMeditation(durationMinutes)`: countdown coroutine emitting remaining seconds via `LiveData`
    - Implement `saveJournalEntry`: validate non-empty text ≤ 2000 chars, persist `JournalEntry` via `JournalRepository`
    - Implement `getJournalEntries()` returning `LiveData<List<JournalEntry>>`
    - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5, 5.6, 5.7_

  - [ ]* 6.2 Write property test for breathing phase order (Property 11)
    - **Property 11: Breathing phase order**
    - **Validates: Requirements 5.1**

  - [ ]* 6.3 Write property test for journal entry round-trip (Property 12)
    - **Property 12: Journal entry round-trip**
    - **Validates: Requirements 5.4, 5.7**

  - [ ]* 6.4 Write property test for journal entry validation (Property 13)
    - **Property 13: Journal entry validation**
    - **Validates: Requirements 5.5, 5.6**

- [x] 7. Pomodoro Study Timer controller
  - [x] 7.1 Implement `StudyTimerControllerImpl`
    - Validate `workMinutes ∈ [1,60]` and `breakMinutes ∈ [1,30]`; return error otherwise
    - Run work/break countdown loop via coroutine, emitting `TimerState` via `LiveData`
    - Ensure `remainingSeconds` never decrements below 0
    - Increment `completedPomodoros` after each work phase; apply LONG_BREAK every 4th pomodoro
    - Implement `pause()`, `resume()`, `reset()`
    - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5, 6.6, 6.7, 6.8, 6.9_

  - [ ]* 7.2 Write property test for Pomodoro remainingSeconds non-negative (Property 14)
    - **Property 14: Pomodoro remainingSeconds non-negative**
    - **Validates: Requirements 6.4**

  - [ ]* 7.3 Write property test for completedPomodoros monotonically increases (Property 15)
    - **Property 15: Pomodoro completedPomodoros monotonically increases**
    - **Validates: Requirements 6.5**

  - [ ]* 7.4 Write property test for Pomodoro phase transition rule (Property 16)
    - **Property 16: Pomodoro phase transition rule**
    - **Validates: Requirements 6.6**

  - [ ]* 7.5 Write property test for Pomodoro input validation (Property 17)
    - **Property 17: Pomodoro input validation**
    - **Validates: Requirements 6.2**

- [x] 8. Checkpoint — Ensure all tests pass, ask the user if questions arise.

- [x] 9. Mindful Games controller
  - [x] 9.1 Implement `GameControllerImpl`
    - Implement `startGame(type)` returning a `GameSession` for each of TAPPING, MEMORY, BREATHING
    - Implement `recordScore(type, score)`: update stored high score only if new score is greater
    - Implement `getHighScore(type)`: return max recorded score for the given type
    - Store high scores in `SharedPreferences` or a lightweight Room table
    - _Requirements: 7.1, 7.2, 7.3, 7.4_

  - [ ]* 9.2 Write property test for game high score is maximum (Property 18)
    - **Property 18: Game high score is maximum of all recorded scores**
    - **Validates: Requirements 7.2, 7.3**

- [x] 10. Location controller (GPS + Google Maps)
  - [x] 10.1 Implement `LocationControllerImpl`
    - Implement `requestCurrentLocation(callback)` using `FusedLocationProviderClient`
    - Implement `findNearbyPlaces(latLng, radiusMeters)`: query Places API for types [park, library, spa, natural_feature, campground], deduplicate by `placeId`, sort by ascending distance, return via `LiveData`
    - Implement `searchPlaces(query, latLng)` for manual text search fallback
    - Handle permission-denied case: show rationale dialog and offer manual search
    - Handle API failure: show error with retry; surface cached results with last-updated timestamp
    - _Requirements: 8.1, 8.2, 8.3, 8.4, 8.5, 8.6_

  - [ ]* 10.2 Write property test for nearby places within radius (Property 19)
    - **Property 19: Nearby places within radius**
    - **Validates: Requirements 8.2**

  - [ ]* 10.3 Write property test for no duplicate placeIds (Property 20)
    - **Property 20: Nearby places no duplicate placeIds**
    - **Validates: Requirements 8.3**

- [x] 11. Routine controller
  - [x] 11.1 Implement `RoutineControllerImpl`
    - Implement `getTodayRoutine()` returning `LiveData<List<RoutineStep>>` filtered by today's date
    - Implement `markStepComplete(stepId)`: validate step exists for today, set `isCompleted = true`, return error if not found
    - Implement `getCompletionPercentage()`: `completedCount / totalCount`, clamped to `[0.0, 1.0]`; return 0.0 for empty list
    - Implement `resetDailyRoutine()`: set all today's steps to `isCompleted = false`
    - Seed default routine steps for today on first launch
    - _Requirements: 9.1, 9.2, 9.3, 9.4, 9.5, 9.6, 9.7_

  - [ ]* 11.2 Write property test for routine completion percentage bounds (Property 21)
    - **Property 21: Routine completion percentage bounds**
    - **Validates: Requirements 9.4**

  - [ ]* 11.3 Write property test for routine step complete round-trip (Property 22)
    - **Property 22: Routine step complete round-trip**
    - **Validates: Requirements 9.2**

- [x] 12. Profile and dark mode controller
  - [x] 12.1 Implement `ProfileControllerImpl`
    - Implement `getProfile()` returning `LiveData<UserProfile>` from `ProfileRepository`
    - Implement `updateName(name)`: reject empty string with `Result.Error`; persist and emit updated profile
    - Implement `updateProfilePicture(uri)`: validate URI is readable; persist and emit updated profile
    - Implement `toggleDarkMode(enabled)`: persist `isDarkMode` and emit updated profile; apply `AppCompatDelegate.setDefaultNightMode` accordingly
    - Implement `logout()`: clear session state and navigate to login screen
    - _Requirements: 10.1, 10.2, 10.3, 10.4, 10.5, 10.6, 11.1, 11.2, 11.3_

  - [ ]* 12.2 Write property test for profile name update round-trip (Property 23)
    - **Property 23: Profile name update round-trip**
    - **Validates: Requirements 10.2**

  - [ ]* 12.3 Write property test for profile name rejects empty string (Property 24)
    - **Property 24: Profile name rejects empty string**
    - **Validates: Requirements 10.3**

  - [ ]* 12.4 Write property test for dark mode preference persistence (Property 25)
    - **Property 25: Dark mode preference persistence**
    - **Validates: Requirements 10.5, 11.3**

- [x] 13. Notification and reminder controller
  - [x] 13.1 Implement `NotificationControllerImpl`
    - Implement `scheduleDaily(hour, minute, message)` using `WorkManager` with a `PeriodicWorkRequest` targeting the specified time
    - Implement `ReminderWorker` that builds and delivers the push notification via `NotificationCompat`
    - Implement `cancelReminder()`: cancel the WorkManager task and update `isReminderActive` state
    - Implement `isReminderActive()`: return current scheduling state
    - Handle `POST_NOTIFICATIONS` permission denial on Android 13+: inform user and provide settings shortcut
    - Wire notification tap to deep-link to `HomeFragment`
    - _Requirements: 12.1, 12.2, 12.3, 12.4, 12.5, 12.6_

  - [ ]* 13.2 Write property test for reminder active state round-trip (Property 26)
    - **Property 26: Reminder active state round-trip**
    - **Validates: Requirements 12.3, 12.4**

- [x] 14. Sound controller
  - [x] 14.1 Implement `SoundControllerImpl`
    - Implement `play(soundType)`: load corresponding audio asset via `MediaPlayer`, request audio focus, begin streaming
    - Implement `pause()` and `stop()` with proper resource release on stop
    - Implement `setTimer(durationMinutes)`: schedule auto-stop via `Handler.postDelayed`
    - Implement `setVolume(level)`: apply to active `MediaPlayer` instance
    - Handle audio focus denial: release resources and notify View
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6_

  - [ ]* 14.2 Write property test for soundscape timer auto-stop (Property 5)
    - **Property 5: Soundscape timer auto-stop**
    - **Validates: Requirements 3.2**

- [x] 15. Checkpoint — Ensure all tests pass, ask the user if questions arise.

- [x] 16. Splash screen and login view
  - [x] 16.1 Implement `SplashActivity`
    - Display splash screen layout for a brief duration, then navigate to `LoginActivity`
    - _Requirements: 13.1_

  - [x] 16.2 Implement `LoginActivity` and `LoginController`
    - Build login form layout with username and password fields
    - Implement `LoginControllerImpl.validateLogin(username, password)`: return error if either field is empty; show field-level error in View
    - On successful validation, navigate to `MainActivity`
    - _Requirements: 13.2, 13.3_

- [x] 17. Main activity and bottom navigation
  - Implement `MainActivity` hosting a `NavHostFragment` wired to the Navigation graph
  - Configure `BottomNavigationView` with four tabs: Home, Today, Tools, Profile
  - Ensure bottom nav remains visible on all main fragments and highlights the active tab
  - _Requirements: 14.1, 14.2, 14.3, 14.4, 14.5, 14.6_

- [x] 18. Home fragment — Mood Check-In UI
  - Build `HomeFragment` layout with emotion selector (emoji/chip group) and optional note input
  - Wire to `MoodControllerImpl`: show confirmation on success, show error on invalid input
  - Display today's daily quote via `QuoteControllerImpl.getDailyQuote()`
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 4.1_

- [x] 19. Today fragment — Routine UI
  - Build `TodayFragment` layout with a `RecyclerView` of `RoutineStep` items and a progress bar
  - Wire to `RoutineControllerImpl`: observe `getTodayRoutine()`, handle step completion taps, update progress bar from `getCompletionPercentage()`
  - _Requirements: 9.1, 9.2, 9.3, 9.4, 9.5, 9.6_

- [x] 20. Tools fragment and sub-screens
  - [x] 20.1 Build `ToolsFragment` as a menu/grid navigating to sub-screens
    - _Requirements: 14.4_

  - [x] 20.2 Build `SoundscapesFragment`
    - Sound type selector, play/pause/stop controls, volume slider, timer picker
    - Wire to `SoundControllerImpl`
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6_

  - [x] 20.3 Build `StressReliefFragment` with tabs for Breathing, Meditation, Journal
    - Breathing tab: pattern selector, animated circle, phase label; wire to `StressReliefControllerImpl`
    - Meditation tab: duration picker, countdown display
    - Journal tab: text input, save button, `RecyclerView` of past entries
    - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5, 5.6, 5.7_

  - [x] 20.4 Build `PomodoroFragment`
    - Work/break duration inputs, timer display, phase label, start/pause/reset controls
    - Wire to `StudyTimerControllerImpl` observing `getSessionState()`
    - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5, 6.6, 6.7, 6.8, 6.9_

  - [x] 20.5 Build `GamesFragment` with three game sub-screens
    - `TappingGameFragment`: tap target, score counter
    - `MemoryGameFragment`: card grid, match logic
    - `BreathingGameFragment`: breath-paced interaction
    - Wire each to `GameControllerImpl` for score recording and high score display
    - _Requirements: 7.1, 7.2, 7.3, 7.4_

  - [x] 20.6 Build `QuotesFragment`
    - Display daily quote, favorite toggle, search bar, favorites `RecyclerView`
    - Wire to `QuoteControllerImpl`
    - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 4.6_

  - [x] 20.7 Build `NearbyPlacesFragment`
    - Embed `SupportMapFragment`, request location permission, display markers for returned `CalmPlace` results
    - Show rationale dialog on permission denial; show retry snackbar on API failure
    - Wire to `LocationControllerImpl`
    - _Requirements: 8.1, 8.2, 8.3, 8.4, 8.5, 8.6_

- [x] 21. Mood history chart fragment
  - Build `MoodHistoryFragment` with an `MPAndroidChart` line/bar chart
  - Observe `MoodControllerImpl.getMoodHistory()` and render data points; support swipe-to-delete entries
  - _Requirements: 2.1, 2.2, 2.3, 2.4_

- [x] 22. Profile fragment
  - Build `ProfileFragment` layout with name field, profile picture (Glide/Coil), dark mode toggle, reminder time picker, logout button
  - Wire to `ProfileControllerImpl` and `NotificationControllerImpl`
  - Apply dark mode immediately via `AppCompatDelegate` when toggle changes
  - _Requirements: 10.1, 10.2, 10.3, 10.4, 10.5, 10.6, 11.1, 11.2, 11.3, 12.1, 12.2, 12.3, 12.4, 12.5_

- [x] 23. Wire dark mode theme restoration on app start
  - In `Application.onCreate()` (or `MainActivity.onCreate()`), read `UserProfile.isDarkMode` from `ProfileRepository` and call `AppCompatDelegate.setDefaultNightMode` before any UI is inflated
  - _Requirements: 11.3_

- [x] 24. Final checkpoint — Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for a faster MVP
- Each task references specific requirements for traceability
- Property tests use Kotest property testing (as specified in design dependencies)
- Room integration tests use `Room.inMemoryDatabaseBuilder` for isolation
- All DB and network operations run on `Dispatchers.IO`; LiveData observed on main thread
- Google Maps API key must be in `local.properties` — never commit it to source control
