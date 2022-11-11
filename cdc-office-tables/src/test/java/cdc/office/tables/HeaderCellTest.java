package cdc.office.tables;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import cdc.office.tables.HeaderCell.PatternCell;

class HeaderCellTest {
    @Test
    void testNameCell() {
        assertThrows(IllegalArgumentException.class,
                     () -> HeaderCell.name(null));
        assertEquals(HeaderCell.name("hello"), HeaderCell.name("hello"));
        assertNotEquals(HeaderCell.name("hello"), HeaderCell.name("Hello"));
        assertNotEquals(HeaderCell.name("hello"), HeaderCell.pattern(".*"));
        assertEquals("hello", HeaderCell.name("hello").getName());
        assertTrue(HeaderCell.name("hello").matches("hello"));
        assertFalse(HeaderCell.name("hello").matches("Hello"));
    }

    @Test
    void testPatternCell() {
        assertThrows(IllegalArgumentException.class,
                     () -> HeaderCell.pattern(null));
        final PatternCell p1 = HeaderCell.pattern("ABC");
        assertTrue(p1.matches("ABC"));
        assertFalse(p1.matches("ABc"));

        final PatternCell p2 = HeaderCell.pattern("A\\(\\)");
        assertTrue(p2.matches("A()"));

        final PatternCell p3 = HeaderCell.pattern("A\\([0-9]*\\)");
        assertTrue(p3.matches("A()"));
        assertTrue(p3.matches("A(1)"));
        assertTrue(p3.matches("A(0)"));
        assertFalse(p3.matches("A(A)"));

        final PatternCell p4 = HeaderCell.pattern("AAA \\(.*\\)");
        assertTrue(p4.matches("AAA ()"));
        assertTrue(p4.matches("AAA (A)"));
    }
}