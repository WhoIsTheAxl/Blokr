package com.blokr.app.ui;

/** Pure-Java responsive sizing policy; values are logical density-independent pixels. */
public final class LayoutPolicy {
    public enum Mode { VERTICAL, HORIZONTAL, GRID, ADAPTIVE }
    private LayoutPolicy() {}

    public static int columns(Mode mode, int availableDp, int minCellDp, int maxColumns) {
        if (mode == null) throw new IllegalArgumentException("mode == null");
        if (availableDp < 0 || minCellDp <= 0 || maxColumns <= 0)
            throw new IllegalArgumentException("Invalid layout dimensions");
        if (mode == Mode.VERTICAL) return 1;
        if (mode == Mode.HORIZONTAL) return maxColumns;
        int fitted = Math.max(1, availableDp / minCellDp);
        return Math.min(maxColumns, fitted);
    }

    public static int cellWidth(int availablePx, int columns, int gapPx) {
        if (availablePx < 0 || columns <= 0 || gapPx < 0)
            throw new IllegalArgumentException("Invalid grid geometry");
        long gaps = (long) (columns - 1) * gapPx;
        return (int) Math.max(0L, ((long) availablePx - gaps) / columns);
    }
}
