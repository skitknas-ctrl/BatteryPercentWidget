package se.dick.batterywidget;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;

/** Användarens inställningar för widgeten. Gäller alla instanser. */
final class Settings {

    private static final String PREFS = "settings";
    private static final String KEY_BAR_BRIGHTNESS = "bar_brightness";
    private static final String KEY_BAR_OPACITY = "bar_opacity";
    private static final String KEY_TEXT_OPACITY = "text_opacity";
    private static final String KEY_TEXT_BLACK = "text_black";
    private static final String KEY_SCALE = "scale";

    /** Den gröna mätarens ljushet, 0–100 (100 = klarast grön). */
    int barBrightness = 100;
    /** Mätarens opacitet, 0–100. Gäller både fylld och tom del. */
    int barOpacity = 50;
    /** Textens opacitet, 0–100. */
    int textOpacity = 75;
    /** Svart text i stället för vit. */
    boolean textBlack = false;
    /** Widgetens storlek inom sin ruta, 50–100 %. */
    int scale = 100;

    static Settings load(Context context) {
        SharedPreferences p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        Settings s = new Settings();
        s.barBrightness = clamp(p.getInt(KEY_BAR_BRIGHTNESS, s.barBrightness), 0, 100);
        s.barOpacity = clamp(p.getInt(KEY_BAR_OPACITY, s.barOpacity), 0, 100);
        s.textOpacity = clamp(p.getInt(KEY_TEXT_OPACITY, s.textOpacity), 0, 100);
        s.textBlack = p.getBoolean(KEY_TEXT_BLACK, s.textBlack);
        s.scale = clamp(p.getInt(KEY_SCALE, s.scale), 50, 100);
        return s;
    }

    void save(Context context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putInt(KEY_BAR_BRIGHTNESS, barBrightness)
                .putInt(KEY_BAR_OPACITY, barOpacity)
                .putInt(KEY_TEXT_OPACITY, textOpacity)
                .putBoolean(KEY_TEXT_BLACK, textBlack)
                .putInt(KEY_SCALE, scale)
                .apply();
    }

    /** Grön i vald ljushet och opacitet: den fyllda delen av mätaren. */
    int barFillColor() {
        return Color.argb(alpha(barOpacity), 0, Math.round(255 * barBrightness / 100f), 0);
    }

    /** Svart i vald opacitet: den tomma delen av mätaren. */
    int barEmptyColor() {
        return Color.argb(alpha(barOpacity), 0, 0, 0);
    }

    /** Röd i vald opacitet: den tomma delen av mätaren vid låg nivå. */
    int barEmptyLowColor() {
        return Color.argb(alpha(barOpacity), 255, 0, 0);
    }

    int textColor() {
        int v = textBlack ? 0 : 255;
        return Color.argb(alpha(textOpacity), v, v, v);
    }

    private static int alpha(int percent) {
        return Math.round(255 * percent / 100f);
    }

    private static int clamp(int v, int lo, int hi) {
        return Math.max(lo, Math.min(hi, v));
    }
}
