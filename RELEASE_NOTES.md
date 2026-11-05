# SkillLink – Release Notes

**Version:** 1.0.0 – Final Academic Release  
**Platform:** Android  
**Development language:** Kotlin  
**User interface:** Jetpack Compose

## 1. Overview

SkillLink 1.0.0 is the final version of the SkillLink Android application. This release builds on the original prototype by improving existing functionality, introducing additional features and strengthening the overall learning experience.

The application allows users to generate personalised AI-powered learning roadmaps, save them, complete lessons and monitor their learning progress.

This release also introduces improvements in authentication, multilingual support, offline functionality, notifications and user customisation.

---

## 2. Improvements to Existing Prototype Features

### Authentication and User Management
- Improved Supabase authentication integration.
- Added Google Sign-In.
- Improved account session handling.
- Added password reset functionality and authentication validation.
- Improved user profile management.

### AI-Powered Roadmap Generation
- Improved communication with the Gemini AI service.
- Improved roadmap generation and JSON validation.
- Improved error handling for unsuccessful AI requests.
- Added support for translating saved roadmaps.
- Improved translation caching to reduce repeated API requests.

### Learning Management
- Improved the My Learning dashboard.
- Improved saved roadmap retrieval.
- Improved roadmap filtering and deletion.
- Improved lesson completion and progress calculations.
- Improved progress synchronisation with Supabase.

---

## 3. New Features Added

### 3.1 Multilingual Support

Added support for three application languages:
- English.
- isiZulu.
- Afrikaans.

Users can change their preferred application language. Saved learning roadmaps can also be translated into isiZulu and Afrikaans using Gemini AI.

A local translation cache stores previously generated translations to improve loading efficiency and reduce unnecessary requests.

### 3.2 Offline Learning with Room and SQLite

Introduced local database storage using Room and SQLite.

Users can:
- Access previously saved roadmaps without an internet connection.
- View locally stored learning content.
- Update lesson completion while offline.
- Access locally stored learning progress.
- Synchronise pending progress changes when connectivity is restored.

### 3.3 Firebase Cloud Messaging

Integrated Firebase Cloud Messaging (FCM) to support push notifications.

Added:
- FCM token registration.
- Notification permission handling.
- Notification preference management.
- Enable and disable notification settings.
- Support for receiving push notifications on Android devices.

### 3.4 Google Sign-In

Introduced Google authentication as an additional login option alongside email and password authentication.

### 3.5 Dark Mode

Introduced a dark mode option through the application settings.

Users can switch between light and dark appearances, providing a more comfortable viewing experience and additional interface customisation.

### 3.6 Profile Picture Customisation

Introduced profile picture management.

Users can:
- Select a profile picture from their device gallery.
- Upload and store their profile picture using Supabase Storage.
- View their profile picture on their profile screen.
- View their profile picture in the Settings screen.
- Change or remove their profile picture.

### 3.7 Improved Translation Caching

Improved the roadmap translation process by implementing language-specific cache handling and preventing unnecessary translation requests during roadmap loading.

This helps improve the experience when users switch between supported languages.

---

## 4. Additional Features

The final version introduces additional functionality intended to improve the learning experience.

| Feature | Description |
|---|---|
| Translation Caching | Stores generated translations locally to reduce repeated requests. |
| Dark Mode | Allows users to switch between light and dark application themes. |
| Profile Picture Customisation | Allows users to upload, change and remove their profile pictures. |

These additional features extend the original application by making learning more personalised, accessible and flexible.

---

## 5. Technical Improvements

The final release also includes:
- Improved Kotlin and Jetpack Compose implementation.
- Improved separation of application functionality into data, network, screen and notification packages.
- Improved Supabase integration.
- Added Room database integration.
- Improved coroutine handling for asynchronous operations.
- Improved error handling and application logging.
- Improved network request timeout configuration.
- Improved local data persistence.
- Improved Git version control and source code organisation.

---

## 6. Preparation for Publication

The final version was prepared for Android release distribution.

Publication preparation includes:
- Generation of a signed Android APK.
- Release build configuration.
- Installation and testing preparation on Android devices.
- Collection of application screenshots for assessment evidence.

---

## 7. Final Release Summary

SkillLink 1.0.0 represents the final development version of the academic project.

Compared with the original prototype, the final application provides improved authentication, enhanced AI roadmap generation, multilingual functionality, offline learning, local data persistence, progress synchronisation, Firebase push notification integration, dark mode and profile picture customisation.

The additional features aim to provide users with a more personalised, accessible and flexible learning experience while allowing them to manage their learning activities both online and offline.

**End of Release Notes**
