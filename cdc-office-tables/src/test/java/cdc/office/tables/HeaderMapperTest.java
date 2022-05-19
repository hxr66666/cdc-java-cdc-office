package cdc.office.tables;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class HeaderMapperTest {
    @Test
    void test() {
        final Header m = Header.builder().names("M1", "M2").build();
        final Header o = Header.builder().names("O1", "O2").build();
        final Header a = Header.builder().names("M1", "M2", "O1", "O2", "A1").build();

        final HeaderMapper mapper = HeaderMapper.builder()
                                                .mandatory(m)
                                                .optional(o)
                                                .actual(a)
                                                .build();

        assertEquals(m, mapper.getMandatoryHeader());
        assertEquals(o, mapper.getOptionalHeader());
        assertEquals(a, mapper.getActualHeader());

        assertEquals(m.getNamesSet(), mapper.getExpectedMandatoryNames());
        assertEquals(o.getNamesSet(), mapper.getExpectedOptionalNames());
        assertEquals(m.getNamesSet(), mapper.getActualMandatoryNames());
        assertEquals(o.getNamesSet(), mapper.getActualOptionalNames());

        assertFalse(mapper.hasMissingMandatoryNames());
        assertFalse(mapper.hasMissingOptionalNames());
        assertTrue(mapper.hasAdditionalNames());
        assertSame(0, mapper.getMissingMandatoryNames().size());
        assertSame(0, mapper.getMissingOptionalNames().size());
        assertSame(1, mapper.getAdditionalNames().size());
    }

    @Test
    void testExceptions() {
        final Header e = Header.builder().names("M1", "M2").build();
        final Header a = Header.builder().names("M1", "M2", "O1", "O2", "A1").build();

        assertThrows(IllegalArgumentException.class,
                     () -> {
                         HeaderMapper.builder()
                                     .build();
                     });
        assertThrows(IllegalArgumentException.class,
                     () -> {
                         HeaderMapper.builder()
                                     .mandatory(e)
                                     .optional(e)
                                     .actual(a)
                                     .build();
                     });
    }
}