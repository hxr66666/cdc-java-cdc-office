package cdc.office.tables;

import org.junit.jupiter.api.Test;

import cdc.office.tables.diff.Side;
import cdc.util.coverage.Coverage;

class SideTest {
    @Test
    void test() {
        Coverage.enumStandardCoverage(Side.class);
    }
}