package cdc.office.tables;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class RowTest {
    @Test
    void testEmpty() {
        final Row row = Row.builder().build();
        final Row other = row;
        assertTrue(row.isEmpty());
        assertSame(0, row.size());
        assertSame(null, row.getValue(0));
        assertEquals("[]", row.toString());
        assertEquals(Row.EMPTY, row);
        assertEquals(other, row);
        assertSame(Row.EMPTY.hashCode(), row.hashCode());
        assertNotEquals(row, "foo");
    }

    @Test
    void testOne() {
        final Row row = Row.builder().addValue("foo").build();
        assertFalse(row.isEmpty());
        assertSame(1, row.size());
        assertEquals("foo", row.getValue(0));
        assertSame(null, row.getValue(1));
        assertSame(null, row.getValue(-1));
        assertEquals("['foo']", row.toString());
    }

    @Test
    void testOneNull() {
        final Row row = Row.builder().addValue(null).build();
        assertFalse(row.isEmpty());
        assertSame(1, row.size());
        assertSame(null, row.getValue(0));
        assertSame(null, row.getValue(1));
        assertEquals("[null]", row.toString());
    }
}