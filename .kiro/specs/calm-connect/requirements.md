# Requirements Document

## Introduction

Calm Connect is a native Android mental wellness application built in Kotlin using the MVC architecture pattern. The app provides users with mood check-in and tracking, ambient soundscapes, daily motivational quotes, stress relief tools (breathing, meditation, journaling), academic support via a Pomodoro timer, mindful games, GPS-based calm location discovery, a daily guided routine, and a user profile with dark mode and reminder settings. All user data is stored locally using Room DB with full CRUD support. A daily reminder system delivers push notifications via WorkManager to encourage consistent engagement.

---

## Glossary

- **App**: The Calm Connect Android application
- **MoodController**: Controller responsible for mood check-in, retrieval, and history
- **SoundController**: Controller responsible for ambient soundscape playback
- **QuoteController**: Controller responsible for daily quote selection and favorites management
- **StressReliefController**: Controller responsible for breathing exercises, guided meditation, and journaling
- **StudyTimerController**: Controller responsible for Pomodoro-style study session management
- **GameController**: Controller responsible for mindful game sessions and scoring
- **LocationController**: Controller responsible for GPS and Google Maps integration
- **RoutineController**: Controller responsible for daily guided routine steps and progress
- **ProfileController**: Controller responsible for user profile data, avatar, and settings
- **NotificationController**: Controller responsible for scheduling and delivering daily reminders
- **LoginController**: Controller responsible for login screen validation
- **Room_DB**: The local Room database storing all persistent app data
- **MoodEntry**: A data entity representing a single mood check-in record
- **Quote**: A data entity representing a motivational quote
- **JournalEntry**: A data entity representing a single journal entry
- **RoutineStep**: A data entity representing one step in the daily guided routine
- **UserProfile**: A data entity representing the user's profile and preferences
- **CalmPlace**: An in-memory data object representing a nearby calm location returned from Google Maps
- **WorkManager**: Android Jetpack component used for scheduling background tasks and reminders
- **MediaPlayer**: Android system component used for audio playback
- **BreathingPattern**: An enum defining the phase sequence for a breathing exercise (BOX_4_4_4_4, RELAXING_4_7_8, ENERGIZING_2_2_4)
- **GameType**: An enum defining the available mindful games (TAPPING, MEMORY, BREATHING)
- **TimerPhase**: An enum defining Pomodoro session phases (WORK, SHORT_BREAK, LONG_BREAK)
- **LiveData**: Android Jetpack reactive data holder that notifies observers on change
- **BottomNavigationBar**: The persistent bottom navigation UI component with tabs: Home, Today, Tools, Profile

---

## Requirements

### Requirement 1: Mood Check-In

**User Story:** As a user, I want to record how I am feeling with an optional note, so that I can track my emotional state over time.

#### Acceptance Criteria

1. WHEN a user selects an emotion and submits the mood check-in form, THE MoodController SHALL save a MoodEntry to Room_DB containing the selected emotion, optional note, timestamp, and formatted date.
2. WHEN a user submits a mood check-in without selecting an emotion, THE MoodController SHALL return an error and prevent the entry from being saved.
3. WHEN a user submits a mood check-in with an emotion value not in the predefined valid emotions set, THE MoodController SHALL return an error and prevent the entry from being saved.
4. WHEN a user provides a note exceeding 500 characters, THE MoodController SHALL return an error and prevent the entry from being saved.
5. THE MoodController SHALL accept a null or empty note as a valid optional field.
6. WHEN a MoodEntry is successfully saved, THE MoodController SHALL confirm success to the calling View.

---

### Requirement 2: Mood History and Tracking

**User Story:** As a user, I want to view my past mood entries in a chart, so that I can understand my emotional patterns over time.

#### Acceptance Criteria

1. WHEN mood history is requested, THE MoodController SHALL return all saved MoodEntry records as a LiveData list.
2. WHEN mood history is returned, THE MoodController SHALL provide entries ordered by timestamp in ascending order.
3. WHEN a MoodEntry is deleted by its ID, THE MoodController SHALL remove it from Room_DB and confirm success.
4. WHEN mood history data is available, THE App SHALL render it as a chart using MPAndroidChart.

---

### Requirement 3: Comfort Soundscapes

**User Story:** As a user, I want to play ambient sounds, so that I can create a calming environment for relaxation or focus.

#### Acceptance Criteria

1. WHEN a user selects a sound type (RAIN_DROPS, OCEAN_WAVES, GENTLE_PIANO, FOREST_AMBIENCE), THE SoundController SHALL begin streaming the corresponding audio asset via MediaPlayer.
2. WHEN a user sets a timer duration, THE SoundController SHALL automatically stop playback after the specified number of minutes has elapsed.
3. WHEN a user adjusts the volume, THE SoundController SHALL apply the new volume level to the active MediaPlayer instance.
4. WHEN a user pauses playback, THE SoundController SHALL pause the MediaPlayer and retain the current position.
5. WHEN a user stops playback, THE SoundController SHALL stop and release the MediaPlayer resources.
6. IF audio focus is denied by the system, THEN THE SoundController SHALL release MediaPlayer resources and notify the View that playback could not start.

---

### Requirement 4: Quote of the Day

**User Story:** As a user, I want to see a motivational quote each day and save my favorites, so that I can find inspiration and revisit quotes I like.

#### Acceptance Criteria

1. WHEN getDailyQuote is called, THE QuoteController SHALL return the same Quote for all calls made on the same calendar date.
2. THE QuoteController SHALL always select a daily quote at an index within the bounds of the full quotes list.
3. WHEN a user saves a quote to favorites, THE QuoteController SHALL persist the quote with isFavorite set to true in Room_DB.
4. WHEN getFavoriteQuotes is called, THE QuoteController SHALL return a LiveData list containing only quotes where isFavorite is true.
5. WHEN a user removes a quote from favorites, THE QuoteController SHALL update isFavorite to false in Room_DB and exclude it from subsequent getFavoriteQuotes results.
6. WHEN searchQuotes is called with a non-empty query string, THE QuoteController SHALL return only quotes whose text or author contains the query string (case-insensitive).

---

### Requirement 5: Stress Relief Tools

**User Story:** As a user, I want access to breathing exercises, guided meditation, and journaling, so that I can manage stress and reflect on my thoughts.

#### Acceptance Criteria

1. WHEN a breathing exercise is started with a BreathingPattern, THE StressReliefController SHALL execute the pattern's phases in their declared order for each cycle.
2. WHEN a breathing exercise is active, THE StressReliefController SHALL display the current phase label and animate the breathing circle accordingly.
3. WHEN a guided meditation is started with a duration, THE StressReliefController SHALL run a countdown session for the specified number of minutes.
4. WHEN a user saves a journal entry with valid text, THE StressReliefController SHALL persist a JournalEntry to Room_DB with the provided text and timestamp.
5. WHEN a user attempts to save a journal entry with empty text, THE StressReliefController SHALL return an error and prevent the entry from being saved.
6. WHEN a user attempts to save a journal entry with text exceeding 2000 characters, THE StressReliefController SHALL return an error and prevent the entry from being saved.
7. WHEN getJournalEntries is called, THE StressReliefController SHALL return a LiveData list of all saved JournalEntry records.

---

### Requirement 6: Academic Support — Pomodoro Timer

**User Story:** As a student, I want a Pomodoro study timer, so that I can structure my study sessions with focused work and regular breaks.

#### Acceptance Criteria

1. WHEN a Pomodoro session is started, THE StudyTimerController SHALL accept workMinutes in the range [1, 60] and breakMinutes in the range [1, 30].
2. IF workMinutes or breakMinutes are outside their valid ranges, THEN THE StudyTimerController SHALL return an error and not start the session.
3. WHILE a Pomodoro session is active, THE StudyTimerController SHALL emit TimerState updates via LiveData with the current TimerPhase, remainingSeconds, and completedPomodoros count.
4. WHILE a Pomodoro session is active, THE StudyTimerController SHALL ensure remainingSeconds never decrements below zero.
5. WHEN a work phase completes, THE StudyTimerController SHALL increment completedPomodoros by one.
6. WHEN completedPomodoros is a multiple of 4, THE StudyTimerController SHALL transition to a LONG_BREAK phase; otherwise THE StudyTimerController SHALL transition to a SHORT_BREAK phase.
7. WHEN a session is paused, THE StudyTimerController SHALL halt the countdown and retain the current remainingSeconds value.
8. WHEN a session is resumed after pause, THE StudyTimerController SHALL continue the countdown from the retained remainingSeconds value.
9. WHEN a session is reset, THE StudyTimerController SHALL return to the initial WORK phase with the original workMinutes duration and zero completedPomodoros.

---

### Requirement 7: Mindful Games

**User Story:** As a user, I want to play mindful games, so that I can reduce stress through gentle, focused activities.

#### Acceptance Criteria

1. WHEN startGame is called with a GameType (TAPPING, MEMORY, BREATHING), THE GameController SHALL initialize and return a valid GameSession for that type.
2. WHEN a score is recorded for a GameType, THE GameController SHALL update the stored high score for that type if the new score is greater than the current high score.
3. WHEN getHighScore is called for a GameType, THE GameController SHALL return the highest score ever recorded for that type.
4. THE App SHALL provide at least three distinct game types: TAPPING, MEMORY, and BREATHING.

---

### Requirement 8: Nearby Calm Locations

**User Story:** As a user, I want to discover calm places near me on a map, so that I can find peaceful environments to visit.

#### Acceptance Criteria

1. WHEN location permission is granted and the user requests nearby places, THE LocationController SHALL obtain the current LatLng from the device GPS and pass it to the Google Maps Places API.
2. WHEN findNearbyPlaces is called with a LatLng and radiusMeters, THE LocationController SHALL return only CalmPlace results whose distance from the provided LatLng is within radiusMeters.
3. WHEN findNearbyPlaces returns results, THE LocationController SHALL ensure no two results share the same placeId.
4. WHEN findNearbyPlaces returns results, THE LocationController SHALL return them sorted by ascending distance from the provided LatLng.
5. IF location permission is denied, THEN THE LocationController SHALL display a rationale dialog and offer a manual search fallback.
6. IF the Google Maps API call fails, THEN THE LocationController SHALL display an error message with a retry option and show any cached results with a last-updated timestamp.

---

### Requirement 9: Today Routine

**User Story:** As a user, I want a daily guided routine with progress tracking, so that I can build consistent wellness habits.

#### Acceptance Criteria

1. WHEN getTodayRoutine is called, THE RoutineController SHALL return a LiveData list of RoutineStep records for the current calendar date.
2. WHEN markStepComplete is called with a valid stepId for today's date, THE RoutineController SHALL set isCompleted to true for that step in Room_DB.
3. IF markStepComplete is called with a stepId that does not exist for today's date, THEN THE RoutineController SHALL return an error.
4. THE RoutineController SHALL ensure getCompletionPercentage always returns a value in the range [0.0, 1.0].
5. WHEN all steps for today are marked complete, THE RoutineController getCompletionPercentage SHALL return 1.0.
6. WHEN no steps for today are marked complete, THE RoutineController getCompletionPercentage SHALL return 0.0.
7. WHEN resetDailyRoutine is called, THE RoutineController SHALL set isCompleted to false for all steps for the current date.

---

### Requirement 10: Profile Management

**User Story:** As a user, I want to manage my profile, including my name, picture, and app preferences, so that the app feels personal and configured to my needs.

#### Acceptance Criteria

1. WHEN getProfile is called, THE ProfileController SHALL return a LiveData UserProfile containing the current name, profile picture URI, dark mode preference, and reminder settings.
2. WHEN updateName is called with a non-empty name string, THE ProfileController SHALL persist the new name to Room_DB and emit the updated UserProfile via LiveData.
3. IF updateName is called with an empty string, THEN THE ProfileController SHALL return an error and not modify the stored name.
4. WHEN updateProfilePicture is called with a valid content URI pointing to a readable image file, THE ProfileController SHALL persist the URI to Room_DB and emit the updated UserProfile via LiveData.
5. WHEN toggleDarkMode is called with a boolean value, THE ProfileController SHALL persist the isDarkMode preference and emit the updated UserProfile via LiveData.
6. WHEN logout is called, THE ProfileController SHALL clear the active session and navigate the user to the login screen.

---

### Requirement 11: Dark Mode

**User Story:** As a user, I want to switch between light and dark themes, so that I can use the app comfortably in different lighting conditions.

#### Acceptance Criteria

1. WHEN isDarkMode is set to true in UserProfile, THE App SHALL apply a dark theme across all screens and fragments.
2. WHEN isDarkMode is set to false in UserProfile, THE App SHALL apply the default light theme across all screens and fragments.
3. WHEN the app is restarted, THE App SHALL restore the previously saved dark mode preference from UserProfile.

---

### Requirement 12: Reminder System

**User Story:** As a user, I want to receive daily reminders, so that I am prompted to engage with the app and maintain my wellness routine.

#### Acceptance Criteria

1. WHEN scheduleDaily is called with a valid hour, minute, and message, THE NotificationController SHALL schedule a recurring daily WorkManager task to deliver a push notification at the specified time.
2. WHEN cancelReminder is called, THE NotificationController SHALL cancel the scheduled WorkManager task and set isReminderActive to false.
3. WHEN isReminderActive is called after scheduling, THE NotificationController SHALL return true.
4. WHEN isReminderActive is called after cancellation, THE NotificationController SHALL return false.
5. IF the user denies the POST_NOTIFICATIONS permission on Android 13 or later, THEN THE App SHALL inform the user that reminders will not be delivered and provide a shortcut to system notification settings.
6. WHEN a reminder notification is tapped by the user, THE App SHALL deep-link to the Home screen.

---

### Requirement 13: Splash Screen and Login

**User Story:** As a user, I want a welcoming splash screen and a validated login flow, so that the app feels polished and my session is properly initialized.

#### Acceptance Criteria

1. WHEN the app is launched, THE App SHALL display a splash screen before navigating to the main screen.
2. WHEN a user submits the login form, THE LoginController SHALL validate that both the username and password fields are non-empty before proceeding.
3. IF the username or password field is empty on login submission, THEN THE LoginController SHALL display a field-level error and prevent navigation to the main screen.

---

### Requirement 14: Bottom Navigation

**User Story:** As a user, I want a persistent bottom navigation bar, so that I can quickly switch between the main sections of the app.

#### Acceptance Criteria

1. THE App SHALL display a BottomNavigationBar with four tabs: Home, Today, Tools, and Profile.
2. WHEN a user taps the Home tab, THE App SHALL navigate to the Home fragment.
3. WHEN a user taps the Today tab, THE App SHALL navigate to the Today (routine) fragment.
4. WHEN a user taps the Tools tab, THE App SHALL navigate to the Tools fragment.
5. WHEN a user taps the Profile tab, THE App SHALL navigate to the Profile fragment.
6. WHILE the user is on any main screen, THE BottomNavigationBar SHALL remain visible and highlight the active tab.

---

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system — essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: Mood save round-trip

*For any* valid MoodEntry (non-empty emotion from the valid set, timestamp > 0, note within 500 chars), saving it via MoodController and then retrieving getMoodHistory should return a list that contains an entry with the same emotion, note, and timestamp.

**Validates: Requirements 1.1, 2.1**

---

### Property 2: Mood validation rejects invalid inputs

*For any* emotion string that is empty or not in the predefined valid emotions set, MoodController.saveMood should return an error and getMoodHistory should remain unchanged.

**Validates: Requirements 1.2, 1.3**

---

### Property 3: Mood note length boundary

*For any* note string whose length exceeds 500 characters, MoodController.saveMood should return an error; for any note string of 500 characters or fewer (including null), it should be accepted.

**Validates: Requirements 1.4, 1.5**

---

### Property 4: Mood history ordering

*For any* set of MoodEntry records saved in arbitrary order, getMoodHistory should return them sorted by timestamp in ascending order.

**Validates: Requirements 2.2**

---

### Property 5: Soundscape timer auto-stop

*For any* positive timer duration n (in minutes), after SoundController.setTimer(n) is called and n minutes elapse, the SoundController should be in a stopped state.

**Validates: Requirements 3.2**

---

### Property 6: Daily quote determinism

*For any* calendar date, calling QuoteController.getDailyQuote twice on the same date should return the same Quote object.

**Validates: Requirements 4.1**

---

### Property 7: Daily quote index bounds

*For any* calendar date, the index computed by QuoteController.getDailyQuote should be in the range [0, size(quotes) - 1].

**Validates: Requirements 4.2**

---

### Property 8: Favorite quote round-trip

*For any* Quote, calling saveToFavorites then getFavoriteQuotes should return a list that includes that Quote with isFavorite set to true.

**Validates: Requirements 4.3, 4.4**

---

### Property 9: Favorite removal exclusion

*For any* Quote that has been saved as a favorite, calling removeFavorite then getFavoriteQuotes should return a list that does not include that Quote.

**Validates: Requirements 4.5**

---

### Property 10: Quote search relevance

*For any* non-empty search query, all Quotes returned by QuoteController.searchQuotes should have their text or author contain the query string (case-insensitive).

**Validates: Requirements 4.6**

---

### Property 11: Breathing phase order

*For any* BreathingPattern, the phases executed by StressReliefController during a session should match the declared phase sequence of that pattern in the correct order.

**Validates: Requirements 5.1**

---

### Property 12: Journal entry round-trip

*For any* valid journal text (non-empty, at most 2000 characters), saving it via StressReliefController and then calling getJournalEntries should return a list containing an entry with the same text.

**Validates: Requirements 5.4, 5.7**

---

### Property 13: Journal entry validation

*For any* journal text that is empty or exceeds 2000 characters, StressReliefController.saveJournalEntry should return an error and getJournalEntries should remain unchanged.

**Validates: Requirements 5.5, 5.6**

---

### Property 14: Pomodoro remainingSeconds non-negative

*For any* valid Pomodoro session configuration, remainingSeconds emitted by StudyTimerController via LiveData should never be less than zero at any tick.

**Validates: Requirements 6.4**

---

### Property 15: Pomodoro completedPomodoros monotonically increases

*For any* Pomodoro session, the completedPomodoros value in emitted TimerState should only ever increase and should equal the number of WORK phases that have fully elapsed.

**Validates: Requirements 6.5**

---

### Property 16: Pomodoro phase transition rule

*For any* Pomodoro session, after each completed work phase, if completedPomodoros is a multiple of 4 the next phase should be LONG_BREAK; otherwise it should be SHORT_BREAK.

**Validates: Requirements 6.6**

---

### Property 17: Pomodoro input validation

*For any* workMinutes outside [1, 60] or breakMinutes outside [1, 30], StudyTimerController.startSession should return an error and not emit any TimerState.

**Validates: Requirements 6.2**

---

### Property 18: Game high score is maximum of all recorded scores

*For any* sequence of scores recorded for a GameType, GameController.getHighScore should return the maximum value from that sequence.

**Validates: Requirements 7.2, 7.3**

---

### Property 19: Nearby places within radius

*For any* LatLng and radiusMeters, all CalmPlace objects returned by LocationController.findNearbyPlaces should have a distance from the provided LatLng that is less than or equal to radiusMeters.

**Validates: Requirements 8.2**

---

### Property 20: Nearby places no duplicate placeIds

*For any* call to LocationController.findNearbyPlaces, the list of returned CalmPlace objects should contain no two entries with the same placeId.

**Validates: Requirements 8.3**

---

### Property 21: Routine completion percentage bounds

*For any* combination of completed and total RoutineStep counts for today, RoutineController.getCompletionPercentage should return a value in the range [0.0, 1.0].

**Validates: Requirements 9.4**

---

### Property 22: Routine step complete round-trip

*For any* valid RoutineStep for today's date, calling markStepComplete then getTodayRoutine should return a list where that step has isCompleted set to true.

**Validates: Requirements 9.2**

---

### Property 23: Profile name update round-trip

*For any* non-empty name string, calling ProfileController.updateName then getProfile should return a UserProfile with the updated name.

**Validates: Requirements 10.2**

---

### Property 24: Profile name rejects empty string

*For any* call to ProfileController.updateName with an empty string, the controller should return an error and getProfile should still return the previously stored name.

**Validates: Requirements 10.3**

---

### Property 25: Dark mode preference persistence

*For any* boolean value passed to ProfileController.toggleDarkMode, calling getProfile immediately after should return a UserProfile where isDarkMode equals that boolean value.

**Validates: Requirements 10.5, 11.3**

---

### Property 26: Reminder active state round-trip

*For any* valid schedule configuration, calling NotificationController.scheduleDaily then isReminderActive should return true; calling cancelReminder then isReminderActive should return false.

**Validates: Requirements 12.3, 12.4**
