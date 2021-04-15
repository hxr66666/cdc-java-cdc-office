package cdc.office.csv;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cdc.office.tables.MemoryTableHandler;
import cdc.office.tables.Row;
import cdc.util.lang.ImplementationException;

public final class CsvLoader {
    private static final Logger LOGGER = LogManager.getLogger(CsvLoader.class);

    private CsvLoader() {
    }

    public static List<Row> load(File file,
                                 String charset,
                                 char separator) throws IOException {
        LOGGER.debug("load({}, {}, {})", file, charset, separator);
        final CsvParser parser = new CsvParser(separator);
        final MemoryTableHandler handler = new MemoryTableHandler();
        try {
            parser.parse(file,
                         charset,
                         handler,
                         0);
        } catch (final IOException e) {
            throw e;
        } catch (final Exception e) {
            throw new ImplementationException(e);
        }
        return handler.getRows();
    }

    public static List<Row> load(InputStream in,
                                 String charset,
                                 char separator) throws IOException {
        LOGGER.debug("load({}, {})", charset, separator);
        final CsvParser parser = new CsvParser(separator);
        final MemoryTableHandler handler = new MemoryTableHandler();
        try {
            parser.parse(in,
                         charset,
                         handler,
                         0);
        } catch (final IOException e) {
            throw e;
        } catch (final Exception e) {
            throw new ImplementationException(e);
        }
        return handler.getRows();
    }
}