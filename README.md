# Tiny Next - Task Picker

Tiny Next is an Android-only Kotlin MVP that helps users pick one small task when a long list feels overwhelming.

This repository is intentionally documented as it grows. Decisions, rejected approaches, and integration boundaries should be recorded close to the code so parallel workers can keep moving without overwriting each other.

## Architecture Notes

Tiny Next is a single Android application with package name `com.batb4016.tinynext`. The MVP keeps monetization isolated from task/domain logic:

- `app/src/main/java/com/batb4016/tinynext/data/monetization/` owns Google Play Billing integration and the local premium entitlement model.
- `PremiumState` is plain Kotlin so a DataStore-backed cache can persist or hydrate premium state without importing BillingClient classes.
- `GooglePlayBillingRepository` is the only class that talks to Google Play Billing. It initializes `BillingClient`, queries product details, starts the purchase flow, restores current purchases, and acknowledges completed purchases.
- `app/src/main/java/com/batb4016/tinynext/ui/components/BannerAdView.kt` owns the Compose wrapper around AdMob `AdView`.
- `app/src/main/java/com/batb4016/tinynext/TinyNextViewModel.kt` wires Room, DataStore settings, picker logic, stats, billing state, and the Compose navigation graph.

Rejected approach: mixing purchase state into task repositories or domain models. Premium status is app access state, not task data, and keeping it separate reduces the chance that ad or billing behavior affects core task-picking logic.

## Monetization

### Ads

Tiny Next uses Google AdMob banner ads for free users only. There are no interstitial ads in this MVP.

`BannerAdView` hides itself when:

- `isPremium` is `true`.
- the configured release ad unit ID is blank.
- Google Mobile Ads reports a banner load failure.

Debug builds use Google's Android banner demo ad unit directly in code:

```text
ca-app-pub-3940256099942544/6300978111
```

Release builds use:

```text
BuildConfig.ADMOB_BANNER_AD_UNIT_ID
```

Provide release AdMob values through either environment variables or `local.properties`:

```properties
ADMOB_APP_ID=ca-app-pub-your-app-id~your-app-id
ADMOB_BANNER_AD_UNIT_ID=ca-app-pub-your-ad-unit/your-banner-unit
```

### Billing

Tiny Next has one non-consumable, one-time product:

```text
remove_ads_premium
```

Billing behavior:

- Query product details with `BillingClient.ProductType.INAPP`.
- Launch only the `remove_ads_premium` purchase flow.
- Query existing purchases on startup/restore.
- Acknowledge completed purchases that are not already acknowledged.
- Treat pending purchases as not premium until payment is complete.
- Expose premium state through `StateFlow<PremiumState>`.
- Keep the debug premium override behind `BuildConfig.ALLOW_DEBUG_PREMIUM_OVERRIDE` only.

The local cache contract is `PremiumState.fromCache(...)`. If a DataStore worker adds persistence, it should store the product ID, purchase token, purchased flag, acknowledged flag, and timestamp, then hydrate with that helper.

## Privacy Notes

The MVP is designed to work without an account. Task data should stay on device unless a future feature explicitly adds sync or export.

AdMob and Google Play Billing may process device, advertising, diagnostics, purchase, and app-interaction data according to Google's SDK behavior. The app should not log task titles, purchase tokens, advertising IDs, or other user-specific content.

Play Store privacy and data-safety notes live in `playstore/`. The public privacy policy page source lives in `public/privacy/index.html` and is configured for Vercel through `vercel.json`.

`BuildConfig.PRIVACY_POLICY_URL` is populated from `TINYNEXT_PRIVACY_POLICY_URL` and defaults to:

```text
https://tiny-next-vercel.vercel.app/privacy
```

Set `TINYNEXT_PRIVACY_POLICY_URL` before the final Play build if Vercel assigns a different production URL.

## Signing

Release signing is configured from environment variables or `local.properties`. Do not commit keystores or passwords.

```properties
TINYNEXT_RELEASE_STORE_FILE=C:\\path\\to\\tinynext-release.jks
TINYNEXT_RELEASE_STORE_PASSWORD=...
TINYNEXT_RELEASE_KEY_ALIAS=...
TINYNEXT_RELEASE_KEY_PASSWORD=...
```

If `TINYNEXT_RELEASE_STORE_FILE` is absent, release builds are left unsigned by this project configuration. That is useful for local compile checks but not for Play upload.

## Build Commands

On Windows PowerShell, set Java 21 when needed:

```powershell
$env:JAVA_HOME='C:\Program Files\Eclipse Adoptium\jdk-21.0.7.6-hotspot'
```

Debug APK:

```powershell
.\gradlew.bat assembleDebug
```

Release app bundle for Play Console:

```powershell
.\gradlew.bat bundleRelease
```

Clean rebuild if Gradle cache state is suspicious:

```powershell
.\gradlew.bat clean assembleDebug
```

## Store Assets

Play Store draft assets are kept in `playstore/`:

- `title.txt`
- `short_description.txt`
- `full_description.txt`
- `privacy_policy_draft.md`
- `data_safety_notes.md`
- `screenshot_plan.md`
- `release_checklist.md`
- `tester_feedback_log.md`
- `internal_release_notes.txt`
- `release_artifact_manifest.md`
- `play_console_submission_guide.md`
- `device_qa_log_2026-05-10.md`

## Google Play Launch Notes

Before uploading to Play, complete the release checklist in `playstore/release_checklist.md`. Console-only steps such as creating the Play app, enrolling in Play App Signing, creating the `remove_ads_premium` product, entering Data Safety answers, and running the personal-account closed test cannot be completed from this repository alone.

For a new personal Play developer account, plan for the closed testing gate: at least 12 testers must remain opted in for at least 14 continuous days before applying for production access. Keep tester notes in `playstore/tester_feedback_log.md` so the production-access answers are evidence-backed.
