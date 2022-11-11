package cdc.office.tables;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

import cdc.office.tables.HeaderCell.NameCell;
import cdc.office.tables.HeaderCell.PatternCell;

/**
 * Class used to represent a header: an ordered list of {@link HeaderCell cells}.
 * <p>
 * A cell can represent one ({@link NameCell}) or several ({@link PatternCell}) columns.<br>
 * A actual header shall only contain names, no patterns.
 *
 * @author Damien Carbonne
 */
public final class Header {
    /** Ordered list of header cells. */
    private final List<HeaderCell> cells;
    private final Map<HeaderCell, Integer> cellToIndex = new HashMap<>();
    private final Map<String, Integer> nameToIndex = new HashMap<>();
    private final Map<Pattern, Integer> patternToIndex = new HashMap<>();
    private final boolean valid;

    public static final Header EMPTY = builder().build();

    /**
     * Checks the validity of this Header.
     *
     * @throws IllegalStateException When this header is invalid.
     */
    private void checkValidity() {
        if (!valid) {
            throw new IllegalStateException("Invalid header (duplicate names): " + this);
        }
    }

    private Header(Builder builder) {
        boolean invalid = false;
        int index = 0;
        for (final HeaderCell cell : builder.cells) {
            if (cellToIndex.containsKey(cell)) {
                invalid = true;
            } else {
                cellToIndex.put(cell, index);
                if (cell instanceof NameCell) {
                    nameToIndex.put(((NameCell) cell).getName(), index);
                } else {
                    patternToIndex.put(((PatternCell) cell).getPattern(), index);
                }
            }
            index++;
        }
        this.cells = Collections.unmodifiableList(builder.cells);
        this.valid = !invalid;
    }

    /**
     * @return {@code true} if this header is valid (it does not contain duplicates).
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * @return {@code true} if this Header only contains {@link NameCell names} (no patterns).
     */
    public boolean hasOnlyNames() {
        return patternToIndex.isEmpty();
    }

    /**
     * @return {@code true} if this Header contains {@link PatternCell patterns}.
     */
    public boolean hasPatterns() {
        return !patternToIndex.isEmpty();
    }

    /**
     * @return The number of cells of this Header.<br>
     *         <b>WARNING:</b> The number of columns that match this header may be greater, if it contains patterns.
     */
    public int size() {
        return cells.size();
    }

    /**
     * Returns the cell at a position.
     *
     * @param index The index.
     * @return The cell at {@code index}.
     * @throws IndexOutOfBoundsException When {@code index} is out of range.
     */
    public HeaderCell getCellAt(int index) {
        return cells.get(index);
    }

    /**
     * Returns the name at a position.
     *
     * @param index The index.
     * @return The name at {@code index}.
     * @throws IndexOutOfBoundsException When {@code index} is out of range.
     */
    public String getNameAt(int index) {
        return ((NameCell) cells.get(index)).getName();
    }

    /**
     * @return An ordered list of all declared cells (even if this header is invalid).
     */
    public List<HeaderCell> getCells() {
        return cells;
    }

    /**
     * @return A set of all declared cells.
     */
    public Set<HeaderCell> getCellsSet() {
        return cellToIndex.keySet();
    }

    /**
     * @return The set of names declared in this Header.
     */
    public Set<String> getNames() {
        return nameToIndex.keySet();
    }

    /**
     * @return The set of patterns declared in this Header.
     */
    public Set<Pattern> getPatterns() {
        return patternToIndex.keySet();
    }

    /**
     * Returns {@code true} when a cell is contained in this header (even if this header is invalid).
     *
     * @param cell The cell.
     * @return {@code true} when {@code cell} is contained in this header.
     */
    public boolean contains(HeaderCell cell) {
        return cellToIndex.containsKey(cell);
    }

    public boolean containsAll(Collection<HeaderCell> cells) {
        return cellToIndex.keySet().containsAll(cells);
    }

    /**
     * Returns the index of the cell that matches an actual name, or -1.
     *
     * @param name The actual name.
     * @return The cell index matching {@code name}, of -1.
     * @throws IllegalStateException When this header is invalid.
     */
    public int getMatchingIndex(String name) {
        checkValidity();
        final int index = nameToIndex.getOrDefault(name, -1);
        if (index >= 0) {
            return index;
        } else if (!patternToIndex.isEmpty()) {
            for (final Map.Entry<Pattern, Integer> entry : patternToIndex.entrySet()) {
                if (entry.getKey().matcher(name).matches()) {
                    return entry.getValue();
                }
            }
            return -1;
        } else {
            return -1;
        }
    }

    /**
     * Returns the cell that matches an actual name.
     *
     * @param name The actual name.
     * @return The cell that matches {@code name}, or {@code null}.
     */
    public HeaderCell getMatchingCell(String name) {
        final int index = getMatchingIndex(name);
        return index >= 0 ? cells.get(index) : null;
    }

    /**
     * Returns {@code true} when this Header contains a cell that matches an actual name.
     *
     * @param name The actual name.
     * @return {@code true} if this Header contains a cell that matches {@code name}.
     */
    public boolean matches(String name) {
        return getMatchingIndex(name) >= 0;
    }

    /**
     * Returns {@code true} if this Header and another one have a non-empty intersection.
     * <p>
     * <b>WARNING:</b> equality is used, not matching.
     *
     * @param other The other Header.
     * @return {@code true} if this Header and {@code other} have a non-empty intersection.
     */
    public boolean intersects(Header other) {
        final Set<HeaderCell> set = new HashSet<>(getCells());
        set.retainAll(other.getCells());
        return !set.isEmpty();
    }

    /**
     * Returns {@code true} if this Header contains another one.
     * <p>
     * <b>WARNING:</b> equality is used, not matching.
     *
     * @param other The other Header.
     * @return {@code true} if this Header contains {@code other}.
     */
    public boolean contains(Header other) {
        return cellToIndex.keySet().containsAll(other.getCells());
    }

    @Override
    public int hashCode() {
        return Objects.hash(cells);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Header)) {
            return false;
        }
        final Header other = (Header) object;
        return Objects.equals(cells, other.cells);
    }

    @Override
    public String toString() {
        return cells.toString();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final List<HeaderCell> cells = new ArrayList<>();

        Builder() {
        }

        public Builder name(String name) {
            this.cells.add(HeaderCell.name(name));
            return this;
        }

        public Builder pattern(String regex) {
            this.cells.add(HeaderCell.pattern(regex));
            return this;
        }

        public Builder names(String... names) {
            for (final String name : names) {
                this.cells.add(HeaderCell.name(name));
            }
            return this;
        }

        public Builder names(Collection<String> names) {
            for (final String name : names) {
                this.cells.add(HeaderCell.name(name));
            }
            return this;
        }

        public Builder names(Header header) {
            this.cells.addAll(header.getCells());
            return this;
        }

        public Builder names(Row row) {
            return names(row.getValues());
        }

        public Header build() {
            return new Header(this);
        }
    }
}