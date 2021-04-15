package cdc.office.ss;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import cdc.office.tables.MemoryTableHandler;
import cdc.office.tables.Row;

/**
 * Class used to load an Office sheet as a table.
 *
 * @author Damien Carbonne
 *
 */
public class SheetLoader {
    private final SheetParserFactory factory = new SheetParserFactory();

    public SheetLoader() {
        super();
    }

    public SheetParserFactory getFactory() {
        return factory;
    }

    /**
     * Loads a sheet from a file as a list of rows.
     * <p>
     * All kinds defined in {@link WorkbookKind} are supported.
     * <p>
     * If necessary, set SheetFactory parameters.
     *
     * @param file The file to load.
     * @param password The password protecting {@code file}.
     * @param sheetName The sheet name. Used if file is a multi-sheet file.
     * @return The sheet as a list of rows.
     * @throws IOException When a IO error occurs.
     */
    public List<Row> load(File file,
                          String password,
                          String sheetName) throws IOException {
        final SheetParser parser = factory.create(file);
        final MemoryTableHandler handler = new MemoryTableHandler();
        try {
            parser.parse(file, password, sheetName, 0, handler);
            return handler.getRows();
        } catch (final IOException e) {
            throw e;
        } catch (final Exception e) {
            throw new IOException(e);
        }
    }

    /**
     * Loads a sheet from a file as a list of rows.
     * <p>
     * All kinds defined in {@link WorkbookKind} are supported.
     * <p>
     * If necessary, set SheetFactory parameters.
     *
     * @param file The file to load.
     * @param password The password protecting {@code file}.
     * @param sheetIndex The 0-based index of the sheet. Used if file is a multi-sheet file.
     * @return The sheet as a list of rows.
     * @throws IOException When a IO error occurs.
     */
    public List<Row> load(File file,
                          String password,
                          int sheetIndex) throws IOException {
        final SheetParser parser = factory.create(file);
        final MemoryTableHandler handler = new MemoryTableHandler();
        try {
            parser.parse(file, password, sheetIndex, 0, handler);
            return handler.getRows();
        } catch (final IOException e) {
            throw e;
        } catch (final Exception e) {
            throw new IOException(e);
        }
    }

    /**
     * Loads a sheet from an InputStream as a list of rows.
     * <p>
     * All kinds defined in {@link WorkbookKind} are supported.
     * <p>
     * If necessary, set SheetFactory parameters.
     *
     *
     * @param in The InoutStream to load.
     * @param kind The sheet kind.
     * @param password The password protecting {@code file}.
     * @param sheetName The sheet name. Used if file is a multi-sheet file.
     * @return The sheet as a list of rows.
     * @throws IOException When a IO error occurs.
     */
    public List<Row> load(InputStream in,
                          WorkbookKind kind,
                          String password,
                          String sheetName) throws IOException {
        final SheetParser parser = factory.create(kind);
        final MemoryTableHandler handler = new MemoryTableHandler();
        try {
            parser.parse(in, kind, password, sheetName, 0, handler);
            return handler.getRows();
        } catch (final IOException e) {
            throw e;
        } catch (final Exception e) {
            throw new IOException(e);
        }
    }

    /**
     * Loads a sheet from an InputStream as a list of rows.
     * <p>
     * All kinds defined in {@link WorkbookKind} are supported.
     * <p>
     * If necessary, set SheetFactory parameters.
     *
     *
     * @param in The InoutStream to load.
     * @param kind The sheet kind.
     * @param password The password protecting {@code file}.
     * @param sheetIndex The 0-based index of the sheet. Used if file is a multi-sheet file.
     * @return The sheet as a list of rows.
     * @throws IOException When a IO error occurs.
     */
    public List<Row> load(InputStream in,
                          WorkbookKind kind,
                          String password,
                          int sheetIndex) throws IOException {
        final SheetParser parser = factory.create(kind);
        final MemoryTableHandler handler = new MemoryTableHandler();
        try {
            parser.parse(in, kind, password, sheetIndex, 0, handler);
            return handler.getRows();
        } catch (final IOException e) {
            throw e;
        } catch (final Exception e) {
            throw new IOException(e);
        }
    }
}