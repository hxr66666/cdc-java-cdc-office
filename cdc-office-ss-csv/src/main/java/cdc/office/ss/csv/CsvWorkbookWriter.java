package cdc.office.ss.csv;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

import cdc.office.csv.CsvWriter;
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
 * Csv implementation of WorkbookWriter.
 *
 * @author Damien Carbonne
 *
 */
public class CsvWorkbookWriter implements WorkbookWriter<CsvWorkbookWriter> {
    private final CsvWriter writer;
    private final WorkbookWriterFeatures features;
    private Section section = Section.WORKBOOK;
    private int rowNumber = -1;
    private int columnNumber = -1;
    private int lastContentRow = 0;
    private int lastContentColumn = -1;

    private final SimpleDateFormat formatDateTime = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    private final SimpleDateFormat formatDate = new SimpleDateFormat("yyyy/MM/dd");
    private final SimpleDateFormat formatTime = new SimpleDateFormat("HH:mm:ss");

    public CsvWorkbookWriter(File file,
                             WorkbookWriterFeatures features)
            throws IOException {
        final WorkbookKind kind = WorkbookKind.from(file);
        if (kind != WorkbookKind.CSV) {
            throw new IllegalArgumentException();
        }

        this.writer = new CsvWriter(file, features.getCharset());
        this.writer.setSeparator(features.getSeparator());
        this.features = features;
    }

    public CsvWorkbookWriter(File file,
                             WorkbookWriterFeatures features,
                             WorkbookWriterFactory factory)
            throws IOException {
        this(file, features);
    }

    public CsvWorkbookWriter(OutputStream out,
                             WorkbookKind kind,
                             WorkbookWriterFeatures features,
                             WorkbookWriterFactory factory) {
        if (kind != WorkbookKind.CSV) {
            throw new IllegalArgumentException();
        }
        this.writer = new CsvWriter(out, features.getCharset());
        this.writer.setSeparator(features.getSeparator());
        this.features = features;
    }

    private void unexpectedState(String context) throws IOException {
        throw new IOException("Unexpected state " + section + " in " + context);
    }

    @Override
    public CsvWorkbookWriter self() {
        return this;
    }

    @Override
    public WorkbookKind getKind() {
        return WorkbookKind.CSV;
    }

    @Override
    public WorkbookWriterFeatures getFeatures() {
        return features;
    }

    @Override
    public boolean isSupported(WorkbookWriterFeatures.Feature feature) {
        return feature == WorkbookWriterFeatures.Feature.CSV_SEPARATE_SHEETS
                || feature == WorkbookWriterFeatures.Feature.CSV_WRITE_SHEET_NAMES
                || feature == WorkbookWriterFeatures.Feature.TRUNCATE_CELLS
                || feature == WorkbookWriterFeatures.Feature.TRUNCATE_CELLS_LINES;
    }

    private void prepareCellWithContent() throws IOException {
        if (rowNumber >= 0 && lastContentRow < rowNumber) {
            for (int i = lastContentRow; i < rowNumber; i++) {
                writer.writeln();
            }
            lastContentRow = rowNumber;
        }

        final int missingColumns = columnNumber - lastContentColumn;
        columnNumber++;
        if (missingColumns > 0) {
            for (int i = 0; i < missingColumns; i++) {
                writer.write("");
            }
        }
        lastContentColumn = columnNumber;
    }

    @Override
    public CsvWorkbookWriter beginSheet(String name) throws IOException {
        if (this.section != Section.WORKBOOK) {
            writer.writeln();
            if (features.isEnabled(WorkbookWriterFeatures.Feature.CSV_SEPARATE_SHEETS)) {
                writer.writeln();
            }
        }
        if (features.isEnabled(WorkbookWriterFeatures.Feature.CSV_WRITE_SHEET_NAMES) && !StringUtils.isNullOrEmpty(name)) {
            writer.write(name);
            writer.writeln();
        }
        this.section = Section.SHEET;
        this.rowNumber = -1;
        this.columnNumber = -1;
        this.lastContentRow = 0;
        this.lastContentColumn = -1;
        return this;
    }

    @Override
    public CsvWorkbookWriter addContentValidation(ContentValidation cv) throws IOException {
        // Not supported
        return this;
    }

    @Override
    public CsvWorkbookWriter beginRow(TableSection section) throws IOException {
        if (this.section == Section.WORKBOOK) {
            unexpectedState("beginRow");
        }
        if (section == TableSection.DATA) {
            this.section = Section.DATA_ROW;
        } else {
            this.section = Section.HEADER_ROW;
        }
        rowNumber++;
        columnNumber = -1;
        lastContentColumn = -1;
        return this;
    }

    @Override
    public CsvWorkbookWriter addCellComment(String comment) {
        // Not supported
        return this;
    }

    @Override
    public CsvWorkbookWriter addEmptyCell() throws IOException {
        switch (section) {
        case WORKBOOK:
        case SHEET:
            unexpectedState("addEmptyCell");
            break;
        default:
            break;
        }
        columnNumber++;

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
        return this;
    }

    @Override
    public CsvWorkbookWriter addCell(boolean value) throws IOException {
        prepareCellWithContent();
        writer.write(value ? "TRUE" : "FALSE");
        return this;
    }

    @Override
    public CsvWorkbookWriter addCell(String value) throws IOException {
        prepareCellWithContent();
        if (value == null) {
            writer.write("");
        } else {
            writer.write(value);
        }
        return this;
    }

    @Override
    public CsvWorkbookWriter addCell(double value) throws IOException {
        prepareCellWithContent();
        writer.write(value);
        return this;
    }

    @Override
    public CsvWorkbookWriter addCell(long value) throws IOException {
        prepareCellWithContent();
        writer.write(value);
        return this;
    }

    @Override
    public CsvWorkbookWriter addCell(Date value) throws IOException {
        prepareCellWithContent();
        if (value == null) {
            writer.write("");
        } else {
            writer.write(formatDateTime.format(value));
        }
        return this;
    }

    @Override
    public CsvWorkbookWriter addCell(LocalDateTime value) throws IOException {
        prepareCellWithContent();
        if (value == null) {
            writer.write("");
        } else {
            writer.write(formatDateTime.format(DateUtils.asDate(value)));
        }
        return this;
    }

    @Override
    public CsvWorkbookWriter addCell(LocalDate value) throws IOException {
        prepareCellWithContent();
        if (value == null) {
            writer.write("");
        } else {
            writer.write(formatDate.format(DateUtils.asDate(value)));
        }
        return this;
    }

    @Override
    public CsvWorkbookWriter addCell(LocalTime value) throws IOException {
        prepareCellWithContent();
        if (value == null) {
            writer.write("");
        } else {
            writer.write(formatTime.format(DateUtils.asDate(value)));
        }
        return this;
    }

    @Override
    public CsvWorkbookWriter addCell(URI uri,
                                     String label) throws IOException {
        prepareCellWithContent();
        if (uri == null) {
            writer.write("");
        } else {
            writer.write(uri.toString());
        }
        return this;
    }

    @Override
    public void flush() throws IOException {
        writer.flush();
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }
}