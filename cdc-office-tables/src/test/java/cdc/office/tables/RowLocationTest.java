package cdc.office.tables;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

class RowLocationTest {
    private static final Logger LOGGER = LogManager.getLogger(RowLocationTest.class);

    private static void testIncrementHeaders(int headers) {
        LOGGER.info("============================");
        LOGGER.info("testIncrementHeaders({})", headers);
        final RowLocation.Builder r = RowLocation.builder();
        assertEquals(RowLocation.builder().set(TableSection.HEADER, 0, 0).build(), r.build());

        for (int i = 0; i < headers + 5; i++) {
            r.incrementNumbers(headers);
            LOGGER.info(r.build());
            if (i < headers) {
                assertEquals(RowLocation.builder().set(TableSection.HEADER, i + 1, i + 1).build(), r.build());
            } else {
                assertEquals(RowLocation.builder().set(TableSection.DATA, i - headers + 1, i + 1).build(), r.build());
            }
        }
    }

    @Test
    void testIncrementHeaders() {
        testIncrementHeaders(0);
        testIncrementHeaders(1);
        testIncrementHeaders(2);
        testIncrementHeaders(3);
        testIncrementHeaders(4);
    }
}