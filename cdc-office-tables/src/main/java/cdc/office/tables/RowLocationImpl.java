package cdc.office.tables;

class RowLocationImpl implements RowLocation {
    private final TableSection section;
    private final int sectionNumber;
    private final int globalNumber;

    RowLocationImpl(TableSection section,
                    int sectionNumber,
                    int globalNumber) {
        this.section = section;
        this.sectionNumber = sectionNumber;
        this.globalNumber = globalNumber;
    }

    @Override
    public TableSection getSection() {
        return section;
    }

    @Override
    public int getSectionNumber() {
        return sectionNumber;
    }

    @Override
    public int getGlobalNumber() {
        return globalNumber;
    }

    @Override
    public int hashCode() {
        return section.hashCode() + 31 * (sectionNumber + 31 * globalNumber);
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof RowLocationImpl)) {
            return false;
        }
        final RowLocationImpl other = (RowLocationImpl) object;
        return section == other.section
                && sectionNumber == other.sectionNumber
                && globalNumber == other.globalNumber;
    }

    @Override
    public String toString() {
        return "[" + getSection() + " " + getSectionNumber() + " " + getGlobalNumber() + "]";
    }
}