# Known Limitations and Future Work

## Current limitations

1. **Mobile API-key security:** Android Keystore protects the saved key at rest, but a key used directly by a mobile client can still be extracted or abused at runtime. This is intentionally documented as a coursework/demo implementation.
2. **Image fallback:** the local fallback cannot genuinely understand an uploaded image. Remote multimodal AI is required for image analysis.
3. **Conversation persistence:** active Physics/Chemistry/Biology chat messages survive configuration changes, while long-term learning records are stored as individual History items rather than complete named chat threads.
4. **Exact cache only:** cache reuse uses normalized exact-question fingerprints, not semantic similarity.
5. **Adaptive local bank size:** the fallback bank varies values/order/scenarios but remains smaller than an online generative model.
6. **Study-plan timing:** the first session starts at the exact Generate timestamp. The prototype does not detect calendar conflicts, create system-calendar events or schedule notifications.
7. **Circuit scope:** the circuit screen illustrates simple series/parallel concepts and does not solve arbitrary networks or simulate instruments.
8. **Periodic-table scope:** the 118-element dataset provides concise reference information rather than a complete chemistry database.
9. **Build verification:** static and pure-Java checks were performed in the packaging environment, but a full Android APK build requires Android Studio/SDK and Gradle dependencies.
10. **Wider-team features:** 2FA, global notifications, leaderboard, avatars and application-wide gamification are not added because they are outside the assigned Science module.

## Recommended future improvements

1. Introduce an authenticated backend proxy with quotas, abuse monitoring and restricted server-side credentials.
2. Use Room plus WorkManager for migrations, offline queues and later synchronization.
3. Store complete named chat threads and add a conversation search screen.
4. Add semantic duplicate detection through embeddings on the backend.
5. Add calendar conflict detection, reminders and optional Google Calendar export.
6. Expand the local adaptive question bank and use spaced repetition by topic.
7. Add teacher-reviewed safety rules and verifiable citations for scientific claims.
8. Add Espresso/UI Automator tests, accessibility scanner checks and performance profiling on real low-end devices.
9. Add localized Vietnamese resources rather than hard-coded English UI strings.
