package cdc.office.ss.excel;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import cdc.office.ss.WorkbookKind;
import cdc.util.lang.Checks;
import cdc.util.lang.UnexpectedValueException;

public final class ExcelUtils {
    private ExcelUtils() {
    }

    /** This value was copied from {@link ZipSecureFile}. */
    public static final double DEFAULT_MIN_INFLATE_RATIO = 0.01;

    /**
     * Creates a {@link Workbook} instance.
     *
     * @param kind The workbook kind.
     * @param streaming When {@code kind} is {@link WorkbookKind#XLSM XLSM} or {@link WorkbookKind#XLSX XLSX},
     *            if {@code true}, a {@link SXSSFWorkbook} is created, a {@link XSSFWorkbook} otherwise.
     * @param useSharedStringTables {@code true} if shared string tables must be used.
     *            Used when a {@link SXSSFWorkbook} is created.<br>
     *            This is necessary when Rich Text must be generated in streaming mode,
     *            but this has a negative impact on performances.
     * @return A new {@link Workbook} instance.
     */
    public static Workbook create(WorkbookKind kind,
                                  boolean streaming,
                                  boolean useSharedStringTables) {
        Checks.isNotNull(kind, "kind");
        switch (kind) {
        case XLS:
            return new HSSFWorkbook();
        case XLSX:
        case XLSM:
            if (streaming) {
                // Streaming is compliant with rich text if shared string tables are used
                // But using them has a negative impact one performances.
                return new SXSSFWorkbook(null, // workbook
                                         SXSSFWorkbook.DEFAULT_WINDOW_SIZE, // row access window size
                                         false, // compress tmp files
                                         useSharedStringTables); // use shared string tables

            } else {
                return new XSSFWorkbook();
            }
        default:
            throw new UnexpectedValueException(kind);
        }
    }

    public static void save(Workbook workbook,
                            File file) throws IOException {
        try (final FileOutputStream fos = new FileOutputStream(file);
                final OutputStream out = new BufferedOutputStream(fos)) {
            workbook.write(out);
        }
    }

    public static Row addRow(Sheet sheet) {
        final Row last = sheet.getRow(sheet.getLastRowNum());
        if (last == null) {
            return sheet.createRow(sheet.getLastRowNum());
        } else {
            return sheet.createRow(sheet.getLastRowNum() + 1);
        }
    }

    public static Cell addCell(Row row) {
        final short last = row.getLastCellNum();
        if (last < 0) {
            return row.createCell(0);
        } else {
            return row.createCell(last);
        }
    }

    public static Cell addCell(Row row,
                               String value) {

        final Cell cell = addCell(row);
        cell.setCellValue(value);
        return cell;
    }

    public static Cell addCell(Row row,
                               CellStyle style,
                               String value) {
        final Cell cell = addCell(row, value);
        if (style != null) {
            cell.setCellStyle(style);
        }
        return cell;
    }

    public static void addCells(Row row,
                                String... values) {
        for (final String value : values) {
            addCell(row, value);
        }
    }

    public static void addCells(Row row,
                                CellStyle style,
                                String... values) {
        for (final String value : values) {
            addCell(row, style, value);
        }
    }

    public static void addCells(Row row,
                                List<String> values) {
        for (final String value : values) {
            addCell(row, value);
        }
    }

    public static void addCells(Row row,
                                CellStyle style,
                                List<String> values) {
        for (final String value : values) {
            addCell(row, style, value);
        }
    }

    public static void setName(Sheet sheet,
                               String name) {
        final Workbook workbook = sheet.getWorkbook();
        workbook.setSheetName(workbook.getSheetIndex(sheet), name);
    }
}