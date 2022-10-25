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

    public static Workbook create(WorkbookKind kind,
                                  boolean streaming) {
        Checks.isNotNull(kind, "kibnd");
        switch (kind) {
        case XLS:
            return new HSSFWorkbook();
        case XLSX:
        case XLSM:
            if (streaming) {
                return new SXSSFWorkbook(null,
                                         SXSSFWorkbook.DEFAULT_WINDOW_SIZE,
                                         false,
                                         true);
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