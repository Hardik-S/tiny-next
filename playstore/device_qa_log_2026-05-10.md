# Tiny Next Device QA Log - 2026-05-10

Device:

- Serial: `R5CYA18JAXH`
- Model reported by Gradle install: `SM-S731W`
- Build installed: debug package `com.batb4016.tinynext.debug`

Commands:

```powershell
adb -s R5CYA18JAXH uninstall com.batb4016.tinynext.debug
.\gradlew.bat :app:installDebug
adb -s R5CYA18JAXH logcat -c
adb -s R5CYA18JAXH shell am start -n com.batb4016.tinynext.debug/com.batb4016.tinynext.MainActivity
adb -s R5CYA18JAXH exec-out uiautomator dump /dev/tty
```

Result:

- Fresh uninstall/install succeeded.
- App launched into first-run onboarding.
- `Use sample tasks` completed onboarding and opened Home.
- Home showed `Active tasks` count `6` after sample task creation.
- Add Task accepted typed input and enabled Save.
- Saving the added task returned Home and increased `Active tasks` to `7`.
- Task List loaded and showed the newly added task.
- Edit Task loaded for the added task.
- Changing the estimate from `5 min` to `15 min` and saving returned to Task List.
- Task List showed the edited task with `Quick • 15 min • Not done yet`.
- Delete showed snackbar text `Task deleted` with `Undo`.
- Tapping `Undo` restored the deleted task in Task List.
- Pick flow opened Result with `Next up`.
- `Another` changed the selected task from `Reply to one message` to `Review today's list`.
- `Done` opened Stats.
- Stats showed `Today = 1`, `Total = 1`, `Streak = 1`, `Best = 1`, and recent completion date `2026-05-10`.

Not completed in this pass:

- Final Stats Back-to-Home recheck: device disconnected before the last tap/dump completed.
- Snooze immediate picker exclusion.
- Settings `Delete all local data`.
- Premium fallback.
- Crash-buffer capture after QA.

Notes:

- The `adb shell input text` command inserted `%20` literally in the test title. This is an adb input encoding artifact from the QA method, not an app UI blocker; the useful evidence from that step is that typed task input was accepted, saved, listed, editable, and restorable after delete/undo.
