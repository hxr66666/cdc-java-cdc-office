package cdc.office.tables;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import cdc.util.lang.Checks;

/**
 * Base interface of header cells. It can be:
 * <ul>
 * <li>a {@link NameCell simple name}, matching exactly one column.
 * <li>a {@link PatternCell pattern}, matching any number of columns.
 * </ul>
 *
 * @author Damien Carbonne
 */
public interface HeaderCell {
    /**
     * @param text The text.
     * @return {@code true} if this cell matches {@code text}.
     */
    public boolean matches(String text);

    /**
     * @param name The name cell.
     * @return {@code true} if this cell matches {@code name}.
     */
    public default boolean matches(NameCell name) {
        return matches(name.getName());
    }

    /**
     * @param name The name.
     * @return A new {@link NameCell}.
     * @throws IllegalArgumentException When {@code name} is {@code null}.
     */
    public static NameCell name(String name) {
        return new NameCell(name);
    }

    /**
     * @param regex The regular expression.
     * @return A new {@link PatternCell}.
     * @throws IllegalArgumentException When {@code regex} is {@code null}.
     * @throws PatternSyntaxException When {@coe regex} can not be compiled.
     */
    public static PatternCell pattern(String regex) {
        return new PatternCell(regex);
    }

    /**
     * Implementation of {@link HeaderCell} used to represent exactly one column.
     *
     * @author Damien Carbonne
     */
    public static class NameCell implements HeaderCell {
        private final String name;

        NameCell(String name) {
            this.name = Checks.isNotNull(name, "name");
        }

        public String getName() {
            return name;
        }

        @Override
        public boolean matches(String text) {
            return name.equals(text);
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (!(object instanceof NameCell)) {
                return false;
            }
            final NameCell other = (NameCell) object;
            return name.equals(other.name);
        }

        @Override
        public String toString() {
            return name;
        }
    }

    /**
     * Implementation of {@link HeaderCell} used to represent several columns.
     *
     * @author Damien Carbonne
     */
    public static class PatternCell implements HeaderCell {
        private final Pattern pattern;

        PatternCell(String regex) {
            this.pattern = Pattern.compile(Checks.isNotNull(regex, "regex"));
        }

        public Pattern getPattern() {
            return pattern;
        }

        @Override
        public boolean matches(String text) {
            return pattern.matcher(text).matches();
        }

        @Override
        public int hashCode() {
            return pattern.hashCode();
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (!(object instanceof PatternCell)) {
                return false;
            }
            final PatternCell other = (PatternCell) object;
            return pattern.equals(other.pattern);
        }

        @Override
        public String toString() {
            return pattern.pattern();
        }
    }
}