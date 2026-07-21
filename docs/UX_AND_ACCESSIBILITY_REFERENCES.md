# UX, Accessibility and AI Integration References

Accessed 21 July 2026.

## Android and accessibility

1. Android Developers, *Core app quality guidelines*  
   https://developer.android.com/docs/quality-guidelines/core-app-quality  
   Used for: consistent UX, state preservation, 48dp touch targets, 4.5:1 small-text contrast and 3:1 large-text/graphics contrast.

2. Android Developers, *Make apps more accessible (Views)*  
   https://developer.android.com/guide/topics/ui/accessibility/views/apps-views  
   Used for: recommended 48×48dp interactive target and accessibility descriptions.

3. W3C, *Web Content Accessibility Guidelines (WCAG) 2.2*  
   https://www.w3.org/TR/WCAG22/  
   Used for: contrast principles and avoiding color-only communication.

## Learning-app design inspiration

4. Duolingo Design, *Typography*  
   https://design.duolingo.com/identity/typography  
   Used for: readable body size and clear hierarchy.

5. Duolingo Blog, *Core tabs redesign*  
   https://blog.duolingo.com/core-tabs-redesign/  
   Used for: balancing consistency with purpose and simplicity with clarity. The Science UI borrows these principles only; it does not copy Duolingo screens or add unrelated gamification.

## Gemini integration and key security

6. Google AI for Developers, *Text generation / generateContent*  
   https://ai.google.dev/gemini-api/docs/generate-content/text-generation  
   Used for: REST `generateContent`, `x-goog-api-key`, text and inline image payload structure.

7. Google AI for Developers, *Using Gemini API keys*  
   https://ai.google.dev/gemini-api/docs/api-key  
   Used for: key confidentiality, API restriction and production backend-proxy recommendation.

## Applied decisions

- Animated background only on chat; static surfaces elsewhere.
- Explicit light/dark Science palette.
- Approximate 48dp minimum interactive targets.
- Checked state includes native check/radio state plus border/background, not color alone.
- Consistent card radius, spacing, typography and button hierarchy.
- API key entered in app and encrypted at rest for coursework; production backend clearly documented.
