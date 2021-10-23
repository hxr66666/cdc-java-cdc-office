package cdc.office.tables;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import cdc.office.tables.diff.CellDiff;
import cdc.office.tables.diff.CellDiffKind;
import cdc.util.coverage.Coverage;

class CellDiffTest {

    private static CellDiff d(String left,
                              String right) {
        return new CellDiff(left, right);
    }

    private static void check(CellDiffKind expected,
                              String left,
                              String right) {
        final CellDiff diff = new CellDiff(left, right);
        assertEquals(expected, diff.getKind());
    }

    @Test
    void testMisc() {
        final CellDiff diff = new CellDiff("X", "Y");
        assertEquals("X", diff.getLeft());
        assertEquals("Y", diff.getRight());
        assertTrue(Coverage.objectBasicCoverage(diff));
        assertEquals(d(null, null), d(null, null));
        assertEquals(d("foo", null), d("foo", null));
        assertEquals(d(null, "foo"), d(null, "foo"));
        assertEquals(d("foo", "bar"), d("foo", "bar"));
        assertNotEquals(d(null, "foo"), d(null, "bar"));
        assertNotEquals(d("foo", null), d("bar", null));
        assertNotEquals(d("foo", "bar"), d("foo", null));
    }

    @Test
    void testSame() {
        check(CellDiffKind.NULL, null, null);
        check(CellDiffKind.SAME, "", "");
        check(CellDiffKind.SAME, "Hello", "Hello");
    }

    @Test
    void testAdded() {
        check(CellDiffKind.ADDED, null, "");
        check(CellDiffKind.ADDED, null, "added");
    }

    @Test
    void testRemoved() {
        check(CellDiffKind.REMOVED, "", null);
        check(CellDiffKind.REMOVED, "removed", null);
    }

    @Test
    void testChanged() {
        check(CellDiffKind.CHANGED, "", "Hello");
        check(CellDiffKind.CHANGED, "Hello", "");
    }
}