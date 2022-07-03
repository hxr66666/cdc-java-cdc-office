package cdc.office.demos;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import cdc.util.strings.StringUtils;

public class CommentDemo {
    private static final Logger LOGGER = LogManager.getLogger(CommentDemo.class);

    public static void addCellComment(Cell cell,
                                      String comment) {
        comment = StringUtils.extract(comment, 10000);
        final Sheet sheet = cell.getSheet();
        final Row row = cell.getRow();
        final int rowIndex = row.getRowNum();
        final int columnIndex = cell.getColumnIndex();
        final Drawing<?> drawing = sheet.createDrawingPatriarch();
        final CreationHelper factory = sheet.getWorkbook().getCreationHelper();
        final ClientAnchor anchor = factory.createClientAnchor();
        // final ClientAnchor anchor = drawing.createAnchor(0, 0, 100, 100, columnIndex, rowIndex, columnIndex + 1, rowIndex + 1);
        // anchor.setAnchorType(ClientAnchor.AnchorType.MOVE_DONT_RESIZE);

        final int maxLineLength = StringUtils.maxLineLength(comment);
        anchor.setCol1(columnIndex);
        anchor.setCol2(columnIndex + 1 + Math.min(maxLineLength / 11, 20));
        anchor.setRow1(rowIndex);
        anchor.setRow2(rowIndex + 1);
        // anchor.setDx1(23);
        // anchor.setDx2(46);
        // anchor.setDy1(24);
        // anchor.setDy2(48);
        final Comment cmt = drawing.createCellComment(anchor);
        final RichTextString str = factory.createRichTextString(comment);
        cmt.setString(str);
        cmt.setRow(rowIndex);
        cmt.setColumn(columnIndex);
        // cmt.setVisible(true);
        cell.setCellComment(cmt);
    }

    private static String comment(int lines,
                                  int cols) {
        final StringBuilder builder = new StringBuilder();
        for (int line = 0; line < lines; line++) {
            if (line > 0) {
                builder.append("\n");
            }
            builder.append(line);
            builder.append(" ");
            for (int col = 0; col < cols; col++) {
                if (col % 10 == 0) {
                    builder.append(" ");
                }
                builder.append(col % 10);
            }
        }

        return builder.toString();
    }

    private static void gen(Workbook workbook,
                            String filename) throws IOException {
        LOGGER.info("Generate {}", filename);
        final File file = new File(filename);
        // final WorkbookKind kind = WorkbookKind.from(file);
        final Sheet sheet = workbook.createSheet("Comment Example");

        final Row row = sheet.createRow(0);
        final Cell a1 = row.createCell(0);
        a1.setCellValue("A1");
        addCellComment(a1, comment(2, 10));

        final Cell a2 = row.createCell(1);
        a2.setCellValue("A2");
        addCellComment(a2, comment(20, 20));

        final Cell a3 = row.createCell(2);
        a3.setCellValue("A3");
        addCellComment(a3, comment(50, 50));

        final Cell a4 = row.createCell(3);
        a4.setCellValue("A4");
        addCellComment(a4, comment(50, 100));

        final Cell a5 = row.createCell(4);
        a5.setCellValue("A5");
        addCellComment(a5, comment(1000, 1000));

        /* Write changes to the workbook */
        try (FileOutputStream out = new FileOutputStream(file)) {
            workbook.write(out);
            out.flush();
        }
    }

    private static void hssf() throws IOException {
        try (final HSSFWorkbook workbook = new HSSFWorkbook()) {
            gen(workbook, "target/comment-hssf.xls");
        }
    }

    private static void xssf() throws IOException {
        try (final XSSFWorkbook workbook = new XSSFWorkbook()) {
            gen(workbook, "target/comment-xssf.xlsx");
        }
    }

    private static void sxssf() throws IOException {
        try (final SXSSFWorkbook workbook = new SXSSFWorkbook(null, 100, false, true)) {
            gen(workbook, "target/comment-sxssf.xlsx");
            workbook.dispose();
        }
    }

    public static void main(String... args) throws IOException {
        hssf();
        xssf();
        sxssf();
    }
}