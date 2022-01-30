package cdc.office.ss.odf;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.odftoolkit.odfdom.doc.OdfSpreadsheetDocument;
import org.odftoolkit.odfdom.doc.table.OdfTable;
import org.odftoolkit.odfdom.doc.table.OdfTableCell;
import org.odftoolkit.odfdom.doc.table.OdfTableRow;

import cdc.office.ss.Section;
import cdc.office.ss.WorkbookKind;
import cdc.office.ss.WorkbookWriter;
import cdc.office.ss.WorkbookWriterFactory;
import cdc.office.ss.WorkbookWriterFeatures;
import cdc.office.tables.TableSection;
import cdc.util.lang.Checks;
import cdc.util.lang.DateUtils;
import cdc.util.strings.StringUtils;

/**
 * Open Office implementation of WorkbookWriter using ODF Toolkit.
 *
 * @author Damien Carbonne
 */
public class OdsWorkbookWriter implements WorkbookWriter<OdsWorkbookWriter> {
    private static final Logger LOGGER = LogManager.getLogger(OdsWorkbookWriter.class);
    private final File file;
    private final WorkbookWriterFeatures features;
    private Section section = Section.WORKBOOK;
    private final OdfSpreadsheetDocument doc;
    /** Current table (sheet). */
    private OdfTable table;
    /** Current row. */
    private OdfTableRow row;
    /** Current row index. */
    private int rowIndex = -1;
    /** Current cell. */
    private OdfTableCell cell;
    /** Current column index. */
    private int columnIndex = -1;

    private int tableCount = 0;

    private final String formatInt;
    private static final String FORMAT_DATE_TIME = "yyyy/MM/dd HH:mm:ss";
    private static final String FORMAT_DATE = "yyyy/MM/dd";
    private static final String FORMAT_TIME = "HH:mm:ss";

    public OdsWorkbookWriter(File file,
                             WorkbookWriterFeatures features)
            throws IOException {
        this.file = file;
        this.features = features;
        final WorkbookKind kind = WorkbookKind.from(file);
        if (kind != WorkbookKind.ODS) {
            throw new IllegalArgumentException();
        }
        try {
            this.doc = OdfSpreadsheetDocument.newSpreadsheetDocument();
        } catch (final Exception e) {
            throw new IOException(e);
        }
        this.table = null;
        this.row = null;
        this.cell = null;

        if (this.features.isEnabled(WorkbookWriterFeatures.Feature.USE_THOUSANDS_SEPARATOR)) {
            this.formatInt = "#,##0";
        } else {
            this.formatInt = "#";
        }
    }

    public OdsWorkbookWriter(File file,
                             WorkbookWriterFeatures features,
                             WorkbookWriterFactory factory)
            throws IOException {
        this(file,
             features);
    }

    @Override
    public OdsWorkbookWriter self() {
        return this;
    }

    @Override
    public WorkbookKind getKind() {
        return WorkbookKind.ODS;
    }

    @Override
    public WorkbookWriterFeatures getFeatures() {
        return features;
    }

    @Override
    public boolean isSupported(WorkbookWriterFeatures.Feature feature) {
        return feature == WorkbookWriterFeatures.Feature.USE_THOUSANDS_SEPARATOR
                || feature == WorkbookWriterFeatures.Feature.TRUNCATE_CELLS
                || feature == WorkbookWriterFeatures.Feature.TRUNCATE_CELLS_LINES;
    }

    private void unexpectedState(String context) throws IOException {
        throw new IOException("Unexpected state " + section + " in " + context);
    }

    public OdfSpreadsheetDocument getDocument() {
        return doc;
    }

    public OdfTable getTable() {
        return table;
    }

    public OdfTableRow getRow() {
        return row;
    }

    public OdfTableCell getCell() {
        return cell;
    }

    @Override
    public OdsWorkbookWriter beginSheet(String name) {
        this.table = OdfTable.newTable(doc);
        this.table.setTableName(name);
        Checks.assertFalse(table == null, "Null table");
        // }
        tableCount++;
        this.rowIndex = -1;
        this.columnIndex = -1;
        this.row = null;
        this.cell = null;
        this.section = Section.SHEET;
        return this;
    }

    @Override
    public OdsWorkbookWriter beginRow(TableSection section) throws IOException {
        if (this.section == Section.WORKBOOK) {
            unexpectedState("beginRow");
        }
        if (section == TableSection.DATA) {
            if (this.section == Section.HEADER_CELL && features.isEnabled(WorkbookWriterFeatures.Feature.AUTO_FILTER_COLUMNS)) {
                // TODO
                // this.doc.addAutoFilter(AutoFilter.builder("Range" + tableCount, this.table, rowIndex, 0, rowIndex, columnIndex)
                // .build());
            }
            this.section = Section.DATA_ROW;
        } else {
            this.section = Section.HEADER_ROW;
        }

        this.rowIndex++;
        this.row = null;
        this.cell = null;
        this.columnIndex = -1;
        return this;
    }

    public void addCell() throws IOException {
        switch (section) {
        case WORKBOOK:
        case SHEET:
            unexpectedState("addEmptyCell");
            break;
        default:
            break;
        }
        if (this.row == null) {
            this.row = table.getRowByIndex(this.rowIndex);
        }
        this.columnIndex++;
        this.cell = row.getCellByIndex(this.columnIndex);

        switch (section) {
        case DATA_ROW:
            this.section = Section.DATA_CELL;
            break;
        case HEADER_ROW:
            this.section = Section.HEADER_CELL;
            break;
        default:
            break;
        }
    }

    @Override
    public OdsWorkbookWriter addCellComment(String comment) {
        LOGGER.warn("addCellComment()) NYI");
        // TODO
        return this;
    }

    @Override
    public OdsWorkbookWriter addEmptyCell() throws IOException {
        this.columnIndex++;
        return this;
    }

    @Override
    public OdsWorkbookWriter addCell(boolean value) throws IOException {
        addCell();
        cell.setBooleanValue(value);
        return this;
    }

    @Override
    public OdsWorkbookWriter addCell(String value) throws IOException {
        if (value == null) {
            addEmptyCell();
        } else {
            addCell();
            if (features.isEnabled(WorkbookWriterFeatures.Feature.TRUNCATE_CELLS)) {
                cell.setStringValue(StringUtils.extract(value, getKind().getMaxCellSize()));
            } else if (features.isEnabled(WorkbookWriterFeatures.Feature.TRUNCATE_CELLS_LINES)) {
                cell.setStringValue(StringUtils.extractAverage(value, features.getMaxLineLength(), getKind().getMaxCellSize()));
            } else {
                cell.setStringValue(value);
            }
        }
        return this;
    }

    @Override
    public OdsWorkbookWriter addCell(double value) throws IOException {
        addCell();
        cell.setDoubleValue(value);
        return this;
    }

    @Override
    public OdsWorkbookWriter addCell(long value) throws IOException {
        addCell();
        cell.setDoubleValue((double) value);
        cell.setFormatString(formatInt);
        return this;
    }

    @Override
    public OdsWorkbookWriter addCell(Date value) throws IOException {
        if (value == null) {
            addEmptyCell();
        } else {
            addCell();
            cell.setDateValue(DateUtils.asCalendar(value));
            cell.setFormatString(FORMAT_DATE_TIME);
        }
        return this;
    }

    @Override
    public OdsWorkbookWriter addCell(LocalDateTime value) throws IOException {
        if (value == null) {
            addEmptyCell();
        } else {
            addCell();
            cell.setDateValue(DateUtils.asCalendar(value));
            cell.setFormatString(FORMAT_DATE_TIME);
        }
        return this;
    }

    @Override
    public OdsWorkbookWriter addCell(LocalDate value) throws IOException {
        if (value == null) {
            addEmptyCell();
        } else {
            addCell();
            cell.setDateValue(DateUtils.asCalendar(value));
            cell.setFormatString(FORMAT_DATE);
        }
        return this;
    }

    @Override
    public OdsWorkbookWriter addCell(LocalTime value) throws IOException {
        if (value == null) {
            addEmptyCell();
        } else {
            addCell();
            cell.setTimeValue(DateUtils.asCalendar(value));
            cell.setFormatString(FORMAT_TIME);
        }
        return this;
    }

    @Override
    public OdsWorkbookWriter addCell(URI uri,
                                     String label) throws IOException {
        if (uri == null) {
            addEmptyCell();
        } else {
            addCell();
            cell.setStringValue(label);
            LOGGER.warn("addCell(URI)) NYI");
            // TODO
            // final Paragraph paragraph = cell.addParagraph(label == null ? uri.toString() : label);
            // paragraph.applyHyperlink(uri);
        }
        return this;
    }

    @Override
    public void flush() throws IOException {
        // Ignore (not supported)
    }

    @Override
    public void close() throws IOException {
        try {
            this.doc.save(file);
            this.doc.close();
        } catch (final Exception e) {
            throw new IOException(e);
        }
    }
}