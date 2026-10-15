# Modern UI and Android safe areas

The main app now uses a shared lavender palette, rounded forms, native ripple
feedback and short activity transitions. Light and dark themes have separate
text and surface colors. Home, Quiz and Settings link to the existing science
activities. Forms scroll, use minimum control heights, and limit their readable
width on larger windows. The menu pager is above the bottom navigation instead
of underneath it. The science welcome panel scrolls when space is limited.

`InsetActivity` owns system insets for every activity. The content container gets
the union of status/navigation bars and cutouts; its bottom padding is the larger
of the keyboard and system bar insets. Padding is derived from its initial value
on each dispatch, so it does not accumulate. Insets are consumed there to prevent
Material navigation controls applying them twice. New activities should inherit
this class and avoid adding another system-bar padding listener.

## Validation

- Debug APK, instrumented test APK, unit tests and Android lint are built locally.
- SystemInsetsTest checks three-button/gesture-sized bottom insets, repeat
  dispatch, keyboard appearance/disappearance and a landscape cutout.
- Instrumented tests still require a connected device or running emulator.
- Local Java is 25, which the repository's Gradle 8.13 wrapper cannot run on.
  Validation uses the installed Gradle 9.4.1 without changing the repository
  wrapper. Use JDK 17 with the existing wrapper in Android Studio.

## Device review still needed

Check login, signup, menu, categories and all science activities with gesture
and three-button navigation. Open/close the keyboard, rotate, and repeat at
320dp, 360dp, 600dp and tablet widths. Repeat with large fonts, light/dark mode
and a cutout. Verify the last control can be scrolled into view, touches near
the system bars reach the intended app control, and the composer stays above
the keyboard. Build/lint success does not replace this visual device review.
