package cdc.office.tables;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import cdc.office.tables.diff.CellDiff;
import cdc.office.tables.diff.DiffKind;

public class CellDiffTest {

    private static void check(DiffKind expected,
                              String left,
                              String right) {
        final CellDiff diff = new CellDiff(left, right);
        assertEquals(expected, diff.getKind());
    }

    @Test
    public void testSame() {
        check(DiffKind.SAME, null, null);
        check(DiffKind.SAME, "", "");
        check(DiffKind.SAME, "Hello", "Hello");
    }

    @Test
    public void testAdded() {
        check(DiffKind.ADDED, null, "");
        check(DiffKind.ADDED, null, "added");
    }

    @Test
    public void testRemoved() {
        check(DiffKind.REMOVED, "", null);
        check(DiffKind.REMOVED, "removed", null);
    }

    @Test
    public void testChanged() {
        check(DiffKind.CHANGED, "", "Hello");
        check(DiffKind.CHANGED, "Hello", "");
    }
}