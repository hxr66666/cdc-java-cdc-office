package cdc.office.tables;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import cdc.util.lang.Checks;

/**
 * Helper class used to map expected (valid mandatory and valid optional) header to an actual (possibly invalid) one.
 * <p>
 * This class checks that :
 * <ul>
 * <li>Some mandatory/optional cells are missing or not.
 * <li>Some actual names are additional or not.
 * </ul>
 * It does not check that order of actual header matches, in some way, an expected order.
 *
 * @author Damien Carbonne
 */
public final class HeaderMapper {
    /** The mandatory header. Should not contain patterns. */
    private final Header mandatory;
    /** The optional header. */
    private final Header optional;
    /** The actual header. Shell not contain patterns. */
    private final Header actual;
    /** The missing mandatory cells. */
    private final Set<HeaderCell> missingMandatoryCells;
    /** The missing optional cells. */
    private final Set<HeaderCell> missingOptionalCells;
    /** The actual mandatory names. */
    private final Set<String> actualMandatoryNames;
    /** The actual optional names. */
    private final Set<String> actualOptionalNames;
    /** The actual additional names. */
    private final Set<String> additionalNames;

    /**
     * Creates a header mapper from a mandatory, optional and actual headers.
     *
     * @param builder The Builder.
     * @throws IllegalArgumentException When {@code mandatory} is {@code null} or invalid,<br>
     *             or when {@code optional} is {@code null} or invalid,<br>
     *             or when {@code actual} is {@code null},<br>
     *             or when {@code mandatory} and {@code optional} intersect each other.
     */
    private HeaderMapper(Builder builder) {
        this.mandatory = Checks.isNotNull(builder.mandatory, "mandatory");
        Checks.isTrue(builder.mandatory.isValid(), "Invalid mandatory header.");

        this.optional = Checks.isNotNull(builder.optional, "optional");
        Checks.isTrue(builder.optional.isValid(), "Invalid optional header.");

        Checks.isFalse(builder.mandatory.intersects(builder.optional), "Mandatory and optional headers can not intersect.");

        this.actual = Checks.isNotNull(builder.actual, "actual");
        Checks.isTrue(builder.actual.hasOnlyNames(), "Actual header can not contain patterns.");

        final Set<HeaderCell> missingMandatorySet = new HashSet<>();
        final Set<String> actualMandatorySet = new HashSet<>();
        missingMandatorySet.addAll(builder.mandatory.getSortedCells());
        for (final String name : actual.getDeclaredNames()) {
            final HeaderCell matching = mandatory.getMatchingCell(name);
            if (matching != null) {
                missingMandatorySet.remove(matching);
                actualMandatorySet.add(name);
            }
        }
        this.missingMandatoryCells = Collections.unmodifiableSet(missingMandatorySet);
        this.actualMandatoryNames = Collections.unmodifiableSet(actualMandatorySet);

        final Set<HeaderCell> missingOptionalSet = new HashSet<>();
        final Set<String> actualOptionalSet = new HashSet<>();
        missingOptionalSet.addAll(builder.optional.getSortedCells());
        for (final String name : actual.getDeclaredNames()) {
            final HeaderCell matching = optional.getMatchingCell(name);
            if (matching != null) {
                missingOptionalSet.remove(matching);
                actualOptionalSet.add(name);
            }
        }
        this.missingOptionalCells = Collections.unmodifiableSet(missingOptionalSet);
        this.actualOptionalNames = Collections.unmodifiableSet(actualOptionalSet);

        final Set<String> additionalSet = new HashSet<>();
        additionalSet.addAll(builder.actual.getDeclaredNames());
        additionalSet.removeAll(this.actualMandatoryNames);
        additionalSet.removeAll(this.actualOptionalNames);
        this.additionalNames = Collections.unmodifiableSet(additionalSet);
    }

    /**
     * @return The mandatory expected header.
     */
    public Header getMandatoryHeader() {
        return mandatory;
    }

    /**
     * @return The optional expected header.
     */
    public Header getOptionalHeader() {
        return optional;
    }

    /**
     * @return The actual header, possibly invalid.
     */
    public Header getActualHeader() {
        return actual;
    }

    /**
     * @return A set of expected mandatory cells.
     */
    public Set<HeaderCell> getExpectedMandatoryCells() {
        return mandatory.getDeclaredCells();
    }

    /**
     * @return A set of missing mandatory cells.
     */
    public Set<HeaderCell> getMissingMandatoryCells() {
        return missingMandatoryCells;
    }

    /**
     * @return A set of actual mandatory names.
     */
    public Set<String> getActualMandatoryNames() {
        return actualMandatoryNames;
    }

    /**
     * @return {@code true} when there are missing mandatory cells.
     */
    public boolean hasMissingMandatoryCells() {
        return !missingMandatoryCells.isEmpty();
    }

    /**
     * @return {@code true} when all mandatory cells are there.<br>
     *         <b>Note</b> that mandatory header may however be invalid.
     */
    public boolean hasAllMandatoryCells() {
        return missingMandatoryCells.isEmpty();
    }

    /**
     * @return A set of expected optional cells.
     */
    public Set<HeaderCell> getExpectedOptionalCells() {
        return optional.getDeclaredCells();
    }

    /**
     * @return A set of missing optional cells.
     */
    public Set<HeaderCell> getMissingOptionalCells() {
        return missingOptionalCells;
    }

    /**
     * @return A set of actual optional names.
     */
    public Set<String> getActualOptionalNames() {
        return actualOptionalNames;
    }

    /**
     * @return {@code true} when there are missing optional cells.
     */
    public boolean hasMissingOptionalCells() {
        return !missingOptionalCells.isEmpty();
    }

    /**
     * @return A set of additional actual names.<br>
     *         They are neither optional nor mandatory.
     */
    public Set<String> getAdditionalNames() {
        return additionalNames;
    }

    /**
     * @return {@code true} when there are additional actual names.
     *         They are neither optional nor mandatory.
     */
    public boolean hasAdditionalNames() {
        return !getAdditionalNames().isEmpty();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Header mandatory;
        private Header optional = Header.EMPTY;
        private Header actual;

        private Builder() {
        }

        public Builder mandatory(Header mandatory) {
            this.mandatory = mandatory;
            return this;
        }

        public Builder optional(Header optional) {
            this.optional = optional;
            return this;
        }

        public Builder actual(Header actual) {
            this.actual = actual;
            return this;
        }

        public HeaderMapper build() {
            return new HeaderMapper(this);
        }
    }
}