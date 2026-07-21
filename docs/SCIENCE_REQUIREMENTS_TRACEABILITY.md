# Science Requirements Traceability Matrix

## Functional requirements

| ID | Requirement | Implementation | Evidence to capture |
|---|---|---|---|
| SCI-01 | Ask a Science question using text | `ScienceActivity`, `ScienceQuestion`, `AskScienceQuestionUseCase` | Send a Physics, Chemistry and Biology text question |
| SCI-02 | Upload or photograph a Science problem | `OpenDocument`, `TakePicture`, Science `FileProvider`, `ScienceImageReader` | Gallery preview and camera preview inside chat |
| SCI-03 | Display chemistry notation clearly | `ChemicalFormulaFormatter` and formatted chat/history text | H₂O, CO₂, H₂SO₄, ions and a balanced equation |
| SCI-04 | Explain phenomena in simple language | Subject tutor strategies and shared response rules | Direct answer, principle, example and misconception |
| SCI-05 | Save history by Physics/Chemistry/Biology | Separate encrypted SQLite history repository | Search and filter History by subject |
| SCI-06 | Bookmark important answers | `SaveBookmarkUseCase`, bookmark repository and activity | Add/remove a bookmark from chat |
| SCI-07 | Suggest further learning | Subject prompt requests applications, safe experiments and follow-up questions | AI answer with a practical application/follow-up |
| SCI-08 | Generate changing 3–5 question quizzes | `GenerateScienceQuizUseCase`, Gemini/local generators and variation repository | Generate two different quizzes for the same topic |
| SCI-09 | Draw a simple Physics circuit | `CircuitDiagramView`, `CircuitDiagramActivity` | Series/parallel and open/closed switch states |
| SCI-10 | Search the periodic table | `science_periodic_elements.json`, `AssetPeriodicTableRepository` | Search by name, symbol and atomic number |
| SCI-11 | Chat interface similar to a modern AI assistant | RecyclerView chat messages, bottom composer and loading/error messages | Multi-turn follow-up conversation |
| SCI-12 | Separate Physics, Chemistry and Biology AI tutors | Three `ScienceTutorPromptStrategy` implementations and three active conversation lists | Switch tabs and show that context is not mixed |
| SCI-13 | Enter API settings from the app | `ScienceSettingsActivity`, `EncryptedScienceApiSettings` | Save/test/remove key without editing code |
| SCI-14 | Adapt quiz to education level and performance | `PerformanceBasedQuizPolicy`, recent attempt repository, challenge-aware local fallback ordering | Low/high score changes support/challenge and question mix but retains level |
| SCI-15 | Generate a study schedule from creation time | `GenerateStudyPlanUseCase`, remote/local planners | First session timestamp equals Generate timestamp |
| SCI-16 | Retain other Science tools | History, bookmarks, circuit, periodic table and progress activities | Open every Science tool from the chat tools panel |

## Assignment-wide requirements addressed inside Science

| ID | Brief requirement | Science implementation |
|---|---|---|
| APP-01 | Text and image question submission | Chat composer, gallery picker and camera |
| APP-02 | Appropriate answer for student level | Education-level selection included in subject prompt |
| APP-03 | Step-by-step solutions and formulas | Tutor-specific prompt rules |
| APP-04 | Search, bookmark and organize by subject | Encrypted History and Bookmark screens |
| APP-05 | AI-generated practice | Adaptive 3–5 question quiz |
| APP-06 | Basic progress tracking | Science question/bookmark/quiz/review statistics |
| APP-07 | Offline access to previous answers | Local SQLite history and bookmark storage |
| APP-08 | Reduce repeated API calls | Normalization plus SHA-256 exact-question fingerprint cache |
| APP-09 | Protect Science data | Android Keystore encryption and private internal image copies |
| APP-10 | Limited budget and junior team | Local fallbacks, shared AI client and manual DI with simple Java interfaces |

## Non-functional requirements

| Area | Design response |
|---|---|
| Reliability | Defensive callbacks, local AI fallback, storage error handling, lifecycle checks and retained quiz state |
| Performance | 1280px image downsampling, background image decoding, small LRU image cache and 30fps chat-only animation |
| Usability | Short navigation path, consistent cards, bottom chat composer and clear loading/error states |
| Accessibility | High-contrast light/dark palettes, visible checked states, content descriptions and approximately 48dp controls |
| Maintainability | Domain/data/presentation separation, use cases, repositories, strategies and dependency injection |
| Privacy | Encrypted Science text, private images and no hard-coded API key |
| Platform | Android Java, API 24+, Material 3 DayNight theme |

## Scope note

Authentication, the team Home/Category screens and other subject modules remain owned by the existing application. The Science package does not replace or rewrite them.
