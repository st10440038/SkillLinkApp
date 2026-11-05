# SkillLink

### Personalised Learning. Structured Progress. AI-Powered Roadmaps.

SkillLink is a native Android learning application developed using Kotlin and Jetpack Compose. It uses artificial intelligence to generate personalised learning roadmaps based on users' learning goals, existing knowledge, experience levels, available study time, and preferred learning pace.

Users can generate, save, and follow structured learning roadmaps, complete individual lessons, and monitor their learning progress. The application also supports multilingual learning, offline access to saved roadmaps, and cloud-based data synchronisation.

---

## Table of Contents

- [Features](#features)
- [How It Works](#how-it-works)
- [Main Screens](#main-screens)
- [Technology Stack](#technology-stack)
- [Application Architecture](#application-architecture)
- [Database and Storage](#database-and-storage)
- [Authentication](#authentication)
- [AI Roadmap Generation](#ai-roadmap-generation)
- [Multilingual Support](#multilingual-support)
- [Offline Functionality](#offline-functionality)
- [Progress Tracking](#progress-tracking)
- [Notifications](#notifications)
- [Installation](#installation)
- [Configuration](#configuration)
- [Running the Application](#running-the-application)
- [Project Structure](#project-structure)
- [Video Demonstration](#video-demonstration)
- [Screenshots](#screenshots)
- [Release Notes](#release-notes)
- [Preparation for Publication](#preparation-for-publication)
- [Security](#security)
- [Future Improvements](#future-improvements)
- [License](#license)

---

## Features

### User Authentication and Profiles
- User registration and login.
- Email and password authentication.
- Email confirmation.
- Google Sign-In.
- Password reset functionality.
- Personal user profiles.
- Profile management and account settings.

### AI-Powered Learning
- Personalised AI-generated learning roadmaps.
- Learning plans based on individual goals and experience.
- Structured weekly learning schedules.
- Roadmap saving and retrieval.
- Multilingual roadmap translations.

### Learning Management
- My Learning dashboard.
- Saved roadmap management.
- Individual lesson completion.
- Overall learning progress tracking.
- Roadmap filtering and deletion.
- Offline access to saved roadmaps and progress.

### Additional Functionality
- English, isiZulu and Afrikaans language support.
- Local storage using Room and SQLite.
- Cloud database integration with Supabase.
- Firebase Cloud Messaging for push notifications.
- Notification preferences.
- Progress synchronisation when an internet connection becomes available.

---

## How It Works

Users provide information about what they want to learn and their learning preferences.

The application sends the relevant learning information to the AI service, which generates a structured roadmap.

```text
       Learning Information
                |
                v
            Gemini AI
                |
                v
       Personalised Roadmap
                |
                v
        Save to Supabase
                |
                v
       Local Room Database
                |
                v
       Lessons and Progress
```

The generated roadmap takes into consideration:

- Target skill.
- Experience level.
- Existing knowledge and skills.
- Learning objectives.
- Available weekly study time.
- Preferred learning pace.

Users can then save the roadmap, access it through My Learning, complete lessons and monitor their progress.

---

## Main Screens

The application includes the following screens:

| Screen | Description |
|---|---|
| Register | Creates a new user account. |
| Login | Allows users to authenticate. |
| Home | Provides access to the main application features. |
| Generate Roadmap | Collects learning preferences and generates a personalised roadmap. |
| Roadmap Details | Displays the generated roadmap and its lessons. |
| My Learning | Displays saved roadmaps and learning progress. |
| Profile | Displays personal user information. |
| Edit Profile | Allows users to update their profile. |
| Settings | Provides language, notification and account preferences. |
| Notifications | Provides access to notification features where available in the application. |

---

## Technology Stack

| Technology | Purpose |
|---|---|
| Kotlin | Primary programming language |
| Jetpack Compose | Native Android user interface |
| Material 3 | UI components and theming |
| Android SDK | Android application development |
| Supabase Auth | User authentication |
| Supabase PostgreSQL | Cloud database |
| Supabase PostgREST | Database communication |
| Supabase Edge Functions | Secure backend operations |
| Gemini API | AI roadmap generation and translation |
| Ktor Client | Network communication |
| Room / SQLite | Local database and offline storage |
| Firebase Cloud Messaging | Push notifications |
| Gradle | Project build and dependency management |
| Git | Version control |
| GitHub | Source code hosting |

---

## Application Architecture

SkillLink uses a layered application structure that separates the user interface, data management, local storage and external services.

```text
+--------------------------------------+
|             SkillLink                |
|                                      |
|       Kotlin + Jetpack Compose       |
|                                      |
|  +-------------+  +---------------+ |
|  |   Screens   |  |  Navigation   | |
|  +------+------+  +---------------+ |
|         |                            |
|         v                            |
|  +--------------------------------+  |
|  |           Data Layer           |  |
|  |                                |  |
|  | Repositories and Services      |  |
|  +---------------+----------------+  |
|                  |                   |
|        +---------+---------+         |
|        |                   |         |
|        v                   v         |
|  +-------------+   +---------------+ |
|  | Room / SQLite|  |   Supabase    | |
|  | Local Storage|  | Cloud Backend | |
|  +-------------+   +---------------+ |
|                            |         |
|                  +---------+-------+ |
|                  |                 | |
|                  v                 v |
|             Gemini AI          Firebase|
|                              Messaging |
+--------------------------------------+
```

The local database supports offline access, while Supabase provides cloud storage and synchronisation. AI-related operations are handled through the backend service.

---

## Database and Storage

SkillLink uses Supabase PostgreSQL for cloud data storage and Room, built on SQLite, for local data persistence.

### Supabase

The cloud database stores application information, including:

| Table | Purpose |
|---|---|
| `profiles` | Stores user profile information. |
| `categories` | Organises available skill categories. |
| `skills` | Stores available skills. |
| `user_skills` | Associates users with their skills. |
| `roadmaps` | Stores generated learning roadmaps. |
| `progress` | Stores lesson completion and progress information. |

Additional tables support application functionality where configured.

Supabase Row Level Security (RLS) policies help protect user-specific information.

### Room / SQLite

The local database provides:
- Local storage of saved roadmaps.
- Offline access to previously saved roadmaps.
- Local lesson progress storage.
- Progress updates without an internet connection.
- Synchronisation of pending progress when connectivity returns.

---

## Authentication

Authentication is implemented using Supabase Auth.

Available authentication functionality includes:

- Email and password registration.
- Email and password login.
- Email confirmation.
- Password reset.
- Google Sign-In.

A PostgreSQL trigger automatically creates a corresponding profile when a new authentication user is registered.

Authenticated users can access their personal learning information and saved roadmaps.

---

## AI Roadmap Generation

SkillLink integrates Google's Gemini AI through a backend service to generate personalised learning roadmaps.

### Example Input

```text
Target Skill: Cybersecurity
Experience Level: Beginner
Known Skills: Python, Networking
Learning Goal: Build cybersecurity fundamentals
Weekly Time: 5–7 hours
Learning Pace: Moderate
```

### Generated Roadmap Structure

```text
Roadmap
|
+-- Title
|
+-- Summary
|
+-- Week 1
|   +-- Lesson 1
|   +-- Lesson 2
|   +-- Lesson 3
|
+-- Week 2
|   +-- Lesson 1
|   +-- Lesson 2
|   +-- Lesson 3
|
+-- Additional Weeks
```

Generated roadmaps are saved to Supabase and can be accessed through the My Learning screen.

---

## Multilingual Support

SkillLink supports three application languages:

- English.
- isiZulu.
- Afrikaans.

Users can change their preferred language through the application's language settings.

The application also supports translating saved learning roadmaps into isiZulu and Afrikaans using Gemini AI.

Translation caching helps avoid repeatedly requesting translations that have already been generated and saved locally.

This improves loading efficiency and reduces unnecessary network requests.

---

## Offline Functionality

SkillLink uses Room and SQLite to support offline learning.

Previously saved roadmaps remain accessible without an internet connection.

Users can:
- View locally saved roadmaps.
- Access roadmap lessons.
- Update lesson completion.
- View locally stored progress.

When an internet connection becomes available, pending progress changes can be synchronised with Supabase.

An internet connection is still required for operations that depend on online services, such as generating new AI roadmaps.

---

## Progress Tracking

Users can mark individual lessons as completed.

Example:

```text
Completed:
[✓] Variables and Data Types
[✓] Functions
[✓] Introduction to Databases

Not Completed:
[ ] Object-Oriented Programming
```

The application calculates learning progress based on completed lessons.

Example:

```text
6 / 12 lessons completed

50% Progress
```

Progress is stored locally and synchronised with Supabase when connectivity is available.

---

## Notifications

SkillLink integrates Firebase Cloud Messaging (FCM) to support push notifications.

The notification functionality includes:
- Firebase Cloud Messaging integration.
- Device registration using FCM tokens.
- Notification permission handling.
- Notification preference management.
- Enabling and disabling notifications.
- Support for receiving push notifications on Android devices.

Notification delivery depends on device permissions, network connectivity and Firebase configuration.

---

## Installation

### Requirements

Before running SkillLink, make sure you have:

- Android Studio.
- Android SDK.
- A compatible JDK.
- An Android emulator or physical Android device.
- An internet connection.
- A configured Supabase project.
- Firebase project configuration.
- Gemini API access through the configured backend.
- Git.

### Clone the Repository

```bash
git clone https://github.com/st10440038/SkillLinkApp.git

cd SkillLinkApp
```

Open the project in Android Studio and allow Gradle to synchronise.

---

## Configuration

### Supabase

Create and configure a Supabase project with:

- Authentication.
- PostgreSQL database.
- Required database tables.
- Row Level Security policies.
- Profile creation trigger.
- Required Edge Functions.

Configure the Supabase project URL and publishable key locally.

### Firebase

Configure Firebase for the Android application.

The project requires the appropriate Firebase configuration file and Cloud Messaging setup.

### Gemini AI

Configure Gemini API access through the Supabase backend service.

A valid API key and correctly configured backend function are required for AI roadmap generation and translation.

**Important:** Never commit private API keys, service-role keys, passwords, signing credentials or other sensitive information to GitHub.

---

## Running the Application

1. Clone the GitHub repository.
2. Open the project in Android Studio.
3. Allow Gradle to synchronise.
4. Configure the required Supabase and Firebase services.
5. Connect an Android device or start an emulator.
6. Select the `app` run configuration.
7. Click **Run**.
8. Register a new account or log in.
9. Start generating personalised learning roadmaps.

---

## Project Structure

The application is organised into packages that separate the user interface, data operations, networking, local storage and supporting functionality.

```text
com.example.skilllinkapp
|
+-- data/
|   +-- Supabase configuration
|   +-- Repositories
|   +-- Room database
|   +-- Local caching
|
+-- network/
|   +-- Gemini service
|
+-- screens/
|   +-- Authentication screens
|   +-- Home screen
|   +-- Roadmap screens
|   +-- My Learning screen
|   +-- Profile screens
|   +-- Settings screen
|
+-- notifications/
|   +-- Notification services
|   +-- FCM integration
|
+-- ui/
|   +-- Components
|   +-- Theme
|
+-- MainActivity.kt
```

The structure above illustrates the main functional areas. Refer to the repository for the complete implementation and actual source files.

---

## Video Demonstration

The video presentation demonstrates SkillLink's main functionality, including authentication, AI roadmap generation, multilingual support, offline functionality, progress tracking, notifications and application settings.

**Video presentation:** [Insert your YouTube or Google Drive video link here]

---

## Release Notes

The final release includes improvements and additional functionality introduced after the initial prototype.

The release notes document:
- Improvements to the application's original functionality.
- New authentication and account features.
- AI roadmap generation improvements.
- Multilingual support.
- Offline storage and progress synchronisation.
- Notification functionality.
- Additional user-defined features.

See [RELEASE_NOTES.md](RELEASE_NOTES.md) for the complete list of updates and the distinction between required and innovative features.

---

## Preparation for Publication

A signed release APK has been generated for SkillLink.

The release APK is intended for installation and testing on compatible Android devices.

**APK:** [Add the signed APK to a GitHub Release and insert its link here.]

Publication evidence should include:
- Signed APK.
- Screenshots of the application running.
- Evidence of successful signed APK generation.
- Google Play Console upload evidence, if applicable.

The generation of a signed APK does not mean that the application has been published on the Google Play Store.

---

## Security

SkillLink uses several security mechanisms:

- Supabase Authentication for account management.
- Row Level Security (RLS) for user-specific database access.
- HTTPS for network communication.
- Secure backend handling of AI-related requests.
- Local storage for offline functionality.

Sensitive credentials must not be committed to the public GitHub repository.

The release signing keystore and its passwords must be stored securely and kept separate from the source code.

---

## Future Improvements

Potential future improvements include:

- More adaptive AI-generated learning recommendations.
- Additional languages.
- Expanded learning resources.
- Enhanced analytics and progress visualisations.
- Further notification personalisation.
- Additional accessibility improvements.

---

## License

This project was developed as an academic software development project.

---

<div align="center">

### SkillLink

**Personalised Learning. Structured Progress. AI-Powered Roadmaps.**

Developed using Kotlin, Jetpack Compose, Supabase, Firebase and Gemini AI.

</div>
