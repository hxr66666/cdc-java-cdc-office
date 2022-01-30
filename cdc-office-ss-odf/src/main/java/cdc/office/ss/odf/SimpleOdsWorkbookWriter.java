package cdc.office.ss.odf;

// import java.io.File;
// import java.io.IOException;
// import java.net.URI;
// import java.time.LocalDate;
// import java.time.LocalDateTime;
// import java.time.LocalTime;
// import java.util.Date;
//
// import org.odftoolkit.simple.SpreadsheetDocument;
//
// import com.github.jferard.fastods.Paragraph;
//
// import cdc.office.ss.Section;
// import cdc.office.ss.WorkbookKind;
// import cdc.office.ss.WorkbookWriter;
// import cdc.office.ss.WorkbookWriterFactory;
// import cdc.office.ss.WorkbookWriterFeatures;
// import cdc.office.tables.TableSection;
// import cdc.util.lang.DateUtils;
// import cdc.util.strings.StringUtils;
//
/// **
// * Open Office implementation of WorkbookWriter using Simple API of ODF Toolkit.
// *
// * @author Damien Carbonne
// *
// */
public class SimpleOdsWorkbookWriter {// implements WorkbookWriter<SimpleOdsWorkbookWriter> {
    // private final File file;
    // private final WorkbookWriterFeatures features;
    // private Section section = Section.WORKBOOK;
    // private final SpreadsheetDocument doc;
    // /** Current table (sheet). */
    // private Table table;
    // /** Current row. */
    // private Row row;
    // /** Current row index. */
    // private int rowIndex = -1;
    // /** Current cell. */
    // private Cell cell;
    // /** Current column index. */
    // private int columnIndex = -1;
    //
    // private int tableCount = 0;
    //
    // private final String formatInt;
    // private static final String FORMAT_DATE_TIME = "yyyy/MM/dd HH:mm:ss";
    // private static final String FORMAT_DATE = "yyyy/MM/dd";
    // private static final String FORMAT_TIME = "HH:mm:ss";
    //
    // public SimpleOdsWorkbookWriter(File file,
    // WorkbookWriterFeatures features)
    // throws IOException {
    // this.file = file;
    // this.features = features;
    // final WorkbookKind kind = WorkbookKind.from(file);
    // if (kind != WorkbookKind.ODS) {
    // throw new IllegalArgumentException();
    // }
    // try {
    // this.doc = SpreadsheetDocument.newSpreadsheetDocument();
    // } catch (final Exception e) {
    // throw new IOException(e);
    // }
    // this.table = null;
    // this.row = null;
    // this.cell = null;
    //
    // if (this.features.isEnabled(WorkbookWriterFeatures.Feature.USE_THOUSANDS_SEPARATOR)) {
    // this.formatInt = "#,##0";
    // } else {
    // this.formatInt = "#";
    // }
    // }
    //
    // public SimpleOdsWorkbookWriter(File file,
    // WorkbookWriterFeatures features,
    // WorkbookWriterFactory factory)
    // throws IOException {
    // this(file,
    // features);
    // }
    //
    // @Override
    // public SimpleOdsWorkbookWriter self() {
    // return this;
    // }
    //
    // @Override
    // public WorkbookKind getKind() {
    // return WorkbookKind.ODS;
    // }
    //
    // @Override
    // public WorkbookWriterFeatures getFeatures() {
    // return features;
    // }
    //
    // @Override
    // public boolean isSupported(WorkbookWriterFeatures.Feature feature) {
    // return feature == WorkbookWriterFeatures.Feature.USE_THOUSANDS_SEPARATOR
    // || feature == WorkbookWriterFeatures.Feature.TRUNCATE_CELLS
    // || feature == WorkbookWriterFeatures.Feature.TRUNCATE_CELLS_LINES;
    // }
    //
    // private void unexpectedState(String context) throws IOException {
    // throw new IOException("Unexpected state " + section + " in " + context);
    // }
    //
    // public SpreadsheetDocument getDocument() {
    // return doc;
    // }
    //
    // public Table getTable() {
    // return table;
    // }
    //
    // public Row getRow() {
    // return row;
    // }
    //
    // public Cell getCell() {
    // return cell;
    // }
    //
    // @Override
    // public SimpleOdsWorkbookWriter beginSheet(String name) {
    // if (tableCount == 0) {
    // this.table = doc.getTableList().get(0);
    // this.table.setTableName(name);
    // } else {
    // this.table = doc.appendSheet(name);
    // }
    // tableCount++;
    // this.rowIndex = -1;
    // this.columnIndex = -1;
    // this.row = null;
    // this.cell = null;
    // this.section = Section.SHEET;
    // return this;
    // }
    //
    // @Override
    // public SimpleOdsWorkbookWriter beginRow(TableSection section) throws IOException {
    // if (this.section == Section.WORKBOOK) {
    // unexpectedState("beginRow");
    // }
    // if (section == TableSection.DATA) {
    // if (this.section == Section.HEADER_CELL && features.isEnabled(WorkbookWriterFeatures.Feature.AUTO_FILTER_COLUMNS)) {
    // // TODO
    // // this.doc.addAutoFilter(AutoFilter.builder("Range" + tableCount, this.table, rowIndex, 0, rowIndex, columnIndex)
    // // .build());
    // }
    // this.section = Section.DATA_ROW;
    // } else {
    // this.section = Section.HEADER_ROW;
    // }
    //
    // this.rowIndex++;
    // this.row = null;
    // this.cell = null;
    // this.columnIndex = -1;
    // return this;
    // }
    //
    // public void addCell() throws IOException {
    // switch (section) {
    // case WORKBOOK:
    // case SHEET:
    // unexpectedState("addEmptyCell");
    // break;
    // default:
    // break;
    // }
    // if (this.row == null) {
    // this.row = table.getRowByIndex(this.rowIndex);
    // }
    // this.columnIndex++;
    // this.cell = row.getCellByIndex(this.columnIndex);
    //
    // switch (section) {
    // case DATA_ROW:
    // this.section = Section.DATA_CELL;
    // break;
    // case HEADER_ROW:
    // this.section = Section.HEADER_CELL;
    // break;
    // default:
    // break;
    // }
    // }
    //
    // @Override
    // public SimpleOdsWorkbookWriter addCellComment(String comment) {
    // // TODO
    // return this;
    // }
    //
    // @Override
    // public SimpleOdsWorkbookWriter addEmptyCell() throws IOException {
    // this.columnIndex++;
    // return this;
    // }
    //
    // @Override
    // public SimpleOdsWorkbookWriter addCell(boolean value) throws IOException {
    // addCell();
    // cell.setBooleanValue(value);
    // return this;
    // }
    //
    // @Override
    // public SimpleOdsWorkbookWriter addCell(String value) throws IOException {
    // if (value == null) {
    // addEmptyCell();
    // } else {
    // addCell();
    // if (features.isEnabled(WorkbookWriterFeatures.Feature.TRUNCATE_CELLS)) {
    // cell.setStringValue(StringUtils.extract(value, getKind().getMaxCellSize()));
    // } else if (features.isEnabled(WorkbookWriterFeatures.Feature.TRUNCATE_CELLS_LINES)) {
    // cell.setStringValue(StringUtils.extractAverage(value, features.getMaxLineLength(), getKind().getMaxCellSize()));
    // } else {
    // cell.setStringValue(value);
    // }
    // }
    // return this;
    // }
    //
    // @Override
    // public SimpleOdsWorkbookWriter addCell(double value) throws IOException {
    // addCell();
    // cell.setDoubleValue(value);
    // return this;
    // }
    //
    // @Override
    // public SimpleOdsWorkbookWriter addCell(long value) throws IOException {
    // addCell();
    // cell.setDoubleValue((double) value);
    // cell.setFormatString(formatInt);
    // return this;
    // }
    //
    // @Override
    // public SimpleOdsWorkbookWriter addCell(Date value) throws IOException {
    // if (value == null) {
    // addEmptyCell();
    // } else {
    // addCell();
    // cell.setDateTimeValue(DateUtils.asCalendar(value));
    // cell.setFormatString(FORMAT_DATE_TIME);
    // }
    // return this;
    // }
    //
    // @Override
    // public SimpleOdsWorkbookWriter addCell(LocalDateTime value) throws IOException {
    // if (value == null) {
    // addEmptyCell();
    // } else {
    // addCell();
    // cell.setDateTimeValue(DateUtils.asCalendar(value));
    // cell.setFormatString(FORMAT_DATE_TIME);
    // }
    // return this;
    // }
    //
    // @Override
    // public SimpleOdsWorkbookWriter addCell(LocalDate value) throws IOException {
    // if (value == null) {
    // addEmptyCell();
    // } else {
    // addCell();
    // cell.setDateValue(DateUtils.asCalendar(value));
    // cell.setFormatString(FORMAT_DATE);
    // }
    // return this;
    // }
    //
    // @Override
    // public SimpleOdsWorkbookWriter addCell(LocalTime value) throws IOException {
    // if (value == null) {
    // addEmptyCell();
    // } else {
    // addCell();
    // cell.setTimeValue(DateUtils.asCalendar(value));
    // cell.setFormatString(FORMAT_TIME);
    // }
    // return this;
    // }
    //
    // @Override
    // public SimpleOdsWorkbookWriter addCell(URI uri,
    // String label) throws IOException {
    // if (uri == null) {
    // addEmptyCell();
    // } else {
    // addCell();
    // final Paragraph paragraph = cell.addParagraph(label == null ? uri.toString() : label);
    // paragraph.applyHyperlink(uri);
    // }
    // return this;
    // }
    //
    // @Override
    // public void flush() throws IOException {
    // // Ignore (not supported)
    // }
    //
    // @Override
    // public void close() throws IOException {
    // try {
    // this.doc.save(file);
    // this.doc.close();
    // } catch (final Exception e) {
    // throw new IOException(e);
    // }
    // }
}