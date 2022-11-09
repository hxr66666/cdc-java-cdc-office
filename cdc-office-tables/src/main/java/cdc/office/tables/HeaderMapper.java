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
 * <li>Some names are missing or not.
 * <li>Some names are additional or not.
 * </ul>
 * It does not check that order of actual header matches, in some way, an expected order.
 *
 * @author Damien Carbonne
 */
public final class HeaderMapper {
    /** The mandatory header. */
    private final Header mandatory;
    /** The optional header. */
    private final Header optional;
    /** The actual header. */
    private final Header actual;
    /** The missing mandatory names. */
    private final Set<String> missingMandatoryNames;
    /** The missing optional names. */
    private final Set<String> missingOptionalNames;
    /** The additional names. */
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
        Checks.isNotNull(builder.mandatory, "mandatory");
        Checks.isTrue(builder.mandatory.isValid(), "Invalid mandatory header");
        Checks.isNotNull(builder.optional, "optional");
        Checks.isTrue(builder.optional.isValid(), "Invalid optional header");
        Checks.isNotNull(builder.actual, "actual");
        Checks.isFalse(builder.mandatory.intersects(builder.optional), "Intersection of mandatory and optional");

        this.mandatory = builder.mandatory;
        this.optional = builder.optional;
        this.actual = builder.actual;

        final Set<String> missingMandatorySet = new HashSet<>();
        missingMandatorySet.addAll(builder.mandatory.getNamesSet());
        missingMandatorySet.removeAll(builder.actual.getNamesSet());
        this.missingMandatoryNames = Collections.unmodifiableSet(missingMandatorySet);

        final Set<String> missingOptionalSet = new HashSet<>();
        missingOptionalSet.addAll(builder.optional.getNamesSet());
        missingOptionalSet.removeAll(builder.actual.getNamesSet());
        this.missingOptionalNames = Collections.unmodifiableSet(missingOptionalSet);

        final Set<String> additionalSet = new HashSet<>();
        additionalSet.addAll(builder.actual.getNamesSet());
        additionalSet.removeAll(builder.mandatory.getNamesSet());
        additionalSet.removeAll(builder.optional.getNamesSet());
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
     * @return A set of expected mandatory names.
     */
    public Set<String> getExpectedMandatoryNames() {
        return mandatory.getNamesSet();
    }

    /**
     * @return A set of missing mandatory names.
     */
    public Set<String> getMissingMandatoryNames() {
        return missingMandatoryNames;
    }

    /**
     * @return A set of actual mandatory names.
     */
    public Set<String> getActualMandatoryNames() {
        final Set<String> set = new HashSet<>(getExpectedMandatoryNames());
        set.removeAll(getMissingMandatoryNames());
        return set;
    }

    /**
     * @return {@code true} when there are missing mandatory names.
     */
    public boolean hasMissingMandatoryNames() {
        return !missingMandatoryNames.isEmpty();
    }

    /**
     * @return {@code true} when all mandatory names are there.<br>
     *         <b>Note</b> that mandatory header may however be invalid.
     */
    public boolean hasAllMandatoryNames() {
        return missingMandatoryNames.isEmpty();
    }

    /**
     * @return A set of expected optional names.
     */
    public Set<String> getExpectedOptionalNames() {
        return optional.getNamesSet();
    }

    /**
     * @return A set of missing optional names.
     */
    public Set<String> getMissingOptionalNames() {
        return missingOptionalNames;
    }

    /**
     * @return A set of actual optional names.
     */
    public Set<String> getActualOptionalNames() {
        final Set<String> set = new HashSet<>(getExpectedOptionalNames());
        set.removeAll(getMissingOptionalNames());
        return set;
    }

    /**
     * @return {@code true} when there are missing mandatory names.
     */
    public boolean hasMissingOptionalNames() {
        return !missingOptionalNames.isEmpty();
    }

    /**
     * @return A set of additional names.<br>
     *         They are neither optional nor mandatory.
     */
    public Set<String> getAdditionalNames() {
        return additionalNames;
    }

    /**
     * @return {@code true} when there are additional names.
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