package cdc.office.tables;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class HeaderTest {
    @Test
    void testEmpty() {
        final Header h = Header.builder().build();
        assertTrue(h.isValid());
        assertSame(0, h.size());
        assertEquals(Header.EMPTY, h);
    }

    @Test
    void test1Name() {
        final Header h = Header.builder().names("K1").build();
        assertTrue(h.isValid());
        assertSame(1, h.size());
        assertTrue(h.contains("K1"));
        assertFalse(h.contains("K"));
    }

    @Test
    void test2Names() {
        final Header h = Header.builder().names("K1", "K2").build();
        assertTrue(h.isValid());
        assertSame(2, h.size());
        assertTrue(h.contains("K1"));
        assertTrue(h.contains("K2"));
        assertFalse(h.contains("K"));
        assertSame(0, h.getIndex("K1"));
        assertSame(1, h.getIndex("K2"));
        assertSame(-1, h.getIndex("K"));
    }

    @Test
    void testInvalid() {
        final Header h = Header.builder().names("K1", "K1").build();
        assertFalse(h.isValid());
    }

    @Test
    void testContains() {
        final Header h12 = Header.builder().names("K1", "K2").build();
        final Header h123 = Header.builder().names("K1", "K2", "K3").build();
        assertTrue(h123.contains(h12));
        assertFalse(h12.contains(h123));
        assertTrue(Header.EMPTY.contains(Header.EMPTY));
        assertTrue(h12.contains(Header.EMPTY));
    }

    @Test
    void testIntersects() {
        final Header h12 = Header.builder().names("K1", "K2").build();
        final Header h123 = Header.builder().names("K1", "K2", "K3").build();
        assertTrue(h123.intersects(h12));
        assertFalse(h12.intersects(Header.EMPTY));
        assertFalse(Header.EMPTY.intersects(Header.EMPTY));
    }
}