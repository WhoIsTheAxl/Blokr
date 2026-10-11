package com.blokr.app.ui;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/** Shared Blokr visual primitives. Uses dp/sp and adapts to available screen width. */
public final class UIFactory {
    public static final int BACKGROUND = 0xFF18112C;
    public static final int SURFACE = 0xFF29203E;
    public static final int ACCENT = 0xFF9564E8;
    public static final int TEXT = Color.WHITE;
    public static final int MUTED = 0xFFCCBDEB;

    private final Context context;

    public UIFactory(Context context) {
        if (context == null) throw new IllegalArgumentException("context == null");
        this.context = context;
    }

    public int dp(float value) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value,
                context.getResources().getDisplayMetrics()));
    }

    public TextView text(CharSequence value, float sp, boolean muted) {
        TextView view = new TextView(context);
        view.setText(value);
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        view.setTextColor(muted ? MUTED : TEXT);
        view.setGravity(Gravity.CENTER_VERTICAL);
        return view;
    }

    public Button button(CharSequence label, View.OnClickListener action) {
        Button button = new Button(context);
        button.setText(label);
        button.setTextColor(TEXT);
        button.setAllCaps(false);
        button.setBackground(rounded(ACCENT, 12));
        button.setMinHeight(dp(48));
        button.setOnClickListener(action);
        return button;
    }

    public GradientDrawable rounded(int color, int radiusDp) {
        GradientDrawable background = new GradientDrawable();
        background.setColor(color);
        background.setCornerRadius(dp(radiusDp));
        return background;
    }

    /** A scrolling, width-constrained page with accessible margins and orientation-aware spacing. */
    public ScrollView page() {
        ScrollView scroll = new ScrollView(context);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(BACKGROUND);
        scroll.setClipToPadding(false);
        return scroll;
    }

    public LinearLayout column() {
        LinearLayout column = new LinearLayout(context);
        column.setOrientation(LinearLayout.VERTICAL);
        column.setGravity(Gravity.TOP);
        int spacing = dp(16);
        column.setPadding(spacing, spacing, spacing, spacing);
        return column;
    }

    /** Caps content width on tablets and in landscape while filling narrow phones. */
    public View pageContent(ScrollView page, LinearLayout content) {
        if (page == null || content == null) throw new IllegalArgumentException("Missing page/content");
        LinearLayout outer = new LinearLayout(context);
        outer.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        int widthDp = context.getResources().getConfiguration().screenWidthDp;
        int marginDp = widthDp >= 600 ? 24 : 8;
        int maxWidth = widthDp >= 600 ? 560 : widthDp >= 480 ? 440 : Math.max(1, widthDp - 2 * marginDp);
        int horizontalMargin = dp(marginDp);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                maxWidth > 0 ? dp(maxWidth) : ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = horizontalMargin;
        params.rightMargin = horizontalMargin;
        outer.addView(content, params);
        page.addView(outer);
        return page;
    }

    /** Creates a container that automatically reflows when its measured width changes. */
    public ResponsiveLayout layout(LayoutPolicy.Mode mode, int minCellDp, int gapDp, int maxColumns) {
        return new ResponsiveLayout(context, mode, minCellDp, gapDp, maxColumns);
    }

    public ResponsiveLayout adaptiveLayout() {
        return layout(LayoutPolicy.Mode.ADAPTIVE, 180, 12, 3);
    }

    public boolean isLandscape() {
        return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }
}
