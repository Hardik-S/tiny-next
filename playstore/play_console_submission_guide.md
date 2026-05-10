# Tiny Next Play Console Submission Guide

Use this as the console-side runbook for `com.batb4016.tinynext`. It is written for a personal Google Play developer account, so the closed-test production-access gate is assumed.

## 1. Create the Play Console App

Open: https://play.google.com/console/u/0/developers/create-application

Use these values:

- App name: `Tiny Next`
- Default language: `English (United States)` unless your Play account defaults to another English locale.
- App or game: `App`
- Free or paid: `Free`
- Declarations: confirm the Developer Program Policies, US export laws, and Play App Signing terms when prompted.

Keep the app unpublished while filling the remaining setup sections.

## 2. Store Listing

Open the app, then go to Store presence > Main store listing.

Paste from:

- App name: `playstore/title.txt`
- Short description: `playstore/short_description.txt`
- Full description: `playstore/full_description.txt`

Use these metadata choices:

- App category: `Productivity`
- Tags: use lightweight productivity/task-management tags if Play offers them.
- Contact email: `hshrestha.hba2026@ivey.ca`
- Privacy policy: `https://tiny-next-vercel.vercel.app/privacy`

Screenshots still need to be captured from a connected device or emulator. Use `playstore/screenshot_plan.md`.

## 3. App Content

Go to Policy and programs > App content.

Recommended answers based on the current build:

- App access: `All functionality is available without special access`; no login required.
- Ads: `Yes, my app contains ads`, because free users may see AdMob banner ads.
- Target audience: general productivity audience, not designed for children.
- News apps: no.
- COVID-19 contact tracing/status: no.
- Health apps: no.
- Financial features: no, beyond Google Play handling the optional in-app purchase.
- Government apps: no.
- Data safety: complete using `playstore/data_safety_notes.md`, then re-check against the exact final AAB and SDK list before submitting.

Privacy policy is required and already deployed at:

```text
https://tiny-next-vercel.vercel.app/privacy
```

## 4. Monetization Setup

Go to Monetize > Products > In-app products.

Create this one-time product:

- Product ID: `remove_ads_premium`
- Name: `Tiny Next Premium`
- Description: `Remove banner ads with a one-time upgrade.`
- Price: `$2.99`
- Status: active

The app code uses exactly `remove_ads_premium`; do not rename the product ID in Play Console.

## 5. AdMob Setup

Open: https://apps.admob.com/v2/apps/list

Create or select Tiny Next:

- Platform: Android
- App listed on a supported app store: choose the option that matches whether Play Console already has the app available.
- Package: `com.batb4016.tinynext`

Create one banner ad unit. Then set these locally before the final ad-enabled release build:

```powershell
$env:ADMOB_APP_ID='ca-app-pub-...~...'
$env:ADMOB_BANNER_AD_UNIT_ID='ca-app-pub-.../...'
. 'C:\Users\hshre\.codex\secrets\tiny-next\release-signing.env.ps1'
.\gradlew.bat clean test assembleDebug bundleRelease
```

After rebuilding, verify `BuildConfig.ADMOB_BANNER_AD_UNIT_ID` is no longer blank and update `playstore/release_artifact_manifest.md`.

## 6. Internal Testing Release

Go to Test and release > Testing > Internal testing.

Create a release:

- Upload: `app/build/outputs/bundle/release/app-release.aab`
- Release name: `Tiny Next 1.0.0 (1)`
- Release notes: paste `playstore/internal_release_notes.txt`

For Play App Signing, choose the Google-generated app signing key unless you specifically need the same signing key for a non-Play store. Use the local key only as the upload key.

If Play asks for the upload certificate, use:

```text
C:\Users\hshre\.codex\secrets\tiny-next\tiny-next-upload-certificate.pem
```

Do not upload the private `.jks` unless Play explicitly asks for an app signing key export, which this plan avoids.

## 7. Closed Test Gate

For a new personal developer account, production access requires at least 12 opted-in closed testers for at least 14 continuous days.

Go to Test and release > Testing > Closed testing:

- Create a closed test track.
- Add at least 12 tester Google accounts.
- Publish the same AAB after the internal test is clean.
- Keep the testers opted in continuously for 14 days.
- Record feedback in `playstore/tester_feedback_log.md`.

Suggested tester prompt:

```text
Please install Tiny Next from the Play test link, complete onboarding, add one custom task, pick a task, try Done/Skip/Snooze/Another, open Stats/Task List/Premium/Settings, and send me your device model plus one thing that worked and one thing that was confusing.
```

## 8. Production Access Application

After the closed-test gate completes, apply from the Play Console dashboard.

Use evidence like:

- The app was tested on a Samsung S25 FE and through Play testing.
- Core flows tested: onboarding, sample tasks, add/edit, delete/undo, pick, done, skip, snooze, another, stats, premium fallback, restore purchase, settings/privacy.
- Known back-stack bug after Done was fixed in commit `d88066c72336ced9ca2dfbfac4b250fe042a7ff0`.
- Current release prep is documented in commit `24fb31046366201eb64f26f77e6e39ffb0f0e20a` and later release-prep commits.
- The app requires no account.
- Task data is local-only.
- Ads are banner-only.
- Premium is a one-time remove-ads purchase.

## 9. Production Release

After production access is approved:

- If the AAB has changed since closed testing, increment `versionCode` before upload.
- Promote the closed-tested artifact when possible.
- Submit for review.
- Monitor Play policy warnings, Android vitals, crash reports, billing setup, and AdMob serving status.
