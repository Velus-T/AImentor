# Science Module Test Plan

## Test environments

Test on at least:

- Android 7/API 24 device or emulator.
- A current Android version.
- One small phone and one normal/large phone.
- Light and dark system themes.
- Valid key, missing key, invalid key and disabled network.

## Functional tests

| ID | Test | Steps | Expected result |
|---|---|---|---|
| CHAT-01 | Physics text question | Select Physics and ask a mechanics question | Physics tutor response and History record |
| CHAT-02 | Chemistry-specific behaviour | Ask for balancing H2 + O2 → H2O | Balanced equation and chemistry reasoning |
| CHAT-03 | Biology-specific behaviour | Ask about photosynthesis | Ordered biological process, not Physics/Chemistry rules |
| CHAT-04 | Separate subject context | Ask in Physics, switch to Biology, then return | Each tab shows only its own active messages |
| CHAT-05 | Follow-up context | Ask a question, then “Explain step 2 again” | AI receives recent same-subject context |
| CHAT-06 | Empty send | Remove text/image and press Send | Validation message; no request or crash |
| CHAT-07 | Gallery image | Select a readable homework image and send | Preview and image message appear; remote AI analyses when configured |
| CHAT-08 | Camera permission denied | Deny Camera permission | Clear explanation; gallery remains usable |
| CHAT-09 | Camera capture | Grant permission and capture | FileProvider image preview appears without storage permission |
| CHAT-10 | Invalid/large image | Pick a corrupt or large image | Safe error/downsampling; Activity remains open |
| CHAT-11 | New chat while loading | Start request, then press New chat | Action is blocked until current answer completes |
| CHAT-12 | Rotate during chat | Enter text/attach image/rotate | input, selected subject and available conversation state restore safely |
| CHAT-13 | Copy/bookmark | Copy and toggle bookmark on saved AI answer | Clipboard/Bookmark state updates safely |
| API-01 | Save key from app | Enter key/model and Save | Key is encrypted; no code edit required |
| API-02 | Test valid key | Tap Test connection | Success state announced |
| API-03 | Invalid key/model | Test invalid values | Readable error; no crash |
| API-04 | Remove key | Remove saved key | Local fallbacks become active |
| QUIZ-01 | Level lock | Choose High School and generate repeatedly | Every request remains High School |
| QUIZ-02 | New variations | Generate same subject/topic twice | seed/order/scenario changes and recent prompts are excluded |
| QUIZ-03 | Low score adaptation | Score below 50%, generate again | Support mode within the same level |
| QUIZ-04 | High score adaptation | Score at least 80%, generate again | Challenge mode within the same level |
| QUIZ-05 | Question formats | Generate 5 questions | Valid mix of MCQ/true-false/fill/matching when available |
| QUIZ-06 | Incomplete submit | Leave one answer empty | Submit is blocked and identifies the unanswered question |
| QUIZ-07 | Rotate quiz | Answer some questions and rotate | Quiz/questions/answers persist; submitted feedback persists |
| QUIZ-08 | Remote failure | Disable network with key saved | Local adaptive quiz is produced |
| PLAN-01 | Exact start timestamp | Note time and press Generate | First session equals captured creation moment, not midnight or +10 minutes |
| PLAN-02 | Daily continuation | Generate multi-day plan | Later sessions use same clock time on following days |
| PLAN-03 | Level/subject limits | Choose Middle School Chemistry | All content stays inside selected level/subject |
| PLAN-04 | Missing key/network | Generate without remote AI | Local fallback plan appears |
| PLAN-05 | Completion persistence | Check sessions, close/reopen | Checkboxes restore from encrypted saved plan |
| PLAN-06 | Clear plan | Clear and confirm | Plan disappears without crash |
| DATA-01 | History search/filter | Create all three subjects and search/filter | Correct records only |
| DATA-02 | Offline History/Bookmarks | Disable network and open screens | Existing data remains available |
| TOOL-01 | Circuit states | Toggle series/parallel and open/closed | Drawing and explanation update |
| TOOL-02 | Periodic lookup | Search Oxygen, O, 8 | Correct element result |
| UI-01 | Light/dark contrast | Inspect every Science screen in both themes | Text/options/controls remain readable |
| UI-02 | Checked states | Select subjects, radio answers and planner checks | Border/background/check mark all indicate selection |
| UI-03 | Animated background | Enable/disable animation and Battery Saver | Animation is chat-only and pauses when required |
| STAB-01 | Rapid navigation | Open/close Science tools repeatedly | No ActivityNotFound or crash |
| STAB-02 | Background/resume | Send/prepare content, background then resume | No stale callback crash or blank state |

## Automated unit tests included

- `SciencePromptFactoryTest` — verifies distinct tutor rules and conversation context.
- `AdaptiveQuizPolicyTest` — verifies level preservation, support/challenge selection, unique seeds and repository-failure fallback.
- `LocalAdaptiveQuizBankTest` — verifies the offline bank changes question mix for Support/Challenge without changing education level.
- `StudyPlanGeneratorTest` — verifies exact generation-time start and retained education level.
- `ScienceQuestionValidatorTest` — verifies text/image acceptance and empty-request rejection.
- `ExampleUnitTest` — original project sample.

Run in Android Studio:

```bash
./gradlew testDebugUnitTest
./gradlew assembleDebug
```

## Evidence recording

For each manual test, record:

- device/API;
- key/network state;
- steps;
- expected result;
- actual result;
- pass/fail;
- screenshot or screen recording;
- defect/fix commit when applicable.

Do not claim a manual or instrumented test passed without running it on an Android device/emulator.
