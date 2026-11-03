package se.dick.batterywidget;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.util.Linkify;
import android.view.View;
import android.view.WindowInsets;
import android.widget.TextView;

/** Informationssida: version, byggdatum och webbadress. Öppnas från appmenyn. */
public class AboutActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        // Marginal mot statusfält / kameraurtag (edge-to-edge)
        View root = findViewById(R.id.about_root);
        int basePad = root.getPaddingTop();
        root.setOnApplyWindowInsetsListener((v, insets) -> {
            android.graphics.Insets bars = insets.getInsets(
                    WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout());
            v.setPadding(v.getPaddingLeft(), basePad + bars.top, v.getPaddingRight(), basePad + bars.bottom);
            return insets;
        });

        String versionName = BuildConfig.VERSION_NAME;
        int versionCode = BuildConfig.VERSION_CODE;
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
            versionName = info.versionName;
            versionCode = (int) info.getLongVersionCode();
        } catch (PackageManager.NameNotFoundException ignored) {
        }
        TextView version = findViewById(R.id.about_version);
        version.setText(getString(R.string.about_version, versionName, versionCode));

        TextView built = findViewById(R.id.about_built);
        built.setText(getString(R.string.about_built, BuildConfig.BUILD_DATE));

        TextView link = findViewById(R.id.about_link);
        link.setText(R.string.about_url);
        Linkify.addLinks(link, Linkify.WEB_URLS);
    }
}
