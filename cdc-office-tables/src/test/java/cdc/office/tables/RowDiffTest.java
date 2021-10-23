package cdc.office.tables;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import cdc.office.tables.diff.CellDiffKind;
import cdc.office.tables.diff.RowDiff;
import cdc.office.tables.diff.RowDiffKind;

class RowDiffTest {
    private static final Logger LOGGER = LogManager.getLogger(RowDiffTest.class);
    private static final CellDiffKind CADDED = CellDiffKind.ADDED;
    private static final CellDiffKind CNULL = CellDiffKind.NULL;
    private static final CellDiffKind CSAME = CellDiffKind.SAME;
    private static final CellDiffKind CCHANGED = CellDiffKind.CHANGED;
    private static final CellDiffKind CREMOVED = CellDiffKind.REMOVED;

    private static final RowDiffKind RADDED = RowDiffKind.ADDED;
    private static final RowDiffKind RSAME = RowDiffKind.SAME;
    private static final RowDiffKind RCHANGED = RowDiffKind.CHANGED;
    private static final RowDiffKind RREMOVED = RowDiffKind.REMOVED;
    private static final String A = "A";
    private static final String B = "B";
    private static final String NS = null;
    private static final String ES = "";
    private static final String FOO = "foo";
    private static final String BAR = "bar";

    private static Row r(String... values) {
        return Row.builder(values).build();
    }

    private static Header h(String... values) {
        return new Header(values);
    }

    private static void check(Row left,
                              Row right,
                              RowDiffKind rowDiffKind,
                              CellDiffKind... cellDiffKinds) {
        final RowDiff diff = new RowDiff(left, right);
        LOGGER.info("{} {}: {}", left, right, diff);
        assertSame(rowDiffKind != RowDiffKind.SAME, diff.containsDifferences());
        assertSame(cellDiffKinds.length, diff.getDiffs().size(), Arrays.toString(cellDiffKinds));
        for (int index = 0; index < cellDiffKinds.length; index++) {
            assertSame(cellDiffKinds[index], diff.getDiffs().get(index).getKind());
        }
        assertSame(rowDiffKind, diff.getKind(), left + " " + right);
    }

    private static void check(Header leftHeader,
                              Row leftRow,
                              Header rightHeader,
                              Row rightRow,
                              RowDiffKind rowDiffKind,
                              CellDiffKind... cellDiffKinds) {
        final RowDiff diff = new RowDiff(leftHeader, leftRow, rightHeader, rightRow);
        LOGGER.info("{} {} {} {} {}", leftHeader, leftRow, rightHeader, rightRow, diff);
        assertSame(cellDiffKinds.length, diff.getDiffs().size(), Arrays.toString(cellDiffKinds) + " " + diff.getDiffs());
        for (int index = 0; index < cellDiffKinds.length; index++) {
            assertSame(cellDiffKinds[index], diff.getDiffs().get(index).getKind());
        }
        assertSame(rowDiffKind, diff.getKind(), leftRow + " " + rightRow);
        assertSame(rowDiffKind != RowDiffKind.SAME, diff.containsDifferences());
    }

    @Test
    void testNoHeaders() {
        // O CellDiffs
        check(r(), r(), RSAME);

        // 1 CellDiffs
        check(r(NS), r(), RSAME, CNULL);
        check(r(FOO), r(FOO), RSAME, CSAME);
        check(r(FOO), r(BAR), RCHANGED, CCHANGED);
        check(r(FOO), r(), RREMOVED, CREMOVED);
        check(r(), r(FOO), RADDED, CADDED);

        // 2 CellDiffs
        check(r(NS, NS), r(), RSAME, CNULL, CNULL);
        check(r(NS, FOO), r(NS, FOO), RSAME, CNULL, CSAME);
        check(r(NS, FOO), r(NS, BAR), RCHANGED, CNULL, CCHANGED);
        check(r(NS, NS), r(NS, FOO), RADDED, CNULL, CADDED);
        check(r(NS, FOO), r(), RREMOVED, CNULL, CREMOVED);

        check(r(FOO, NS), r(FOO), RSAME, CSAME, CNULL);
        check(r(FOO, BAR), r(FOO, BAR), RSAME, CSAME, CSAME);
        check(r(FOO, FOO), r(FOO, BAR), RCHANGED, CSAME, CCHANGED);
        check(r(FOO, NS), r(FOO, BAR), RCHANGED, CSAME, CADDED);
        check(r(FOO, BAR), r(FOO), RCHANGED, CSAME, CREMOVED);

        check(r(FOO, NS), r(BAR), RCHANGED, CCHANGED, CNULL);
        check(r(FOO, BAR), r(BAR, BAR), RCHANGED, CCHANGED, CSAME);
        check(r(FOO, FOO), r(BAR, BAR), RCHANGED, CCHANGED, CCHANGED);
        check(r(FOO, NS), r(BAR, BAR), RCHANGED, CCHANGED, CADDED);
        check(r(FOO, BAR), r(BAR), RCHANGED, CCHANGED, CREMOVED);

        check(r(NS, NS), r(FOO), RADDED, CADDED, CNULL);
        check(r(NS, BAR), r(FOO, BAR), RCHANGED, CADDED, CSAME);
        check(r(NS, FOO), r(FOO, BAR), RCHANGED, CADDED, CCHANGED);
        check(r(NS, NS), r(BAR, BAR), RADDED, CADDED, CADDED);
        check(r(NS, BAR), r(BAR), RCHANGED, CADDED, CREMOVED);

        check(r(FOO, NS), r(), RREMOVED, CREMOVED, CNULL);
        check(r(FOO, BAR), r(NS, BAR), RCHANGED, CREMOVED, CSAME);
        check(r(FOO, FOO), r(NS, BAR), RCHANGED, CREMOVED, CCHANGED);
        check(r(FOO, NS), r(NS, BAR), RCHANGED, CREMOVED, CADDED);
        check(r(FOO, BAR), r(), RREMOVED, CREMOVED, CREMOVED);

        // Misc
        check(r(), r(), RSAME);
        check(r(), r(NS), RSAME, CNULL);
        check(r(NS), r(), RSAME, CNULL);
        check(r(NS), r(NS), RSAME, CNULL);

        check(r(NS, NS), r(), RSAME, CNULL, CNULL);
        check(r(), r(FOO), RADDED, CADDED);
        check(r(NS), r(FOO), RADDED, CADDED);
        check(r(ES), r(FOO), RCHANGED, CCHANGED);
        check(r(FOO), r(), RREMOVED, CREMOVED);
        check(r(FOO), r(NS), RREMOVED, CREMOVED);
        check(r(FOO), r(ES), RCHANGED, CCHANGED);

        check(r(FOO), r(FOO), RSAME, CSAME);
        check(r(FOO), r(BAR), RCHANGED, CCHANGED);

        check(r(FOO, NS), r(FOO), RSAME, CSAME, CNULL);
        check(r(FOO, NS), r(FOO, NS), RSAME, CSAME, CNULL);
        check(r(FOO), r(FOO, NS), RSAME, CSAME, CNULL);

        check(r(FOO, ES), r(FOO), RCHANGED, CSAME, CREMOVED);
        check(r(FOO, ES), r(FOO, NS), RCHANGED, CSAME, CREMOVED);
        check(r(FOO, ES), r(FOO, ES), RSAME, CSAME, CSAME);

        check(r(FOO), r(FOO, ES), RCHANGED, CSAME, CADDED);
        check(r(FOO, NS), r(FOO, ES), RCHANGED, CSAME, CADDED);

        check(r(NS, FOO), r(FOO), RCHANGED, CADDED, CREMOVED);
        check(r(ES, FOO), r(FOO), RCHANGED, CCHANGED, CREMOVED);
        check(r(NS, FOO), r(FOO, NS), RCHANGED, CADDED, CREMOVED);
        check(r(NS, FOO), r(FOO, ES), RCHANGED, CADDED, CCHANGED);

        check(r(FOO), r(NS, FOO), RCHANGED, CREMOVED, CADDED);
        check(r(FOO), r(ES, FOO), RCHANGED, CCHANGED, CADDED);

        check(r(FOO, BAR), r(FOO), RCHANGED, CSAME, CREMOVED);
        check(r(FOO), r(FOO, BAR), RCHANGED, CSAME, CADDED);
    }

    @Test
    void testSameHeaders() {
        // O CellDiffs
        check(h(), r(), h(), r(), RSAME);

        // 1 CellDiffs
        check(h(A), r(), h(A), r(), RSAME, CNULL);
        check(h(A), r(FOO), h(A), r(FOO), RSAME, CSAME);
        check(h(A), r(FOO), h(A), r(BAR), RCHANGED, CCHANGED);
        check(h(A), r(), h(A), r(FOO), RADDED, CADDED);
        check(h(A), r(FOO), h(A), r(), RREMOVED, CREMOVED);

        // 2 CellDiffs
        check(h(A, B), r(), h(A, B), r(), RSAME, CNULL, CNULL);
        check(h(A, B), r(NS, FOO), h(A, B), r(NS, FOO), RSAME, CNULL, CSAME);
        check(h(A, B), r(NS, FOO), h(A, B), r(NS, BAR), RCHANGED, CNULL, CCHANGED);
        check(h(A, B), r(), h(A, B), r(NS, FOO), RADDED, CNULL, CADDED);
        check(h(A, B), r(NS, FOO), h(A, B), r(), RREMOVED, CNULL, CREMOVED);

        check(h(A, B), r(FOO), h(A, B), r(FOO), RSAME, CSAME, CNULL);
        check(h(A, B), r(FOO, BAR), h(A, B), r(FOO, BAR), RSAME, CSAME, CSAME);
        check(h(A, B), r(FOO, FOO), h(A, B), r(FOO, BAR), RCHANGED, CSAME, CCHANGED);
        check(h(A, B), r(FOO), h(A, B), r(FOO, BAR), RCHANGED, CSAME, CADDED);
        check(h(A, B), r(FOO, BAR), h(A, B), r(FOO), RCHANGED, CSAME, CREMOVED);

        check(h(A, B), r(FOO), h(A, B), r(BAR), RCHANGED, CCHANGED, CNULL);
        check(h(A, B), r(FOO, BAR), h(A, B), r(BAR, BAR), RCHANGED, CCHANGED, CSAME);
        check(h(A, B), r(FOO, FOO), h(A, B), r(BAR, BAR), RCHANGED, CCHANGED, CCHANGED);
        check(h(A, B), r(FOO), h(A, B), r(BAR, BAR), RCHANGED, CCHANGED, CADDED);
        check(h(A, B), r(FOO, BAR), h(A, B), r(BAR), RCHANGED, CCHANGED, CREMOVED);

        check(h(A, B), r(), h(A, B), r(FOO), RADDED, CADDED, CNULL);
        check(h(A, B), r(NS, BAR), h(A, B), r(BAR, BAR), RCHANGED, CADDED, CSAME);
        check(h(A, B), r(NS, FOO), h(A, B), r(BAR, BAR), RCHANGED, CADDED, CCHANGED);
        check(h(A, B), r(), h(A, B), r(BAR, BAR), RADDED, CADDED, CADDED);
        check(h(A, B), r(NS, BAR), h(A, B), r(BAR), RCHANGED, CADDED, CREMOVED);

        check(h(A, B), r(FOO), h(A, B), r(), RREMOVED, CREMOVED, CNULL);
        check(h(A, B), r(FOO, BAR), h(A, B), r(NS, BAR), RCHANGED, CREMOVED, CSAME);
        check(h(A, B), r(FOO, FOO), h(A, B), r(NS, BAR), RCHANGED, CREMOVED, CCHANGED);
        check(h(A, B), r(FOO), h(A, B), r(NS, BAR), RCHANGED, CREMOVED, CADDED);
        check(h(A, B), r(FOO, BAR), h(A, B), r(), RREMOVED, CREMOVED, CREMOVED);

        // 0 header
        check(h(), r(), h(), r(), RSAME);

        check(h(A), r(), h(A), r(), RSAME, CNULL);
        check(h(A), r(FOO), h(A), r(FOO), RSAME, CSAME);
        check(h(A), r(), h(A), r(FOO), RADDED, CADDED);
        check(h(A), r(FOO), h(A), r(), RREMOVED, CREMOVED);
        check(h(A), r(FOO), h(A), r(BAR), RCHANGED, CCHANGED);

        // 1 header
        check(h(A), r(), h(A), r(), RSAME, CNULL);
        check(h(A), r(), h(A), r(NS), RSAME, CNULL);
        check(h(A), r(), h(A), r(ES), RADDED, CADDED);
        check(h(A), r(), h(A), r(FOO), RADDED, CADDED);

        check(h(A), r(NS), h(A), r(), RSAME, CNULL);
        check(h(A), r(NS), h(A), r(NS), RSAME, CNULL);
        check(h(A), r(NS), h(A), r(ES), RADDED, CADDED);
        check(h(A), r(NS), h(A), r(FOO), RADDED, CADDED);

        check(h(A), r(ES), h(A), r(), RREMOVED, CREMOVED);
        check(h(A), r(ES), h(A), r(NS), RREMOVED, CREMOVED);
        check(h(A), r(ES), h(A), r(ES), RSAME, CSAME);
        check(h(A), r(ES), h(A), r(FOO), RCHANGED, CCHANGED);

        check(h(A), r(FOO), h(A), r(), RREMOVED, CREMOVED);
        check(h(A), r(FOO), h(A), r(NS), RREMOVED, CREMOVED);
        check(h(A), r(FOO), h(A), r(ES), RCHANGED, CCHANGED);
        check(h(A), r(FOO), h(A), r(FOO), RSAME, CSAME);

        // 2 headers
        check(h(A, B), r(), h(A, B), r(), RSAME, CNULL, CNULL);
        check(h(A, B), r(), h(A, B), r(NS), RSAME, CNULL, CNULL);
        check(h(A, B), r(), h(A, B), r(NS, NS), RSAME, CNULL, CNULL);
        check(h(A, B), r(), h(A, B), r(ES), RADDED, CADDED, CNULL);
        check(h(A, B), r(), h(A, B), r(ES, NS), RADDED, CADDED, CNULL);
        check(h(A, B), r(), h(A, B), r(ES, ES), RADDED, CADDED, CADDED);
        check(h(A, B), r(), h(A, B), r(NS, ES), RADDED, CNULL, CADDED);
        check(h(A, B), r(), h(A, B), r(FOO), RADDED, CADDED, CNULL);
        check(h(A, B), r(), h(A, B), r(FOO, NS), RADDED, CADDED, CNULL);
        check(h(A, B), r(), h(A, B), r(FOO, BAR), RADDED, CADDED, CADDED);
        check(h(A, B), r(), h(A, B), r(NS, BAR), RADDED, CNULL, CADDED);

        check(h(A, B), r(NS), h(A, B), r(), RSAME, CNULL, CNULL);
        check(h(A, B), r(NS), h(A, B), r(NS), RSAME, CNULL, CNULL);
        check(h(A, B), r(NS), h(A, B), r(NS, NS), RSAME, CNULL, CNULL);
        check(h(A, B), r(NS), h(A, B), r(ES), RADDED, CADDED, CNULL);
        check(h(A, B), r(NS), h(A, B), r(ES, NS), RADDED, CADDED, CNULL);
        check(h(A, B), r(NS), h(A, B), r(ES, ES), RADDED, CADDED, CADDED);
        check(h(A, B), r(NS), h(A, B), r(NS, ES), RADDED, CNULL, CADDED);
        check(h(A, B), r(NS), h(A, B), r(FOO), RADDED, CADDED, CNULL);
        check(h(A, B), r(NS), h(A, B), r(FOO, NS), RADDED, CADDED, CNULL);
        check(h(A, B), r(NS), h(A, B), r(FOO, BAR), RADDED, CADDED, CADDED);
        check(h(A, B), r(NS), h(A, B), r(NS, BAR), RADDED, CNULL, CADDED);

        check(h(A, B), r(NS, NS), h(A, B), r(), RSAME, CNULL, CNULL);
        check(h(A, B), r(NS, NS), h(A, B), r(NS), RSAME, CNULL, CNULL);
        check(h(A, B), r(NS, NS), h(A, B), r(NS, NS), RSAME, CNULL, CNULL);
        check(h(A, B), r(NS, NS), h(A, B), r(ES), RADDED, CADDED, CNULL);
        check(h(A, B), r(NS, NS), h(A, B), r(ES, NS), RADDED, CADDED, CNULL);
        check(h(A, B), r(NS, NS), h(A, B), r(ES, ES), RADDED, CADDED, CADDED);
        check(h(A, B), r(NS, NS), h(A, B), r(NS, ES), RADDED, CNULL, CADDED);
        check(h(A, B), r(NS, NS), h(A, B), r(FOO), RADDED, CADDED, CNULL);
        check(h(A, B), r(NS, NS), h(A, B), r(FOO, NS), RADDED, CADDED, CNULL);
        check(h(A, B), r(NS, NS), h(A, B), r(FOO, BAR), RADDED, CADDED, CADDED);
        check(h(A, B), r(NS, NS), h(A, B), r(NS, BAR), RADDED, CNULL, CADDED);

        check(h(A, B), r(ES), h(A, B), r(), RREMOVED, CREMOVED, CNULL);
        check(h(A, B), r(ES), h(A, B), r(NS), RREMOVED, CREMOVED, CNULL);
        check(h(A, B), r(ES), h(A, B), r(NS, NS), RREMOVED, CREMOVED, CNULL);
        check(h(A, B), r(ES), h(A, B), r(ES), RSAME, CSAME, CNULL);
        check(h(A, B), r(ES), h(A, B), r(ES, NS), RSAME, CSAME, CNULL);
        check(h(A, B), r(ES), h(A, B), r(ES, ES), RCHANGED, CSAME, CADDED);
        check(h(A, B), r(ES), h(A, B), r(NS, ES), RCHANGED, CREMOVED, CADDED);
        check(h(A, B), r(ES), h(A, B), r(FOO), RCHANGED, CCHANGED, CNULL);
        check(h(A, B), r(ES), h(A, B), r(FOO, NS), RCHANGED, CCHANGED, CNULL);
        check(h(A, B), r(ES), h(A, B), r(FOO, BAR), RCHANGED, CCHANGED, CADDED);
        check(h(A, B), r(ES), h(A, B), r(NS, BAR), RCHANGED, CREMOVED, CADDED);

        check(h(A, B), r(FOO), h(A, B), r(), RREMOVED, CREMOVED, CNULL);
        check(h(A, B), r(FOO), h(A, B), r(NS), RREMOVED, CREMOVED, CNULL);
        check(h(A, B), r(FOO), h(A, B), r(NS, NS), RREMOVED, CREMOVED, CNULL);
        check(h(A, B), r(FOO), h(A, B), r(ES), RCHANGED, CCHANGED, CNULL);
        check(h(A, B), r(FOO), h(A, B), r(ES, NS), RCHANGED, CCHANGED, CNULL);
        check(h(A, B), r(FOO), h(A, B), r(ES, ES), RCHANGED, CCHANGED, CADDED);
        check(h(A, B), r(FOO), h(A, B), r(NS, ES), RCHANGED, CREMOVED, CADDED);
        check(h(A, B), r(FOO), h(A, B), r(FOO), RSAME, CSAME, CNULL);
        check(h(A, B), r(FOO), h(A, B), r(FOO, NS), RSAME, CSAME, CNULL);
        check(h(A, B), r(FOO), h(A, B), r(FOO, BAR), RCHANGED, CSAME, CADDED);
        check(h(A, B), r(FOO), h(A, B), r(NS, BAR), RCHANGED, CREMOVED, CADDED);

        check(h(A, B), r(NS, FOO), h(A, B), r(), RREMOVED, CNULL, CREMOVED);
        check(h(A, B), r(NS, FOO), h(A, B), r(NS), RREMOVED, CNULL, CREMOVED);
        check(h(A, B), r(NS, FOO), h(A, B), r(NS, NS), RREMOVED, CNULL, CREMOVED);
        check(h(A, B), r(NS, FOO), h(A, B), r(ES), RCHANGED, CADDED, CREMOVED);
        check(h(A, B), r(NS, FOO), h(A, B), r(ES, NS), RCHANGED, CADDED, CREMOVED);
        check(h(A, B), r(NS, FOO), h(A, B), r(ES, ES), RCHANGED, CADDED, CCHANGED);
        check(h(A, B), r(NS, FOO), h(A, B), r(NS, ES), RCHANGED, CNULL, CCHANGED);
        check(h(A, B), r(NS, FOO), h(A, B), r(FOO), RCHANGED, CADDED, CREMOVED);
        check(h(A, B), r(NS, FOO), h(A, B), r(FOO, NS), RCHANGED, CADDED, CREMOVED);
        check(h(A, B), r(NS, FOO), h(A, B), r(FOO, BAR), RCHANGED, CADDED, CCHANGED);
        check(h(A, B), r(NS, FOO), h(A, B), r(NS, BAR), RCHANGED, CNULL, CCHANGED);

        assertThrows(IllegalArgumentException.class, () -> {
            check(h(), r("a"), h(), r(), RSAME);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            check(h(), r(), h(), r("b"), RSAME);
        });
    }

    @Test
    void testSameHeadersScrambled() {
        // 2 CellDiffs
        check(h(B, A), r(), h(A, B), r(), RSAME, CNULL, CNULL);
        check(h(B, A), r(FOO, NS), h(A, B), r(NS, FOO), RSAME, CNULL, CSAME);
        check(h(B, A), r(FOO, NS), h(A, B), r(NS, BAR), RCHANGED, CNULL, CCHANGED);
        check(h(B, A), r(), h(A, B), r(NS, FOO), RADDED, CNULL, CADDED);
        check(h(B, A), r(FOO), h(A, B), r(), RREMOVED, CNULL, CREMOVED);

        check(h(B, A), r(NS, FOO), h(A, B), r(FOO), RSAME, CSAME, CNULL);
        check(h(B, A), r(BAR, FOO), h(A, B), r(FOO, BAR), RSAME, CSAME, CSAME);
        check(h(B, A), r(FOO, FOO), h(A, B), r(FOO, BAR), RCHANGED, CSAME, CCHANGED);
        check(h(B, A), r(NS, FOO), h(A, B), r(FOO, BAR), RCHANGED, CSAME, CADDED);
        check(h(B, A), r(BAR, FOO), h(A, B), r(FOO), RCHANGED, CSAME, CREMOVED);

        check(h(B, A), r(NS, FOO), h(A, B), r(BAR), RCHANGED, CCHANGED, CNULL);
        check(h(B, A), r(BAR, FOO), h(A, B), r(BAR, BAR), RCHANGED, CCHANGED, CSAME);
        check(h(B, A), r(FOO, FOO), h(A, B), r(BAR, BAR), RCHANGED, CCHANGED, CCHANGED);
        check(h(B, A), r(NS, FOO), h(A, B), r(BAR, BAR), RCHANGED, CCHANGED, CADDED);
        check(h(B, A), r(BAR, FOO), h(A, B), r(BAR), RCHANGED, CCHANGED, CREMOVED);

        check(h(B, A), r(), h(A, B), r(FOO), RADDED, CADDED, CNULL);
        check(h(B, A), r(BAR), h(A, B), r(BAR, BAR), RCHANGED, CADDED, CSAME);
        check(h(B, A), r(FOO), h(A, B), r(BAR, BAR), RCHANGED, CADDED, CCHANGED);
        check(h(B, A), r(), h(A, B), r(BAR, BAR), RADDED, CADDED, CADDED);
        check(h(B, A), r(BAR), h(A, B), r(BAR), RCHANGED, CADDED, CREMOVED);

        check(h(B, A), r(NS, FOO), h(A, B), r(), RREMOVED, CREMOVED, CNULL);
        check(h(B, A), r(BAR, FOO), h(A, B), r(NS, BAR), RCHANGED, CREMOVED, CSAME);
        check(h(B, A), r(FOO, FOO), h(A, B), r(NS, BAR), RCHANGED, CREMOVED, CCHANGED);
        check(h(B, A), r(NS, FOO), h(A, B), r(NS, BAR), RCHANGED, CREMOVED, CADDED);
        check(h(B, A), r(BAR, FOO), h(A, B), r(), RREMOVED, CREMOVED, CREMOVED);
    }

    @Test
    void testDifferentHeaders() {
        check(h(A), r(), h(B), r(), RSAME, CNULL, CNULL);
        check(h(A), r(FOO), h(B), r(), RREMOVED, CNULL, CREMOVED);
        check(h(A), r(), h(B), r(BAR), RADDED, CADDED, CNULL);
        check(h(A), r(FOO), h(B), r(BAR), RCHANGED, CADDED, CREMOVED);
    }
}