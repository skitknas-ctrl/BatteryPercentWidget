package se.dick.batterywidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.BatteryManager;
import android.os.Bundle;
import android.widget.RemoteViews;

public class BatteryWidget extends AppWidgetProvider {

    public static final String ACTION_REFRESH = "se.dick.batterywidget.REFRESH";

    /** Vid denna nivå eller lägre blir den tomma delen av mätaren röd. */
    static final int LOW_THRESHOLD = 15;

    // Geometri i batteriramens originalbild (andelar av bredd respektive höjd).
    // Fönstret är den helt genomskinliga insidan; topp- och bottendelen innehåller polerna
    // och ramens rundade hörn, mittdelen bara de raka sidorna och kan dras ut i höjd.
    static final float WINDOW_LEFT = 0.111f, WINDOW_RIGHT = 0.888f;
    static final float WINDOW_TOP = 0.108f, WINDOW_BOTTOM = 0.932f;
    static final float CAP_TOP_END = 0.125f, CAP_BOTTOM_START = 0.915f;
    /** Mätarens nederkant: vid metallkanten under glaset, så att inget glapp syns. */
    static final float BAR_BOTTOM = 0.945f;

    /** Extra marginal i sidled runt mätaren, andel av widgetens bredd. */
    static final float BAR_INSET = 0.01f;
    /** Textens marginal inne i fönstret, andel av fönstrets bredd/höjd. */
    static final float TEXT_INSET = 0.10f;

    /** Bitmapens bredd i pixlar; höjden följer rutans proportioner. */
    static final int RENDER_WIDTH = 360;

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        // Alla andra broadcasts (laddare i/ur, låg/ok batteri, boot, tryck på widgeten)
        // leder till en omritning av alla instanser.
        String action = intent.getAction();
        if (action != null && !AppWidgetManager.ACTION_APPWIDGET_UPDATE.equals(action)
                && !AppWidgetManager.ACTION_APPWIDGET_OPTIONS_CHANGED.equals(action)) {
            updateAll(context);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager manager, int[] ids) {
        for (int id : ids) {
            updateOne(context, manager, id);
        }
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager manager, int id, Bundle options) {
        updateOne(context, manager, id);
    }

    @Override
    public void onEnabled(Context context) {
        RefreshJob.schedule(context);
    }

    @Override
    public void onDisabled(Context context) {
        RefreshJob.cancel(context);
    }

    public static void updateAll(Context context) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        int[] ids = manager.getAppWidgetIds(new ComponentName(context, BatteryWidget.class));
        for (int id : ids) {
            updateOne(context, manager, id);
        }
    }

    static void updateOne(Context context, AppWidgetManager manager, int id) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_battery);
        int percent = readBatteryPercent(context);
        views.setImageViewBitmap(R.id.widget_image,
                renderWidget(context, Settings.load(context), percent, widgetAspect(manager, id)));

        // Klick på widgeten ritar om den
        Intent refresh = new Intent(context, BatteryWidget.class).setAction(ACTION_REFRESH);
        PendingIntent pi = PendingIntent.getBroadcast(
                context, 0, refresh, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widget_root, pi);

        manager.updateAppWidget(id, views);
    }

    /** Rutans höjd delat med bredd (stående läge), eller 1.5 om storleken inte är känd. */
    static float widgetAspect(AppWidgetManager manager, int id) {
        Bundle options = manager.getAppWidgetOptions(id);
        int w = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
        int h = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT);
        return (w > 0 && h > 0) ? (float) h / w : 1.5f;
    }

    /** Ritar hela widgeten i rutans proportioner och krymper den enligt storleksinställningen. */
    static Bitmap renderWidget(Context context, Settings settings, int percent, float aspect) {
        Bitmap full = renderFull(context, settings, percent, aspect);
        if (settings.scale >= 100) {
            return full;
        }
        int W = full.getWidth(), H = full.getHeight();
        Bitmap out = Bitmap.createBitmap(W, H, Bitmap.Config.ARGB_8888);
        float f = settings.scale / 100f;
        float dw = W * f, dh = H * f;
        float left = (W - dw) / 2f, top = (H - dh) / 2f;
        new Canvas(out).drawBitmap(full, null, new RectF(left, top, left + dw, top + dh),
                new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
        return out;
    }

    /** Ritar mätaren i ramens fönster, ramen i tre delar och texten överst, i full storlek. */
    static Bitmap renderFull(Context context, Settings settings, int percent, float aspect) {
        int W = RENDER_WIDTH;
        int H = Math.max(1, Math.round(W * aspect));
        Bitmap out = Bitmap.createBitmap(W, H, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(out);

        Bitmap frame = BitmapFactory.decodeResource(context.getResources(), R.drawable.battery_frame);
        int fw = frame.getWidth(), fh = frame.getHeight();
        float scale = (float) W / fw;

        // Ramens tre delar: topp och botten skalas likformigt efter bredden, mitten fyller resten.
        int capTopSrc = Math.round(fh * CAP_TOP_END);
        int capBottomSrc = Math.round(fh * CAP_BOTTOM_START);
        float capTopDst = capTopSrc * scale;
        float capBottomDst = (fh - capBottomSrc) * scale;
        if (capTopDst + capBottomDst > H) {
            // Rutan är lägre än polerna tillsammans: skala hela bilden rakt av.
            capTopDst = H * (float) capTopSrc / fh;
            capBottomDst = H * (float) (fh - capBottomSrc) / fh;
        }
        float middleTop = capTopDst, middleBottom = H - capBottomDst;

        // Fönstret i målbilden
        float winLeft = W * WINDOW_LEFT, winRight = W * WINDOW_RIGHT;
        float winTop = middleTop - (CAP_TOP_END - WINDOW_TOP) * fh * scale;
        float winBottom = middleBottom + (WINDOW_BOTTOM - CAP_BOTTOM_START) * fh * scale;

        // Lager 1: mätaren
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        float bl = winLeft + W * BAR_INSET, br = winRight - W * BAR_INSET;
        float bt = winTop;
        float bb = middleBottom + (BAR_BOTTOM - CAP_BOTTOM_START) * fh * scale;
        float fillTop = bb - (bb - bt) * Math.max(0, Math.min(100, percent)) / 100f;
        paint.setColor(percent <= LOW_THRESHOLD ? settings.barEmptyLowColor() : settings.barEmptyColor());
        canvas.drawRect(bl, bt, br, fillTop, paint);
        paint.setColor(settings.barFillColor());
        canvas.drawRect(bl, fillTop, br, bb, paint);

        // Lager 2: ramen i tre delar
        paint.setColor(0xFFFFFFFF);
        canvas.drawBitmap(frame, new Rect(0, 0, fw, capTopSrc),
                new RectF(0, 0, W, middleTop), paint);
        canvas.drawBitmap(frame, new Rect(0, capTopSrc, fw, capBottomSrc),
                new RectF(0, middleTop, W, middleBottom), paint);
        canvas.drawBitmap(frame, new Rect(0, capBottomSrc, fw, fh),
                new RectF(0, middleBottom, W, H), paint);

        // Lager 3: texten, sträckt att fylla fönstret minus marginal
        float mx = (winRight - winLeft) * TEXT_INSET, my = (winBottom - winTop) * TEXT_INSET;
        Bitmap text = renderText(percent + "%", settings.textColor());
        canvas.drawBitmap(text, null,
                new RectF(winLeft + mx, winTop + my, winRight - mx, winBottom - my), paint);

        return out;
    }

    /** Ritar texten till en bitmap som är exakt så stor som glyferna och roterar den 90 grader
     *  moturs, så att den kan sträckas fritt till textytan. */
    static Bitmap renderText(String text, int color) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
        paint.setColor(color);
        paint.setTypeface(Typeface.create("sans-serif-condensed", Typeface.BOLD));
        paint.setTextSize(300f);
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        Bitmap bitmap = Bitmap.createBitmap(
                Math.max(1, bounds.width()), Math.max(1, bounds.height()), Bitmap.Config.ARGB_8888);
        new Canvas(bitmap).drawText(text, -bounds.left, -bounds.top, paint);
        Matrix matrix = new Matrix();
        matrix.postRotate(-90);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    static int readBatteryPercent(Context context) {
        BatteryManager bm = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
        return bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
    }
}
