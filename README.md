# SkillLink

### Personalised Learning. Structured Progress. AI-Powered Roadmaps.

**SkillLink** is a native Android learning application developed using Kotlin and Jetpack Compose. It uses artificial intelligence to generate personalised learning roadmaps based on users' learning goals, existing knowledge, experience levels, available study time and preferred learning pace.

Users can generate, save and follow structured learning roadmaps, complete individual lessons and monitor their learning progress. The application also supports multilingual learning, offline access to saved roadmaps, cloud-based data synchronisation and push notifications.

---

## Video Demonstration

The video presentation demonstrates SkillLink's main functionality, including authentication, AI roadmap generation, multilingual support, offline functionality, progress tracking, notifications and application settings.

**Watch the video presentation:**

[SkillLink Application Demonstration](https://www.youtube.com/watch?v=xaousjsazW0)

---

## Table of Contents

- [Project Overview](#project-overview)
- [Purpose and Objectives](#purpose-and-objectives)
- [Features](#features)
- [Innovative Features](#innovative-features)
- [Design Considerations](#design-considerations)
- [Main Application Screens](#main-application-screens)
- [How SkillLink Works](#how-skilllink-works)
- [Technology Stack](#technology-stack)
- [Application Architecture](#application-architecture)
- [Database and Storage](#database-and-storage)
- [Authentication](#authentication)
- [AI Roadmap Generation](#ai-roadmap-generation)
- [Multilingual Support](#multilingual-support)
- [Offline Functionality](#offline-functionality)
- [Progress Tracking](#progress-tracking)
- [Notifications](#notifications)
- [Testing and Quality Assurance](#testing-and-quality-assurance)
- [GitHub and Version Control](#github-and-version-control)
- [GitHub Actions and Continuous Integration](#github-actions-and-continuous-integration)
- [Installation](#installation)
- [Configuration](#configuration)
- [Running the Application](#running-the-application)
- [Project Structure](#project-structure)
- [Release Notes](#release-notes)
- [Preparation for Publication](#preparation-for-publication)
- [Security](#security)
- [Future Improvements](#future-improvements)
- [Conclusion](#conclusion)
- [License](#license)

---

## Project Overview

SkillLink is designed to make independent learning more organised, accessible and personalised.

Many learners struggle to identify what they need to study, organise learning activities and maintain consistent progress towards their goals.

SkillLink addresses these challenges by allowing users to generate structured learning roadmaps that reflect their individual needs.

Instead of following a generic learning plan, users can specify their learning objectives, current knowledge, experience level and preferred study pace. The application uses Gemini AI to generate a personalised learning structure.

SkillLink combines AI-powered learning assistance with progress management, multilingual support and offline functionality to provide an integrated learning experience.

## Purpose and Objectives

The main purpose of SkillLink is to provide users with a practical platform for organising and managing their independent learning journeys.

The application's objectives are to:

- Help users identify and organise the skills they want to develop.
- Generate personalised learning roadmaps using artificial intelligence.
- Divide learning objectives into manageable weekly lessons.
- Allow users to monitor their learning progress.
- Support learning in English, isiZulu and Afrikaans.
- Provide access to previously saved learning material without an internet connection.
- Synchronise learning progress with cloud storage when connectivity becomes available.
- Encourage consistent learning through notifications and progress monitoring.

The intended users include students, independent learners and individuals interested in developing new technical or professional skills.

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
- Profile picture upload, updating and removal.

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
- Light and dark application themes.

---

## Innovative Features

SkillLink incorporates several features intended to improve the traditional learning experience.

### 1. AI-Powered Personalised Roadmaps

Gemini AI generates learning roadmaps based on each user's individual learning requirements.

Users can specify their target skill, experience level, existing knowledge, learning goal, available weekly study time and preferred learning pace.

This provides a more personalised alternative to following the same predefined learning plan as every other user.

### 2. Multilingual AI Learning

SkillLink supports English, isiZulu and Afrikaans.

In addition to translating application interface text, the application allows users to translate saved learning roadmaps using Gemini AI.

This feature aims to make learning resources more accessible to users who prefer studying in different languages.

### 3. Offline Learning and Progress Synchronisation

SkillLink uses Room and SQLite to maintain locally stored roadmaps and learning progress.

Users can continue accessing saved learning material and updating lesson completion without an internet connection. Pending progress changes can be synchronised with Supabase when connectivity returns.

### 4. Integrated Learning Progress Management

Users can complete individual lessons and monitor their overall progress through their saved roadmaps.

This allows learners to track completed activities and maintain a clearer view of their learning journey.

### 5. Personalised Application Experience

Users can manage their profiles, upload profile pictures, change their preferred language and switch between light and dark themes.

These features allow users to customise aspects of the application according to their preferences.

---

## Design Considerations

The design of SkillLink focuses on usability, consistency, accessibility and simplicity.

### User Interface and Visual Design

SkillLink was developed using Jetpack Compose and Material 3 components.

A purple and lavender colour palette was selected to create a consistent visual identity throughout the application.

The interface aims to provide a clean and organised learning environment without overwhelming users with unnecessary information.

Consistent styling, reusable interface components and clear visual hierarchy help maintain a unified experience across the different screens.

### Navigation and Usability

The application separates its functionality into dedicated screens, allowing users to navigate between authentication, home, roadmap generation, saved learning activities, profiles and settings.

The navigation structure is intended to make the main learning functions easy to locate and use.

Learning roadmaps are organised into weeks and individual lessons to make larger learning objectives easier to manage.

### Accessibility and Multilingual Design

Supporting English, isiZulu and Afrikaans helps accommodate users with different language preferences.

Text labels and interface messages are localised where translations are available. The application also includes fallback behaviour for missing translations.

The interface aims to maintain readable text and clear contrast across its light and dark themes.

### Dark Mode

Dark mode provides an alternative appearance for users who prefer a darker interface.

The theme can be changed through application settings and is designed to maintain visual consistency throughout the application.

### Data Persistence and Reliability

The application combines local and cloud storage to improve availability.

Room provides access to previously saved learning information offline, while Supabase provides cloud-based storage.

This approach reduces dependence on continuous internet connectivity for accessing existing learning material.

---

## Main Application Screens

The following screenshots illustrate the actual application interface.

**Replace the image paths below with screenshots captured from the final installed APK.**

| Screen | Description |
|---|---|
| Registration | Creates a new user account. |
| Login | Allows users to authenticate. |
| Home | Provides access to the main application features. |
| Generate Roadmap | Collects learning preferences and generates a personalised roadmap. |
| Roadmap Details | Displays the generated roadmap and its lessons. |
| My Learning | Displays saved roadmaps and learning progress. |
| Profile | Displays personal user information. |
| Edit Profile | Allows users to update their profile. |
| Settings | Provides language, notification and account preferences. |
| Notifications | Provides access to notification-related functionality. |
| Dark Mode | Demonstrates the alternative application theme. |

### Application Screenshots

**Login and Registration**

![SkillLink Login](docs/screenshots/login.png)

**Home Screen**

![SkillLink Home](docs/screenshots/home.png)

**AI Roadmap Generation**

![SkillLink Roadmap Generation](docs/screenshots/generate-roadmap.png)

**Roadmap Details**

![SkillLink Roadmap Details](docs/screenshots/roadmap-details.png)

**My Learning and Progress**

![SkillLink My Learning](docs/screenshots/my-learning.png)

**Profile**

![SkillLink Profile](docs/screenshots/profile.png)

**Settings and Language Selection**

![SkillLink Settings](docs/screenshots/settings.png)

**Dark Mode**

![SkillLink Dark Mode](docs/screenshots/dark-mode.png)

**Notifications**

![SkillLink Notifications](docs/screenshots/notifications.png)

---

## How SkillLink Works

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
                |
                v
        Progress Synchronisation
```

The generated roadmap takes into consideration:

- Target skill.
- Experience level.
- Existing knowledge and skills.
- Learning objectives.
- Available weekly study time.
- Preferred learning pace.

Users can then save the roadmap, access it through My Learning, complete lessons and monitor their learning progress.

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
| GitHub | Source code hosting and project management |
| GitHub Actions | Automated testing and continuous integration |

---

## Application Architecture

SkillLink uses a layered application structure that separates the user interface, data management, local storage and external services.

```text
+--------------------------------------+
|              SkillLink               |
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
|  +---------------+----------------+ |
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
|              Gemini AI          Firebase
|                                Messaging
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

An internet connection and an available Gemini API service are required for generation.

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

## Testing and Quality Assurance

SkillLink includes automated unit and Android instrumentation tests to verify important application functionality.

### Unit Tests

The `LanguageSupportTest.kt` file tests:

- English, isiZulu and Afrikaans translations.
- Fallback behaviour for missing translations.
- Localised learning progress messages.

### Room Database Instrumentation Tests

The `SkillLinkDatabaseTest.kt` file tests:

- Saving and retrieving lesson progress.
- Updating existing lesson progress.
- Removing synchronised progress from the pending synchronisation list.

### Continuous Integration Testing

GitHub Actions is configured to automatically execute the unit tests and Android instrumentation tests.

The workflow runs on pushes and pull requests targeting the configured development branches.

**Workflow:** `.github/workflows/android-tests.yml`

**Testing evidence:** The configured GitHub Actions test jobs completed successfully during the final testing process.

---

## GitHub and Version Control

GitHub was used throughout the development of SkillLink to host, manage and maintain the application's source code.

The project repository is:

[SkillLinkApp – GitHub Repository](https://github.com/st10440038/SkillLinkApp)

### Repository Management

The GitHub repository contains the Kotlin source code, application resources, Gradle configuration, automated testing workflows and project documentation.

The repository provides a central location for maintaining the project and accessing its development history.

### Git and Version Control

Git was used to track changes made during development.

The development workflow involved:

- Committing completed changes to preserve development progress.
- Pushing local changes to the remote GitHub repository.
- Maintaining an updated version of the application source code.
- Tracking modifications to application features and configuration.
- Maintaining documentation alongside the source code.

Version control also provides a history of changes that can be reviewed when troubleshooting or restoring previous working versions.

### Benefits of Using GitHub

Using GitHub provided several advantages:

- Centralised source code management.
- Version history and change tracking.
- Backup of the project source code.
- Access to automated testing results.
- Easier maintenance of project documentation.
- A record of the application's development progress.

---

## GitHub Actions and Continuous Integration

GitHub Actions was integrated into SkillLink to automate testing and improve software quality.

Continuous integration allows configured tests to execute automatically when changes are pushed to the repository or when pull requests are created.

### Automated Workflow

The workflow is stored in:

`.github/workflows/android-tests.yml`

The configured testing process includes:

1. Checking out the GitHub repository.
2. Setting up the required Java and Android build environment.
3. Preparing the Gradle build.
4. Executing unit tests.
5. Executing Room database instrumentation tests.
6. Reporting the results through GitHub Actions.

### Purpose of GitHub Actions

Automated testing helps identify problems earlier in development.

It reduces the need to execute every test manually after each code change and provides visible evidence of whether the configured tests pass.

Successful workflow runs are displayed in the repository's Actions tab.

**GitHub Actions:** [View SkillLink Workflow Runs](https://github.com/st10440038/SkillLinkApp/actions)

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

## Release Notes

The final release includes improvements and additional functionality introduced after the initial prototype.

The release notes document:

- Improvements to the application's original functionality.
- New authentication and account features.
- AI roadmap generation.
- Multilingual support.
- Offline storage and progress synchronisation.
- Notification functionality.
- Additional user-defined features.

See [RELEASE_NOTES.md](RELEASE_NOTES.md) for the complete list of updates and the distinction between required and innovative features.

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

## Conclusion

SkillLink combines artificial intelligence, multilingual learning, offline storage and progress management in a native Android application.

The project demonstrates the use of modern Android development technologies and cloud-based services to address challenges associated with independent learning.

The integration of personalised roadmaps, multilingual functionality and offline learning provides an enhanced learning experience while allowing users to organise and monitor their individual learning journeys.

The development process also demonstrates the practical use of GitHub for version control and GitHub Actions for automated software testing.

---

## License

This project was developed as an academic software development project.

---

<div align="center">

### SkillLink

**Personalised Learning. Structured Progress. AI-Powered Roadmaps.**

Developed using Kotlin, Jetpack Compose, Supabase, Firebase and Gemini AI.

</div>
