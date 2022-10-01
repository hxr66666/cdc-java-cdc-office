package cdc.office.ss.excel;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import cdc.office.ss.SheetParser;
import cdc.office.ss.SheetParserFactory;
import cdc.office.tables.VoidTablesHandler;

class ZipBombTest {
    private static final File FILE = new File("src/test/resources/ZipBomb.xlsx");

    @Test
    void testZipBombDetectionEnabledPoiStandard() throws IOException {
        final SheetParserFactory factory = new SheetParserFactory();
        factory.setEnabled(SheetParserFactory.Feature.POI_STANDARD, true);
        final SheetParser parser = factory.create(FILE);
        assertThrows(Exception.class,
                     () -> {
                         parser.parse(FILE, null, 0, VoidTablesHandler.INSTANCE);
                     });
    }

    @Test
    void testZipBombDetectionEnabledPoiStream() throws IOException {
        final SheetParserFactory factory = new SheetParserFactory();
        factory.setEnabled(SheetParserFactory.Feature.POI_STREAMING, true);
        final SheetParser parser = factory.create(FILE);
        assertThrows(Exception.class,
                     () -> {
                         parser.parse(FILE, null, 0, VoidTablesHandler.INSTANCE);
                     });
    }

    @Test
    void testZipBombDetectionEnabledPoiSax() throws IOException {
        final SheetParserFactory factory = new SheetParserFactory();
        factory.setEnabled(SheetParserFactory.Feature.POI_SAX, true);
        final SheetParser parser = factory.create(FILE);
        assertThrows(Exception.class,
                     () -> {
                         parser.parse(FILE, null, 0, VoidTablesHandler.INSTANCE);
                     });
    }

    @Test
    void testZipBombDetectionDisabledPoiStandard() throws IOException {
        final SheetParserFactory factory = new SheetParserFactory();
        factory.setEnabled(SheetParserFactory.Feature.POI_STANDARD, true);
        factory.setEnabled(SheetParserFactory.Feature.DISABLE_VULNERABILITY_PROTECTIONS, true);
        final SheetParser parser = factory.create(FILE);
        parser.parse(FILE, null, 0, VoidTablesHandler.INSTANCE);
        assertTrue(true);
    }

    @Test
    void testZipBombDetectionDisabledPoiStreaming() throws IOException {
        final SheetParserFactory factory = new SheetParserFactory();
        factory.setEnabled(SheetParserFactory.Feature.POI_STREAMING, true);
        factory.setEnabled(SheetParserFactory.Feature.DISABLE_VULNERABILITY_PROTECTIONS, true);
        final SheetParser parser = factory.create(FILE);
        parser.parse(FILE, null, 0, VoidTablesHandler.INSTANCE);
        assertTrue(true);
    }

    @Test
    void testZipBombDetectionDisabledPoiSax() throws IOException {
        final SheetParserFactory factory = new SheetParserFactory();
        factory.setEnabled(SheetParserFactory.Feature.POI_SAX, true);
        factory.setEnabled(SheetParserFactory.Feature.DISABLE_VULNERABILITY_PROTECTIONS, true);
        final SheetParser parser = factory.create(FILE);
        parser.parse(FILE, null, 0, VoidTablesHandler.INSTANCE);
        assertTrue(true);
    }
}