package cdc.office.demos;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import cdc.office.ss.WorkbookKind;

public class DataValidationDemo {
    private static final Logger LOGGER = LogManager.getLogger(DataValidationDemo.class);

    private static DataValidation createListDV(DataValidationHelper dvh,
                                               WorkbookKind kind,
                                               int col) {
        final CellRangeAddressList addressList = new CellRangeAddressList(1, kind.getMaxRows() - 1, col, col);
        final DataValidationConstraint dvc = dvh.createExplicitListConstraint(new String[] { "One", "Two", "Three" });
        final DataValidation dv = dvh.createValidation(dvc, addressList);
        dv.createPromptBox("List choice", "One, Two, Three");
        dv.setShowPromptBox(true);
        return dv;
    }

    private static DataValidation createIntegerRangeDV(DataValidationHelper dvh,
                                                       WorkbookKind kind,
                                                       int col) {
        final CellRangeAddressList addressList = new CellRangeAddressList(1, kind.getMaxRows() - 1, col, col);
        final DataValidationConstraint dvc = dvh.createIntegerConstraint(DataValidationConstraint.OperatorType.BETWEEN, "0", "100");
        final DataValidation dv = dvh.createValidation(dvc, addressList);
        dv.setErrorStyle(DataValidation.ErrorStyle.STOP);
        dv.createPromptBox("Integer Range", "Value in 0 .. 100");
        dv.setShowPromptBox(true);
        dv.setShowErrorBox(true);
        return dv;
    }

    private static void gen(Workbook workbook,
                            String filename) throws IOException {
        LOGGER.info("Generate {}", filename);
        final File file = new File(filename);
        final WorkbookKind kind = WorkbookKind.from(file);
        final Sheet sheet = workbook.createSheet("Data Validation Example");

        final Row row = sheet.createRow(0);
        Cell cell = row.createCell(0);
        cell.setCellValue("List");
        cell = row.createCell(1);
        cell.setCellValue("Integer range");

        final DataValidationHelper dvh = sheet.getDataValidationHelper();
        sheet.addValidationData(createListDV(dvh, kind, 0));
        sheet.addValidationData(createIntegerRangeDV(dvh, kind, 1));

        /* Write changes to the workbook */
        try (FileOutputStream out = new FileOutputStream(file)) {
            workbook.write(out);
            out.flush();
        }
    }

    private static void hssf() throws IOException {
        try (final HSSFWorkbook workbook = new HSSFWorkbook()) {
            gen(workbook, "target/data-validation-hssf.xls");
        }
    }

    private static void xssf() throws IOException {
        try (final XSSFWorkbook workbook = new XSSFWorkbook()) {
            gen(workbook, "target/data-validation-xssf.xlsx");
        }
    }

    private static void sxssf() throws IOException {
        try (final SXSSFWorkbook workbook = new SXSSFWorkbook(null, 100, false, true)) {
            gen(workbook, "target/data-validation-sxssf.xlsx");
            workbook.dispose();
        }
    }

    public static void main(String[] args) throws IOException {
        hssf();
        xssf();
        sxssf();
    }
}