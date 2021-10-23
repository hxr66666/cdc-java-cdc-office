package cdc.office.ss.excel;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import cdc.office.ss.Section;
import cdc.office.ss.WorkbookKind;
import cdc.office.ss.WorkbookWriter;
import cdc.office.ss.WorkbookWriterFactory;
import cdc.office.ss.WorkbookWriterFeatures;
import cdc.office.tables.TableSection;
import cdc.util.lang.DateUtils;
import cdc.util.strings.StringUtils;

/**
 * Excel implementation of WorkbookWriter.
 *
 * @author Damien Carbonne
 *
 */
public class ExcelWorkbookWriter implements WorkbookWriter<ExcelWorkbookWriter> {
    private final File file;
    private final WorkbookWriterFeatures features;
    private final WorkbookKind kind;
    private final Workbook workbook;
    private Section section = Section.WORKBOOK;
    /** Current sheet. */
    private Sheet sheet;
    /** Current row. */
    private Row row;
    /** Current row index. */
    private int rowIndex = -1;
    /** Current cell. */
    private Cell cell;
    /** Current column index. */
    private int columnIndex = -1;
    /** Max column index in current sheet. */
    private int maxColumnIndex = -1;

    private final CellStyle styleText;
    private final CellStyle styleInt;
    private final CellStyle styleDateTime;
    private final CellStyle styleDate;
    private final CellStyle styleTime;

    public ExcelWorkbookWriter(File file,
                               WorkbookWriterFeatures features,
                               boolean streaming) {
        this.file = file;
        this.features = features;
        this.kind = WorkbookKind.from(file);
        this.workbook = ExcelUtils.create(kind, streaming);
        this.sheet = null;
        this.row = null;
        this.cell = null;

        final DataFormat format = workbook.createDataFormat();

        this.styleText = workbook.createCellStyle();
        this.styleText.setDataFormat(format.getFormat("@"));
        // this.styleText.setWrapText(true);

        this.styleInt = workbook.createCellStyle();
        if (this.features.isEnabled(WorkbookWriterFeatures.Feature.USE_THOUSANDS_SEPARATOR)) {
            this.styleInt.setDataFormat(format.getFormat("#,##0"));
        } else {
            this.styleInt.setDataFormat(format.getFormat("0"));
        }

        this.styleDateTime = workbook.createCellStyle();
        this.styleDateTime.setDataFormat(format.getFormat("yyyy/mm/dd hh:mm:ss"));

        this.styleDate = workbook.createCellStyle();
        this.styleDate.setDataFormat(format.getFormat("yyyy/mm/dd"));

        this.styleTime = workbook.createCellStyle();
        this.styleTime.setDataFormat(format.getFormat("hh:mm:ss"));

    }

    public ExcelWorkbookWriter(File file,
                               WorkbookWriterFeatures features,
                               WorkbookWriterFactory factory) {
        this(file,
             features,
             factory.isEnabled(WorkbookWriterFactory.Hint.POI_STREAMING));
    }

    @Override
    public ExcelWorkbookWriter self() {
        return this;
    }

    @Override
    public WorkbookKind getKind() {
        return kind;
    }

    @Override
    public WorkbookWriterFeatures getFeatures() {
        return features;
    }

    @Override
    public boolean isSupported(WorkbookWriterFeatures.Feature feature) {
        return feature == WorkbookWriterFeatures.Feature.AUTO_FILTER_COLUMNS
                || feature == WorkbookWriterFeatures.Feature.AUTO_SIZE_COLUMNS
                || feature == WorkbookWriterFeatures.Feature.USE_THOUSANDS_SEPARATOR
                || feature == WorkbookWriterFeatures.Feature.TRUNCATE_CELLS
                || feature == WorkbookWriterFeatures.Feature.TRUNCATE_CELLS_LINES
                || feature == WorkbookWriterFeatures.Feature.NO_CELL_STYLES
                || feature == WorkbookWriterFeatures.Feature.COMMENTS;
    }

    private void unexpectedState(String context) throws IOException {
        throw new IOException("Unexpected state " + section + " in " + context);
    }

    public Workbook getWorkbook() {
        return workbook;
    }

    public Sheet getSheet() {
        return sheet;
    }

    public Row getRow() {
        return row;
    }

    public Cell getCell() {
        return cell;
    }

    private void autosizeColumns() {
        if (features.isEnabled(WorkbookWriterFeatures.Feature.AUTO_SIZE_COLUMNS) && sheet != null) {
            for (int column = 0; column <= maxColumnIndex; column++) {
                sheet.autoSizeColumn(column);
            }
        }
    }

    @Override
    public ExcelWorkbookWriter beginSheet(String name) {
        // autosize columns of previous sheet
        autosizeColumns();

        sheet = workbook.createSheet(name);
        if (features.isEnabled(WorkbookWriterFeatures.Feature.AUTO_SIZE_COLUMNS)
                && sheet instanceof SXSSFSheet) {
            ((SXSSFSheet) sheet).trackAllColumnsForAutoSizing();
        }
        rowIndex = -1;
        row = null;
        cell = null;
        columnIndex = -1;
        section = Section.SHEET;
        maxColumnIndex = -1;
        return this;
    }

    @Override
    public ExcelWorkbookWriter beginRow(TableSection section) throws IOException {
        if (this.section == Section.WORKBOOK) {
            unexpectedState("beginRow");
        }
        if (section == TableSection.DATA) {
            if (this.section == Section.HEADER_CELL && features.isEnabled(WorkbookWriterFeatures.Feature.AUTO_FILTER_COLUMNS)) {
                sheet.setAutoFilter(new CellRangeAddress(rowIndex, rowIndex, 0, columnIndex));
            }
            this.section = Section.DATA_ROW;
        } else {
            this.section = Section.HEADER_ROW;
        }
        rowIndex++;
        row = null;
        cell = null;
        columnIndex = -1;
        return this;
    }

    private void addCell() throws IOException {
        switch (section) {
        case WORKBOOK:
        case SHEET:
            unexpectedState("addEmptyCell");
            break;
        default:
            break;
        }
        if (row == null) {
            row = sheet.createRow(this.rowIndex);
        }
        columnIndex++;
        cell = row.createCell(columnIndex);
        if (columnIndex > maxColumnIndex) {
            maxColumnIndex = columnIndex;
        }

        switch (section) {
        case DATA_ROW:
            section = Section.DATA_CELL;
            break;
        case HEADER_ROW:
            section = Section.HEADER_CELL;
            break;
        default:
            break;
        }
    }

    private void setCellStyle(Cell cell,
                              CellStyle style) {
        if (!features.isEnabled(WorkbookWriterFeatures.Feature.NO_CELL_STYLES)) {
            cell.setCellStyle(style);
        }
    }

    @Override
    public ExcelWorkbookWriter addCellComment(String comment) {
        final Drawing<?> drawingPatriarch = sheet.createDrawingPatriarch();
        final CreationHelper factory = workbook.getCreationHelper();
        final ClientAnchor anchor = factory.createClientAnchor();
        // anchor.setAnchorType(AnchorType.MOVE_DONT_RESIZE);

        anchor.setCol1(cell.getColumnIndex());
        anchor.setCol2(cell.getColumnIndex() + 2);
        anchor.setRow1(row.getRowNum());
        anchor.setRow2(row.getRowNum() + 3);
        final Comment cmt = drawingPatriarch.createCellComment(anchor);
        final RichTextString str = factory.createRichTextString(comment);
        cmt.setString(str);
        cmt.setRow(rowIndex);
        cmt.setColumn(columnIndex);
        cell.setCellComment(cmt);
        return this;
    }

    @Override
    public ExcelWorkbookWriter addEmptyCell() throws IOException {
        columnIndex++;
        if (columnIndex > maxColumnIndex) {
            maxColumnIndex = columnIndex;
        }
        return this;
    }

    @Override
    public ExcelWorkbookWriter addCell(boolean value) throws IOException {
        addCell();
        cell.setCellValue(value);
        return this;
    }

    @Override
    public ExcelWorkbookWriter addCell(String value) throws IOException {
        if (value == null) {
            addEmptyCell();
        } else {
            addCell();
            if (features.isEnabled(WorkbookWriterFeatures.Feature.TRUNCATE_CELLS)) {
                cell.setCellValue(StringUtils.extract(value, getKind().getMaxCellSize()));
            } else if (features.isEnabled(WorkbookWriterFeatures.Feature.TRUNCATE_CELLS_LINES)) {
                cell.setCellValue(StringUtils.extractAverage(value, features.getMaxLineLength(), getKind().getMaxCellSize()));
            } else {
                cell.setCellValue(value);
            }
            setCellStyle(cell, styleText);
        }
        return this;
    }

    @Override
    public ExcelWorkbookWriter addCell(double value) throws IOException {
        addCell();
        cell.setCellValue(value);
        return this;
    }

    @Override
    public ExcelWorkbookWriter addCell(long value) throws IOException {
        addCell();
        cell.setCellValue(value);
        setCellStyle(cell, styleInt);
        return this;
    }

    @Override
    public ExcelWorkbookWriter addCell(Date value) throws IOException {
        if (value == null) {
            addEmptyCell();
        } else {
            addCell();
            cell.setCellValue(value);
            setCellStyle(cell, styleDateTime);
        }
        return this;
    }

    @Override
    public ExcelWorkbookWriter addCell(LocalDateTime value) throws IOException {
        if (value == null) {
            addEmptyCell();
        } else {
            addCell();
            cell.setCellValue(DateUtils.asDate(value));
            setCellStyle(cell, styleDateTime);
        }
        return this;
    }

    @Override
    public ExcelWorkbookWriter addCell(LocalDate value) throws IOException {
        if (value == null) {
            addEmptyCell();
        } else {
            addCell();
            cell.setCellValue(DateUtils.asDate(value));
            setCellStyle(cell, styleDate);
        }
        return this;
    }

    @Override
    public ExcelWorkbookWriter addCell(LocalTime value) throws IOException {
        if (value == null) {
            addEmptyCell();
        } else {
            addCell();
            cell.setCellValue(DateUtils.asDate(value));
            setCellStyle(cell, styleTime);
        }
        return this;
    }

    @Override
    public ExcelWorkbookWriter addCell(URI uri,
                                       String label) throws IOException {
        if (uri == null) {
            addEmptyCell();
        } else {
            addCell();
            final Hyperlink link = workbook.getCreationHelper().createHyperlink(HyperlinkType.URL);
            final URL url = uri.toURL();
            link.setAddress(url.toString());
            cell.setCellValue(label == null ? url.toString() : label);
            cell.setHyperlink(link);
        }
        return this;
    }

    @Override
    public void flush() throws IOException {
        // Ignore (not supported)
    }

    @Override
    public void close() throws IOException {
        // autosize columns of last sheet
        autosizeColumns();

        ExcelUtils.save(workbook, file);
        if (workbook instanceof SXSSFWorkbook) {
            ((SXSSFWorkbook) workbook).dispose();
        }
        this.workbook.close();
    }
}