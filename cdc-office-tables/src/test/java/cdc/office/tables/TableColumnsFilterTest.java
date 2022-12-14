package cdc.office.tables;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TableColumnsFilterTest {
    @Test
    void test() throws Exception {
        final MemoryTableHandler handler = new MemoryTableHandler();
        final TableColumnsFilter filter = new TableColumnsFilter(handler,
                                                                 c -> c < 2);

        filter.processBeginTable(null, -1);
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
        filter.processEndTable(null);

        assertTrue(handler.hasHeaders());
        assertEquals(8, handler.getRowsCount());
        for (int index = 0; index < handler.getRowsCount(); index++) {
            assertEquals(2, handler.getRow(index).size(), index + ":" + handler.getRow(index));
        }
    }
}