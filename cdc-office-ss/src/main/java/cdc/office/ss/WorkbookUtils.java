package cdc.office.ss;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public final class WorkbookUtils {
    private static final Set<Class<?>> SUPPORTED_TYPES = new HashSet<>();
    static {
        SUPPORTED_TYPES.add(String.class);

        SUPPORTED_TYPES.add(Character.class);
        SUPPORTED_TYPES.add(Boolean.class);
        SUPPORTED_TYPES.add(Double.class);
        SUPPORTED_TYPES.add(Float.class);
        SUPPORTED_TYPES.add(Long.class);
        SUPPORTED_TYPES.add(Integer.class);
        SUPPORTED_TYPES.add(Short.class);
        SUPPORTED_TYPES.add(Byte.class);

        SUPPORTED_TYPES.add(char.class);
        SUPPORTED_TYPES.add(boolean.class);
        SUPPORTED_TYPES.add(double.class);
        SUPPORTED_TYPES.add(float.class);
        SUPPORTED_TYPES.add(long.class);
        SUPPORTED_TYPES.add(int.class);
        SUPPORTED_TYPES.add(short.class);
        SUPPORTED_TYPES.add(byte.class);

        SUPPORTED_TYPES.add(Date.class);
        SUPPORTED_TYPES.add(LocalDateTime.class);
        SUPPORTED_TYPES.add(LocalDate.class);
        SUPPORTED_TYPES.add(LocalTime.class);
        SUPPORTED_TYPES.add(Enum.class);
        SUPPORTED_TYPES.add(URI.class);
    }

    private WorkbookUtils() {
    }

    /**
     * Returns {@code true} when a class has dedicated addCell method in {@link WorkbookWriter}.
     * <p>
     * The answer is {@code true} when {@code cls} is one of:
     * <ul>
     * <li>String
     * <li>Character or char
     * <li>Boolean or boolean
     * <li>Double or double
     * <li>Float or float
     * <li>Long or long
     * <li>Integer or int
     * <li>Short or short
     * <li>Byte or byte
     * <li>Date
     * <li>LocalDateTime
     * <li>LocalDate
     * <li>LocalTime
     * <li>Enum
     * </ul>
     *
     * @param cls The class.
     * @return {@code true} when {@code cls} has dedicated addCell method in {@link WorkbookWriter}.
     */
    public static boolean isSupportedType(Class<?> cls) {
        return SUPPORTED_TYPES.contains(cls);
    }

    /**
     * Returns {@code true} when a value has dedicated addCell method in {@link WorkbookWriter}.
     * <p>
     * The answer is {@code true} when {@code value} is {@code null} or is an instance of:
     * <ul>
     * <li>String
     * <li>Character or char
     * <li>Boolean or boolean
     * <li>Double or double
     * <li>Float or float
     * <li>Long or long
     * <li>Integer or int
     * <li>Short or short
     * <li>Byte or byte
     * <li>Date
     * <li>LocalDateTime
     * <li>LocalDate
     * <li>LocalTime
     * <li>Enum
     * <li>URI
     * </ul>
     *
     * @param value The value.
     * @return {@code true} when {@code value} has dedicated addCell method in {@link WorkbookWriter}.
     */
    public static boolean isNullOrHasSupportedType(Object value) {
        return value == null
                || WorkbookUtils.isSupportedType(value.getClass());
    }
}