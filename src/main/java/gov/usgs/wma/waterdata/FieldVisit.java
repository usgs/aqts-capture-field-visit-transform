package gov.usgs.wma.waterdata;

import java.util.Objects;

public class FieldVisit {

    private String locationIdentifier;

    public FieldVisit(String locationIdentifier) {
        this.locationIdentifier = locationIdentifier;
    }

    public String getLocationIdentifier() {
        return locationIdentifier;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof FieldVisit)) {
            return false;
        }
        FieldVisit fieldVisit = (FieldVisit) o;
        return Objects.equals(locationIdentifier, fieldVisit.locationIdentifier);
    }
    @Override
    public int hashCode() {
        return Objects.hash(locationIdentifier);
    }
}
