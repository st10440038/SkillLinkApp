# SkillLink

### Personalised Learning. Structured Progress. AI-Powered Roadmaps.

SkillLink is a native Android application that uses **AI to generate personalised learning roadmaps** based on a user's learning goals, existing knowledge, experience level, available study time, and preferred learning pace.

Users can generate, save, and follow learning roadmaps while tracking lesson completion and overall progress.

---

##  Table of Contents

* [Features](#-features)
* [How It Works](#-how-it-works)
* [Main Screens](#-main-screens)
* [Technology Stack](#-technology-stack)
* [Architecture](#-architecture)
* [Project Structure](#-project-structure)
* [Database](#-database)
* [Authentication](#-authentication)
* [AI Roadmap Generation](#-ai-roadmap-generation)
* [Progress Tracking](#-progress-tracking)
* [Installation](#-installation)
* [Configuration](#-configuration)
* [Running the Application](#-running-the-application)
* [Git Workflow](#-git-workflow)
* [Current Status](#-current-status)
* [Future Improvements](#-future-improvements)
* [Security](#-security)
* [License](#-license)

---

##  Features

*  User registration and login
*  Email confirmation
*  Personal user profiles
*  Profile editing
*  AI-powered learning roadmap generation
*  Personalised learning plans
*  Roadmap storage
*  Lesson completion tracking
*  Learning progress tracking
*  My Learning dashboard
*  Roadmap filtering
*  Roadmap deletion
*  Last-accessed tracking
*  Settings and account management
*  Supabase cloud database
*  Row Level Security

---

##  How It Works

Users provide information about what they want to learn and how they prefer to learn it.

```text
Learning Information
        │
        ▼
   Gemini AI
        │
        ▼
Personalised Roadmap
        │
        ▼
    Supabase
        │
        ▼
Lessons & Progress
```

The roadmap is generated using:

* **Target skill**
* **Experience level**
* **Existing skills**
* **Learning goal**
* **Weekly study time**
* **Learning pace**

Users can then save the roadmap, complete individual lessons, and monitor their progress.

---

##  Main Screens

### Authentication

* Register
* Login

### Main Application

* Home
* Generate Roadmap
* Roadmap Details
* My Learning
* Profile
* Edit Profile
* Settings

---

##  Technology Stack

| Technology             | Purpose                         |
| ---------------------- | ------------------------------- |
| **Kotlin**             | Primary programming language    |
| **Jetpack Compose**    | User interface                  |
| **Material 3**         | UI components and theming       |
| **Android SDK**        | Android application development |
| **Supabase Auth**      | User authentication             |
| **PostgreSQL**         | Cloud database                  |
| **Supabase PostgREST** | REST API                        |
| **Gemini API**         | AI roadmap generation           |
| **Ktor Client**        | Network communication           |
| **Git**                | Version control                 |
| **GitHub**             | Source code management          |

---

##  Architecture

SkillLink uses a simple layered architecture that separates the user interface, data operations, and external services.

```text
┌──────────────────────────────────┐
│          Android App             │
│                                  │
│     Kotlin + Jetpack Compose     │
│                                  │
│  ┌────────────┐ ┌─────────────┐ │
│  │  Screens   │ │ Navigation  │ │
│  └─────┬──────┘ └─────────────┘ │
│        │                         │
│        ▼                         │
│  ┌────────────────────────────┐ │
│  │        Data Layer          │ │
│  │                            │ │
│  │ Supabase + Repositories    │ │
│  └──────────────┬─────────────┘ │
└─────────────────┼────────────────┘
                  │
          ┌───────┴────────┐
          ▼                ▼
   ┌─────────────┐   ┌─────────────┐
   │  Supabase   │   │  Gemini API │
   │   Backend   │   │  AI Service │
   └─────────────┘   └─────────────┘
```

---

##  Project Structure

```text
com.example.skilllinkapp
│
├── MainActivity.kt
│
├── data
│   ├── SupabaseClient.kt
│   ├── SkillRepository.kt
│   └── ProgressRepository.kt
│
├── network
│   └── GeminiService.kt
│
├── screens
│   ├── RegisterScreen.kt
│   ├── LoginScreen.kt
│   ├── HomeScreen.kt
│   ├── GenerateRoadmapScreen.kt
│   ├── RoadmapScreen.kt
│   ├── MyLearningScreen.kt
│   ├── ProfileScreen.kt
│   ├── EditProfileScreen.kt
│   └── SettingsScreen.kt
│
└── ui
    └── theme
        ├── Color.kt
        ├── Theme.kt
        └── Type.kt
```

---

##  Database

SkillLink uses **Supabase PostgreSQL** to store application data.

### Main Tables

```text
profiles
categories
skills
user_skills
roadmaps
progress
```

| Table         | Purpose                               |
| ------------- | ------------------------------------- |
| `profiles`    | Stores user profile information       |
| `categories`  | Organises available skills            |
| `skills`      | Stores available skills               |
| `user_skills` | Associates users with their skills    |
| `roadmaps`    | Stores generated learning roadmaps    |
| `progress`    | Stores lesson completion and progress |

Supabase **Row Level Security (RLS)** is used to protect user-specific data.

---

##  Authentication

Authentication is handled using **Supabase Auth**.

```text
Register
   │
   ▼
Email Confirmation
   │
   ▼
Login
   │
   ▼
Authenticated Application
```

A PostgreSQL trigger automatically creates a corresponding profile when a new authentication user is registered.

---

##  AI Roadmap Generation

The **Gemini API** is used to generate personalised learning roadmaps.

### Example Input

```text
Target Skill: Cybersecurity
Experience Level: Beginner
Known Skills: Python, Networking
Learning Goal: Build cybersecurity fundamentals
Weekly Time: 5–7 hours
Learning Pace: Moderate
```

### Generated Roadmap

```text
Roadmap
│
├── Title
├── Summary
│
├── Week 1
│   ├── Lesson 1
│   ├── Lesson 2
│   └── Lesson 3
│
├── Week 2
│   ├── Lesson 1
│   ├── Lesson 2
│   └── Lesson 3
│
└── Additional Weeks
```

The generated roadmap is stored in Supabase and can be accessed through **My Learning**.

---

##  Progress Tracking

Users can mark individual lessons as completed.

Example:

```text
☑ Variables and Data Types
☑ Functions
☐ Object-Oriented Programming
```

Progress is stored in Supabase and remains available when the roadmap is reopened.

Example:

```text
6 / 12 lessons completed
50% progress
```

---

##  Installation

### Requirements

Before running SkillLink, make sure you have:

* Android Studio
* Android SDK
* Compatible JDK
* Android emulator or physical Android device
* Internet connection
* Supabase project
* Gemini API access
* Git

### Clone the Repository

```bash
git clone <(https://github.com/st10440038/SkillLinkApp)>
cd SkillLink
```

Open the project in **Android Studio** and allow Gradle to synchronise.

---

## ⚙️ Configuration

### Supabase

Create a Supabase project and configure:

* Authentication
* PostgreSQL database
* Required tables
* Row Level Security policies
* Profile creation trigger

The main tables are:

```text
profiles
categories
skills
user_skills
roadmaps
progress
```

Configure the Supabase project URL and publishable key locally.

### Gemini API

A valid Gemini API key is required for AI roadmap generation.

>  **Important:** Do not commit API keys or other sensitive credentials to GitHub.

For production use, the Gemini API should ideally be accessed through a secure backend rather than directly from the Android application.

---

##  Running the Application

1. Open the project in **Android Studio**.
2. Synchronise Gradle.
3. Connect an Android device or start an emulator.
4. Select the `app` configuration.
5. Click **Run**.
6. Register a new account or log in.
7. Open the **Home** screen.
8. Generate a learning roadmap.

---

## 🌿 Git Workflow

Git and GitHub are used for version control.

### Example Feature Branches

```text
feature/authentication
feature/roadmap-generation
feature/progress-tracking
feature/profile
feature/ui
```

### Basic Git Commands

```bash
git status
git add .
git commit -m "Update SkillLink UI"
git push
```

Recommended workflow:

```text
Create Feature
      ↓
Implement
      ↓
Build & Test
      ↓
Fix Errors
      ↓
Commit
      ↓
Push to GitHub
```

---

##  Current Status

###  Completed

* Android project setup
* Kotlin and Jetpack Compose
* Material 3 theme
* Supabase integration
* User authentication
* Email confirmation
* Automatic profile creation
* Profile management
* Settings
* AI roadmap generation
* Gemini API integration
* Roadmap storage
* Roadmap details
* Lesson progress tracking
* My Learning
* Roadmap filtering
* Roadmap deletion
* Main navigation
* UI refinement

###  Current Focus

The current development focus is improving the visual consistency and overall UI quality of the application.

This includes:

* Typography
* Spacing
* Buttons
* Cards
* Icons
* Navigation
* Progress indicators
* Authentication screens
* Profile screens
* Settings screens

---

## Future Improvements

Potential future features include:

*  Google SSO
*  Offline support with Room
*  Push notifications
*  Multilingual support
*  Profile image uploads
*  More adaptive AI-generated roadmaps

---

## Security

SkillLink uses:

* **Supabase Authentication** for account management
* **Row Level Security (RLS)** for user data protection
* **HTTPS** for network communication

Sensitive credentials such as API keys should never be committed to the repository.

---

## License

This project was developed as an **academic software development project**.

---

<div align="center">

### SkillLink

**Personalised Learning. Structured Progress. AI-Powered Roadmaps.**

</div>
