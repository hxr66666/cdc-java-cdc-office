package cdc.office.tables;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TableRowsFilterTest {
    @Test
    void testWithHeader() throws Exception {
        final MemoryTableHandler handler = new MemoryTableHandler();
        final TableRowsFilter filter = new TableRowsFilter(handler,
                                                           (r,
                                                            l) -> l.isHeader() || l.getSectionNumber() % 2 == 0);

        filter.processBegin(null, -1);
        assertFalse(handler.hasHeaders());

        final RowLocation.Builder location = RowLocation.builder();

        final Row h1 = Row.builder("A", "B", "C").build();
        final Row d1 = Row.builder("1a", "1b", "1c").build();
        final Row d2 = Row.builder("2a", "2b", "2c").build();
        final Row d3 = Row.builder("3a", "3b", "3c").build();
        final Row d4 = Row.builder("4a", "4b", "4c").build();
        final Row d5 = Row.builder("5a", "5b", "5c").build();
        final Row d6 = Row.builder("6a", "6b", "6c").build();
        final Row d7 = Row.builder("7a", "7b", "7c").build();

        filter.processHeader(h1, location.incrementNumbers(TableSection.HEADER).build());
        filter.processData(d1, location.incrementNumbers(TableSection.DATA).build());
        filter.processData(d2, location.incrementNumbers(TableSection.DATA).build());
        filter.processData(d3, location.incrementNumbers(TableSection.DATA).build());
        filter.processData(d4, location.incrementNumbers(TableSection.DATA).build());
        filter.processData(d5, location.incrementNumbers(TableSection.DATA).build());
        filter.processData(d6, location.incrementNumbers(TableSection.DATA).build());
        filter.processData(d7, location.incrementNumbers(TableSection.DATA).build());
        filter.processEnd();

        assertTrue(handler.hasHeaders());
        assertEquals(4, handler.getRowsCount());
        assertEquals(h1, handler.getRow(0));
        assertEquals(d2, handler.getRow(1));
        assertEquals(d4, handler.getRow(2));
        assertEquals(d6, handler.getRow(3));
    }

    @Test
    void testWithoutHeader() throws Exception {
        final MemoryTableHandler handler = new MemoryTableHandler();
        final TableRowsFilter filter = new TableRowsFilter(handler,
                                                           (r,
                                                            l) -> l.getSectionNumber() % 2 == 0);

        filter.processBegin(null, -1);
        assertFalse(handler.hasHeaders());

        final RowLocation.Builder location = RowLocation.builder();

        final Row d1 = Row.builder("1a", "1b", "1c").build();
        final Row d2 = Row.builder("2a", "2b", "2c").build();
        final Row d3 = Row.builder("3a", "3b", "3c").build();
        final Row d4 = Row.builder("4a", "4b", "4c").build();
        final Row d5 = Row.builder("5a", "5b", "5c").build();
        final Row d6 = Row.builder("6a", "6b", "6c").build();
        final Row d7 = Row.builder("7a", "7b", "7c").build();

        filter.processData(d1, location.incrementNumbers(TableSection.DATA).build());
        filter.processData(d2, location.incrementNumbers(TableSection.DATA).build());
        filter.processData(d3, location.incrementNumbers(TableSection.DATA).build());
        filter.processData(d4, location.incrementNumbers(TableSection.DATA).build());
        filter.processData(d5, location.incrementNumbers(TableSection.DATA).build());
        filter.processData(d6, location.incrementNumbers(TableSection.DATA).build());
        filter.processData(d7, location.incrementNumbers(TableSection.DATA).build());
        filter.processEnd();

        assertFalse(handler.hasHeaders());
        assertEquals(3, handler.getRowsCount());
        assertEquals(d2, handler.getRow(0));
        assertEquals(d4, handler.getRow(1));
        assertEquals(d6, handler.getRow(2));
    }
}