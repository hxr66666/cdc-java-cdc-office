package cdc.office.tables;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Class used to represent a header (an ordered list of names).
 *
 * @author Damien Carbonne
 */
public class Header {
    private static final Logger LOGGER = LogManager.getLogger(Header.class);
    private final List<String> names;
    private final Map<String, Integer> map = new HashMap<>();
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

    /**
     * Creates a Header from a String array.
     *
     * @param names The names.
     * @throws IllegalArgumentException When a name is {@code null}.
     * @deprecated Use {@link Builder}.
     */
    @Deprecated(since = "2022-05-19", forRemoval = true)
    public Header(String... names) {
        boolean invalid = false;
        int index = 0;
        final List<String> list = new ArrayList<>();
        for (final String name : names) {
            if (name == null) {
                throw new IllegalArgumentException("Null name");
            }
            list.add(name);
            if (map.containsKey(name)) {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn("Duplicate name {} in {}", name, Arrays.toString(names));
                }
                invalid = true;
            } else {
                map.put(name, index);
            }
            index++;
        }
        this.names = Collections.unmodifiableList(list);
        this.valid = !invalid;
    }

    /**
     * Creates a TableHeader from a String list.
     *
     * @param names The names.
     * @throws IllegalArgumentException When a name is {@code null} or duplicate.
     * @deprecated Use Builder.
     */
    @Deprecated(since = "2022-05-19", forRemoval = true)
    public Header(List<String> names) {
        this(names.toArray(new String[names.size()]));
    }

    /**
     * Creates a TableHeader from a row.
     *
     * @param row The row.
     * @throws IllegalArgumentException When a name is {@code null} or duplicate.
     * @deprecated Use Builder.
     */
    @Deprecated(since = "2022-05-19", forRemoval = true)
    public Header(Row row) {
        this(row.getValues());
    }

    /**
     * @return {@code true} if this header is valid (it does not contain duplicates).
     */
    public boolean isValid() {
        return valid;
    }

    public int size() {
        return names.size();
    }

    public String getNameAt(int index) {
        return names.get(index);
    }

    /**
     * @return A list of names (even if this header is invalid).
     */
    public List<String> getNames() {
        return names;
    }

    /**
     * @return A set of names (even if this header is invalid).
     */
    public Set<String> getNamesSet() {
        return map.keySet();
    }

    /**
     * Returns {@code true} when a name is contained in this header (even if this header is invalid).
     *
     * @param name The name.
     * @return {@code true} when {@code name} is contained in this header.
     */
    public boolean contains(String name) {
        return map.containsKey(name);
    }

    public boolean containsAll(Collection<String> names) {
        return map.keySet().containsAll(names);
    }

    /**
     * Returns the index of a name or -1.
     *
     * @param name The name.
     * @return The index of {@code name} of -1.
     * @throws IllegalStateException When this header is invalid.
     */
    public int getIndex(String name) {
        checkValidity();
        return map.getOrDefault(name, -1);
    }

    public boolean intersects(Header other) {
        final Set<String> set = new HashSet<>(getNames());
        set.retainAll(other.getNames());
        return !set.isEmpty();
    }

    public boolean contains(Header other) {
        return getNamesSet().containsAll(other.getNames());
    }

    @Override
    public int hashCode() {
        return Objects.hash(names,
                            map,
                            valid);
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
        return Objects.equals(names, other.names)
                && Objects.equals(map, other.map)
                && valid == other.valid;
    }

    @Override
    public String toString() {
        return names.toString();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final List<String> names = new ArrayList<>();

        Builder() {
        }

        public Builder name(String name) {
            this.names.add(name);
            return this;
        }

        public Builder names(String... names) {
            Collections.addAll(this.names, names);
            return this;
        }

        public Builder names(Collection<String> names) {
            this.names.addAll(names);
            return this;
        }

        public Builder names(Header header) {
            this.names.addAll(header.getNames());
            return this;
        }

        public Builder names(Row row) {
            this.names.addAll(row.getValues());
            return this;
        }

        public Header build() {
            return new Header(names);
        }
    }
}