package cdc.office.tables;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import cdc.office.tables.Header;
import cdc.office.tables.Row;
import cdc.office.tables.diff.RowDiff;

class RowDiffTest {
    private static final Logger LOGGER = LogManager.getLogger(RowDiffTest.class);

    private static Row toRow(String... values) {
        return Row.builder(values).build();
    }

    private static Header toHeader(String... values) {
        return new Header(values);
    }

    private static void check(Row left,
                              Row right) {
        final RowDiff diff = new RowDiff(left, right);
        LOGGER.info("{} {}:{}", left, right, diff);
    }

    private static void check(Header leftHeader,
                              Row leftRow,
                              Header rightHeader,
                              Row rightRow) {
        final RowDiff diff = new RowDiff(leftHeader, leftRow, rightHeader, rightRow);
        LOGGER.info("{} {} {} {} {}", leftHeader, leftRow, rightHeader, rightRow, diff);
    }

    // @Test
    public void testWithColumns() {
        check(toRow(), toRow());
        check(toRow("Hello"), toRow("Hello"));
        check(toRow("Hello"), toRow("world"));
        check(toRow("Hello", "World"), toRow("Hello"));
        check(toRow("Hello"), toRow("Hello", "World"));
    }

    @Test
    void testWithHeaders() {
        check(toHeader(), toRow(), toHeader(), toRow());

        check(toHeader("A"), toRow("a"), toHeader("A"), toRow("a"));
        check(toHeader("A"), toRow("a"), toHeader("A"), toRow(""));
        check(toHeader("A"), toRow("a"), toHeader("A"), toRow("b"));
        check(toHeader("A"), toRow(""), toHeader("A"), toRow("a"));

        check(toHeader("A"), toRow("a"), toHeader("A"), toRow());

        check(toHeader("A"), toRow(), toHeader("A"), toRow("a"));

        check(toHeader("A", "B"), toRow("a", "b"), toHeader("B", "A"), toRow("b", "a"));
        check(toHeader("A", "B"), toRow("a", "b"), toHeader("B", "A"), toRow("b", ""));
        check(toHeader("A", "B"), toRow("a", "b"), toHeader("B", "A"), toRow("", "a"));
        check(toHeader("A", "B"), toRow("a", "b"), toHeader("B", "A"), toRow("b"));
        check(toHeader("A", "B"), toRow("a", "b"), toHeader("B", "A"), toRow(null, "a"));
        check(toHeader("A", "B"), toRow("a", "b"), toHeader("B", "A"), toRow(null, null));
        check(toHeader("A", "B"), toRow("a", "b"), toHeader("B", "A"), toRow((String) null));
        check(toHeader("A", "B"), toRow("a", "b"), toHeader("B", "A"), toRow());
        check(toHeader("A", "B"), toRow("a", "b"), toHeader("B", "A"), toRow("", ""));
    }
}