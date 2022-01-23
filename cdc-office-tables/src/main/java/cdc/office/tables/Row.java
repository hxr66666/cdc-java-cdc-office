package cdc.office.tables;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cdc.converters.defaults.StringToDate;
import cdc.converters.defaults.StringToLocalDate;
import cdc.converters.defaults.StringToLocalDateTime;
import cdc.converters.defaults.StringToLocalTime;
import cdc.util.encoding.Encoders;
import cdc.util.lang.CollectionUtils;
import cdc.util.lang.FailureReaction;

/**
 * Interface used to access table row content.
 * <p>
 * This may be a header line or data line.<br>
 * A row is a list of strings. Conversions functions for elementary types are provided.
 *
 * @author D. Carbonne
 *
 */
public interface Row {
    public static final Logger LOGGER = LogManager.getLogger(Row.class);

    public static final Comparator<Row> LEXICOGRAPHIC_COMPARATOR =
            (r1,
             r2) -> CollectionUtils.compareLexicographic(r1.getValues(),
                                                         r2.getValues());
    /**
     * An empty Row.
     */
    public static final Row EMPTY = builder().build();

    /**
     * @return A List of values of this row.
     */
    public List<String> getValues();

    /**
     * @return The number of columns in the row.
     */
    public default int size() {
        return getValues().size();
    }

    public default boolean isEmpty() {
        return getValues().isEmpty();
    }

    /**
     * Returns the value associated to a column in the row, or {@code null}.
     *
     * @param column Index (0-based) of the searched column.
     * @return The value located at {@code column} in the row or {@code null} if {@code column} is invalid.
     */
    public default String getValue(int column) {
        return getValue(column, null);
    }

    /**
     * Returns the value associated to a column in the row, or a default value.
     *
     * @param column Index of the column.
     * @param def Value to return when no value is associated to column.
     * @return The value located at {@code column} or {@code def} if {@code column} is invalid.
     */
    public default String getValue(int column,
                                   String def) {
        final List<String> values = getValues();
        if (column >= 0 && column < values.size()) {
            final String value = values.get(column);
            return value == null ? def : value;
        } else {
            return def;
        }
    }

    /**
     * Returns the conversion of the value associated to a column
     * or a default value is none is found.
     *
     * @param <T> The result type.
     * @param column Index of the column.
     * @param converter The String to {@code <T>} converter.
     * @param def Value to return when no value is associated to column
     *            or when conversion fails and {@code reaction}
     *            is {@link FailureReaction#DEFAULT} or {@link FailureReaction#WARN}.
     * @param reaction The reaction to adopt when conversion fails.
     * @return The conversion of the string value located at {@code column} or {@code def}.
     * @throws IllegalArgumentException When value located at {@code column}
     *             can not be converted and {@code reaction} is {@link FailureReaction#FAIL}.
     */
    public default <T> T getValue(int column,
                                  Function<String, T> converter,
                                  T def,
                                  FailureReaction reaction) {
        final List<String> values = getValues();
        if (column >= 0 && column < values.size()) {
            final String value = values.get(column);
            if (value == null || value.isEmpty()) {
                return def;
            } else {
                try {
                    return converter.apply(value);
                } catch (final Exception e) {
                    return FailureReaction.onError("Failed to convert '" + value + "'",
                                                   LOGGER,
                                                   reaction,
                                                   def,
                                                   message -> new IllegalArgumentException(message, e));
                }
            }
        } else {
            return def;
        }
    }

    public default <T> T getValue(int column,
                                  Function<String, T> converter,
                                  T def) {
        return getValue(column, converter, def, FailureReaction.FAIL);
    }

    /**
     * Returns the value associated to a column as a Boolean.
     *
     * @param column Index of the column.
     * @param def The default value.
     * @param reaction The reaction to adopt when conversion fails.
     * @return The conversion of the string value located at {@code column} or {@code def}.
     * @throws IllegalArgumentException When value located at {@code column}
     *             can not be converted and {@code reaction} is {@link FailureReaction#FAIL}.
     */
    public default Boolean getValueAsBoolean(int column,
                                             Boolean def,
                                             FailureReaction reaction) {
        return getValue(column, Boolean::valueOf, def, reaction);
    }

    /**
     * Returns the value associated to a column as a Boolean.
     *
     * @param column Index of the column.
     * @param def The default value.
     * @return The conversion of the string value located at {@code column} or {@code def}.
     * @throws IllegalArgumentException When value located at {@code column}
     *             can not be converted.
     */
    public default Boolean getValueAsBoolean(int column,
                                             Boolean def) {
        return getValueAsBoolean(column, def, FailureReaction.FAIL);
    }

    /**
     * Returns the value associated to a column as a Double.
     *
     * @param column Index of the column.
     * @param def The default value.
     * @param reaction The reaction to adopt when conversion fails.
     * @return The conversion of the string value located at {@code column} or {@code def}.
     * @throws IllegalArgumentException When value located at {@code column}
     *             can not be converted and {@code reaction} is {@link FailureReaction#FAIL}.
     */
    public default Double getValueAsDouble(int column,
                                           Double def,
                                           FailureReaction reaction) {
        return getValue(column, Double::valueOf, def, reaction);
    }

    /**
     * Returns the value associated to a column as a Double.
     *
     * @param column Index of the column.
     * @param def The default value.
     * @return The conversion of the string value located at {@code column} or {@code def}.
     * @throws IllegalArgumentException When value located at {@code column}
     *             can not be converted.
     */
    public default Double getValueAsDouble(int column,
                                           Double def) {
        return getValueAsDouble(column, def, FailureReaction.FAIL);
    }

    /**
     * Returns the value associated to a column as a Float.
     *
     * @param column Index of the column.
     * @param def The default value.
     * @param reaction The reaction to adopt when conversion fails.
     * @return The conversion of the string value located at {@code column} or {@code def}.
     * @throws IllegalArgumentException When value located at {@code column}
     *             can not be converted and {@code reaction} is {@link FailureReaction#FAIL}.
     */
    public default Float getValueAsFloat(int column,
                                         Float def,
                                         FailureReaction reaction) {
        return getValue(column, Float::valueOf, def, reaction);
    }

    /**
     * Returns the value associated to a column as a Float.
     *
     * @param column Index of the column.
     * @param def The default value.
     * @return The conversion of the string value located at {@code column} or {@code def}.
     * @throws IllegalArgumentException When value located at {@code column}
     *             can not be converted.
     */
    public default Float getValueAsFloat(int column,
                                         Float def) {
        return getValueAsFloat(column, def, FailureReaction.FAIL);
    }

    /**
     * Returns the value associated to a column as a Long.
     *
     * @param column Index of the column.
     * @param def The default value.
     * @param reaction The reaction to adopt when conversion fails.
     * @return The conversion of the string value located at {@code column} or {@code def}.
     * @throws IllegalArgumentException When value located at {@code column}
     *             can not be converted and {@code reaction} is {@link FailureReaction#FAIL}.
     */
    public default Long getValueAsLong(int column,
                                       Long def,
                                       FailureReaction reaction) {
        return getValue(column, Long::valueOf, def, reaction);
    }

    /**
     * Returns the value associated to a column as a Long.
     *
     * @param column Index of the column.
     * @param def The default value.
     * @return The conversion of the string value located at {@code column} or {@code def}.
     * @throws IllegalArgumentException When value located at {@code column}
     *             can not be converted.
     */
    public default Long getValueAsLong(int column,
                                       Long def) {
        return getValueAsLong(column, def, FailureReaction.FAIL);
    }

    /**
     * Returns the value associated to a column as an Integer.
     *
     * @param column Index of the column.
     * @param def The default value.
     * @param reaction The reaction to adopt when conversion fails.
     * @return The conversion of the string value located at {@code column} or {@code def}.
     * @throws IllegalArgumentException When value located at {@code column}
     *             can not be converted and {@code reaction} is {@link FailureReaction#FAIL}.
     */
    public default Integer getValueAsInteger(int column,
                                             Integer def,
                                             FailureReaction reaction) {
        return getValue(column, Integer::valueOf, def, reaction);
    }

    /**
     * Returns the value associated to a column as an Integer.
     *
     * @param column Index of the column.
     * @param def The default value.
     * @return The conversion of the string value located at {@code column} or {@code def}.
     * @throws IllegalArgumentException When value located at {@code column}
     *             can not be converted.
     */
    public default Integer getValueAsInteger(int column,
                                             Integer def) {
        return getValueAsInteger(column, def, FailureReaction.FAIL);
    }

    /**
     * Returns the value associated to a column as a Short.
     *
     * @param column Index of the column.
     * @param def The default value.
     * @param reaction The reaction to adopt when conversion fails.
     * @return The conversion of the string value located at {@code column} or {@code def}.
     * @throws IllegalArgumentException When value located at {@code column}
     *             can not be converted and {@code reaction} is {@link FailureReaction#FAIL}.
     */
    public default Short getValueAsShort(int column,
                                         Short def,
                                         FailureReaction reaction) {
        return getValue(column, Short::valueOf, def, reaction);
    }

    /**
     * Returns the value associated to a column as a Short.
     *
     * @param column Index of the column.
     * @param def The default value.
     * @return The conversion of the string value located at {@code column} or {@code def}.
     * @throws IllegalArgumentException When value located at {@code column}
     *             can not be converted.
     */
    public default Short getValueAsShort(int column,
                                         Short def) {
        return getValueAsShort(column, def, FailureReaction.FAIL);
    }

    /**
     * Returns the value associated to a column as a Byte.
     *
     * @param column Index of the column.
     * @param def The default value.
     * @param reaction The reaction to adopt when conversion fails.
     * @return The conversion of the string value located at {@code column} or {@code def}.
     * @throws IllegalArgumentException When value located at {@code column}
     *             can not be converted and {@code reaction} is {@link FailureReaction#FAIL}.
     */
    public default Byte getValueAsByte(int column,
                                       Byte def,
                                       FailureReaction reaction) {
        return getValue(column, Byte::valueOf, def, reaction);
    }

    /**
     * Returns the value associated to a column as a Byte.
     *
     * @param column Index of the column.
     * @param def The default value.
     * @return The conversion of the string value located at {@code column} or {@code def}.
     * @throws IllegalArgumentException When value located at {@code column}
     *             can not be converted.
     */
    public default Byte getValueAsByte(int column,
                                       Byte def) {
        return getValueAsByte(column, def, FailureReaction.FAIL);
    }

    /**
     * Returns the value associated to a column as an Enum.
     *
     * @param <E> The Enum type.
     * @param column Index of the column.
     * @param enumClass The Enum class.
     * @param def The default value.
     * @param reaction The reaction to adopt when conversion fails.
     * @return The conversion of the string value located at {@code column} or {@code def}.
     * @throws IllegalArgumentException When value located at {@code column}
     *             can not be converted and {@code reaction} is {@link FailureReaction#FAIL}.
     */
    public default <E extends Enum<E>> E getValueAsEnum(int column,
                                                        Class<E> enumClass,
                                                        E def,
                                                        FailureReaction reaction) {
        final Function<String, E> converter = s -> Encoders.getNameEncoder(enumClass).decode(s);
        return getValue(column, converter, def, reaction);
    }

    /**
     * Returns the value associated to a column as an Enum.
     *
     * @param <E> The Enum type.
     * @param column Index of the column.
     * @param enumClass The Enum class.
     * @param def The default value.
     * @return The conversion of the string value located at {@code column} or {@code def}.
     * @throws IllegalArgumentException When value located at {@code column}
     *             can not be converted.
     */
    public default <E extends Enum<E>> E getValueAsEnum(int column,
                                                        Class<E> enumClass,
                                                        E def) {
        return getValueAsEnum(column, enumClass, def, FailureReaction.FAIL);
    }

    public default Date getValueAsDate(int column,
                                       Date def,
                                       FailureReaction reaction) {
        return getValue(column, StringToDate.AUTO, def, reaction);
    }

    public default Date getValueAsDate(int column,
                                       Date def) {
        return getValueAsDate(column, def, FailureReaction.FAIL);
    }

    public default LocalDate getValueAsLocalDate(int column,
                                                 LocalDate def,
                                                 FailureReaction reaction) {
        return getValue(column, StringToLocalDate.AUTO, def, reaction);
    }

    public default LocalDate getValueAsLocalDate(int column,
                                                 LocalDate def) {
        return getValueAsLocalDate(column, def, FailureReaction.FAIL);
    }

    public default LocalTime getValueAsLocalTime(int column,
                                                 LocalTime def,
                                                 FailureReaction reaction) {
        return getValue(column, StringToLocalTime.AUTO, def, reaction);
    }

    public default LocalTime getValueAsLocalTime(int column,
                                                 LocalTime def) {
        return getValueAsLocalTime(column, def, FailureReaction.FAIL);
    }

    public default LocalDateTime getValueAsLocalDateTime(int column,
                                                         LocalDateTime def,
                                                         FailureReaction reaction) {
        return getValue(column, StringToLocalDateTime.AUTO, def, reaction);
    }

    public default LocalDateTime getValueAsLocalDateTime(int column,
                                                         LocalDateTime def) {
        return getValueAsLocalDateTime(column, def, FailureReaction.FAIL);
    }

    /**
     * @return An empty Row builder.
     */
    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(String... values) {
        return new Builder().addValues(values);
    }

    /**
     * Returns a Row Builder initialized with values.
     *
     * @param values The values.
     * @return A Row builder initialized with {@code values}.
     */
    public static Builder builder(Object... values) {
        return new Builder().addValues(values);
    }

    /**
     * Returns a Row Builder initialized with values.
     *
     * @param values The values.
     * @return A Row builder initialized with {@code values}.
     */
    public static Builder builder(List<String> values) {
        return new Builder().addValues(values);
    }

    /**
     * Row builder.
     *
     * @author Damien Carbonne
     *
     */
    public static class Builder {
        private final List<Object> values = new ArrayList<>();

        public Row build() {
            return new RowImpl(values);
        }

        public boolean isEmpty() {
            return values.isEmpty();
        }

        public int size() {
            return values.size();
        }

        public Builder clear() {
            values.clear();
            return this;
        }

        public Builder addValue(Object value) {
            this.values.add(value);
            return this;
        }

        public Builder addValues(String... values) {
            for (final String value : values) {
                this.values.add(value);
            }
            return this;
        }

        public Builder addValues(Object... values) {
            for (final Object value : values) {
                this.values.add(value);
            }
            return this;
        }

        public Builder addValues(List<? extends Object> values) {
            this.values.addAll(values);
            return this;
        }
    }
}
