# Tiny Next MVP Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a production-ready Android MVP named "Tiny Next - Task Picker" that can produce a debug APK and release Android App Bundle today.

**Architecture:** A single Kotlin/Compose app module owns UI, Room persistence, DataStore settings, domain picking/statistics logic, AdMob banner integration, and Play Billing integration. The domain layer stays testable without Android UI, while repositories isolate local storage, billing, ads, and purchase state.

**Tech Stack:** Kotlin, Android Gradle Plugin, Jetpack Compose Material 3, Navigation Compose, Room, DataStore Preferences, Coroutines/StateFlow, Google Mobile Ads SDK, Google Play Billing Library 8.3.0.

---

### Task 1: Android Project Baseline

**Files:**
- Create: `settings.gradle.kts`
- Create: `build.gradle.kts`
- Create: `gradle.properties`
- Create: `gradlew`
- Create: `gradlew.bat`
- Create: `gradle/wrapper/gradle-wrapper.properties`
- Create: `app/build.gradle.kts`
- Create: `app/proguard-rules.pro`
- Create: `app/src/main/AndroidManifest.xml`
- Create: `app/src/main/res/values/strings.xml`
- Create: `app/src/main/res/values/colors.xml`
- Create: `app/src/main/res/xml/backup_rules.xml`
- Create: `app/src/main/res/xml/data_extraction_rules.xml`
- Create: `app/src/main/java/com/batb4016/tinynext/MainActivity.kt`
- Create: `app/src/main/java/com/batb4016/tinynext/TinyNextApp.kt`

- [ ] Add Gradle wrapper and project files that compile SDK 35, target SDK 35, min SDK 23, package `com.batb4016.tinynext`, and use JDK 21 from `JAVA_HOME` or local Gradle properties.
- [ ] Add manifest entries for `INTERNET` and AdMob app id placeholder only. Do not request sensitive permissions.
- [ ] Add a minimal Compose app entry point with Material 3 theme and Navigation host placeholder.
- [ ] Run `./gradlew clean` and `./gradlew assembleDebug`.
- [ ] Commit and push baseline scaffold.

### Task 2: Data, Settings, Picker, and Stats

**Files:**
- Create/modify files under `app/src/main/java/com/batb4016/tinynext/data/`
- Create/modify files under `app/src/main/java/com/batb4016/tinynext/domain/`
- Create/modify files under `app/src/test/java/com/batb4016/tinynext/`

- [ ] Write unit tests for archived exclusion, snoozed exclusion, never-completed boost, starred boost, skipped penalty, completed-today stats, and streak calculation.
- [ ] Implement Room entities and DAOs for tasks, categories, completion events, and task pick events.
- [ ] Implement DataStore settings and purchase cache repositories.
- [ ] Implement weighted random task picker with documented eligibility and weight rules.
- [ ] Implement stats calculator for today, total, current streak, best streak, and recent completions.
- [ ] Run `./gradlew test`.
- [ ] Commit and push data/domain slice.

### Task 3: Compose Product Flow

**Files:**
- Create/modify files under `app/src/main/java/com/batb4016/tinynext/ui/`
- Modify: `app/src/main/java/com/batb4016/tinynext/TinyNextApp.kt`

- [ ] Build screens for onboarding, home, add task, task list, result, stats, premium, and settings/privacy.
- [ ] Implement sample task creation, add task validation, category chips, estimate chips, star toggle, recurrence selection, filters, archive/delete/edit, undo delete snackbar, and result actions.
- [ ] Keep Result screen ad-free and show exactly one task.
- [ ] Wire UI state through ViewModels/StateFlow backed by repositories.
- [ ] Run `./gradlew assembleDebug`.
- [ ] Commit and push UI slice.

### Task 4: Ads, Billing, Play Store Assets, and Docs

**Files:**
- Create/modify monetization files under `app/src/main/java/com/batb4016/tinynext/data/monetization/`
- Create/modify UI ad/premium files under `app/src/main/java/com/batb4016/tinynext/ui/`
- Create: `playstore/title.txt`
- Create: `playstore/short_description.txt`
- Create: `playstore/full_description.txt`
- Create: `playstore/privacy_policy_draft.md`
- Create: `playstore/data_safety_notes.md`
- Create: `playstore/screenshot_plan.md`
- Modify: `README.md`

- [ ] Implement BannerAdView Compose wrapper using Google demo banner ID in debug and `BuildConfig.ADMOB_BANNER_AD_UNIT_ID` in release.
- [ ] Hide banner container on ad failure and suppress ads when `isPremium` is true.
- [ ] Implement BillingRepository for product `remove_ads_premium`, product details, purchase launch, restore, acknowledgment, unavailable fallback, and local purchase cache.
- [ ] Add hidden premium override only for debug builds.
- [ ] Add Play Store draft assets with the requested copy.
- [ ] Expand README with architecture rationale, AdMob, Billing, signing, privacy, and build instructions.
- [ ] Commit and push monetization/docs slice.

### Task 5: Release Verification

**Files:**
- Modify only files required to fix verification failures.

- [ ] Run `./gradlew clean`.
- [ ] Run `./gradlew test`.
- [ ] Run `./gradlew assembleDebug`.
- [ ] Run `./gradlew bundleRelease`.
- [ ] Confirm `app/build/outputs/bundle/release/app-release.aab` exists.
- [ ] Inspect manifest permissions from merged manifest or APK output and verify no sensitive permissions beyond internet/ad id behavior from dependencies.
- [ ] Commit and push fixes, then report exact commit, branch, artifact path, and verification commands.

