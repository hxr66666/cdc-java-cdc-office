package cdc.office.tables;

/**
 * Interface giving access to the location of a row.
 *
 * @author Damien Carbonne
 *
 */
public interface RowLocation {
    /**
     * @return The current section.
     */
    public TableSection getSection();

    public default boolean isHeader() {
        return getSection() == TableSection.HEADER;
    }

    public default boolean isData() {
        return getSection() == TableSection.DATA;
    }

    /**
     * @return The row number in the current section.
     *         First line in each section is numbered 1.
     */
    public int getSectionNumber();

    /**
     * @return The global row number.
     *         If section is {@code HEADER}, the result is the section row number.
     *         Otherwise it is the number of header rows plus the section row number.
     */
    public int getGlobalNumber();

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private TableSection section = TableSection.HEADER;
        private int sectionNumber = 0;
        private int globalNumber = 0;

        Builder() {
            // Ignore
        }

        public RowLocation build() {
            return new RowLocationImpl(section,
                                       sectionNumber,
                                       globalNumber);
        }

        public TableSection getSection() {
            return section;
        }

        public int getSectionNumber() {
            return sectionNumber;
        }

        public int getGlobalNumber() {
            return globalNumber;
        }

        public Builder set(TableSection section,
                           int sectionNumber,
                           int globalNumber) {
            this.section = section;
            this.sectionNumber = sectionNumber;
            this.globalNumber = globalNumber;
            return this;
        }

        public Builder incrementNumbers(TableSection section) {
            if (this.section == section) {
                sectionNumber++;
            } else {
                this.section = section;
                sectionNumber = 1;
            }
            globalNumber++;
            return this;
        }

        public Builder incrementNumbers(int headers) {
            if (globalNumber == headers) {
                section = TableSection.DATA;
                sectionNumber = 0;
            }
            sectionNumber++;
            globalNumber++;
            return this;
        }
    }
}