package cdc.office.ss.odf;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.jferard.fastods.AnonymousOdsFileWriter;
import com.github.jferard.fastods.NamedOdsFileWriter;
import com.github.jferard.fastods.OdsDocument;
import com.github.jferard.fastods.OdsFactory;
import com.github.jferard.fastods.Table;
import com.github.jferard.fastods.TableCell;
import com.github.jferard.fastods.TableRow;
import com.github.jferard.fastods.TableRowImpl;
import com.github.jferard.fastods.Text;
import com.github.jferard.fastods.TextBuilder;
import com.github.jferard.fastods.util.AutoFilter;

import cdc.office.ss.ContentValidation;
import cdc.office.ss.Section;
import cdc.office.ss.WorkbookKind;
import cdc.office.ss.WorkbookWriter;
import cdc.office.ss.WorkbookWriterFactory;
import cdc.office.ss.WorkbookWriterFeatures;
import cdc.office.tables.TableSection;
import cdc.util.lang.DateUtils;
import cdc.util.strings.StringUtils;

/**
 * Open Office implementation of WorkbookWriter using FastOds.
 *
 * @author Damien Carbonne
 */
public class FastOdsWorkbookWriter implements WorkbookWriter<FastOdsWorkbookWriter> {
    private static final Logger LOGGER = LogManager.getLogger(FastOdsWorkbookWriter.class);
    private final File file;
    private final OutputStream out;
    private final WorkbookWriterFeatures features;
    private Section section = Section.WORKBOOK;
    // private final AnonymousOdsFileWriter writer;
    private final NamedOdsFileWriter namedWriter;
    private final AnonymousOdsFileWriter anonymousWriter;
    private final OdsDocument doc;
    /** Current table (sheet). */
    private Table table;
    /** Current row. */
    private TableRowImpl row;
    /** Current row index. */
    private int rowIndex = -1;
    /** Current cell. */
    private TableCell cell;
    /** Current column index. */
    private int columnIndex = -1;

    private int tableCount = 0;

    private final String formatInt;
    private static final String PATTERN_DATE_TIME = "yyyy/MM/dd HH:mm:ss";
    private static final SimpleDateFormat FORMAT_DATE_TIME = new SimpleDateFormat(PATTERN_DATE_TIME);
    private static final String PATTERN_DATE = "yyyy/MM/dd";
    private static final SimpleDateFormat FORMAT_DATE = new SimpleDateFormat(PATTERN_DATE);
    private static final String PATTERN_TIME = "HH:mm:ss";
    private static final SimpleDateFormat FORMAT_TIME = new SimpleDateFormat(PATTERN_TIME);

    private FastOdsWorkbookWriter(File file,
                                  OutputStream out,
                                  WorkbookKind kind,
                                  WorkbookWriterFeatures features)
            throws IOException {
        this.file = file;
        this.out = out;
        this.features = features;
        if (kind != WorkbookKind.ODS) {
            throw new IllegalArgumentException();
        }
        try {
            final OdsFactory odsFactory = OdsFactory.create();
            if (file == null) {
                this.anonymousWriter = odsFactory.createWriter();
                this.namedWriter = null;
                this.doc = anonymousWriter.document();
            } else {
                this.anonymousWriter = null;
                this.namedWriter = odsFactory.createWriter(file);
                this.doc = namedWriter.document();
            }
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

    public FastOdsWorkbookWriter(File file,
                                 WorkbookWriterFeatures features)
            throws IOException {
        this(file,
             null,
             WorkbookKind.from(file),
             features);
    }

    public FastOdsWorkbookWriter(OutputStream out,
                                 WorkbookWriterFeatures features)
            throws IOException {
        this(null,
             out,
             WorkbookKind.ODS,
             features);
    }

    public FastOdsWorkbookWriter(File file,
                                 WorkbookWriterFeatures features,
                                 WorkbookWriterFactory factory)
            throws IOException {
        this(file,
             null,
             WorkbookKind.from(file),
             features);
    }

    public FastOdsWorkbookWriter(OutputStream out,
                                 WorkbookKind kind,
                                 WorkbookWriterFeatures features,
                                 WorkbookWriterFactory factory)
            throws IOException {
        this(null,
             out,
             kind,
             features);
    }

    @Override
    public FastOdsWorkbookWriter self() {
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
        return feature == WorkbookWriterFeatures.Feature.AUTO_FILTER_COLUMNS
                || feature == WorkbookWriterFeatures.Feature.USE_THOUSANDS_SEPARATOR
                || feature == WorkbookWriterFeatures.Feature.TRUNCATE_CELLS_LINES
                || feature == WorkbookWriterFeatures.Feature.TRUNCATE_CELLS;
    }

    private void unexpectedState(String context) throws IOException {
        throw new IOException("Unexpected state " + section + " in " + context);
    }

    public OdsDocument getDocument() {
        return doc;
    }

    public Table getTable() {
        return table;
    }

    public TableRow getRow() {
        return row;
    }

    public TableCell getCell() {
        return cell;
    }

    @Override
    public FastOdsWorkbookWriter beginSheet(String name) throws IOException {
        this.table = this.doc.addTable(name);
        tableCount++;
        this.rowIndex = -1;
        this.columnIndex = -1;
        this.row = null;
        this.cell = null;
        this.section = Section.SHEET;
        return this;
    }

    @Override
    public FastOdsWorkbookWriter addContentValidation(ContentValidation cv) throws IOException {
        // Not supported
        LOGGER.warn("addContentValidation(...) is not supported");
        return this;
    }

    @Override
    public FastOdsWorkbookWriter beginRow(TableSection section) throws IOException {
        if (this.section == Section.WORKBOOK) {
            unexpectedState("beginRow");
        }
        if (this.rowIndex >= 0 && this.row == null) {
            this.row = table.getRow(this.rowIndex);
        }
        if (section == TableSection.DATA) {
            if (this.section == Section.HEADER_CELL && features.isEnabled(WorkbookWriterFeatures.Feature.AUTO_FILTER_COLUMNS)) {
                this.doc.addAutoFilter(AutoFilter.builder("Range" + tableCount, this.table, rowIndex, 0, rowIndex, columnIndex)
                                                 .build());
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
            this.row = table.getRow(this.rowIndex);
        }
        this.columnIndex++;
        this.cell = row.getOrCreateCell(this.columnIndex);

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
    public FastOdsWorkbookWriter addCellComment(String comment) {
        LOGGER.warn("addCellComment(...) NYI");
        // TODO
        return this;
    }

    @Override
    public FastOdsWorkbookWriter addEmptyCell() throws IOException {
        this.columnIndex++;
        return this;
    }

    @Override
    public FastOdsWorkbookWriter addCell(boolean value) throws IOException {
        addCell();
        cell.setBooleanValue(value);
        return this;
    }

    @Override
    public FastOdsWorkbookWriter addCell(String value) throws IOException {
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
    public FastOdsWorkbookWriter addCell(double value) throws IOException {
        addCell();
        cell.setFloatValue(Double.valueOf(value));
        return this;
    }

    @Override
    public FastOdsWorkbookWriter addCell(long value) throws IOException {
        addCell();
        cell.setFloatValue(Long.valueOf(value));
        return this;
    }

    @Override
    public FastOdsWorkbookWriter addCell(Date value) throws IOException {
        if (value == null) {
            addEmptyCell();
        } else {
            addCell();
            cell.setDateValue(DateUtils.asCalendar(value));
        }
        return this;
    }

    @Override
    public FastOdsWorkbookWriter addCell(LocalDateTime value) throws IOException {
        if (value == null) {
            addEmptyCell();
        } else {
            addCell();
            cell.setDateValue(DateUtils.asCalendar(value));
        }
        return this;
    }

    @Override
    public FastOdsWorkbookWriter addCell(LocalDate value) throws IOException {
        if (value == null) {
            addEmptyCell();
        } else {
            addCell();
            cell.setDateValue(DateUtils.asCalendar(value));
        }
        return this;
    }

    @Override
    public FastOdsWorkbookWriter addCell(LocalTime value) throws IOException {
        if (value == null) {
            addEmptyCell();
        } else {
            addCell();
            cell.setDateValue(DateUtils.asCalendar(value));
        }
        return this;
    }

    @Override
    public FastOdsWorkbookWriter addCell(URI uri,
                                         String label) throws IOException {
        if (uri == null) {
            addCell(label);
        } else {
            addCell();
            final Text text = TextBuilder.create().par().link(label, uri).build();
            cell.setText(text);
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
            if (namedWriter == null) {
                this.anonymousWriter.save(out);
            } else {
                this.namedWriter.save();
            }
        } catch (final Exception e) {
            throw new IOException(e);
        }
    }
}