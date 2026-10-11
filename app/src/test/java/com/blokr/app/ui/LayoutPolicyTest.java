package com.blokr.app.ui;

import org.junit.Test;
import static org.junit.Assert.*;

public class LayoutPolicyTest {
    @Test public void adaptiveLayoutFitsPhonesAndTablets() {
        assertEquals(1, LayoutPolicy.columns(LayoutPolicy.Mode.ADAPTIVE, 320, 180, 3));
        assertEquals(2, LayoutPolicy.columns(LayoutPolicy.Mode.ADAPTIVE, 420, 180, 3));
        assertEquals(3, LayoutPolicy.columns(LayoutPolicy.Mode.ADAPTIVE, 900, 180, 3));
    }

    @Test public void explicitModesArePredictable() {
        assertEquals(1, LayoutPolicy.columns(LayoutPolicy.Mode.VERTICAL, 1000, 180, 4));
        assertEquals(4, LayoutPolicy.columns(LayoutPolicy.Mode.HORIZONTAL, 320, 180, 4));
        assertEquals(2, LayoutPolicy.columns(LayoutPolicy.Mode.GRID, 450, 180, 4));
    }

    @Test public void spacingNeverCreatesNegativeCells() {
        assertEquals(90, LayoutPolicy.cellWidth(200, 2, 20));
        assertEquals(0, LayoutPolicy.cellWidth(10, 3, 20));
    }

    @Test public void invalidInputsFailFast() {
        try {
            LayoutPolicy.columns(LayoutPolicy.Mode.ADAPTIVE, 320, 0, 3);
            fail("Expected invalid cell size");
        } catch (IllegalArgumentException expected) {}
        try {
            LayoutPolicy.cellWidth(200, 0, 12);
            fail("Expected invalid column count");
        } catch (IllegalArgumentException expected) {}
    }
}
