package com.tactshoot.game;

import com.badlogic.gdx.Gdx;

/**
 * Optional logcat telemetry for automated device checks.
 *
 * Everything is a no-op unless the launcher sets {@link #enabled}, which only
 * happens when the app is started with the intent extra
 * {@code tactshoot.debugInput=true}. Normal players never trigger it.
 */
public final class DebugProbe {

    public static boolean enabled;
    public static boolean autostartPractice;

    private static TouchControls touch;
    private static float accum;

    private DebugProbe() {
    }

    /** Remembers the live touch layout so a test driver can swipe the real controls. */
    public static void layout(TouchControls controls) {
        touch = controls;
        if (!enabled || controls == null) return;
        Gdx.app.log("TSDBG", "layout w=" + controls.width() + " h=" + controls.height()
                + " stick=" + controls.stickCenterX() + "," + controls.stickCenterY()
                + " r=" + controls.stickRadius());
    }

    public static void touch(float x, float y) {
        if (!enabled) return;
        Gdx.app.log("TSDBG", "touch x=" + x + " y=" + y);
    }

    /** Called once per frame from the game loop; logs about four samples per second. */
    public static void frame(float dt, PlayerEntity local, InputState in) {
        if (!enabled || local == null) return;
        accum += dt;
        if (accum < 0.25f) return;
        accum = 0f;
        Gdx.app.log("TSDBG", "frame x=" + local.pos.x + " y=" + local.pos.y + " z=" + local.pos.z
                + " yaw=" + local.yaw + " pitch=" + local.pitch
                + " ammo=" + local.mag[local.weapon == 0 ? 0 : 1]
                + " hp=" + local.hp
                + " fire=" + (in != null && in.fire));
    }
}
