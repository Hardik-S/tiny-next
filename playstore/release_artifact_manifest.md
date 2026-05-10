# Tiny Next Release Artifact Manifest

Generated: 2026-05-10, America/Toronto

## Current Signed Play Upload Candidate

- Package: `com.batb4016.tinynext`
- Version code: `1`
- Version name: `1.0.0`
- Privacy policy URL: `https://tiny-next-vercel.vercel.app/privacy`
- Release AdMob banner ID in this build: blank, because production AdMob IDs are not configured locally yet.

| Artifact | Path | Size | SHA-256 |
| --- | --- | ---: | --- |
| Release AAB | `app/build/outputs/bundle/release/app-release.aab` | 13,645,050 bytes | `E06BB787EC016AFC570DE27A77E76AB4C780E6EEA6444781A6A2991D4E6F4230` |
| Debug APK | `app/build/outputs/apk/debug/app-debug.apk` | 19,620,583 bytes | `D2B124B65ED31C2AE53522180C0ED423BDC1BF56108498FF7D3093F3D80D1034` |

## Signing Material

The upload key is intentionally outside the repository:

- Env script: `C:\Users\hshre\.codex\secrets\tiny-next\release-signing.env.ps1`
- Upload keystore: `C:\Users\hshre\.codex\secrets\tiny-next\tiny-next-upload-key.jks`
- Upload public certificate: `C:\Users\hshre\.codex\secrets\tiny-next\tiny-next-upload-certificate.pem`

The private keystore and passwords must not be committed or uploaded anywhere except where Play Console explicitly asks for the upload key certificate. Play App Signing should use a Google-generated app signing key unless a future cross-store distribution requirement changes that decision.

## Verification Evidence

Signed build command:

```powershell
. 'C:\Users\hshre\.codex\secrets\tiny-next\release-signing.env.ps1'
.\gradlew.bat clean test assembleDebug bundleRelease
```

Result: `BUILD SUCCESSFUL`, 110 actionable tasks, release bundle signed by Gradle.

Signature verification command:

```powershell
& 'C:\Program Files\Eclipse Adoptium\jdk-21.0.7.6-hotspot\bin\jarsigner.exe' -verify -verbose -certs .\app\build\outputs\bundle\release\app-release.aab
```

Result: `jar verified`; signer is `CN=Tiny Next Upload, OU=Tiny Next, O=Hardik-S, L=Toronto, ST=Ontario, C=CA`. The self-signed certificate warning is expected for a local upload key.

Privacy page verification command:

```powershell
Invoke-WebRequest -Uri 'https://tiny-next-vercel.vercel.app/privacy' -UseBasicParsing -TimeoutSec 30
```

Result: HTTP 200; page contains `Tiny Next Privacy Policy` and `hshrestha.hba2026@ivey.ca`.

## Before Uploading This Exact AAB

This AAB is signed and structurally ready for Play upload, but production AdMob IDs are still blank in the release BuildConfig. Upload it to internal testing only if you are comfortable testing without live banner ad serving. For a final ad-enabled build, set `ADMOB_APP_ID` and `ADMOB_BANNER_AD_UNIT_ID`, rebuild, and regenerate this manifest.
