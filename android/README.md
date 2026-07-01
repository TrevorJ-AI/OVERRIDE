# OVERRIDE (Android) — minimal wake-quiz alarm

A from-scratch Android companion to the Windows OVERRIDE app: set a daily alarm time,
and when it fires you get a full-screen, lock-screen-busting math quiz that's the only
way to silence the (max-volume, looping, vibrating) alarm.

This is **not a port** of the Windows scripts — Windows Task Scheduler, the Core Audio
API, and HTA windows have no Android equivalent. It's a new, minimal app built to the
same spirit: solve math to make it stop.

## What it does
- `MainActivity` — pick a time, **SET ALARM** (daily, reschedules itself each day),
  **CANCEL ALARM**, or **TEST NOW (10s)** to fire immediately and confirm it actually
  works on your phone.
- Alarm fires via `AlarmManager.setAlarmClock()` (same mechanism real alarm-clock apps
  use — exempt from Doze/battery restrictions and allowed to launch a full-screen
  activity even from the background).
- `AlarmService` (foreground service) forces `STREAM_ALARM` to max volume, loops the
  device's default alarm sound, vibrates, and holds a wake lock.
- `AlarmActivity` shows over the lock screen with a random arithmetic problem; only a
  correct answer stops the service. Back button is disabled.
- `BootReceiver` re-arms the saved daily alarm after a reboot.

## Limitations (be aware before relying on it)
- No Android Studio / Gradle / AndroidX involved — this network sandbox blocks
  `dl.google.com`, so it's compiled directly against the **API 23 (Android 6.0)**
  platform jar shipped in Ubuntu's package archive, using classic `aapt`/`dx` tools
  (see `build.sh`). It still installs and runs fine on modern Android (targetSdk 23
  apps get legacy permission/background behavior), but the UI is plain framework
  widgets, not Material.
- Self-signed with a throw-away debug-style keystore (`override.keystore`, gitignored).
  If you rebuild from a different machine you'll get a different signature — you'd
  need to uninstall the old APK before installing the new one.
- No true kiosk/device-owner lock — same honest caveat as the Windows version: an
  admin/user can always force-stop or uninstall the app. It's friction, not a prison.
- **Test it before you trust it**: use the in-app "TEST NOW (10s)" button once on your
  actual phone (screen off, or locked) to confirm the full-screen alarm and sound
  actually come through on your Android version/OEM skin. Some manufacturers
  (Xiaomi/Huawei/etc.) aggressively kill background apps — if so, whitelist OVERRIDE
  in battery-optimization settings.

## Rebuilding
Requires `aapt`, `dx`, `zipalign`, `apksigner`, `javac`/`keytool`, and the API 23
platform jar (all installable via `apt-get install aapt android-sdk-build-tools
android-sdk-platform-23 dalvik-exchange` on Debian/Ubuntu). Then:

```sh
./build.sh
```

Output: `bin/OVERRIDE.apk`.

To evolve this into a real Android Studio project later (Material UI, AndroidX,
notification channels, exact-alarm permission handling for API 31+, etc.), import
`src/` and `res/` into a new Gradle project — the four Java classes have no
Gradle-specific dependencies.
