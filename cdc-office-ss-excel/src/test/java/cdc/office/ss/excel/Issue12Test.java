package cdc.office.ss.excel;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.IOUtils;
import org.junit.jupiter.api.Test;

import cdc.office.ss.SheetParser;
import cdc.office.ss.SheetParserFactory;
import cdc.office.ss.WorkbookKind;
import cdc.office.ss.WorkbookWriter;
import cdc.office.ss.WorkbookWriterFactory;
import cdc.office.tables.RowLocation;
import cdc.office.tables.TableSection;
import cdc.office.tables.TablesHandler;
import cdc.util.function.Evaluation;

class Issue12Test {
    private static final Logger LOGGER = LogManager.getLogger(Issue12Test.class);

    private static File test(WorkbookKind kind,
                             int columns,
                             int rows) throws IOException {
        LOGGER.info("START {}:{}x{}", kind, columns, rows);
        final File file = new File("target/issue12-" + columns + "x" + rows + "." + kind.getExtension());

        final List<String> header = new ArrayList<>();
        for (int i = 0; i < columns; i++) {
            header.add("Col" + i);
        }
        final List<String> data = new ArrayList<>();
        for (int i = 0; i < columns; i++) {
            data.add("ABCDEFGHIKKLMNOPQRSTUVWXYZ");
        }

        final WorkbookWriterFactory factory = new WorkbookWriterFactory();
        try (final WorkbookWriter<?> w = factory.create(file)) {
            LOGGER.info("{}", w.getClass().getCanonicalName());
            w.beginSheet("Sheet");
            w.addRow(TableSection.HEADER, header);
            for (int i = 0; i < rows; i++) {
                w.addRow(TableSection.DATA, data);
            }
            w.flush();
        }
        LOGGER.info("DONE {}:{}x{}", kind, columns, rows);
        return file;
    }

    private static void read(File file) throws IOException {
        LOGGER.info("read({})", file);
        final SheetParserFactory factory = new SheetParserFactory();
        final SheetParser parser = factory.create(file);
        final TablesHandler handler = new TablesHandler() {
            @Override
            public Evaluation processHeader(cdc.office.tables.Row header,
                                            RowLocation location) {
                return Evaluation.CONTINUE;
            }

            @Override
            public Evaluation processData(cdc.office.tables.Row data,
                                          RowLocation location) {
                return Evaluation.CONTINUE;
            }
        };
        parser.parse(file, null, 1, handler);
        LOGGER.info("read({})", file);
    }

    private static void addRow(final Sheet sheet,
                               int rowIndex,
                               List<String> values) {
        final Row row = sheet.createRow(rowIndex);
        int columnIndex = 0;
        for (final String value : values) {
            final Cell cell = row.createCell(columnIndex);
            cell.setCellValue(value);
            columnIndex++;
        }
    }

    private static File testXls(int columns,
                                int rows) throws IOException {
        LOGGER.info("START_XLS:{}x{}", columns, rows);
        final File file = new File("target/issue12-poi-" + columns + "x" + rows + ".xls");

        final List<String> header = new ArrayList<>();
        for (int i = 0; i < columns; i++) {
            header.add("Col" + i);
        }
        final List<String> data = new ArrayList<>();
        for (int i = 0; i < columns; i++) {
            data.add("ABCDEFGHIKKLMNOPQRSTUVWXYZ");
        }

        try (final Workbook w = new HSSFWorkbook()) {
            final Sheet sheet = w.createSheet("Sheet");
            int rowIndex = 0;
            addRow(sheet, rowIndex, header);
            rowIndex++;
            for (int i = 0; i < rows; i++) {
                addRow(sheet, rowIndex, data);
                rowIndex++;
            }
            try (final FileOutputStream fos = new FileOutputStream(file);
                    final OutputStream out = new BufferedOutputStream(fos)) {
                w.write(out);
            }
        }
        LOGGER.info("DONE_XLS:{}x{}", columns, rows);
        return file;
    }

    @Test
    void test() throws IOException {
        final int[] rowss = { 15000, 20000, 30000, 60000 };
        // This generates (with poi-5.1 or poi-5.2, not with 5.0):
        // WARN org.apache.poi.POIDocument - DocumentSummaryInformation property set came back as null
        // WARN org.apache.poi.POIDocument - SummaryInformation property set came back as null
        final File file1 = test(WorkbookKind.XLS, 10, 10);
        read(file1);
        IOUtils.setByteArrayMaxOverride(256_000_000);
        for (final int rows : rowss) {
            final File file2 = testXls(100, rows);
            read(file2);
        }
    }
}