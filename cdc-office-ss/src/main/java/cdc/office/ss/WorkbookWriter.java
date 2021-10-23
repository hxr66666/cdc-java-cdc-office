package cdc.office.ss;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;

import cdc.office.tables.Row;
import cdc.office.tables.TableSection;
import cdc.util.strings.StringConversion;

/**
 * Base interface of objects that can be used to generate simple spread sheets using a stream-like API.
 * <p>
 * <b>WARNING:</b> Streaming may not be offered by some implementations. This impact memory usage and speed.<br>
 * <b>WARNING:</b> {@link #flush()} is not necessarily supported.
 *
 * @author Damien Carbonne
 * @param <W> The concrete WorkbookWriter type.
 */

public interface WorkbookWriter<W extends WorkbookWriter<W>> extends Closeable, Flushable {
    public W self();

    /**
     * @return The workbook kind of this writer.
     */
    public WorkbookKind getKind();

    public WorkbookWriterFeatures getFeatures();

    /**
     * Returns {@code true} if a feature is supported by this writer.
     *
     * @param feature The feature.
     * @return {@code true} if {@code feature} is supported by this writer.
     */
    public boolean isSupported(WorkbookWriterFeatures.Feature feature);

    /**
     * Starts the writing of a new sheet.
     *
     * @param name The sheet name.
     * @return This WorkbookWriter.
     * @throws IOException When an IO error occurs.
     */
    public W beginSheet(String name) throws IOException;

    /**
     * Starts the writing of a new row.
     *
     * @param section The row section.
     * @return This WorkbookWriter.
     * @throws IOException When an IO error occurs.
     */
    public W beginRow(TableSection section) throws IOException;

    /**
     * Adds a comment to current cell, if possible.
     *
     * @param comment The comment.
     * @return This WorkbookWriter.
     * @throws IOException When an IO error occurs.
     */
    public W addCellComment(String comment) throws IOException;

    /**
     * Adds an empty cell to current row.
     *
     * @return This WorkbookWriter.
     * @throws IOException When an IO error occurs.
     */
    public W addEmptyCell() throws IOException;

    /**
     * Adds empty cells to current row.
     *
     * @param count The number of empty cells to add.
     * @return This WorkbookWriter.
     * @throws IOException When an IO error occurs.
     */
    public default W addEmptyCells(int count) throws IOException {
        for (int index = 0; index < count; index++) {
            addEmptyCell();
        }
        return self();
    }

    /**
     * Adds a boolean cell to current row.
     *
     * @param value The boolean value.
     * @return This WorkbookWriter.
     * @throws IOException When an IO error occurs.
     */
    public W addCell(boolean value) throws IOException;

    /**
     * Adds a Boolean cell to current row.
     * <p>
     * If {@code value} is {@code null}, adds an empty cell.
     *
     * @param value The Boolean value.
     * @return This WorkbookWriter.
     * @throws IOException When an IO error occurs.
     */
    public default W addCell(Boolean value) throws IOException {
        if (value == null) {
            return addEmptyCell();
        } else {
            return addCell(value.booleanValue());
        }
    }

    /**
     * Adds a text cell to current row.
     * <p>
     * If {@code value} is {@code null}, adds an empty cell.
     *
     * @param value The string value.
     * @return This WorkbookWriter.
     * @throws IOException When an IO error occurs.
     */
    public W addCell(String value) throws IOException;

    /**
     * Adds text cells to current row.
     *
     * @param values The values.
     * @return This WorkbookWriter.
     * @throws IOException When an IO error occurs.
     */
    public default W addCells(String... values) throws IOException {
        for (final String value : values) {
            addCell(value);
        }
        return self();
    }

    /**
     * Begin a new row and write cells.
     *
     * @param section The row section.
     * @param values The values.
     * @return This WorkbookWriter.
     * @throws IOException When an IO error occurs.
     */
    public default W addRow(TableSection section,
                            String... values) throws IOException {
        beginRow(section);
        return addCells(values);
    }

    public default W addRow(TableSection section,
                            List<String> values) throws IOException {
        beginRow(section);
        return addCells(values);
    }

    public default W addRow(TableSection section,
                            Row row) throws IOException {
        beginRow(section);
        return addCells(row.getValues());
    }

    /**
     * Begin a new row and write cells.
     *
     * @param section The row section.
     * @param values The values.
     * @return This WorkbookWriter.
     * @throws IOException When an IO error occurs.
     */
    public default W addRow(TableSection section,
                            Object... values) throws IOException {
        beginRow(section);
        return addCells(values);
    }

    /**
     * Adds text cells to current row.
     *
     * @param values The values.
     * @return This WorkbookWriter.
     * @throws IOException When an IO error occurs.
     */
    public default W addCells(List<String> values) throws IOException {
        for (final String value : values) {
            addCell(value);
        }
        return self();
    }

    /**
     * Adds a character cell to current row.
     *
     * @param value The character value.
     * @return This WorkbookWriter.
     * @throws IOException When an IO error occurs.
     */
    public default W addCell(char value) throws IOException {
        return addCell(Character.toString(value));
    }

    /**
     * Adds a Character cell to current row.
     * <p>
     * If {@code value} is {@code null}, adds an empty cell.
     *
     * @param value The Character value.
     * @return This WorkbookWriter.
     * @throws IOException When an IO error occurs.
     */
    public default W addCell(Character value) throws IOException {
        if (value == null) {
            return addEmptyCell();
        } else {
            return addCell(value.charValue());
        }
    }

    /**
     * Adds an enumerated cell to current row.
     * <p>
     * If {@code value} is {@code null}, adds an empty cell.
     *
     * @param value The enumerated value.
     * @return This WorkbookWriter.
     * @throws IOException When an IO error occurs.
     */
    public default W addCell(Enum<?> value) throws IOException {
        if (value == null) {
            return addEmptyCell();
        } else {
            return addCell(StringConversion.asString(value));
        }
    }

    /**
     * Adds a double cell to current row.
     *
     * @param value The double value.
     * @return This WorkbookWriter.
     * @throws IOException When an IO error occurs.
     */
    public W addCell(double value) throws IOException;

    /**
     * Adds a Double cell to current row.
     * <p>
     * If {@code value} is {@code null}, adds an empty cell.
     *
     * @param value The Double value.
     * @return This WorkbookWriter.
     * @throws IOException When an IO error occurs.
     */
    public default W addCell(Double value) throws IOException {
        if (value == null) {
            return addEmptyCell();
        } else {
            return addCell(value.doubleValue());
        }
    }

    /**
     * Adds a float cell to current row.
     *
     * @param value The float value.
     * @return This WorkbookWriter.
     * @throws IOException When an IO error occurs.
     */
    public default W addCell(float value) throws IOException {
        return addCell((double) value);
    }

    /**
     * Adds a Float cell to current row.
     * <p>
     * If {@code value} is {@code null}, adds an empty cell.
     *
     * @param value The Float value.
     * @return This WorkbookWriter.
     * @throws IOException When an IO error occurs.
     */
    public default W addCell(Float value) throws IOException {
        if (value == null) {
            return addEmptyCell();
        } else {
            return addCell(value.floatValue());
        }
    }

    /**
     * Adds a long cell to current row.
     * <p>
     * <b>Note:</b> depending on target, large values may be rounded.
     *
     * @param value The long value.
     * @return This WorkbookWriter.
     * @throws IOException When an IO error occurs.
     */
    public W addCell(long value) throws IOException;

    /**
     * Adds a Long cell to current row.
     * <p>
     * If {@code value} is {@code null}, adds an empty cell.
     *
     * @param value The Long value.
     * @return This WorkbookWriter.
     * @throws IOException When an IO error occurs.
     */
    public default W addCell(Long value) throws IOException {
        if (value == null) {
            return addEmptyCell();
        } else {
            return addCell(value.longValue());
        }
    }

    /**
     * Adds an int cell to current row.
     *
     * @param value The int value.
     * @return This WorkbookWriter.
     * @throws IOException When an IO error occurs.
     */
    public default W addCell(int value) throws IOException {
        return addCell((long) value);
    }

    /**
     * Adds an Integer cell to current row.
     * <p>
     * If {@code value} is {@code null}, adds an empty cell.
     *
     * @param value The Integer value.
     * @return This WorkbookWriter.
     * @throws IOException When an IO error occurs.
     */
    public default W addCell(Integer value) throws IOException {
        if (value == null) {
            return addEmptyCell();
        } else {
            return addCell(value.intValue());
        }
    }

    /**
     * Adds a short cell to current row.
     *
     * @param value The short value.
     * @return This WorkbookWriter.
     * @throws IOException When an IO error occurs.
     */
    public default W addCell(short value) throws IOException {
        return addCell((long) value);
    }

    /**
     * Adds a Short cell to current row.
     * <p>
     * If {@code value} is {@code null}, adds an empty cell.
     *
     * @param value The Short value.
     * @return This WorkbookWriter.
     * @throws IOException When an IO error occurs.
     */
    public default W addCell(Short value) throws IOException {
        if (value == null) {
            return addEmptyCell();
        } else {
            return addCell(value.shortValue());
        }
    }

    /**
     * Adds a byte cell to current row.
     *
     * @param value The byte value.
     * @return This WorkbookWriter.
     * @throws IOException When an IO error occurs.
     */
    public default W addCell(byte value) throws IOException {
        return addCell((long) value);
    }

    /**
     * Adds a Byte cell to current row.
     * <p>
     * If {@code value} is {@code null}, adds an empty cell.
     *
     * @param value The Byte value.
     * @return This WorkbookWriter.
     * @throws IOException When an IO error occurs.
     */
    public default W addCell(Byte value) throws IOException {
        if (value == null) {
            return addEmptyCell();
        } else {
            return addCell(value.byteValue());
        }
    }

    /**
     * Adds a Date cell to current row.
     * <p>
     * Value will be formatted with {@code YYYY-MM-DD HH:MM::SS}.
     * <p>
     * If {@code value} is {@code null}, adds an empty cell.
     *
     * @param value The Date value.
     * @return This WorkbookWriter.
     * @throws IOException When an IO error occurs.
     */
    public W addCell(Date value) throws IOException;

    /**
     * Adds a LocalDateTime cell to current row.
     * <p>
     * Value will be formatted with {@code yyyy/MM/dd HH:mm::ss}.
     * <p>
     * If {@code value} is {@code null}, adds an empty cell.
     *
     * @param value The LocalDateTime value.
     * @return This WorkbookWriter.
     * @throws IOException When an IO error occurs.
     */
    public W addCell(LocalDateTime value) throws IOException;

    /**
     * Adds a LocalDate cell to current row.
     * <p>
     * Value will be formatted with {@code yyyy/MM/dd}.
     *
     * @param value The LocalDate value.
     * @return This WorkbookWriter.
     * @throws IOException When an IO error occurs.
     */
    public W addCell(LocalDate value) throws IOException;

    /**
     * Adds a LocalTime cell to current row.
     * <p>
     * Value will be formatted with {@code HH:mm:ss}.
     * <p>
     * If {@code value} is {@code null}, adds an empty cell.
     *
     * @param value The LocalTime value.
     * @return This WorkbookWriter.
     * @throws IOException When an IO error occurs.
     */
    public W addCell(LocalTime value) throws IOException;

    /**
     * Adds an hyperlink cell to current row.
     * <p>
     * <b>WARNING:</b> when hyperlink is not supported, text is written.
     *
     * @param uri The URI.
     * @param label The optional label. If {@code null}, the URI text is used instead.
     * @return This WorkbookWriter.
     * @throws IOException When an IO error occurs.
     */
    public W addCell(URI uri,
                     String label) throws IOException;

    /**
     * Adds an hyperlink cell to current row.
     * <p>
     * <b>WARNING:</b> when hyperlink is not supported, text is written.
     *
     * @param uri The URI.
     * @return This WorkbookWriter.
     * @throws IOException When an IO error occurs.
     */
    public default W addCell(URI uri) throws IOException {
        return addCell(uri, uri.toString());
    }

    /**
     * Adds an object using the appropriate basic function. Otherwise use toString().
     * <p>
     * Recognized types are:
     * <ul>
     * <li>String
     * <li>Character
     * <li>Boolean
     * <li>Double
     * <li>Float
     * <li>Long
     * <li>Integer
     * <li>Short
     * <li>Byte
     * <li>Date
     * <li>LocalDateTime
     * <li>LocalDate
     * <li>LocalTime
     * <li>Enum
     * <li>URI
     * </ul>
     * For example, if {@code value} is a Boolean, calls {@code addCell((Boolean) value);}.
     * <p>
     * If {@code value} is {@code null}, adds an empty cell.
     *
     * @param value The object value.
     * @return This WorkbookWriter.
     * @throws IOException When an IO error occurs.
     */
    public default W addCell(Object value) throws IOException {
        if (value == null) {
            return addEmptyCell();
        } else if (value instanceof String) {
            return addCell((String) value);
        } else if (value instanceof Character) {
            return addCell((Character) value);
        } else if (value instanceof Boolean) {
            return addCell((Boolean) value);
        } else if (value instanceof Double) {
            return addCell((Double) value);
        } else if (value instanceof Float) {
            return addCell((Float) value);
        } else if (value instanceof Long) {
            return addCell((Long) value);
        } else if (value instanceof Integer) {
            return addCell((Integer) value);
        } else if (value instanceof Short) {
            return addCell((Short) value);
        } else if (value instanceof Byte) {
            return addCell((Byte) value);
        } else if (value instanceof Date) {
            return addCell((Date) value);
        } else if (value instanceof LocalDateTime) {
            return addCell((LocalDateTime) value);
        } else if (value instanceof LocalDate) {
            return addCell((LocalDate) value);
        } else if (value instanceof LocalTime) {
            return addCell((LocalTime) value);
        } else if (value instanceof Enum) {
            return addCell((Enum<?>) value);
        } else if (value instanceof URI) {
            return addCell((URI) value);
        } else {
            return addCell(value.toString());
        }
    }

    /**
     * Adds objects using the appropriate basic function. Otherwise use toString().
     *
     * @param values The object values.
     * @return This WorkbookWriter.
     * @throws IOException When an IO error occurs.
     * @see #addCell(Object)
     */
    public default W addCells(Object... values) throws IOException {
        for (final Object value : values) {
            addCell(value);
        }
        return self();
    }
}