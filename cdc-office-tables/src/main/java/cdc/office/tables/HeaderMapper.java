package cdc.office.tables;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import cdc.util.lang.Checks;

/**
 * Helper class used to map an expected (valid) header to an actual (possibly invalid) one.
 * <p>
 * This class checks that :
 * <ul>
 * <li>Some names are missing or not.
 * <li>Some names are additional or not.
 * </ul>
 * It does not check that order of actual header matches, in some way, the expected order.
 *
 * @author Damien Carbonne
 *
 */
public class HeaderMapper {
    /** The expected header. */
    private final Header expected;
    /** The actual header. */
    private final Header actual;
    /** The missing names. */
    private final Set<String> missingNames;
    /** The additional names. */
    private final Set<String> additionalNames;

    /**
     * Creates a header mapper from an expected and actual headers.
     *
     * @param expected The expected header.
     * @param actual The actual header.
     * @throws IllegalArgumentException When {@code expected} is {@code null} or invalid,
     *             or when {@code actual} is {@code null}.
     */
    public HeaderMapper(Header expected,
                        Header actual) {
        Checks.isNotNull(expected, "expected");
        Checks.isNotNull(actual, "actual");
        Checks.isTrue(expected.isValid(), "Invalid expected header");

        this.expected = expected;
        this.actual = actual;

        final Set<String> missingSet = new HashSet<>();
        missingSet.addAll(expected.getNamesSet());
        missingSet.removeAll(actual.getNamesSet());
        this.missingNames = Collections.unmodifiableSet(missingSet);

        final Set<String> additionalSet = new HashSet<>();
        additionalSet.addAll(actual.getNamesSet());
        additionalSet.removeAll(expected.getNamesSet());
        this.additionalNames = Collections.unmodifiableSet(additionalSet);
    }

    /**
     * @return The expected (valid) header.
     */
    public Header getExpectedHeader() {
        return expected;
    }

    /**
     * @return The actual header, possibly invalid.
     */
    public Header getActualHeader() {
        return actual;
    }

    /**
     * @return A set of missing names.
     */
    public Set<String> getMissingNames() {
        return missingNames;
    }

    /**
     * @return A set of additional names.
     */
    public Set<String> getAdditionalNames() {
        return additionalNames;
    }

    /**
     * @return {@code true} when there are missing names.
     */
    public boolean hasMissingNames() {
        return !missingNames.isEmpty();
    }

    /**
     * @return {@code true} when there are additional names.
     */
    public boolean hasAdditionalNames() {
        return !additionalNames.isEmpty();
    }

    /**
     * @return {@code true} when all expected names are there.<br>
     *         <b>Note</b> that expected header may however be invalid.
     */
    public boolean hasAllExpectedNames() {
        return missingNames.isEmpty();
    }

    /**
     * @return {@code true} when all expected names are there
     *         and there are no additional names.<br>
     *         <b>Note</b> that expected header may however be invalid.
     */
    public boolean hasAllAndOnlyExpectedNames() {
        return missingNames.isEmpty() && additionalNames.isEmpty();
    }
}