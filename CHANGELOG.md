# Changelog

All notable changes to Lite++ will be documented in this file.

## [1.2.0] - 2026-09-16

- Fix and improve browser clear data and clear cache
- Modern splash activity for Android 12+
- New app icon with multi-density support
- Add file upload support in WebView
- Add native dark mode in WebView dropping legacy webkit dependency
- Add swipe to refresh and edge-to-edge layout
- Let user control the app theme (light/dark/system)
- Handle WebView lifecycle in main activity
- Merge desktop WebView and mobile WebView clients
- Refactor cookies formatting and cookie dialog UI
- Show active logged-in account status in cookie dialog
- Modern update checker with background thread
- Route update checks through jsDelivr CDN to avoid rate limits
- Replace AboutActivity with simple Material 3 dialog
- New Phosphor icons replacing legacy drawables
- In-app modern auto updater with download progress dialog
- Strict SHA-256 cryptographic verification from GitHub Releases
- Auto post-install cache cleanup for update APKs
- Add GitHub Action for automatic release builds, signing, and build provenance
- Allow custom debug signing via local.properties
- Add copy feedback toast in CrashActivity
- Standardize codebase and externalize all strings to strings.xml
- Update dependencies (appcompat and material components)
- Fix CookieManager sync by explicitly flushing cookies before WebView reload
- General bug fixes and codebase performance improvements

## [1.1.0] - 2025-12-24

- Added multi-format cookie support (String, Netscape, JSON Array, JSON Dictionary)
- Enhanced cookie dialogs with format selection and conversion
- Updated UI to Material Design 3

## [1.0.0] - 2025-12-01

- Initial release of Lite++ Facebook browser wrapper
