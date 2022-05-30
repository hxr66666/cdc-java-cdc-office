package cdc.office.demos;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class RichTextDemo {
    private static void gen(Workbook workbook,
                            String filename) throws IOException {
        final Sheet sheet = workbook.createSheet("Rich Text Format Example");
        /* We define a rich Text String, that we split into three parts by using applyFont method */
        final Font font1 = workbook.createFont();
        final Font font2 = workbook.createFont();
        final Font font3 = workbook.createFont();
        font1.setColor(Font.COLOR_RED);
        font2.setBold(true);
        font3.setItalic(true);

        final RichTextString rts = workbook.getCreationHelper().createRichTextString("RichTextFormat");
        rts.applyFont(0, 4, font1);
        rts.applyFont(4, 8, font2);
        rts.applyFont(8, 14, font3);

        /* Attach these links to cells */
        final Row row = sheet.createRow(0);
        final Cell cell = row.createCell(0);
        cell.setCellValue(rts);

        /* Write changes to the workbook */
        try (FileOutputStream out = new FileOutputStream(new File(filename))) {
            workbook.write(out);
            out.flush();
        }
    }

    private static void hssf() throws IOException {
        // Works
        try (final HSSFWorkbook workbook = new HSSFWorkbook()) {
            gen(workbook, "target/rich-text-hssf.xls");
        }
    }

    private static void xssf() throws IOException {
        // Works
        try (final XSSFWorkbook workbook = new XSSFWorkbook()) {
            gen(workbook, "target/rich-text-xssf.xlsx");
        }
    }

    private static void sxssf() throws IOException {
        // Does not work
        try (final SXSSFWorkbook workbook = new SXSSFWorkbook(null, 100, false, true)) {
            gen(workbook, "target/rich-text-sxssf.xlsx");
            workbook.dispose();
        }
    }

    public static void main(String[] args) throws IOException {
        hssf();
        xssf();
        sxssf();
    }
}