package com.blokr.app.ui;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

/** Responsive, wrapping grid that reflows visible children on width changes. */
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

    private int visibleCount() {
        int count = 0;
        for (int i = 0; i < getChildCount(); i++)
            if (getChildAt(i).getVisibility() != GONE) count++;
        return count;
    }

    private int availableWidth(int width) {
        return Math.max(0, width - getPaddingLeft() - getPaddingRight());
    }

    @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.UNSPECIFIED)
            width = getPaddingLeft() + getPaddingRight() + minCellPx;
        int available = availableWidth(width);
        int count = visibleCount();
        int proposed = LayoutPolicy.columnsForPixels(mode, available, minCellPx, gapPx, maxColumns);
        columns = Math.max(1, Math.min(proposed, Math.max(1, count)));
        int cell = LayoutPolicy.cellWidth(available, columns, gapPx);
        int rows = (count + columns - 1) / columns;
        int[] heights = new int[rows];
        int visibleIndex = 0;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == GONE) continue;
            ViewGroup.LayoutParams params = child.getLayoutParams();
            int childHeight = params != null && params.height >= 0
                    ? MeasureSpec.makeMeasureSpec(params.height, MeasureSpec.EXACTLY)
                    : MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
            child.measure(MeasureSpec.makeMeasureSpec(cell, MeasureSpec.EXACTLY), childHeight);
            int row = visibleIndex++ / columns;
            heights[row] = Math.max(heights[row], child.getMeasuredHeight());
        }
        long height = (long) getPaddingTop() + getPaddingBottom();
        for (int rowHeight : heights) height += rowHeight;
        if (rows > 1) height += (long) (rows - 1) * gapPx;
        setMeasuredDimension(resolveSize(width, widthMeasureSpec),
                resolveSize((int) Math.min(Integer.MAX_VALUE, height), heightMeasureSpec));
    }

    @Override protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int cell = LayoutPolicy.cellWidth(availableWidth(getWidth()), columns, gapPx);
        int y = getPaddingTop();
        int visibleIndex = 0;
        int rowHeight = 0;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == GONE) continue;
            int col = visibleIndex % columns;
            if (col == 0 && visibleIndex > 0) {
                y += rowHeight + gapPx;
                rowHeight = 0;
            }
            int x = getPaddingLeft() + col * (cell + gapPx);
            child.layout(x, y, x + cell, y + child.getMeasuredHeight());
            rowHeight = Math.max(rowHeight, child.getMeasuredHeight());
            visibleIndex++;
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
