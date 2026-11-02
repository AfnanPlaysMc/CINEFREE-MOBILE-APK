# Implementation Plan - CINEFREE Movie Streaming App

A high-fidelity, ad-free Android movie streaming application built with Jetpack Compose and Firebase. The app replicates a premium cinema experience with real-time metadata, user-synced watchlists, and integrated video providers.

### User Review & Critical Decisions

> [!IMPORTANT]
> To ensure the best experience, we are adopting a "Clean Play" strategy using popular movie aggregators while keeping the UI entirely native.

- **Confirmed Decision 1**: **Provider Strategy** - We will use TMDB for metadata (search, posters, cast) and established aggregators (VidSrc, Embed.su) for the video streams.
- **Confirmed Decision 2**: **Authentication** - Google Sign-In via Jetpack Credential Manager is mandatory for syncing watchlists and reviews across devices.
- **Open Question / Choice**: **Offline Downloads** - True offline video downloads are limited by third-party embed providers. We will implement "Watchlist Metadata Caching" and support direct downloads only for movies where the admin provides a direct MP4/HLS link.

---

### 1. Overview & Core Concept

- **What It Does**: A centralized movie hub where users can search for any film or TV show, view detailed information, and stream it instantly within a custom, ad-blocked native player.
- **Target Audience**: Movie enthusiasts looking for a clean, mobile-first streaming experience without the clutter of web-based ads.
- **Key Value**: Synchronized viewing experience (watchlist/history) across devices using Firebase, paired with a high-performance video engine.

---

### 2. User Experience & Visual Design

- **Key User Flows**:
    1. **Onboarding**: Splash screen -> Google Sign-In gate.
    2. **Discovery**: Hero banner carousel -> Horizontal rows (Trending, Genres).
    3. **Search**: Instant results as you type (TMDB-powered).
    4. **Details**: Dynamic backdrop -> Synopsis -> Cast -> "Play Now" button.
    5. **Playback**: Landscape player -> Quality/Provider selection -> Gesture controls.
    6. **Social/Personal**: Post reviews -> Manage Watchlist -> Admin Dashboard (for designated users).

- **Visual Identity & Theme**:
    - *Aesthetic Direction*: **Premium Cinematic Dark**. High contrast, deep blacks, and vibrant movie art.
    - *Color Palette*:
        - Background: `#0A0A0A` (Deepest Black)
        - Primary: `#E50914` (Cinema Red)
        - Surface: `#1A1A1A` (Layered Gray)
    - *Typography*: **Montserrat** for bold headlines and **Inter** for legible body text (installed via `font-util`).
    - *Layout*: Edge-to-edge content, glassmorphism for overlays, and staggered grid reveals.

---

### 3. Key Product Decisions & Trade-Offs

- **Decision 1: Video Playback Engine**
    - *Chosen Approach*: **Advanced WebView + Media3 Hybrid**.
    - *Why*: Most "free" providers use iFrame embeds. We will use a custom WebView configuration that blocks popup ads and standardizes the controls, with a fallback to `ExoPlayer` (Media3) for direct HLS/MP4 links provided in the Admin Dashboard.
- **Decision 2: Admin Dashboard & Custom Database**
    - *Chosen Approach*: **Firestore-backed CMS**.
    - *Why*: Allows the owner to "feature" specific movies, fix broken links, or add custom titles that aren't on TMDB. The dashboard will be gated by a `role: "admin"` flag in the user document.

---

### 4. Technical Architecture & Data Strategy

```unicode
┌───────────────────────────────────────────────────────────┐
│                   Android App (Kotlin/Compose)            │
├───────────────────────────────────────────────────────────┤
│  ┌──────────────┐      ┌──────────────┐      ┌─────────┐  │
│  │  UI Screens  │ ◄─── │  ViewModels  │ ◄─── │ NavHost │  │
│  └──────┬───────┘      └──────┬───────┘      └─────────┘  │
│         ▼                     ▼                           │
│  ┌──────────────┐      ┌──────────────┐                   │
│  │ Repositories │ ◄─── │ Domain Models│                   │
│  └──────┬───────┘      └──────────────┘                   │
│         │                                                 │
│         ▼                                                 │
│  ┌──────────────┐      ┌──────────────┐      ┌─────────┐  │
│  │ Firebase SDK │      │ Retrofit/Ktor│      │ Media3  │  │
│  │ (Auth/DB/CS) │      │ (TMDB API)   │      │ Player  │  │
│  └──────────────┘      └──────────────┘      └─────────┘  │
└───────────────────────────────────────────────────────────┘
            │                     │
            ▼                     ▼
     [ Firebase Cloud ]     [ TMDB / Hosts ]
```

- **Data Models**:
    - `Movie`: TMDB ID, title, poster path, backdrop, rating, release date.
    - `User`: UID, display name, email, role (user/admin).
    - `Review`: MovieID, UserID, rating, comment, timestamp.
- **Persistence**:
    - **Firestore**: Global movie overrides, user profiles, watchlists, reviews.
    - **Room**: Local caching of TMDB results for smooth browsing.
- **Integrations**:
    - **TMDB API**: Primary metadata source.
    - **Credential Manager**: Secure Google Sign-In.
    - **Coil**: Advanced image loading with blur-up placeholders.
