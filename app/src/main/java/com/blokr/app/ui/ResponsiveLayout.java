package com.blokr.app.ui;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

/**
 * Wrap-content responsive layout with vertical, horizontal, grid and adaptive modes.
 * Re-measures and reflows children when the available width changes, including rotation.
 * Place inside a ScrollView for long content. Child widths are managed by this layout.
 */
public final class ResponsiveLayout extends ViewGroup {
    private final LayoutPolicy.Mode mode;
    private final int minCellPx;
    private final int gapPx;
    private final int maxColumns;
    private int columns = 1;

    public ResponsiveLayout(Context context, LayoutPolicy.Mode mode, int minCellDp,
                            int gapDp, int maxColumns) {
        super(context);
        if (mode == null || minCellDp <= 0 || gapDp < 0 || maxColumns <= 0)
            throw new IllegalArgumentException("Invalid responsive layout configuration");
        this.mode = mode;
        float density = getResources().getDisplayMetrics().density;
        minCellPx = Math.max(1, Math.round(minCellDp * density));
        gapPx = Math.round(gapDp * density);
        this.maxColumns = maxColumns;
    }

    public int getResolvedColumns() { return columns; }

    @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        if (widthMode == MeasureSpec.UNSPECIFIED)
            width = getPaddingLeft() + getPaddingRight() + minCellPx;
        int available = Math.max(0, width - getPaddingLeft() - getPaddingRight());
        int proposed = LayoutPolicy.columns(mode, available / getResources().getDisplayMetrics().density > Integer.MAX_VALUE
                ? Integer.MAX_VALUE : Math.round(available / getResources().getDisplayMetrics().density),
                Math.max(1, Math.round(minCellPx / getResources().getDisplayMetrics().density)), maxColumns);
        columns = mode == LayoutPolicy.Mode.HORIZONTAL
                ? Math.max(1, Math.min(proposed, Math.max(1, getChildCount()))) : proposed;
        int cell = LayoutPolicy.cellWidth(available, columns, gapPx);
        int[] rowHeights = new int[(getChildCount() + columns - 1) / columns];
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == GONE) continue;
            child.measure(MeasureSpec.makeMeasureSpec(cell, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
            int row = i / columns;
            rowHeights[row] = Math.max(rowHeights[row], child.getMeasuredHeight());
        }
        int height = getPaddingTop() + getPaddingBottom();
        for (int rowHeight : rowHeights) height += rowHeight;
        if (rowHeights.length > 1) height += (rowHeights.length - 1) * gapPx;
        setMeasuredDimension(resolveSize(width, widthMeasureSpec), resolveSize(height, heightMeasureSpec));
    }

    @Override protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int available = Math.max(0, getWidth() - getPaddingLeft() - getPaddingRight());
        int cell = LayoutPolicy.cellWidth(available, columns, gapPx);
        int y = getPaddingTop();
        int count = getChildCount();
        for (int start = 0; start < count; start += columns) {
            int rowHeight = 0;
            for (int i = start; i < Math.min(start + columns, count); i++) {
                View child = getChildAt(i);
                if (child.getVisibility() != GONE) rowHeight = Math.max(rowHeight, child.getMeasuredHeight());
            }
            for (int i = start; i < Math.min(start + columns, count); i++) {
                View child = getChildAt(i);
                if (child.getVisibility() == GONE) continue;
                int col = i - start;
                int x = getPaddingLeft() + col * (cell + gapPx);
                child.layout(x, y, x + cell, y + child.getMeasuredHeight());
            }
            y += rowHeight + gapPx;
        }
    }

    @Override protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    }

    @Override protected LayoutParams generateLayoutParams(LayoutParams params) {
        return new LayoutParams(params);
    }

    @Override public LayoutParams generateLayoutParams(android.util.AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override protected boolean checkLayoutParams(LayoutParams params) { return params != null; }
}
