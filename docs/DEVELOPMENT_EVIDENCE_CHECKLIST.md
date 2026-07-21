# Development Evidence Checklist — Assignment Part 2

Capture real evidence after opening the project in Android Studio.

## Architecture and process

1. Science-only package structure: `domain`, `data`, `di`, `presentation`, `util`.
2. `ScienceModule` dependency composition.
3. Three tutor strategies.
4. Shared `GeminiApiClient` used by chat and planning.
5. Adaptive quiz policy and repositories.
6. Separate Science database tables in Database Inspector.
7. Git branch/commits showing Science-only changes.

## Functional evidence

1. ChatGPT-style Science chat.
2. Separate Physics/Chemistry/Biology conversations.
3. Text question and follow-up context.
4. Gallery and FileProvider camera image.
5. Chemistry formula rendering.
6. In-app API Settings and Test Connection.
7. History search/filter.
8. Bookmark list.
9. Two different adaptive quiz generations at the same level/topic.
10. Support/On-level/Challenge status after scores.
11. Quiz state after rotation.
12. Study plan whose first session equals the Generate timestamp.
13. Saved planner completion checkboxes.
14. Circuit diagram.
15. Periodic table search.
16. Science progress screen.
17. Light and dark high-contrast controls.
18. Chat animation enabled/disabled and Battery Saver behaviour.

## Reliability evidence

1. Missing key fallback.
2. Invalid key error.
3. Network-disabled fallback.
4. Camera permission denial.
5. Invalid/large image handling.
6. Rapid tool navigation.
7. Background/resume and rotation.
8. Unit-test output.
9. Successful `assembleDebug` output from Android Studio.

For every item, record task, implementation steps, difficulty, resolution, requirement ID, screenshot and commit. Do not claim evidence that was not actually produced.
