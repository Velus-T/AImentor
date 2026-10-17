# Thay đổi học tập ngày 17/10/2026

- **Streak:** chỉ nhận khi trả lời đầy đủ và nộp quiz thành công, kể cả ôn offline; chat không được tính. Một ngày tính một lần, dùng chung cho bốn môn.
- **Không giới hạn thời gian:** đã bỏ đồng hồ đếm ngược và tự nộp bài của Toán; các môn đều nộp bài chủ động.
- **Ôn câu sai:** thêm bộ lọc sau khi nộp quiz, giữ đáp án và giải thích; xem lại không cộng XP lần nữa.
- **XP tuần:** hiển thị riêng với level cá nhân. Giải đấu nhiều người chưa mở; cần backend xác thực điểm, ghép nhóm và xử lý thăng/xuống hạng.
- **Tham khảo mã nguồn mở:** AnkiDroid, Moodle và LibreLingo; áp dụng tương tác phù hợp bằng code riêng. Chưa thêm thuật toán ôn cách quãng.
- **Sửa lỗi phát hiện trong kiểm tra:** hộp máy tính Toán tự nhường chiều cao cho hàng nút khi chữ lớn khiến nút xếp dọc; vùng phím vẫn cuộn được.

Chi tiết kỹ thuật và bàn giao phía dưới.

## Streak, weekly competition and open-source references

## Implemented

- Streak is account-scoped and awarded only after a nonempty quiz is successfully submitted and saved. Chat, AI answers, opening the app, creating a question set and empty attempts do not count.
- Completing at least one quiz in any subject earns one calendar day. Offline quizzes count. A score of zero still counts if the learner answered every question.
- Current streak may continue from yesterday until today ends. Missing a whole day resets the current streak but preserves the personal best.
- The home screen shows current streak, personal best, today's status and seven recent calendar days. Vietnamese/English, light/dark and accessibility date descriptions are supported.
- A separate day ledger survives deletion of chat/quiz history. Account deletion removes its ledger. Writes join quiz transactions; a failed ledger write cannot falsely commit a quiz in the paths tested.
- Existing completed quiz attempts are imported once per account. Existing chat messages are never imported. Old attempts lack answer-by-answer completion metadata, so migration can only recognize persisted nonempty attempts.
- Removed the mathematics countdown, elapsed-realtime deadline, forced submission and deadline state. All subjects now require explicit submission with every question answered. Old unfinished bundles with a timeout flag no longer lock answers.
- The common result screen can show only incorrect questions, or all questions. It keeps original answers, feedback and numbering, preserves the filter after recreation, and does not save or award XP again.
- Weekly device progress sums XP from completed quizzes across four subjects, from Monday 00:00 through the next Monday (exclusive), in the current local timezone. Old, future and empty attempts are excluded. This is a local preview, not a competitive rank.

## Ranking: current status and backend handoff

No existing leaderboard based on personal levels was found in app/module source or resources. Personal levels remain learning-progress indicators. The home screen now separates weekly quiz XP from lifetime progress and clearly says leagues are not open yet. No fake competitors, local-account standings or invented promotions are displayed.

Competitive leagues are **not implemented on a server** in this change, consistent with the requested backend handoff. The local XP card does not claim that device XP has been verified or synchronized.

Backend implementation should provide:

1. A verified `quiz_attempt_id`/idempotency key, authenticated owner, subject, validated answers, server-computed XP and authoritative completion time. Replays must not award XP twice.
2. A weekly `season_id`, fixed `starts_at`, `ends_at` and cohort timezone. Do not recalculate competitive weeks when the device timezone changes.
3. Cohort membership within a league, matched by study activity/timezone rather than lifetime personal level. Cohort size and promotion/relegation slots should be configurable.
4. Standings ordered by **verified season XP**, with a published deterministic tie rule. Return explicit rank, member ID/display name, XP, current-user flag and promotion/retention/relegation zone.
5. Atomic season closure, promotion/relegation and reset of seasonal XP. Lifetime EXP and quiz streak stay separate. Return actual league names and thresholds instead of hardcoding current Duolingo rules.
6. Opt-out/privacy controls, removal of invalid XP, offline submission reconciliation and clearly identified cached/offline standings.

Suggested read response (contract proposal, no endpoint exists yet):

```json
{
  "status": "ACTIVE",
  "season_id": "server-assigned",
  "starts_at": "ISO-8601",
  "ends_at": "ISO-8601",
  "server_now": "ISO-8601",
  "league": {"id": "bronze", "name": "Bronze"},
  "cohort_id": "server-assigned",
  "promotion_slots": 5,
  "relegation_slots": 5,
  "standings": [],
  "my_rank": null,
  "verified_weekly_xp": 0
}
```

The example slot counts are proposals for AIMentor, not claims about Duolingo's current configuration. Other states should include `NOT_JOINED`, `DISABLED`, `OFFLINE` and `UNAVAILABLE`. Account identity must come from authentication, not a client-supplied user ID.

## Open-source research and adaptation

Checked official project repositories/manuals on 17 October 2026. Adapted interaction ideas with original AIMentor implementation; no external application code or assets were copied.

| Source | Useful idea | AIMentor adaptation |
| --- | --- | --- |
| [AnkiDroid repository](https://github.com/ankidroid/Anki-Android), [manual](https://docs.ankidroid.org/) | Visible learning progress, review-centered learning, night mode and spaced repetition | Added the compact streak/calendar and personal weekly progress; kept offline question-bank review. FSRS/spaced-repetition scheduling is a future feature, not implemented or claimed here. |
| [Moodle quiz settings](https://docs.moodle.org/503/en/Quiz_settings#Review_options) | Review responses, correctness and feedback; focus review on wrong answers | Added the shared mistakes-only review filter, with restoration and no duplicate XP. Chose untimed explicit submission per user instruction. |
| [LibreLingo repository](https://github.com/kantord/LibreLingo) | Shared exercises and saved progress across device sizes | Retained a single quiz form/result flow for all subjects instead of adding a second experience. Repository is archived as of the checked page; no runtime dependency was added. |
| [Duolingo league explanation](https://blog.duolingo.com/duolingo-leagues-leaderboards/) | Weekly cohorts across courses, activity/timezone matching and league movement | Defined weekly XP-based backend handoff; local progress is explicitly distinct from server standings. Duolingo is a product reference here, not an open-source dependency. |

## Limits

Streak and preview XP are currently on this device. Changing the device clock is not prevented. Cross-device streak sync and competitive anti-abuse validation belong to the backend. Local preview XP is derived from persisted quiz attempts and can change when those attempts are deleted; the independent streak ledger is retained. Real-device/tablet verification remains pending.

## Validation

Final debug/release/test APK builds, 110 unit tests and whole-project lint passed (0 errors, 479 warnings). The complete compact/offline device run passed 58 functional tests; 8 opt-in live API tests were deliberately skipped (runner reports OK, 66 tests). Screen: 320dp width, font scale 1.3, three-button Android navigation; configured cases cover English/dark mode. See WORK_IN_PROGRESS.md for logs and the initial calculator clipping failure that was corrected before the successful full rerun.
