package gov.usgs.wma.waterdata;

import java.util.Objects;

public class FieldVisit {
    private String fieldVisitIdentifier;

    public FieldVisit(String fieldVisitIdentifier) {
        this.fieldVisitIdentifier = fieldVisitIdentifier;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof FieldVisit)) {
            return false;
        }
        FieldVisit fieldVisit = (FieldVisit) o;
        return Objects.equals(fieldVisitIdentifier, fieldVisit.fieldVisitIdentifier);
    }
    @Override
    public int hashCode() {
        return Objects.hash(fieldVisitIdentifier);
    }
}
