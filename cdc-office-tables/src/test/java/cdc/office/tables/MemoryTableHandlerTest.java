package cdc.office.tables;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class MemoryTableHandlerTest {

    @Test
    void testEmptyTrailing() {
        final String name = "Table";
        final MemoryTableHandler handler = new MemoryTableHandler(false);

        handler.processBeginTable(name, -1);
        handler.processHeader(Row.builder().addValues("A", "B", "C").build(),
                              RowLocation.builder().set(TableSection.HEADER, 1, 1).build());
        handler.processData(Row.builder().addValues("x", "x", "x").build(),
                            RowLocation.builder().set(TableSection.DATA, 1, 2).build());
        handler.processData(Row.builder().addValues("x", "x", "x").build(),
                            RowLocation.builder().set(TableSection.DATA, 2, 3).build());
        handler.processData(Row.EMPTY,
                            RowLocation.builder().set(TableSection.DATA, 3, 4).build());
        handler.processData(Row.builder().addValues("x", "x", "x").build(),
                            RowLocation.builder().set(TableSection.DATA, 4, 5).build());
        handler.processData(Row.EMPTY,
                            RowLocation.builder().set(TableSection.DATA, 5, 6).build());
        handler.processData(Row.EMPTY,
                            RowLocation.builder().set(TableSection.DATA, 6, 7).build());
        handler.processEndTable(name);

        assertEquals(7, handler.getRows().size());
        assertEquals(1, handler.getHeaderRowsCount());
        assertEquals(6, handler.getDataRowsCount());
        assertEquals(2, handler.getEmptyTrailingRowsCount());

        handler.removeEmptyTrailingtRows();
        assertEquals(5, handler.getRows().size());
        assertEquals(1, handler.getHeaderRowsCount());
        assertEquals(4, handler.getDataRowsCount());
        assertEquals(0, handler.getEmptyTrailingRowsCount());
    }
}
