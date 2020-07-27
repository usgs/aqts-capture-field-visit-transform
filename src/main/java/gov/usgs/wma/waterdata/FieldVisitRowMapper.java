package gov.usgs.wma.waterdata;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

public class FieldVisitRowMapper implements RowMapper<FieldVisit> {

    @Override
    public FieldVisit mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new FieldVisit(rs.getString("location_identifier"));
    }
}
