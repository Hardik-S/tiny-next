# Google Play Data Safety Notes

These notes are a working checklist for Play Console answers. They are not legal advice.

## Current MVP Design

- Account creation: none.
- Server-side task sync: none.
- Primary task data location: local device storage.
- Ads: Google AdMob banner ads for non-premium users.
- In-app purchases: Google Play Billing one-time product remove_ads_premium.
- Interstitials: none.
- Rewarded ads: none.

## Likely Data Types To Review In Play Console

- App activity: app interactions may be processed by AdMob for ad delivery, measurement, fraud prevention, and diagnostics.
- Device or other IDs: advertising ID and app-set identifiers may be collected by Google Mobile Ads SDK.
- Diagnostics: SDK crash/performance diagnostics may be collected by Google SDKs.
- Purchases: Google Play processes purchase transaction data. The app keeps a local entitlement cache for premium status.
- User-provided task content: expected to remain on device in this MVP. Do not mark as collected unless a later worker adds sync, backup export, analytics, or support upload.

## Safety Decisions

- Do not add custom analytics until the data-safety form is updated.
- Keep premium restoration based on Google Play Billing plus local cache only.
- Do not log task titles, purchase tokens, or ad identifiers.
- If crash reporting is added later, verify whether task text can appear in breadcrumbs, logs, or exceptions before release.

## Store Listing Claims To Keep Aligned

- Free version uses banner ads only.
- Premium removes banner ads through a one-time purchase.
- No account required.
- No interstitial ad interruptions.
