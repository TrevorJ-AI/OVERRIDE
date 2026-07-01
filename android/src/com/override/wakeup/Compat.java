package com.override.wakeup;

import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.lang.reflect.Method;

/**
 * This app compiles against the API 23 SDK (see build.sh), so newer APIs like
 * Context#startForegroundService (API 26) aren't in the compile classpath even
 * though they exist at runtime on newer devices. Reflection bridges the gap.
 */
public class Compat {
    public static void startForegroundServiceCompat(Context ctx, Intent intent) {
        if (Build.VERSION.SDK_INT >= 26) {
            try {
                Method m = Context.class.getMethod("startForegroundService", Intent.class);
                m.invoke(ctx, intent);
                return;
            } catch (Exception ignored) {
                // fall through to startService below
            }
        }
        ctx.startService(intent);
    }
}
