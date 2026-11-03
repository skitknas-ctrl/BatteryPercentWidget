package se.dick.batterywidget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsets;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

/** Inställningar för widgeten. Öppnas från appmenyn eller via widgetens "ändra"-knapp (långtryck). */
public class SettingsActivity extends Activity {

    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private Settings settings;
    private ImageView preview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Marginal mot statusfält / kameraurtag (edge-to-edge)
        View root = findViewById(R.id.settings_root);
        int basePad = root.getPaddingTop();
        root.setOnApplyWindowInsetsListener((v, insets) -> {
            android.graphics.Insets bars = insets.getInsets(
                    WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout());
            v.setPadding(v.getPaddingLeft(), basePad + bars.top, v.getPaddingRight(), basePad + bars.bottom);
            return insets;
        });

        // Startad som konfigurationsaktivitet för en viss widget?
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }
        // Om användaren backar ur ska en nyss tillagd widget ändå behållas.
        setResult(RESULT_OK, new Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId));

        settings = Settings.load(this);
        preview = findViewById(R.id.settings_preview);

        bindSeekBar(R.id.seek_bar_brightness, R.id.label_bar_brightness, R.string.setting_bar_brightness,
                0, settings.barBrightness, v -> settings.barBrightness = v);
        bindSeekBar(R.id.seek_bar_opacity, R.id.label_bar_opacity, R.string.setting_bar_opacity,
                0, settings.barOpacity, v -> settings.barOpacity = v);
        bindSeekBar(R.id.seek_text_opacity, R.id.label_text_opacity, R.string.setting_text_opacity,
                0, settings.textOpacity, v -> settings.textOpacity = v);
        bindSeekBar(R.id.seek_scale, R.id.label_scale, R.string.setting_scale,
                50, settings.scale, v -> settings.scale = v);

        RadioGroup textColor = findViewById(R.id.radio_text_color);
        textColor.check(settings.textBlack ? R.id.radio_text_black : R.id.radio_text_white);
        textColor.setOnCheckedChangeListener((group, checkedId) -> {
            settings.textBlack = checkedId == R.id.radio_text_black;
            changed();
        });

        findViewById(R.id.button_reset).setOnClickListener(v -> {
            settings = new Settings();
            settings.save(this);
            recreate();
        });
        findViewById(R.id.button_about).setOnClickListener(v ->
                startActivity(new Intent(this, AboutActivity.class)));
        findViewById(R.id.button_done).setOnClickListener(v -> finish());

        updatePreview();
    }

    private interface IntSetter {
        void set(int value);
    }

    /** Kopplar ett reglage till en inställning. Reglaget går från min till 100. */
    private void bindSeekBar(int seekId, int labelId, int labelRes, int min, int value, IntSetter setter) {
        SeekBar seek = findViewById(seekId);
        TextView label = findViewById(labelId);
        seek.setMax(100 - min);
        seek.setProgress(value - min);
        label.setText(getString(labelRes, value));
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
                int v = progress + min;
                label.setText(getString(labelRes, v));
                setter.set(v);
                changed();
            }

            @Override
            public void onStartTrackingTouch(SeekBar bar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar bar) {
            }
        });
    }

    private void changed() {
        settings.save(this);
        updatePreview();
    }

    private void updatePreview() {
        int percent = BatteryWidget.readBatteryPercent(this);
        Bitmap bitmap = BatteryWidget.renderWidget(this, settings, percent, 1.5f);
        preview.setImageBitmap(bitmap);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Rita om alla widgetar på hemskärmen med de nya inställningarna
        BatteryWidget.updateAll(this);
    }
}
